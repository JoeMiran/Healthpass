package pt.healthpass.identity;

/**
 * Papel de uma identidade na rede HealthPass.
 *
 * <p>Cada participante tem um papel que determina o que pode fazer. Isto e
 * essencial numa blockchain <b>permissionada</b>: so emissores autorizados
 * podem emitir credenciais e so validadores autorizados podem aprovar blocos.</p>
 *
 * <p><b>Origem:</b> novo (a TemplarCoin nao tinha papeis).</p>
 *
 * @author HealthPass
 */
public enum IdentityRole {

    /** Instituicao emissora de credenciais (ex.: hospital). */
    ISSUER,

    /** Titular de credenciais (ex.: utente). */
    HOLDER,

    /** No autorizado a validar blocos no consenso PoA. */
    VALIDATOR,

    /** Administrador da rede (pode autorizar emissores/validadores). */
    ADMIN
}
