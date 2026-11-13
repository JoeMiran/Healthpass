package pt.healthpass.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;

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
 * <b>SHA3-256withRSA</b> (o mesmo do original).</p>
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
    // TODO (Etapa 6): implementar com Signature.getInstance("SHA3-256withRSA").
    public static byte[] assinar(byte[] dados, PrivateKey chavePrivada) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }

    /**
     * Verifica uma assinatura com a chave publica do emissor.
     *
     * @param dados bytes originais
     * @param assinatura assinatura a validar
     * @param chavePublica chave publica do emissor
     * @return {@code true} se a assinatura for valida
     */
    // TODO (Etapa 6): implementar com Signature.initVerify/verify.
    public static boolean verificar(byte[] dados, byte[] assinatura, PublicKey chavePublica) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }
}
