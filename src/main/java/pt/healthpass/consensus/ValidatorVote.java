package pt.healthpass.consensus;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.Objects;
import pt.healthpass.crypto.CanonicalSerializer;
import pt.healthpass.crypto.SignatureService;
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

    /** Cria um voto assinado com SHA256withRSA. */
    public static ValidatorVote assinar(DID validador, String hashBloco, boolean aprova,
                                        PrivateKey chavePrivada) {
        Objects.requireNonNull(validador, "validador obrigatório");
        Objects.requireNonNull(hashBloco, "hashBloco obrigatório");
        Objects.requireNonNull(chavePrivada, "chave privada obrigatória");
        VotoPayload payload = new VotoPayload(validador.toString(), hashBloco, aprova);
        byte[] dados = CanonicalSerializer.paraJson(payload).getBytes(StandardCharsets.UTF_8);
        String assinatura = Base64.getEncoder().encodeToString(SignatureService.assinar(dados, chavePrivada));
        return new ValidatorVote(validador, hashBloco, aprova, assinatura);
    }

    /** Verifica autorização, alvo do voto e assinatura criptográfica. */
    public boolean isValid(ValidatorRegistry registry, String hashEsperado) {
        if (registry == null || validador == null || hashBloco == null || assinatura == null
                || !hashBloco.equals(hashEsperado)) return false;
        Validator autorizado = registry.procurar(validador);
        if (autorizado == null || autorizado.getPublicKey() == null) return false;
        try {
            VotoPayload payload = new VotoPayload(validador.toString(), hashBloco, aprova);
            byte[] dados = CanonicalSerializer.paraJson(payload).getBytes(StandardCharsets.UTF_8);
            return SignatureService.verificar(dados, Base64.getDecoder().decode(assinatura), autorizado.getPublicKey());
        } catch (RuntimeException e) {
            return false;
        }
    }

    private record VotoPayload(String validador, String hashBloco, boolean aprova) { }
}
