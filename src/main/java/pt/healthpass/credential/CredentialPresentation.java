package pt.healthpass.credential;

import pt.healthpass.crypto.CanonicalSerializer;

/**
 * Apresentacao de uma credencial.
 *
 * <p>Representa a credencial tal como o titular a apresenta a um verificador:
 * a credencial completa (off-chain) mais a prova de que e autentica. No caso
 * mais simples, a apresentacao inclui a credencial e a sua assinatura; em
 * cenarios futuros podera incluir apenas os campos necessarios (divulgacao
 * seletiva).</p>
 *
 * <p>Serve tambem de base para a exportacao da credencial (por exemplo, para
 * um codigo QR de demonstracao, ver {@code gui.HolderWalletView}).</p>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public class CredentialPresentation {

    /** A credencial apresentada. */
    private Credential credencial;

    /** Construtor vazio para serializacao JSON. */
    public CredentialPresentation() {
    }

    /**
     * @param credencial credencial a apresentar
     */
    public CredentialPresentation(Credential credencial) {
        this.credencial = credencial;
    }

    /**
     * @return a credencial apresentada.
     */
    public Credential getCredencial() {
        return credencial;
    }

    /**
     * Serializa a apresentacao para JSON (para exportacao/QR).
     *
     * @return o JSON da apresentacao
     */
    public String paraJson() {
        return CanonicalSerializer.paraJson(this);
    }
}
