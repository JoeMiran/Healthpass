package pt.healthpass.network;

/**
 * Tipo de mensagem trocada entre nos P2P.
 *
 * <p>Tipos previstos:</p>
 * <ul>
 *   <li>{@code HELLO} — apresentacao do no e sincronizacao de estado.</li>
 *   <li>{@code NEW_TRANSACTION} — difusao de uma nova transacao.</li>
 *   <li>{@code BLOCK_PROPOSAL} — proposta de bloco pelo proponente.</li>
 *   <li>{@code BLOCK_VOTE} — voto de um validador.</li>
 *   <li>{@code NEW_BLOCK} — difusao de um bloco aprovado.</li>
 *   <li>{@code GET_CHAIN_STATUS} — pedido de estado da cadeia.</li>
 *   <li>{@code GET_BLOCKS} — pedido de blocos em falta.</li>
 *   <li>{@code BLOCKS_RESPONSE} — resposta com blocos.</li>
 * </ul>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public enum MessageType {

    /** Apresentacao do no e troca de estado inicial. */
    HELLO,

    /** Difusao de uma nova transacao. */
    NEW_TRANSACTION,

    /** Proposta de bloco. */
    BLOCK_PROPOSAL,

    /** Voto de um validador. */
    BLOCK_VOTE,

    /** Difusao de um bloco aprovado. */
    NEW_BLOCK,

    /** Pedido do estado atual da cadeia. */
    GET_CHAIN_STATUS,

    /** Pedido de blocos em falta (sincronizacao). */
    GET_BLOCKS,

    /** Resposta com blocos. */
    BLOCKS_RESPONSE
}
