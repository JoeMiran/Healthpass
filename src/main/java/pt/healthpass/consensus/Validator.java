package pt.healthpass.consensus;

import pt.healthpass.identity.DID;

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

    /**
     * @param did DID do validador
     */
    public Validator(DID did) {
        this.did = did;
    }

    /**
     * @return o DID do validador.
     */
    public DID getDid() {
        return did;
    }

    @Override
    public String toString() {
        return "Validator{" + did + '}';
    }
}
