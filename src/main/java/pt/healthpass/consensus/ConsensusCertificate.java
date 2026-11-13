package pt.healthpass.consensus;

import java.util.List;

/**
 * Certificado de consenso.
 *
 * <p>Reune os votos que aprovaram um bloco. E guardado <b>separado</b> do bloco
 * (associado a ele) para <b>evitar dependencia circular</b>: se os votos
 * entrassem no calculo do hash do bloco, e o hash do bloco estivesse nos votos,
 * nunca haveria um valor final.</p>
 *
 * <p>O certificado permite a qualquer no verificar que o bloco teve o quorum
 * necessario, conferindo as assinaturas dos validadores.</p>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public class ConsensusCertificate {

    /** Hash do bloco a que este certificado diz respeito. */
    private final String hashBloco;

    /** Votos recolhidos. */
    private final List<ValidatorVote> votos;

    /**
     * @param hashBloco hash do bloco aprovado
     * @param votos votos recolhidos
     */
    public ConsensusCertificate(String hashBloco, List<ValidatorVote> votos) {
        this.hashBloco = hashBloco;
        this.votos = votos;
    }

    /**
     * @return o hash do bloco aprovado.
     */
    public String getHashBloco() {
        return hashBloco;
    }

    /**
     * @return a lista de votos.
     */
    public List<ValidatorVote> getVotos() {
        return votos;
    }

    /**
     * Conta os votos de aprovacao validos (validadores autorizados e
     * assinaturas corretas).
     *
     * @param registry registo de validadores autorizados
     * @return o numero de votos de aprovacao validos
     */
    // TODO (Etapa 7): filtrar votos duplicados/invalidos e contar aprovacoes.
    public int contarAprovacoes(ValidatorRegistry registry) {
        throw new UnsupportedOperationException("A implementar na Etapa 7");
    }
}
