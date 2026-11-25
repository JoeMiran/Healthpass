package pt.healthpass.credential;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import pt.healthpass.crypto.CanonicalSerializer;
import pt.healthpass.crypto.SignatureService;
import pt.healthpass.identity.DID;
import pt.healthpass.ledger.Transaction;
import pt.healthpass.ledger.TransactionType;

import pt.healthpass.identity.IdentityService;
import pt.healthpass.ledger.Blockchain;

/**
 * Verificador de credenciais.
 *
 * <p><b>Principio importante:</b> a verificacao tem varias partes
 * independentes. Nao basta uma delas passar. Apresentamos cada resultado
 * separadamente, para se perceber exatamente porque uma credencial e aceite ou
 * rejeitada:</p>
 * <ul>
 *   <li>Assinatura valida ou invalida.</li>
 *   <li>Hash correspondente ou divergente (integridade).</li>
 *   <li>Emissor autorizado ou desconhecido.</li>
 *   <li>Credencial ativa ou revogada (estado on-chain).</li>
 *   <li>Dentro ou fora do prazo de validade.</li>
 * </ul>
 *
 * <p>A credencial so e considerada <b>tecnicamente valida</b> quando todas as
 * verificacoes obrigatorias passam.</p>
 *
 * <p><b>AVISO:</b> isto e validacao <b>tecnica</b> (criptografica), nao
 * clinica. O sistema nao avalia se a vacinacao e clinicamente adequada.</p>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public class CredentialVerifier {

    private final Blockchain blockchain;
    private final IdentityService identityService;

    /**
     * @param blockchain cadeia para consultar o estado da credencial
     * @param identityService servico para confirmar o emissor
     */
    public CredentialVerifier(Blockchain blockchain, IdentityService identityService) {
        this.blockchain = blockchain;
        this.identityService = identityService;
    }

    /**
     * Resultado detalhado de uma verificacao.
     *
     * @param assinaturaValida assinatura criptografica valida
     * @param hashCorresponde hash da credencial coincide com o registado on-chain
     * @param emissorAutorizado o emissor e um emissor autorizado
     * @param ativa a credencial nao esta revogada
     * @param dentroValidade a credencial esta dentro do prazo
     */
    public record Resultado(boolean assinaturaValida, boolean hashCorresponde,
                            boolean emissorAutorizado, boolean ativa,
                            boolean dentroValidade) {

        /**
         * @return {@code true} se todas as verificacoes obrigatorias passaram.
         */
        public boolean tecnicamenteValida() {
            return assinaturaValida && hashCorresponde && emissorAutorizado
                    && ativa && dentroValidade;
        }
    }

    /**
     * Executa todas as verificacoes e devolve o resultado detalhado.
     *
     * @param credencial credencial a verificar
     * @return o resultado com cada verificacao separada
     */
    public Resultado verificar(Credential credencial) {
        if (credencial == null) return new Resultado(false, false, false, false, false);
        DID issuer;
        try {
            issuer = DID.parse(credencial.getIssuerDID());
        } catch (RuntimeException e) {
            return new Resultado(false, false, false, false, false);
        }
        var publicKey = identityService.getChavePublica(issuer);
        boolean autorizado = identityService.ehEmissorAutorizado(issuer) && publicKey != null;

        boolean assinaturaValida = false;
        try {
            byte[] dadosAssinados = CanonicalSerializer.paraJsonSemAssinatura(credencial)
                    .getBytes(StandardCharsets.UTF_8);
            assinaturaValida = credencial.getSignature() != null && publicKey != null
                    && SignatureService.verificar(dadosAssinados,
                            Base64.getDecoder().decode(credencial.getSignature()), publicKey);
        } catch (RuntimeException ignored) { }

        CredentialTransactionData.Issuance emissao = null;
        if (credencial.getCredentialId() != null) {
            for (var bloco : blockchain.getCadeia()) {
                for (Transaction tx : bloco.getTransactions()) {
                    if (tx.getType() != TransactionType.CREDENTIAL_ISSUED) continue;
                    try {
                        var candidato = CanonicalSerializer.deJson(tx.getPayload(), CredentialTransactionData.Issuance.class);
                        if (!credencial.getCredentialId().equals(candidato.credentialId())) continue;
                        boolean assinaturaTx = publicKey != null && issuer.toString().equals(candidato.issuerDID())
                                && SignatureService.verificar(tx.getPayload().getBytes(StandardCharsets.UTF_8),
                                        Base64.getDecoder().decode(tx.getSignature()), publicKey);
                        if (assinaturaTx) emissao = candidato;
                    } catch (RuntimeException ignored) { }
                }
            }
        }
        boolean hashCorresponde = emissao != null
                && emissao.credentialHash().equals(credencial.calcularHash())
                && emissao.issuerDID().equals(credencial.getIssuerDID())
                && emissao.holderDID().equals(credencial.getHolderDID());

        boolean revogada = false;
        if (emissao != null && publicKey != null) {
            for (var bloco : blockchain.getCadeia()) {
                for (Transaction tx : bloco.getTransactions()) {
                    if (tx.getType() != TransactionType.CREDENTIAL_REVOKED) continue;
                    try {
                        var revogacao = CanonicalSerializer.deJson(tx.getPayload(), CredentialTransactionData.Revocation.class);
                        if (credencial.getCredentialId().equals(revogacao.credentialId())
                                && issuer.toString().equals(revogacao.issuerDID())
                                && SignatureService.verificar(tx.getPayload().getBytes(StandardCharsets.UTF_8),
                                        Base64.getDecoder().decode(tx.getSignature()), publicKey)) {
                            revogada = true;
                        }
                    } catch (RuntimeException ignored) { }
                }
            }
        }
        boolean ativa = emissao != null && !revogada;
        boolean dentroValidade;
        try {
            Instant issuedAt = Instant.parse(credencial.getIssuedAt());
            Instant agora = Instant.now();
            dentroValidade = !issuedAt.isAfter(agora)
                    && (credencial.getExpiresAt() == null
                        || agora.isBefore(Instant.parse(credencial.getExpiresAt())));
        } catch (RuntimeException e) {
            dentroValidade = false;
        }
        return new Resultado(assinaturaValida, hashCorresponde, autorizado, ativa, dentroValidade);
    }
}
