package pt.healthpass.consensus;

import java.util.List;
import java.util.Objects;
import java.util.Map;
import java.util.stream.Collectors;

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
        this.hashBloco = Objects.requireNonNull(hashBloco, "hashBloco obrigatório");
        this.votos = List.copyOf(Objects.requireNonNull(votos, "votos obrigatórios"));
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
    public int contarAprovacoes(ValidatorRegistry registry) {
        if (registry == null) return 0;
        Map<String, Long> porValidador = votos.stream().filter(Objects::nonNull)
                .filter(v -> v.getValidador() != null)
                .collect(Collectors.groupingBy(v -> v.getValidador().toString(), Collectors.counting()));
        return (int) votos.stream().filter(Objects::nonNull)
                .filter(v -> v.getValidador() != null)
                .filter(v -> porValidador.getOrDefault(v.getValidador().toString(), 0L) == 1L)
                .filter(v -> v.isAprova() && v.isValid(registry, hashBloco)).count();
    }
}
