package pt.healthpass.consensus;

import pt.healthpass.identity.DID;
import java.security.PublicKey;
import java.util.Objects;

/**
 * Validador autorizado.
 *
 * <p>Numa blockchain <b>permissionada</b> como o HealthPass, os validadores sao
 * conhecidos e previamente autorizados. Cada validador tem um DID e uma chave
 * publica, usada para verificar os seus votos assinados.</p>
 *
 * <p><b>Origem:</b> novo (a TemplarCoin nao tinha validadores; usava PoW).</p>
 *
 * @author HealthPass
 */
public class Validator {

    private final DID did;
    private final PublicKey publicKey;

    /**
     * @param did DID do validador
     */
    public Validator(DID did) {
        this(did, null);
    }

    public Validator(DID did, PublicKey publicKey) {
        this.did = Objects.requireNonNull(did, "DID do validador obrigatorio");
        this.publicKey = publicKey;
    }

    /**
     * @return o DID do validador.
     */
    public DID getDid() {
        return did;
    }

    public PublicKey getPublicKey() { return publicKey; }

    @Override
    public String toString() {
        return "Validator{" + did + '}';
    }
}
