package pt.healthpass.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.GeneralSecurityException;
import java.security.Signature;
import java.util.Objects;

/**
 * Servico de assinatura digital.
 *
 * <p><b>Para que serve a assinatura digital?</b><br>
 * Assinar um documento com a chave <b>privada</b> permite que qualquer pessoa,
 * usando a chave <b>publica</b> correspondente, confirme duas coisas:</p>
 * <ol>
 *   <li><b>Autenticidade:</b> quem assinou possui a chave privada.</li>
 *   <li><b>Integridade:</b> o documento nao foi alterado depois de assinado.</li>
 * </ol>
 *
 * <p>No HealthPass, o emissor de uma credencial assina-a com a sua chave
 * privada. O verificador usa a chave publica do emissor para validar a
 * assinatura. A chave privada <b>nunca</b> sai do emissor nem entra na
 * blockchain.</p>
 *
 * <p><b>Origem:</b> adaptado de {@code utils.SecurityUtils.sign/verifySign} da
 * TemplarCoin. Por decisao do projeto mantem-se o algoritmo
 * <b>SHA256withRSA</b>, conforme a decisão criptográfica do HealthPass. A
 * TemplarCoin original usava SHA3-256withRSA.</p>
 *
 * @author HealthPass
 */
public final class SignatureService {

    /** Classe utilitaria: nao deve ser instanciada. */
    private SignatureService() {
        // Construtor privado.
    }

    /**
     * Assina dados com uma chave privada.
     *
     * @param dados bytes a assinar
     * @param chavePrivada chave privada do emissor
     * @return a assinatura em bytes
     */
    public static byte[] assinar(byte[] dados, PrivateKey chavePrivada) {
        Objects.requireNonNull(dados, "dados nao podem ser null");
        Objects.requireNonNull(chavePrivada, "chavePrivada nao pode ser null");
        try {
            Signature assinatura = Signature.getInstance("SHA256withRSA");
            assinatura.initSign(chavePrivada);
            assinatura.update(dados);
            return assinatura.sign();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Falha ao assinar dados com RSA", e);
        }
    }

    /**
     * Verifica uma assinatura com a chave publica do emissor.
     *
     * @param dados bytes originais
     * @param assinatura assinatura a validar
     * @param chavePublica chave publica do emissor
     * @return {@code true} se a assinatura for valida
     */
    public static boolean verificar(byte[] dados, byte[] assinatura, PublicKey chavePublica) {
        if (dados == null || assinatura == null || chavePublica == null) return false;
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(chavePublica);
            verifier.update(dados);
            return verifier.verify(assinatura);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            return false;
        }
    }
}
