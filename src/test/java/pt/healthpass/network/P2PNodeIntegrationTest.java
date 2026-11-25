package pt.healthpass.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.ServerSocket;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pt.healthpass.consensus.ConsensusEngine;
import pt.healthpass.consensus.Validator;
import pt.healthpass.consensus.ValidatorRegistry;
import pt.healthpass.identity.DID;
import pt.healthpass.ledger.Blockchain;
import pt.healthpass.ledger.ChainRepository;
import pt.healthpass.ledger.Transaction;
import pt.healthpass.ledger.TransactionType;

class P2PNodeIntegrationTest {

    @TempDir Path tempDir;

    @Test
    void threeLocalProcessesReplicateTransactionsConsensusBlocksAndRecoverLaggingNode() throws Exception {
        List<Integer> ports = freePorts(3);
        var keyGenerator = KeyPairGenerator.getInstance("RSA");
        keyGenerator.initialize(2048);
        List<KeyPair> keys = List.of(keyGenerator.generateKeyPair(), keyGenerator.generateKeyPair(), keyGenerator.generateKeyPair());
        List<DID> dids = List.of(new DID("validator-a"), new DID("validator-b"), new DID("validator-c"));
        ValidatorRegistry registry = new ValidatorRegistry();
        for (int i = 0; i < 3; i++) registry.adicionar(new Validator(dids.get(i), keys.get(i).getPublic()));

        NodeFixture a = node("a", 0, ports, dids, keys, registry);
        NodeFixture b = node("b", 1, ports, dids, keys, registry);
        NodeFixture c = node("c", 2, ports, dids, keys, registry);
        NodeFixture initialC = c;
        try {
            a.node.arrancar();
            b.node.arrancar();
            initialC.node.arrancar();
            a.node.anunciarPresenca();
            b.node.anunciarPresenca();
            initialC.node.anunciarPresenca();

            Transaction first = new Transaction(TransactionType.IDENTITY_REGISTERED,
                    "{\"did\":\"did:healthpass:replicated-1\"}", "synthetic-signature", 1001);
            assertTrue(a.node.submeterTransacao(first));
            assertTrue(waitFor(() -> b.chain.getPendentes().size() == 1 && initialC.chain.getPendentes().size() == 1,
                    Duration.ofSeconds(5)));
            a.node.proporBloco();
            assertTrue(waitFor(() -> allAtHeight(List.of(a, b, initialC), 1), Duration.ofSeconds(8)));
            assertAllSameTip(List.of(a, b, initialC));

            initialC.node.parar();
            Transaction second = new Transaction(TransactionType.IDENTITY_REGISTERED,
                    "{\"did\":\"did:healthpass:replicated-2\"}", "synthetic-signature", 1002);
            assertTrue(a.node.submeterTransacao(second));
            assertTrue(waitFor(() -> b.chain.getPendentes().size() == 1,
                    Duration.ofSeconds(5)));
            b.node.proporBloco(); // altura 2 seleciona validator-b
            assertTrue(waitFor(() -> allAtHeight(List.of(a, b), 2), Duration.ofSeconds(8)));

            c = node("c", 2, ports, dids, keys, registry); // recarrega sua cadeia persistida na altura 1
            c.node.arrancar();
            c.node.anunciarPresenca();
            NodeFixture restartedC = c;
            assertTrue(waitFor(() -> restartedC.chain.getAltura() == 2, Duration.ofSeconds(8)));
            assertAllSameTip(List.of(a, b, restartedC));
            assertTrue(restartedC.chain.validarCadeia());
        } finally {
            a.node.parar();
            b.node.parar();
            c.node.parar();
        }
    }

    @Test
    void rejectsOversizedAndMalformedNetworkMessages() throws Exception {
        List<Integer> ports = freePorts(1);
        Blockchain chain = new Blockchain(new ChainRepository(tempDir.resolve("malformed-chain.json").toString()));
        chain.inicializar();
        NodeConfig config = new NodeConfig("127.0.0.1", ports.get(0));
        P2PNode node = new P2PNode(config, new PeerManager(), chain);
        assertFalse(node.isRunning());
        node.tratarMensagem(new NetworkMessage("", MessageType.NEW_TRANSACTION, "{}"));
        node.tratarMensagem(new NetworkMessage("valid-id", MessageType.NEW_TRANSACTION, "x".repeat(1_048_577)));
        assertTrue(chain.getPendentes().isEmpty());
    }

    private NodeFixture node(String name, int localIndex, List<Integer> ports, List<DID> dids,
                             List<KeyPair> keys, ValidatorRegistry registry) {
        int port = ports.get(localIndex);
        NodeConfig config = new NodeConfig("127.0.0.1", port);
        for (int i = 0; i < ports.size(); i++) {
            if (i != localIndex) config.adicionarPeer("127.0.0.1:" + ports.get(i));
        }
        Blockchain chain = new Blockchain(new ChainRepository(tempDir.resolve(name + "-chain.json").toString()));
        chain.inicializar();
        P2PNode node = new P2PNode(config, new PeerManager(), chain,
                new ConsensusEngine(registry, 2), dids.get(localIndex), keys.get(localIndex).getPrivate());
        return new NodeFixture(node, chain);
    }

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) { return socket.getLocalPort(); }
    }

    private static List<Integer> freePorts(int count) throws Exception {
        Set<Integer> unique = new HashSet<>();
        while (unique.size() < count) unique.add(freePort());
        return new ArrayList<>(unique);
    }

    private static boolean allAtHeight(List<NodeFixture> nodes, int height) {
        return nodes.stream().allMatch(n -> n.chain.getAltura() == height);
    }

    private static void assertAllSameTip(List<NodeFixture> nodes) {
        String hash = nodes.get(0).chain.getUltimoBloco().getHash();
        for (NodeFixture node : nodes) {
            assertEquals(hash, node.chain.getUltimoBloco().getHash());
            assertTrue(node.chain.validarCadeia());
        }
    }

    private static boolean waitFor(BooleanSupplier condition, Duration timeout) throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) return true;
            Thread.sleep(20);
        }
        return condition.getAsBoolean();
    }

    private record NodeFixture(P2PNode node, Blockchain chain) { }
}
