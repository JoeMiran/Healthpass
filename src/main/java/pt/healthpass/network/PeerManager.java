package pt.healthpass.network;

import java.util.ArrayList;
import java.util.List;

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
    private final List<String> peers = new ArrayList<>();

    /**
     * Regista um peer.
     *
     * @param peer endereco no formato host:porta
     */
    public void adicionarPeer(String peer) {
        peers.add(peer);
    }

    /**
     * Envia uma mensagem para todos os peers (difusao).
     *
     * @param mensagem mensagem a difundir
     */
    // TODO (Etapa 7): serializar em JSON e enviar por socket para cada peer.
    public void difundir(NetworkMessage mensagem) {
        throw new UnsupportedOperationException("A implementar na Etapa 7");
    }

    /**
     * @return a lista de peers conhecidos.
     */
    public List<String> getPeers() {
        return peers;
    }
}
