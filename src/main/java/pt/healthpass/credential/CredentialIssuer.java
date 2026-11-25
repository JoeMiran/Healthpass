package pt.healthpass.credential;

import java.security.PrivateKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import pt.healthpass.crypto.CanonicalSerializer;
import pt.healthpass.crypto.SignatureService;
import pt.healthpass.identity.DID;
import pt.healthpass.identity.IdentityService;
import pt.healthpass.ledger.Blockchain;
import pt.healthpass.ledger.Transaction;
import pt.healthpass.ledger.TransactionType;
import pt.healthpass.storage.OffChainCredentialStore;

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
    private final OffChainCredentialStore credentialStore;

    /**
     * @param issuerDID DID do emissor
     * @param chavePrivada chave privada do emissor (mantida em segredo)
     * @param blockchain cadeia onde a transacao sera registada
     * @param identityService servico para confirmar que o emissor e autorizado
     */
    public CredentialIssuer(DID issuerDID, PrivateKey chavePrivada,
                            Blockchain blockchain, IdentityService identityService) {
        this(issuerDID, chavePrivada, blockchain, identityService, new OffChainCredentialStore());
    }

    public CredentialIssuer(DID issuerDID, PrivateKey chavePrivada,
                            Blockchain blockchain, IdentityService identityService,
                            OffChainCredentialStore credentialStore) {
        this.issuerDID = issuerDID;
        this.chavePrivada = chavePrivada;
        this.blockchain = blockchain;
        this.identityService = identityService;
        this.credentialStore = Objects.requireNonNull(credentialStore, "credentialStore obrigatorio");
    }

    /**
     * Assina a credencial com a chave privada do emissor e devolve a assinatura.
     *
     * @param credencial credencial a assinar
     * @return a assinatura em Base64
     */
    public String assinar(Credential credencial) {
        validarCampos(credencial);
        credencial.setSignature(null);
        byte[] dados = CanonicalSerializer.paraJsonSemAssinatura(credencial).getBytes(StandardCharsets.UTF_8);
        String assinatura = Base64.getEncoder().encodeToString(SignatureService.assinar(dados, chavePrivada));
        var chavePublica = identityService.getChavePublica(issuerDID);
        if (chavePublica == null || !SignatureService.verificar(dados,
                Base64.getDecoder().decode(assinatura), chavePublica)) {
            throw new IllegalArgumentException("Chave privada não corresponde à identidade emissora registada");
        }
        credencial.setSignature(assinatura);
        return assinatura;
    }

    /**
     * Emite a credencial: assina, calcula o hash e regista a transacao
     * {@code CREDENTIAL_ISSUED} na blockchain.
     *
     * @param credencial credencial a emitir
     */
    public void emitir(Credential credencial) {
        validarCampos(credencial);
        if (!identityService.ehEmissorAutorizado(issuerDID)) {
            throw new IllegalStateException("DID emissor nao esta registado/autorizado");
        }
        if (!issuerDID.toString().equals(credencial.getIssuerDID())) {
            throw new IllegalArgumentException("O issuerDID da credencial nao corresponde ao emissor autenticado");
        }
        if (identityService.procurar(DID.parse(credencial.getHolderDID())) == null) {
            throw new IllegalArgumentException("DID titular não está registado na cadeia");
        }
        assinar(credencial);
        CredentialTransactionData.Issuance registo = new CredentialTransactionData.Issuance(
                credencial.getCredentialId(), credencial.getIssuerDID(), credencial.getHolderDID(),
                credencial.calcularHash());
        String payload = CanonicalSerializer.paraJson(registo);
        String assinaturaTransacao = assinarTexto(payload);
        credentialStore.guardar(credencial);
        blockchain.adicionarTransacao(new Transaction(TransactionType.CREDENTIAL_ISSUED,
                payload, assinaturaTransacao));
    }

    /**
     * Revoga uma credencial ja emitida, registando uma transacao
     * {@code CREDENTIAL_REVOKED}.
     *
     * @param credentialId identificador da credencial a revogar
     */
    public void revogar(String credentialId) {
        if (credentialId == null || credentialId.isBlank()) throw new IllegalArgumentException("credentialId obrigatório");
        if (!identityService.ehEmissorAutorizado(issuerDID)) throw new IllegalStateException("Emissor nao autorizado");
        boolean emitidaPorEsteEmissor = blockchain.getCadeia().stream().flatMap(b -> b.getTransactions().stream())
                .filter(t -> t.getType() == TransactionType.CREDENTIAL_ISSUED)
                .map(t -> CanonicalSerializer.deJson(t.getPayload(), CredentialTransactionData.Issuance.class))
                .anyMatch(i -> credentialId.equals(i.credentialId()) && issuerDID.toString().equals(i.issuerDID()));
        if (!emitidaPorEsteEmissor) throw new IllegalArgumentException("Credencial emitida nao encontrada na cadeia");
        CredentialTransactionData.Revocation dados = new CredentialTransactionData.Revocation(
                credentialId, issuerDID.toString(), System.currentTimeMillis());
        String payload = CanonicalSerializer.paraJson(dados);
        blockchain.adicionarTransacao(new Transaction(TransactionType.CREDENTIAL_REVOKED,
                payload, assinarTexto(payload)));
    }

    private String assinarTexto(String texto) {
        return Base64.getEncoder().encodeToString(SignatureService.assinar(
                texto.getBytes(StandardCharsets.UTF_8), chavePrivada));
    }

    private void validarCampos(Credential credencial) {
        Objects.requireNonNull(credencial, "credencial obrigatoria");
        if (credencial.getCredentialId() == null || credencial.getCredentialId().isBlank()
                || credencial.getHolderDID() == null || credencial.getHolderDID().isBlank()
                || credencial.getIssuedAt() == null || credencial.getIssuedAt().isBlank()) {
            throw new IllegalArgumentException("credentialId, holderDID e issuedAt são obrigatórios");
        }
        DID.parse(credencial.getHolderDID());
        Instant.parse(credencial.getIssuedAt());
        if (credencial.getExpiresAt() != null) Instant.parse(credencial.getExpiresAt());
    }

}
