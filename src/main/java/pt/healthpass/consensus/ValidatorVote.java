package pt.healthpass.consensus;

import pt.healthpass.identity.DID;

/**
 * Voto de um validador.
 *
 * <p>Cada validador autorizado emite um voto assinado sobre uma proposta de
 * bloco. A assinatura garante que o voto e autentico e que nao foi alterado.
 * Os votos sao contados para o quorum.</p>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public class ValidatorVote {

    private final DID validador;
    private final String hashBloco;
    private final boolean aprova;
    private final String assinatura;

    /**
     * @param validador DID do validador que vota
     * @param hashBloco hash do bloco alvo do voto
     * @param aprova se o validador aprova o bloco
     * @param assinatura assinatura do voto (Base64)
     */
    public ValidatorVote(DID validador, String hashBloco, boolean aprova, String assinatura) {
        this.validador = validador;
        this.hashBloco = hashBloco;
        this.aprova = aprova;
        this.assinatura = assinatura;
    }

    /**
     * @return o DID do validador.
     */
    public DID getValidador() {
        return validador;
    }

    /**
     * @return o hash do bloco votado.
     */
    public String getHashBloco() {
        return hashBloco;
    }

    /**
     * @return {@code true} se o voto e de aprovacao.
     */
    public boolean isAprova() {
        return aprova;
    }

    /**
     * @return a assinatura do voto.
     */
    public String getAssinatura() {
        return assinatura;
    }
}
