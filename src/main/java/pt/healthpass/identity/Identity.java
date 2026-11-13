package pt.healthpass.identity;

import java.security.PublicKey;

/**
 * Identidade de um participante do HealthPass.
 *
 * <p>Agrega os elementos de uma identidade digital:</p>
 * <ul>
 *   <li>o {@link DID} (identificador tecnico);</li>
 *   <li>o {@link IdentityRole} (papel na rede);</li>
 *   <li>a chave publica (usada para verificar assinaturas).</li>
 * </ul>
 *
 * <p>A chave <b>privada</b> correspondente nao faz parte deste objeto: fica
 * guardada em seguranca pelo dono (ver {@code storage.KeyStoreService}).</p>
 *
 * <p><b>Origem:</b> evolucao de {@code digitalID.core.DigitalID} da TemplarCoin,
 * que apenas tinha nome + chave publica + assinatura.</p>
 *
 * @author HealthPass
 */
public class Identity {

    private final DID did;
    private final IdentityRole role;
    private final PublicKey publicKey;

    /**
     * Cria uma identidade.
     *
     * @param did identificador descentralizado
     * @param role papel na rede
     * @param publicKey chave publica do participante
     */
    public Identity(DID did, IdentityRole role, PublicKey publicKey) {
        this.did = did;
        this.role = role;
        this.publicKey = publicKey;
    }

    /**
     * @return o DID desta identidade.
     */
    public DID getDid() {
        return did;
    }

    /**
     * @return o papel desta identidade.
     */
    public IdentityRole getRole() {
        return role;
    }

    /**
     * @return a chave publica desta identidade.
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    @Override
    public String toString() {
        return "Identity{" + did + ", role=" + role + '}';
    }
}
