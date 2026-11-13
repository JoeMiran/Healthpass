package pt.healthpass.credential;

/**
 * Estado de uma credencial.
 *
 * <p>O estado e registado <b>on-chain</b> (na blockchain) para que qualquer
 * verificador possa confirmar se a credencial continua valida. Os dados
 * completos da credencial ficam <b>off-chain</b> (armazenamento local).</p>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public enum CredentialStatus {

    /** Credencial emitida e valida. */
    ACTIVE,

    /** Credencial revogada pelo emissor. */
    REVOKED,

    /** Credencial cujo prazo de validade terminou. */
    EXPIRED
}
