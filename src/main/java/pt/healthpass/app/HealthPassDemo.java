package pt.healthpass.app;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import pt.healthpass.config.HealthPassConfig;
import pt.healthpass.consensus.ConsensusEngine;
import pt.healthpass.consensus.Validator;
import pt.healthpass.consensus.ValidatorRegistry;
import pt.healthpass.credential.Credential;
import pt.healthpass.credential.CredentialIssuer;
import pt.healthpass.credential.CredentialPresentation;
import pt.healthpass.credential.CredentialVerifier;
import pt.healthpass.credential.VaccinationCredential;
import pt.healthpass.identity.DID;
import pt.healthpass.identity.Identity;
import pt.healthpass.identity.IdentityRole;
import pt.healthpass.identity.IdentityService;
import pt.healthpass.ledger.Blockchain;
import pt.healthpass.ledger.ChainRepository;
import pt.healthpass.ledger.Transaction;
import pt.healthpass.network.NodeConfig;
import pt.healthpass.network.P2PNode;
import pt.healthpass.network.PeerManager;
import pt.healthpass.storage.OffChainCredentialStore;

/** Coordena o percurso didático completo numa rede de três nós locais. */
public final class HealthPassDemo implements AutoCloseable {
    public static final DID ISSUER_DID = DID.parse("did:healthpass:issuer-demo-01");
    public static final DID HOLDER_DID = DID.parse("did:healthpass:holder-demo-01");
    private static final List<DID> VALIDATOR_DIDS = List.of(
            DID.parse("did:healthpass:validator-demo-a"),
            DID.parse("did:healthpass:validator-demo-b"),
            DID.parse("did:healthpass:validator-demo-c"));

    private final Path sessionDirectory;
    private final List<Blockchain> chains = new ArrayList<>();
    private final List<P2PNode> nodes = new ArrayList<>();
    private final Map<DID, KeyPair> validatorKeys = new LinkedHashMap<>();
    private final KeyPair issuerKeys;
    private Credential latestCredential;

    public HealthPassDemo() {
        this(Path.of("data", "demo", "sessions"));
    }

    public HealthPassDemo(Path sessionsBase) {
        try {
            sessionDirectory = createSessionDirectory(sessionsBase);
            issuerKeys = gerarParDeChaves();
            KeyPair holderKeys = gerarParDeChaves();
            arrancarRede();
            registarIdentidades(holderKeys);
        } catch (Exception e) {
            close();
            throw new IllegalStateException("Não foi possível preparar a demonstração HealthPass", e);
        }
    }

    private static Path createSessionDirectory(Path base) throws IOException {
        Files.createDirectories(base);
        return Files.createTempDirectory(base, "session-");
    }

