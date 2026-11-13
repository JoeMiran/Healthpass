package pt.healthpass.ledger;

/**
 * Tipo de operacao registada numa transacao.
 *
 * <p>Ao contrario da TemplarCoin (transacoes monetarias), no HealthPass uma
 * transacao representa uma operacao da aplicacao:</p>
 * <ul>
 *   <li>{@code IDENTITY_REGISTERED} — registo de uma identidade (DID).</li>
 *   <li>{@code CREDENTIAL_ISSUED} — emissao de uma credencial (hash on-chain).</li>
 *   <li>{@code CREDENTIAL_REVOKED} — revogacao de uma credencial.</li>
 * </ul>
 *
 * <p><b>Origem:</b> novo (substitui o modelo monetario from/to/value).</p>
 *
 * @author HealthPass
 */
public enum TransactionType {

    /** Registo de uma nova identidade na rede. */
    IDENTITY_REGISTERED,

    /** Emissao de uma credencial. */
    CREDENTIAL_ISSUED,

    /** Revogacao de uma credencial. */
    CREDENTIAL_REVOKED
}
