package pt.healthpass.network;

/**
 * Mensagem da rede P2P.
 *
 * <p>Envelope de comunicacao com tres partes:</p>
 * <ul>
 *   <li>{@code id} — identificador unico da mensagem (para detetar repeticoes);</li>
 *   <li>{@code type} — ver {@link MessageType};</li>
 *   <li>{@code payload} — conteudo (JSON).</li>
 * </ul>
 *
 * <p><b>Seguranca:</b> a serializacao e feita em <b>JSON</b>, nunca com
 * serializacao Java de objetos. Antes de processar, validar tamanho, formato e
 * tipo; usar timeouts; e ignorar mensagens repetidas (usando o {@code id}).</p>
 *
 * <p>A rececao de um bloco <b>nunca</b> implica aceitacao automatica: cada no
 * verifica independentemente o conteudo e o certificado de consenso.</p>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public class NetworkMessage {

    private String id;
    private MessageType type;
    private String payload;

    /** Construtor vazio para serializacao JSON. */
    public NetworkMessage() {
    }

    /**
     * @param id identificador unico da mensagem
     * @param type tipo da mensagem
     * @param payload conteudo em JSON
     */
    public NetworkMessage(String id, MessageType type, String payload) {
        this.id = id;
        this.type = type;
        this.payload = payload;
    }

    /**
     * @return o identificador da mensagem.
     */
    public String getId() {
        return id;
    }

    /**
     * @return o tipo da mensagem.
     */
    public MessageType getType() {
        return type;
    }

    /**
     * @return o conteudo da mensagem.
     */
    public String getPayload() {
        return payload;
    }
}