    private static KeyPair gerarParDeChaves() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, new SecureRandom());
        return generator.generateKeyPair();
    }

    private void arrancarRede() throws Exception {
        ValidatorRegistry registry = new ValidatorRegistry();
        for (DID did : VALIDATOR_DIDS) {
            KeyPair keys = gerarParDeChaves();
            validatorKeys.put(did, keys);
            registry.adicionar(new Validator(did, keys.getPublic()));
        }
        ConsensusEngine engine = new ConsensusEngine(registry, HealthPassConfig.QUORUM);
        List<Integer> ports = new ArrayList<>();
        for (int i = 0; i < VALIDATOR_DIDS.size(); i++) ports.add(portaDisponivel());
        List<String> addresses = ports.stream().map(p -> "127.0.0.1:" + p).toList();
        for (int i = 0; i < VALIDATOR_DIDS.size(); i++) {
            Blockchain chain = new Blockchain(new ChainRepository(sessionDirectory
                    .resolve("node-" + (i + 1) + ".json").toString()));
            chain.inicializar();
            chains.add(chain);
            PeerManager peers = new PeerManager();
            for (String address : addresses) peers.adicionarPeer(address);
            DID did = VALIDATOR_DIDS.get(i);
            P2PNode node = new P2PNode(new NodeConfig("127.0.0.1", ports.get(i)), peers, chain,
                    engine, did, validatorKeys.get(did).getPrivate());
            nodes.add(node);
        }
        nodes.forEach(P2PNode::arrancar);
        nodes.forEach(P2PNode::anunciarPresenca);
    }

    private static int portaDisponivel() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); }
    }

    private void registarIdentidades(KeyPair holderKeys) {
        IdentityService service = new IdentityService(chains.get(0));
        service.registar(new Identity(ISSUER_DID, IdentityRole.ISSUER, issuerKeys.getPublic()), issuerKeys.getPrivate());
        service.registar(new Identity(HOLDER_DID, IdentityRole.HOLDER, holderKeys.getPublic()), holderKeys.getPrivate());
        propagarPendentes();
        confirmarBloco();
    }

    /** Emite e confirma uma credencial com dados fictícios. */
    public synchronized Credential emitir(String vaccineCode, String administrationDate) {
        if (vaccineCode == null || vaccineCode.isBlank()) throw new IllegalArgumentException("Indique o código fictício da vacina");
        String date = administrationDate == null || administrationDate.isBlank()
                ? java.time.LocalDate.now().toString() : administrationDate.trim();
        try { java.time.LocalDate.parse(date); }
        catch (java.time.format.DateTimeParseException e) {
            throw new IllegalArgumentException("A data deve usar o formato AAAA-MM-DD", e);
        }
        Instant issued = Instant.now();
        VaccinationCredential credential = new VaccinationCredential("cred-" + UUID.randomUUID(),
                ISSUER_DID.toString(), HOLDER_DID.toString(), issued.toString(),
                issued.plus(365, ChronoUnit.DAYS).toString(), vaccineCode.trim(),
                date);
        IdentityService identityService = new IdentityService(chains.get(0));
        CredentialIssuer issuer = new CredentialIssuer(ISSUER_DID, issuerKeys.getPrivate(), chains.get(0),
                identityService, new OffChainCredentialStore(sessionDirectory.resolve("credentials")));
        issuer.emitir(credential);
        latestCredential = credential;
        propagarPendentes();
        confirmarBloco();
        return credential;
    }

    public synchronized CredentialPresentation apresentar() {
        if (latestCredential == null) throw new IllegalStateException("Emita primeiro uma credencial");
        return new CredentialPresentation(latestCredential);
    }

    public synchronized CredentialVerifier.Resultado verificar() {
        if (latestCredential == null) throw new IllegalStateException("Emita primeiro uma credencial");
        return new CredentialVerifier(chains.get(1), new IdentityService(chains.get(1))).verificar(latestCredential);
    }

    public synchronized void revogar() {
        if (latestCredential == null) throw new IllegalStateException("Emita primeiro uma credencial");
        IdentityService identityService = new IdentityService(chains.get(0));
        CredentialIssuer issuer = new CredentialIssuer(ISSUER_DID, issuerKeys.getPrivate(), chains.get(0),
                identityService, new OffChainCredentialStore(sessionDirectory.resolve("credentials")));
        issuer.revogar(latestCredential.getCredentialId());
        propagarPendentes();
        confirmarBloco();
    }

    private void propagarPendentes() {
        List<Transaction> pendentes = List.copyOf(chains.get(0).getPendentes());
        for (Transaction tx : pendentes) nodes.get(0).propagarTransacao(tx);
        aguardar(() -> chains.stream().allMatch(c -> c.getPendentes().size() >= pendentes.size()), "propagação das transações");
    }

    private void confirmarBloco() {
        if (chains.get(0).getPendentes().isEmpty()) return;
        int expectedHeight = chains.get(0).getAltura() + 1;
        DID leader = new ConsensusEngine(registry(), HealthPassConfig.QUORUM)
                .proponenteParaAltura(expectedHeight);
        int index = VALIDATOR_DIDS.indexOf(leader);
        nodes.get(index).proporBloco();
        aguardar(() -> chains.stream().allMatch(c -> c.getAltura() >= expectedHeight), "consenso e replicação do bloco");
        String expectedHash = chains.get(index).getUltimoBloco().getHash();
        aguardar(() -> chains.stream().allMatch(c -> c.getAltura() == expectedHeight
                && expectedHash.equals(c.getUltimoBloco().getHash())), "convergência das cadeias");
    }

    private ValidatorRegistry registry() {
        ValidatorRegistry registry = new ValidatorRegistry();
        VALIDATOR_DIDS.forEach(did -> registry.adicionar(new Validator(did, validatorKeys.get(did).getPublic())));
        return registry;
    }

    private static void aguardar(java.util.function.BooleanSupplier condição, String etapa) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            if (condição.getAsBoolean()) return;
            try { Thread.sleep(25); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException("Espera interrompida", e); }
        }
        throw new IllegalStateException("Tempo esgotado durante " + etapa);
    }

    public synchronized String estado() {
        StringBuilder out = new StringBuilder("Sessão: ").append(sessionDirectory).append('\n');
        for (int i = 0; i < nodes.size(); i++) {
            var chain = chains.get(i);
            out.append("Nó ").append((char) ('A' + i)).append(" (porta ").append(nodes.get(i).getConfig().getPorta())
                    .append("): altura ").append(chain.getAltura()).append(", pendentes ")
                    .append(chain.getPendentes().size()).append(", cadeia válida ").append(chain.validarCadeia())
                    .append(", hash ").append(chain.getUltimoBloco().getHash()).append('\n');
        }
        return out.toString();
    }

    public Path getSessionDirectory() { return sessionDirectory; }
    public boolean verificarIdentidadesRegistadas() {
        IdentityService service = new IdentityService(chains.get(0));
        return service.procurar(ISSUER_DID) != null && service.procurar(HOLDER_DID) != null;
    }
    public List<Integer> alturas() { return chains.stream().map(Blockchain::getAltura).toList(); }
    public Credential ultimaCredencial() { return latestCredential; }
    @Override public void close() { nodes.forEach(node -> { try { node.close(); } catch (Exception ignored) { } }); }
}
