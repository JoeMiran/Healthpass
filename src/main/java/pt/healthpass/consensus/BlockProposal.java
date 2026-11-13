package pt.healthpass.consensus;

import pt.healthpass.identity.DID;
import pt.healthpass.ledger.Block;

/**
 * Proposta de bloco.
 *
 * <p>Um validador autorizado (o <b>proponente</b>) constroi um bloco candidato e
 * propoe-no aos restantes. A proposta inclui o bloco e o DID de quem o propoe.</p>
 *
 * <p>Os restantes validadores recebem a proposta, verificam o bloco
 * (estrutura, hash anterior, Merkle Root, transacoes e assinaturas) e, se
 * estiver correta, emitem um {@link ValidatorVote}.</p>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public class BlockProposal {

    private final Block bloco;
    private final DID proponente;

    /**
     * @param bloco bloco candidato
     * @param proponente DID do validador que propoe
     */
    public BlockProposal(Block bloco, DID proponente) {
        this.bloco = bloco;
        this.proponente = proponente;
    }

    /**
     * @return o bloco proposto.
     */
    public Block getBloco() {
        return bloco;
    }

    /**
     * @return o DID do proponente.
     */
    public DID getProponente() {
        return proponente;
    }
}
