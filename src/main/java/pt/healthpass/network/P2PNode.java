package pt.healthpass.network;

import pt.healthpass.ledger.Blockchain;

/**
 * No da rede P2P.
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Abrir um {@code ServerSocket} para aceitar ligacoes de outros nos.</li>
 *   <li>Criar uma thread por ligacao (para tratar varios peers em paralelo).</li>
 *   <li>Ler e escrever mensagens JSON ({@link NetworkMessage}).</li>
 *   <li>Encaminhar cada mensagem para o tratamento adequado (transacao, bloco,
 *       proposta, voto).</li>
 * </ul>
 *
 * <p><b>Seguranca:</b> validar o tamanho/formato das mensagens, aplicar
 * timeouts e ignorar mensagens repetidas. Um bloco recebido nunca e aceite
 * automaticamente: passa pela validacao local e pelo consenso.</p>
 *
 * <p><b>Origem:</b> novo — a TemplarCoin nao tinha rede; cada instancia lia o
 * mesmo ficheiro {@code blockchain.bc} local.</p>
 *
 * @author HealthPass
 */
public class P2PNode {

    private final NodeConfig config;
    private final PeerManager peerManager;
    private final Blockchain blockchain;

    /**
     * @param config configuracao do no (endereco, porta, peers)
     * @param peerManager gestor de peers
     * @param blockchain cadeia local deste no
     */
    public P2PNode(NodeConfig config, PeerManager peerManager, Blockchain blockchain) {
        this.config = config;
        this.peerManager = peerManager;
        this.blockchain = blockchain;
    }

    /**
     * Arranca o no: abre o servidor e liga-se aos peers iniciais.
     */
    // TODO (Etapa 7): ServerSocket + thread de aceitacao + ligacoes iniciais.
    public void arrancar() {
        throw new UnsupportedOperationException("A implementar na Etapa 7");
    }

    /**
     * Para o no e fecha as ligacoes.
     */
    // TODO (Etapa 7): fechar sockets e terminar threads.
    public void parar() {
        throw new UnsupportedOperationException("A implementar na Etapa 7");
    }

    /**
     * Trata uma mensagem recebida.
     *
     * @param mensagem mensagem recebida de um peer
     */
    // TODO (Etapa 7): encaminhar por tipo e validar antes de aplicar.
    public void tratarMensagem(NetworkMessage mensagem) {
        throw new UnsupportedOperationException("A implementar na Etapa 7");
    }

    /**
     * @return a configuracao do no.
     */
    public NodeConfig getConfig() {
        return config;
    }
}
