package pt.healthpass.network;

import java.util.List;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CopyOnWriteArrayList;
import pt.healthpass.crypto.CanonicalSerializer;

/**
 * Gestor de peers (nos vizinhos).
 *
 * <p>Mantem a lista de ligacoes abertas e permite enviar mensagens para todos
 * os peers (difusao) ou para um peer especifico. Tambem ajuda a recuperar
 * blocos em falta (sincronizacao da cadeia).</p>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public class PeerManager {

    /** Enderecos dos peers conhecidos, no formato host:porta. */
    private final List<String> peers = new CopyOnWriteArrayList<>();

    private static final int CONNECT_TIMEOUT_MS = 800;
    private static final int READ_TIMEOUT_MS = 1500;
    private static final int MAX_MESSAGE_CHARS = 1_048_576;

    /**
     * Regista um peer.
     *
     * @param peer endereco no formato host:porta
     */
    public void adicionarPeer(String peer) {
        if (peer != null && !peers.contains(peer)) peers.add(peer);
    }

    /**
     * Envia uma mensagem para todos os peers (difusao).
     *
     * @param mensagem mensagem a difundir
     */
    public void difundir(NetworkMessage mensagem) {
        for (String peer : peers) enviar(peer, mensagem);
    }

    /** Envia uma mensagem JSON numa ligação TCP curta para um peer. */
    public boolean enviar(String peer, NetworkMessage mensagem) {
        if (peer == null || mensagem == null) return false;
        try {
            String[] partes = peer.split(":", 2);
            if (partes.length != 2) return false;
            int porta = Integer.parseInt(partes[1]);
            if (porta < 1 || porta > 65535) return false;
            String json = CanonicalSerializer.paraJson(mensagem);
            if (json.length() > MAX_MESSAGE_CHARS) return false;
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(partes[0], porta), CONNECT_TIMEOUT_MS);
                socket.setSoTimeout(READ_TIMEOUT_MS);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                        socket.getOutputStream(), StandardCharsets.UTF_8));
                writer.write(json);
                writer.write('\n');
                writer.flush();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @return a lista de peers conhecidos.
     */
    public List<String> getPeers() {
        return List.copyOf(peers);
    }
}
