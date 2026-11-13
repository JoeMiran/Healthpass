package pt.healthpass.network;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuracao de um no da rede.
 *
 * <p>Indica o endereco/porta onde o no escuta e a lista de peers iniciais a
 * que se deve ligar.</p>
 *
 * <p>Topologia de demonstracao prevista:</p>
 * <pre>
 *   No A  127.0.0.1:5001  (Hospital A)
 *   No B  127.0.0.1:5002  (Hospital B)
 *   No C  127.0.0.1:5003  (Validador adicional)
 * </pre>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public class NodeConfig {

    private final String host;
    private final int porta;
    private final List<String> peersIniciais = new ArrayList<>();

    /**
     * @param host endereco de escuta (ex.: "127.0.0.1")
     * @param porta porta de escuta (ex.: 5001)
     */
    public NodeConfig(String host, int porta) {
        this.host = host;
        this.porta = porta;
    }

    /**
     * Adiciona um peer inicial no formato {@code host:porta}.
     *
     * @param peer peer a adicionar
     */
    public void adicionarPeer(String peer) {
        peersIniciais.add(peer);
    }

    /**
     * @return o host de escuta.
     */
    public String getHost() {
        return host;
    }

    /**
     * @return a porta de escuta.
     */
    public int getPorta() {
        return porta;
    }

    /**
     * @return a lista de peers iniciais.
     */
    public List<String> getPeersIniciais() {
        return peersIniciais;
    }
}
