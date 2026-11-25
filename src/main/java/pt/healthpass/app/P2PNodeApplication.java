package pt.healthpass.app;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pt.healthpass.consensus.ConsensusEngine;
import pt.healthpass.consensus.Validator;
import pt.healthpass.consensus.ValidatorRegistry;
import pt.healthpass.crypto.CanonicalSerializer;
import pt.healthpass.identity.DID;
import pt.healthpass.ledger.Blockchain;
import pt.healthpass.ledger.ChainRepository;
import pt.healthpass.ledger.Transaction;
import pt.healthpass.ledger.TransactionType;
import pt.healthpass.network.NodeConfig;
import pt.healthpass.network.P2PNode;
import pt.healthpass.network.PeerManager;
import pt.healthpass.storage.KeyStoreService;

/**
 * Lançador CLI de um nó HealthPass para a demonstração local multi-processo.
 * Recebe comandos simples por stdin: status, hello, propose, tx TYPE BASE64(JSON) BASE64(signature), stop.
 */
public final class P2PNodeApplication {
    private P2PNodeApplication() { }

    public static void main(String[] args) throws Exception {
        Map<String, String> opcoes = parseArgs(args);
        String id = required(opcoes, "id");
        int port = Integer.parseInt(required(opcoes, "port"));
        Path dataDir = Path.of(required(opcoes, "data"));
        Path keyDir = Path.of(required(opcoes, "key-dir"));
        String password = System.getenv("HEALTHPASS_NODE_KEY_PASSWORD");
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Defina a variável de ambiente HEALTHPASS_NODE_KEY_PASSWORD");
        }
        String validatorDidText = required(opcoes, "validator-did");
        DID validatorDid = DID.parse(validatorDidText);

        ValidatorRegistry registry = carregarRegisto(Path.of(required(opcoes, "registry")));
        var privateKey = new KeyStoreService(keyDir).carregarChavePrivada(id, password);
        Blockchain blockchain = new Blockchain(new ChainRepository(dataDir.resolve("chain.json").toString()));
        blockchain.inicializar();

        NodeConfig config = new NodeConfig("127.0.0.1", port);
        String peers = opcoes.getOrDefault("peers", "");
        if (!peers.isBlank()) for (String peer : peers.split(",")) config.adicionarPeer(peer.trim());
        P2PNode node = new P2PNode(config, new PeerManager(), blockchain,
                new ConsensusEngine(registry, 2), validatorDid, privateKey);
        Runtime.getRuntime().addShutdownHook(new Thread(node::parar));
        node.arrancar();
        node.anunciarPresenca();
        System.out.println("READY " + id + " " + port);

        try (BufferedReader input = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = input.readLine()) != null) {
                String response;
                try {
                    response = executarComando(line, node, blockchain);
                } catch (RuntimeException e) {
                    response = "ERROR " + e.getClass().getSimpleName() + " " + safe(e.getMessage());
                }
                System.out.println(response);
                if (line.trim().equalsIgnoreCase("stop")) break;
            }
        } finally {
            node.parar();
        }
    }

    private static String executarComando(String line, P2PNode node, Blockchain blockchain) {
        String trimmed = line.trim();
        if (trimmed.equalsIgnoreCase("status")) {
            var ultimo = blockchain.getUltimoBloco();
            return "STATUS " + blockchain.getAltura() + " " + (ultimo == null ? "-" : ultimo.getHash())
                    + " " + blockchain.getPendentes().size();
        }
        if (trimmed.equalsIgnoreCase("hello")) {
            node.anunciarPresenca();
            return "HELLO_SENT";
        }
        if (trimmed.equalsIgnoreCase("propose")) {
            var proposta = node.proporBloco();
            return "PROPOSAL " + proposta.getBloco().getHash();
        }
        if (trimmed.equalsIgnoreCase("stop")) return "STOPPING";
        if (trimmed.startsWith("tx ")) {
            String[] fields = trimmed.split("\\s+", 4);
            if (fields.length != 4) return "ERROR usage: tx TYPE BASE64_PAYLOAD BASE64_SIGNATURE";
            Transaction tx = new Transaction(TransactionType.valueOf(fields[1]),
                    new String(Base64.getDecoder().decode(fields[2]), StandardCharsets.UTF_8),
                    fields[3]);
            return "TX_SUBMITTED " + node.submeterTransacao(tx);
        }
        return "ERROR unknown command";
    }

    private static ValidatorRegistry carregarRegisto(Path path) throws Exception {
        String json = Files.readString(path, StandardCharsets.UTF_8);
        List<ValidatorEntry> entradas = CanonicalSerializer.deJsonLista(json, ValidatorEntry.class);
        ValidatorRegistry registry = new ValidatorRegistry();
        for (ValidatorEntry entrada : entradas) {
            PublicKey key = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(
                    Base64.getDecoder().decode(entrada.publicKey())));
            registry.adicionar(new Validator(DID.parse(entrada.did()), key));
        }
        return registry;
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("--") || i + 1 >= args.length) {
                throw new IllegalArgumentException("Argumentos esperados no formato --nome valor");
            }
            result.put(args[i].substring(2), args[++i]);
        }
        return result;
    }

    private static String required(Map<String, String> options, String name) {
        String value = options.get(name);
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Falta --" + name);
        return value;
    }

    private static String safe(String message) {
        return message == null ? "falha" : message.replace('\n', ' ').replace('\r', ' ');
    }

    public record ValidatorEntry(String did, String publicKey) { }
}
