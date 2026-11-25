package pt.healthpass.network;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import pt.healthpass.consensus.BlockProposal;
import pt.healthpass.consensus.ConsensusCertificate;
import pt.healthpass.consensus.ConsensusEngine;
import pt.healthpass.consensus.ValidatorVote;
import pt.healthpass.crypto.CanonicalSerializer;
import pt.healthpass.identity.DID;
import pt.healthpass.ledger.Block;
import pt.healthpass.ledger.Blockchain;
import pt.healthpass.ledger.Transaction;

/** Nó TCP da rede P2P do HealthPass. Cada mensagem é uma linha JSON independente. */
public class P2PNode implements AutoCloseable {

    private static final int MAX_MESSAGE_CHARS = 1_048_576;
    private static final int SOCKET_READ_TIMEOUT_MS = 3000;
    private static final int MAX_SEEN_IDS = 100_000;

    private final NodeConfig config;
    private final PeerManager peerManager;
    private final Blockchain blockchain;
    private final ConsensusEngine consensusEngine;
    private final DID validatorDid;
    private final PrivateKey validatorPrivateKey;
    private final AtomicBoolean running = new AtomicBoolean();
    private final LinkedHashMap<String, Boolean> seenIds = new LinkedHashMap<>(256, 0.75f, true);
    private final ConcurrentMap<String, BlockProposal> proposals = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConcurrentMap<String, ValidatorVote>> votes = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ConsensusCertificate> certificates = new ConcurrentHashMap<>();

    private volatile ServerSocket serverSocket;
    private volatile Thread acceptThread;
    private volatile ExecutorService handlers;

    public P2PNode(NodeConfig config, PeerManager peerManager, Blockchain blockchain) {
        this(config, peerManager, blockchain, null, null, null);
    }

    public P2PNode(NodeConfig config, PeerManager peerManager, Blockchain blockchain,
                   ConsensusEngine consensusEngine, DID validatorDid, PrivateKey validatorPrivateKey) {
        this.config = Objects.requireNonNull(config, "config obrigatória");
        this.peerManager = Objects.requireNonNull(peerManager, "peerManager obrigatório");
        this.blockchain = Objects.requireNonNull(blockchain, "blockchain obrigatória");
        this.consensusEngine = consensusEngine;
        this.validatorDid = validatorDid;
        this.validatorPrivateKey = validatorPrivateKey;
        if ((validatorDid == null) != (validatorPrivateKey == null)) {
            throw new IllegalArgumentException("DID e chave privada do validador devem ser fornecidos juntos");
        }
        for (String peer : config.getPeersIniciais()) peerManager.adicionarPeer(peer);
    }

