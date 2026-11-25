package pt.healthpass.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pt.healthpass.app.P2PNodeApplication;
import pt.healthpass.crypto.CanonicalSerializer;
import pt.healthpass.identity.DID;
import pt.healthpass.ledger.TransactionType;
import pt.healthpass.storage.KeyStoreService;

class P2PMultiProcessIntegrationTest {

    private static final String PASSWORD = "demo-only-test-password";
    @TempDir Path tempDir;

    @Test
    void separateJvmNodesReachConsensusAndRestartedProcessSynchronizes() throws Exception {
        List<Integer> ports = freePorts(3);
        Path keysDir = tempDir.resolve("keys");
        Path registryFile = tempDir.resolve("validators.json");
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        List<KeyPair> keys = List.of(generator.generateKeyPair(), generator.generateKeyPair(), generator.generateKeyPair());
        List<DID> dids = List.of(new DID("validator-a"), new DID("validator-b"), new DID("validator-c"));
        KeyStoreService keyStore = new KeyStoreService(keysDir);
        List<P2PNodeApplication.ValidatorEntry> registry = new ArrayList<>();
        for (int i = 0; i < dids.size(); i++) {
            keyStore.guardarChavePublica(keys.get(i).getPublic(), "node-" + i);
            keyStore.guardarChavePrivada(keys.get(i).getPrivate(), "node-" + i, PASSWORD);
            registry.add(new P2PNodeApplication.ValidatorEntry(dids.get(i).toString(),
                    Base64.getEncoder().encodeToString(keys.get(i).getPublic().getEncoded())));
        }
        java.nio.file.Files.writeString(registryFile, CanonicalSerializer.paraJson(registry));

        List<NodeProcess> processes = new ArrayList<>();
        try {
            NodeProcess a = startNode(0, ports, dids, registryFile, keysDir);
            processes.add(a);
            NodeProcess b = startNode(1, ports, dids, registryFile, keysDir);
            processes.add(b);
            NodeProcess c = startNode(2, ports, dids, registryFile, keysDir);
            processes.add(c);
            a.command("hello"); b.command("hello"); c.command("hello");

            String payload1 = "{\"did\":\"did:healthpass:process-one\"}";
            assertEquals("TX_SUBMITTED true", a.command(txCommand(payload1, 2001)));
            assertTrue(waitFor(() -> a.status().height == 0 && b.status().pending == 1
                    && c.status().pending == 1, Duration.ofSeconds(8)));
            assertTrue(a.command("propose").startsWith("PROPOSAL "));
            assertTrue(waitFor(() -> a.status().height == 1 && b.status().height == 1
                    && c.status().height == 1, Duration.ofSeconds(10)));
            assertEquals(a.status().hash, b.status().hash);
            assertEquals(a.status().hash, c.status().hash);

            c.stop();
            processes.remove(c);
            assertEquals("TX_SUBMITTED true", a.command(txCommand(
                    "{\"did\":\"did:healthpass:process-two\"}", 2002)));
            assertTrue(waitFor(() -> b.status().pending == 1, Duration.ofSeconds(8)));
            assertTrue(b.command("propose").startsWith("PROPOSAL "));
            assertTrue(waitFor(() -> a.status().height == 2 && b.status().height == 2,
                    Duration.ofSeconds(10)));

            NodeProcess restartedC = startNode(2, ports, dids, registryFile, keysDir);
            processes.add(restartedC);
            assertTrue(waitFor(() -> restartedC.status().height == 2, Duration.ofSeconds(10)));
            assertEquals(a.status().hash, restartedC.status().hash);
        } finally {
            for (NodeProcess process : List.copyOf(processes)) process.stopQuietly();
        }
    }

    private NodeProcess startNode(int index, List<Integer> ports, List<DID> dids,
                                  Path registryFile, Path keysDir) throws Exception {
        List<String> peers = new ArrayList<>();
        for (int i = 0; i < ports.size(); i++) if (i != index) peers.add("127.0.0.1:" + ports.get(i));
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        ProcessBuilder builder = new ProcessBuilder(java, "-cp", classpath,
                P2PNodeApplication.class.getName(),
                "--id", "node-" + index,
                "--port", ports.get(index).toString(),
                "--data", tempDir.resolve("node-" + index).toString(),
                "--key-dir", keysDir.toString(),
                "--validator-did", dids.get(index).toString(),
                "--registry", registryFile.toString(),
                "--peers", String.join(",", peers));
        builder.environment().put("HEALTHPASS_NODE_KEY_PASSWORD", PASSWORD);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        NodeProcess node = new NodeProcess(process);
        String ready = node.reader.readLine();
        if (ready == null || !ready.startsWith("READY ")) {
            node.stopQuietly();
            throw new IllegalStateException("Nó filho não iniciou: " + ready);
        }
        return node;
    }

    private static String txCommand(String payload, long timestamp) {
        // A assinatura é sintética neste teste de transporte; o consenso verifica estrutura e hash da transação.
        String encodedPayload = Base64.getEncoder().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
        return "tx IDENTITY_REGISTERED " + encodedPayload + " " + Base64.getEncoder().encodeToString(
                ("test-signature-" + timestamp).getBytes(StandardCharsets.UTF_8));
    }

    private static List<Integer> freePorts(int count) throws Exception {
        Set<Integer> unique = new HashSet<>();
        while (unique.size() < count) {
            try (ServerSocket socket = new ServerSocket(0)) { unique.add(socket.getLocalPort()); }
        }
        return new ArrayList<>(unique);
    }

    private static boolean waitFor(java.util.function.BooleanSupplier condition, Duration timeout)
            throws InterruptedException {
        long deadline = System.nanoTime() + timeout.toNanos();
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) return true;
            Thread.sleep(50);
        }
        return condition.getAsBoolean();
    }

    private static final class NodeProcess {
        final Process process;
        final BufferedReader reader;
        final BufferedWriter writer;

        NodeProcess(Process process) {
            this.process = process;
            this.reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream(), StandardCharsets.UTF_8));
        }

        synchronized String command(String command) throws Exception {
            writer.write(command);
            writer.newLine();
            writer.flush();
            String response = reader.readLine();
            if (response == null) throw new IllegalStateException("Processo do nó terminou inesperadamente");
            return response;
        }

        Status status() {
            try {
                String[] fields = command("status").split(" ");
                return new Status(Integer.parseInt(fields[1]), fields[2], Integer.parseInt(fields[3]));
            } catch (Exception e) {
                return new Status(-1, "-", -1);
            }
        }

        void stop() throws Exception {
            if (!process.isAlive()) return;
            command("stop");
            if (!process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)) process.destroyForcibly();
        }

        void stopQuietly() {
            try { stop(); } catch (Exception ignored) { process.destroyForcibly(); }
        }
    }

    private record Status(int height, String hash, int pending) { }
}
