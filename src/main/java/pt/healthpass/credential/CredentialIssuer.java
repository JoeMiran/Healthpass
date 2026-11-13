package pt.healthpass.credential;

import java.security.PrivateKey;
import pt.healthpass.identity.DID;
import pt.healthpass.identity.IdentityService;
import pt.healthpass.ledger.Blockchain;

/**
 * Emissor de credenciais.
 *
 * <p>Responsabilidades:</p>
 * <ol>
 *   <li>Criar uma credencial (ex.: {@link VaccinationCredential}).</li>
 *   <li>Assina-la com a chave <b>privada</b> do emissor.</li>
 *   <li>Criar uma transacao {@code CREDENTIAL_ISSUED} com o <b>hash</b> da
 *       credencial (on-chain).</li>
 *   <li>Submeter a transacao na blockchain.</li>
 * </ol>
 *
 * <p>A credencial completa fica <b>off-chain</b> (ver
 * {@code storage.OffChainCredentialStore}). A chave privada nunca e
 * transmitida.</p>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public class CredentialIssuer {

    private final DID issuerDID;
    private final PrivateKey chavePrivada;
    private final Blockchain blockchain;
    private final IdentityService identityService;

    /**
     * @param issuerDID DID do emissor
     * @param chavePrivada chave privada do emissor (mantida em segredo)
     * @param blockchain cadeia onde a transacao sera registada
     * @param identityService servico para confirmar que o emissor e autorizado
     */
    public CredentialIssuer(DID issuerDID, PrivateKey chavePrivada,
                            Blockchain blockchain, IdentityService identityService) {
        this.issuerDID = issuerDID;
        this.chavePrivada = chavePrivada;
        this.blockchain = blockchain;
        this.identityService = identityService;
    }

    /**
     * Assina a credencial com a chave privada do emissor e devolve a assinatura.
     *
     * @param credencial credencial a assinar
     * @return a assinatura em Base64
     */
    // TODO (Etapa 6): CanonicalSerializer (sem signature) + SignatureService.assinar.
    public String assinar(Credential credencial) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }

    /**
     * Emite a credencial: assina, calcula o hash e regista a transacao
     * {@code CREDENTIAL_ISSUED} na blockchain.
     *
     * @param credencial credencial a emitir
     */
    // TODO (Etapa 6): validar emissor autorizado, assinar, criar Transaction e submeter.
    public void emitir(Credential credencial) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }

    /**
     * Revoga uma credencial ja emitida, registando uma transacao
     * {@code CREDENTIAL_REVOKED}.
     *
     * @param credentialId identificador da credencial a revogar
     */
    // TODO (Etapa 6): criar Transaction de CREDENTIAL_REVOKED e submeter.
    public void revogar(String credentialId) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }
}