    /** Abre o servidor TCP e começa a aceitar mensagens dos peers. */
    public synchronized void arrancar() {
        if (running.get()) return;
        try {
            ServerSocket server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(config.getHost(), config.getPorta()));
            serverSocket = server;
            handlers = Executors.newCachedThreadPool(r -> {
                Thread t = new Thread(r, "healthpass-peer-handler-" + config.getPorta());
                t.setDaemon(true);
                return t;
            });
            running.set(true);
            acceptThread = new Thread(this::acceptLoop, "healthpass-accept-" + config.getPorta());
            acceptThread.setDaemon(true);
            acceptThread.start();
        } catch (Exception e) {
            parar();
            throw new IllegalStateException("Não foi possível iniciar o nó na porta " + config.getPorta(), e);
        }
    }

    /** Fecha o servidor e termina as threads de atendimento. */
    public synchronized void parar() {
        running.set(false);
        try { if (serverSocket != null) serverSocket.close(); } catch (Exception ignored) { }
        if (handlers != null) handlers.shutdownNow();
        serverSocket = null;
        handlers = null;
    }

    @Override public void close() { parar(); }

    /** Envia um HELLO a todos os peers conhecidos; útil após iniciar a topologia. */
    public void anunciarPresenca() {
        publicar(MessageType.HELLO, CanonicalSerializer.paraJson(statusLocal()));
    }

    /** Adiciona uma transação à pool local e propaga-a pela rede. */
    public boolean submeterTransacao(Transaction transacao) {
        Objects.requireNonNull(transacao, "transacao obrigatória");
        String hash = transacao.calcularHash();
        if (transacaoConhecida(hash)) return false;
        blockchain.adicionarTransacao(transacao);
        propagarTransacao(transacao);
        return true;
    }

    /** Propaga uma transação que um serviço local já colocou na pool. */
    public void propagarTransacao(Transaction transacao) {
        Objects.requireNonNull(transacao, "transacao obrigatória");
        transacao.calcularHash();
        if (!transacaoConhecida(transacao.calcularHash())) blockchain.adicionarTransacao(transacao);
        publicar(MessageType.NEW_TRANSACTION, CanonicalSerializer.paraJson(transacao));
    }

    /** Propõe o próximo bloco e inicia o recolhimento distribuído de votos. */
    public BlockProposal proporBloco() {
        if (consensusEngine == null || validatorDid == null) {
            throw new IllegalStateException("Este nó não está configurado como validador");
        }
        BlockProposal proposta = consensusEngine.propor(blockchain);
        if (!validatorDid.equals(proposta.getProponente())) {
            throw new IllegalStateException("A altura atual seleciona outro validador como proponente");
        }
        String json = CanonicalSerializer.paraJson(new ProposalWire(proposta.getBloco(), proposta.getProponente().toString()));
        NetworkMessage mensagem = criarMensagem(MessageType.BLOCK_PROPOSAL, json);
        marcarComoVista(mensagem.getId());
        processarProposta(proposta);
        peerManager.difundir(mensagem);
        return proposta;
    }

    /** Trata uma mensagem recebida, ignorando mensagens repetidas ou malformadas. */
    public void tratarMensagem(NetworkMessage mensagem) {
        if (!mensagemValida(mensagem) || !marcarComoVista(mensagem.getId())) return;
        if (mensagem.getSender() != null) peerManager.adicionarPeer(mensagem.getSender());
        try {
            switch (mensagem.getType()) {
                case HELLO -> tratarHello(mensagem);
                case GET_CHAIN_STATUS -> responderStatus(mensagem.getSender());
                case GET_BLOCKS -> tratarPedidoBlocos(mensagem);
                case BLOCKS_RESPONSE -> tratarRespostaBlocos(mensagem);
                case NEW_TRANSACTION -> tratarTransacao(mensagem);
                case BLOCK_PROPOSAL -> tratarProposta(mensagem);
                case BLOCK_VOTE -> tratarVoto(mensagem);
                case NEW_BLOCK -> tratarNovoBloco(mensagem);
            }
            if (mensagem.getType() == MessageType.NEW_TRANSACTION
                    || mensagem.getType() == MessageType.BLOCK_PROPOSAL
                    || mensagem.getType() == MessageType.BLOCK_VOTE
                    || mensagem.getType() == MessageType.NEW_BLOCK) {
                peerManager.difundir(mensagem);
            }
        } catch (RuntimeException ignored) {
            // Mensagem inválida não pode terminar o loop de atendimento do nó.
        }
    }

    public NodeConfig getConfig() { return config; }
    public boolean isRunning() { return running.get(); }

    private void acceptLoop() {
        while (running.get()) {
            try {
                Socket socket = serverSocket.accept();
                socket.setSoTimeout(SOCKET_READ_TIMEOUT_MS);
                ExecutorService pool = handlers;
                if (pool != null) pool.submit(() -> receber(socket));
                else socket.close();
            } catch (Exception e) {
                if (running.get()) continue;
                break;
            }
        }
    }

    private void receber(Socket socket) {
        try (socket; BufferedReader reader = new BufferedReader(new InputStreamReader(
                socket.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder linha = new StringBuilder();
            int caractere;
            while ((caractere = reader.read()) != -1 && caractere != '\n') {
                if (linha.length() >= MAX_MESSAGE_CHARS) return;
                if (caractere != '\r') linha.append((char) caractere);
            }
            if (linha.isEmpty()) return;
            tratarMensagem(CanonicalSerializer.deJson(linha.toString(), NetworkMessage.class));
        } catch (Exception ignored) {
            // Timeout, desconexão, JSON inválido ou mensagem acima do limite.
        }
    }

    private void tratarHello(NetworkMessage mensagem) {
        PeerStatus remoto = CanonicalSerializer.deJson(mensagem.getPayload(), PeerStatus.class);
        PeerStatus local = statusLocal();
        if (remoto.height() > local.height()) {
            enviarPara(mensagem.getSender(), MessageType.GET_BLOCKS,
                    CanonicalSerializer.paraJson(new BlocksRequest(local.height() + 1)));
        } else if (local.height() > remoto.height()) {
            enviarPara(mensagem.getSender(), MessageType.BLOCKS_RESPONSE,
                    CanonicalSerializer.paraJson(respostaBlocos(remoto.height() + 1)));
        } else if (!Objects.equals(local.latestHash(), remoto.latestHash())) {
            enviarPara(mensagem.getSender(), MessageType.GET_CHAIN_STATUS, "{}");
        }
    }

    private void responderStatus(String peer) {
        enviarPara(peer, MessageType.HELLO, CanonicalSerializer.paraJson(statusLocal()));
    }

    private void tratarPedidoBlocos(NetworkMessage mensagem) {
        BlocksRequest pedido = CanonicalSerializer.deJson(mensagem.getPayload(), BlocksRequest.class);
        enviarPara(mensagem.getSender(), MessageType.BLOCKS_RESPONSE,
                CanonicalSerializer.paraJson(respostaBlocos(pedido.fromHeight())));
    }

    private BlocksResponse respostaBlocos(int fromHeight) {
        List<BlockWire> resultado = new ArrayList<>();
        int inicio = Math.max(1, fromHeight); // genesis é conhecido e não exige certificado
        for (int altura = inicio; altura <= blockchain.getAltura(); altura++) {
            Block bloco = blockchain.getCadeia().get(altura);
            ConsensusCertificate certificado = certificates.get(bloco.getHash());
            if (certificado == null) break;
            resultado.add(paraWire(bloco, certificado));
        }
        return new BlocksResponse(resultado);
    }

    private void tratarRespostaBlocos(NetworkMessage mensagem) {
        BlocksResponse resposta = CanonicalSerializer.deJson(mensagem.getPayload(), BlocksResponse.class);
        for (BlockWire wire : resposta.blocks()) {
            if (wire.block().getHeight() != blockchain.getAltura() + 1) break;
            aceitarWire(wire);
        }
    }

    private void tratarTransacao(NetworkMessage mensagem) {
        Transaction transacao = CanonicalSerializer.deJson(mensagem.getPayload(), Transaction.class);
        String hash = transacao.calcularHash();
        if (!transacaoConhecida(hash)) blockchain.adicionarTransacao(transacao);
    }

    private void tratarProposta(NetworkMessage mensagem) {
        ProposalWire wire = CanonicalSerializer.deJson(mensagem.getPayload(), ProposalWire.class);
        BlockProposal proposta = new BlockProposal(wire.block(), DID.parse(wire.proponent()));
        processarProposta(proposta);
    }

    private void processarProposta(BlockProposal proposta) {
        if (consensusEngine == null || !consensusEngine.validarProposta(proposta, blockchain)) return;
        String hash = proposta.getBloco().getHash();
        proposals.putIfAbsent(hash, proposta);
        if (validatorDid != null && validatorPrivateKey != null) {
            try {
                ValidatorVote voto = consensusEngine.votar(proposta, blockchain, validatorDid, validatorPrivateKey);
                receberVoto(voto);
                publicar(MessageType.BLOCK_VOTE, CanonicalSerializer.paraJson(paraWire(voto)));
            } catch (RuntimeException ignored) { }
        }
    }

    private void tratarVoto(NetworkMessage mensagem) {
        VoteWire wire = CanonicalSerializer.deJson(mensagem.getPayload(), VoteWire.class);
        receberVoto(deWire(wire));
    }

    private void receberVoto(ValidatorVote voto) {
        if (consensusEngine == null || voto == null || voto.getValidador() == null) return;
        String hash = voto.getHashBloco();
        BlockProposal proposta = proposals.get(hash);
        if (proposta == null || !consensusEngine.validarProposta(proposta, blockchain)
                || !voto.isValid(consensusEngine.getRegistry(), hash)) return;
        ConcurrentMap<String, ValidatorVote> porValidador = votes.computeIfAbsent(hash,
                ignored -> new ConcurrentHashMap<>());
        porValidador.putIfAbsent(voto.getValidador().toString(), voto);
        List<ValidatorVote> reunidos = List.copyOf(porValidador.values());
        ConsensusCertificate certificado = new ConsensusCertificate(hash, reunidos);
        if (!consensusEngine.temQuorum(certificado)) return;
        if (blockchain.getAltura() >= proposta.getBloco().getHeight()) {
            certificates.putIfAbsent(hash, certificado);
            return;
        }
        try {
            consensusEngine.aceitar(proposta, certificado, blockchain);
            certificates.put(hash, certificado);
            publicar(MessageType.NEW_BLOCK, CanonicalSerializer.paraJson(paraWire(proposta.getBloco(), certificado)));
        } catch (RuntimeException ignored) { }
    }

    private void tratarNovoBloco(NetworkMessage mensagem) {
        BlockWire wire = CanonicalSerializer.deJson(mensagem.getPayload(), BlockWire.class);
        aceitarWire(wire);
    }

    private void aceitarWire(BlockWire wire) {
        if (consensusEngine == null || wire == null || wire.block() == null
                || wire.block().getHeight() <= blockchain.getAltura()) return;
        BlockProposal proposta = new BlockProposal(wire.block(), DID.parse(wire.block().getValidatorId()));
        ConsensusCertificate certificado = deWire(wire);
        consensusEngine.aceitar(proposta, certificado, blockchain);
        certificates.put(wire.block().getHash(), certificado);
    }

    private boolean transacaoConhecida(String hash) {
        return blockchain.getPendentes().stream().anyMatch(t -> t.calcularHash().equals(hash))
                || blockchain.getCadeia().stream().flatMap(b -> b.getTransactions().stream())
                    .anyMatch(t -> t.calcularHash().equals(hash));
    }

    private PeerStatus statusLocal() {
        Block ultimo = blockchain.getUltimoBloco();
        return new PeerStatus(blockchain.getAltura(), ultimo == null ? null : ultimo.getHash());
    }

    private void publicar(MessageType tipo, String payload) {
        NetworkMessage mensagem = criarMensagem(tipo, payload);
        marcarComoVista(mensagem.getId());
        peerManager.difundir(mensagem);
    }

    private void enviarPara(String peer, MessageType tipo, String payload) {
        if (peer == null || peer.equals(config.getHost() + ":" + config.getPorta())) return;
        NetworkMessage mensagem = criarMensagem(tipo, payload);
        marcarComoVista(mensagem.getId());
        peerManager.enviar(peer, mensagem);
    }

    private NetworkMessage criarMensagem(MessageType tipo, String payload) {
        return new NetworkMessage(UUID.randomUUID().toString(), tipo, payload,
                config.getHost() + ":" + config.getPorta());
    }

    private boolean mensagemValida(NetworkMessage mensagem) {
        return mensagem != null && mensagem.getId() != null && !mensagem.getId().isBlank()
                && mensagem.getId().length() <= 128
                && mensagem.getType() != null && mensagem.getPayload() != null
                && mensagem.getPayload().length() <= MAX_MESSAGE_CHARS;
    }

    private synchronized boolean marcarComoVista(String id) {
        if (id == null || seenIds.containsKey(id)) return false;
        seenIds.put(id, Boolean.TRUE);
        if (seenIds.size() > MAX_SEEN_IDS) {
            String eldest = seenIds.keySet().iterator().next();
            seenIds.remove(eldest);
        }
        return true;
    }

    private static VoteWire paraWire(ValidatorVote voto) {
        return new VoteWire(voto.getValidador().toString(), voto.getHashBloco(),
                voto.isAprova(), voto.getAssinatura());
    }

    private static ValidatorVote deWire(VoteWire voto) {
        return new ValidatorVote(DID.parse(voto.validator()), voto.blockHash(), voto.approves(), voto.signature());
    }

    private static BlockWire paraWire(Block bloco, ConsensusCertificate certificado) {
        return new BlockWire(bloco, certificado.getHashBloco(),
                certificado.getVotos().stream().map(P2PNode::paraWire).toList());
    }

    private static ConsensusCertificate deWire(BlockWire wire) {
        return new ConsensusCertificate(wire.blockHash(), wire.votes().stream().map(P2PNode::deWire).toList());
    }

    public record PeerStatus(int height, String latestHash) { }
    public record BlocksRequest(int fromHeight) { }
    public record BlocksResponse(List<BlockWire> blocks) {
        public BlocksResponse { blocks = List.copyOf(blocks); }
    }
    public record ProposalWire(Block block, String proponent) { }
    public record VoteWire(String validator, String blockHash, boolean approves, String signature) { }
    public record BlockWire(Block block, String blockHash, List<VoteWire> votes) {
        public BlockWire { votes = List.copyOf(votes); }
    }
}
