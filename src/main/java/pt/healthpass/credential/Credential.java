package pt.healthpass.credential;

import pt.healthpass.crypto.CanonicalSerializer;
import pt.healthpass.crypto.HashUtil;

/**
 * Credencial digital generica.
 *
 * <p>Uma credencial e um documento assinado por um emissor que atribui uma
 * caracteristica ao titular. Exemplo (dados <b>sinteticos</b>):</p>
 *
 * <pre>
 * {
 *   "credentialId": "cred-demo-0001",
 *   "type": "VaccinationCredential",
 *   "issuerDID": "did:healthpass:issuer-demo-01",
 *   "holderDID": "did:healthpass:holder-demo-07",
 *   "issuedAt": "2026-10-01T10:00:00Z",
 *   "expiresAt": null,
 *   "signature": "DIGITAL_SIGNATURE"
 * }
 * </pre>
 *
 * <p><b>Assinatura:</b> e calculada sobre a representacao canonica da
 * credencial (JSON determinista) <b>sem</b> o campo {@code signature}. Ver
 * {@code crypto.CanonicalSerializer} e {@code crypto.SignatureService}.</p>
 *
 * <p>Esta classe e a base; {@link VaccinationCredential} especializa-a.</p>
 *
 * <p><b>Origem:</b> novo (nao existia nada de credenciais na TemplarCoin).</p>
 *
 * @author HealthPass
 */
public class Credential {

    /** Identificador tecnico da credencial (ex.: "cred-demo-0001"). */
    protected String credentialId;

    /** Tipo da credencial (ex.: "VaccinationCredential"). */
    protected String type;

    /** DID do emissor. */
    protected String issuerDID;

    /** DID do titular. */
    protected String holderDID;

    /** Data/hora de emissao (formato ISO-8601, texto). */
    protected String issuedAt;

    /** Data/hora de expiracao, ou {@code null} se nao expirar. */
    protected String expiresAt;

    /** Assinatura digital do emissor, em Base64. */
    protected String signature;

    /** Construtor vazio para serializacao JSON (Jackson). */
    public Credential() {
    }

    /**
     * @return o identificador da credencial.
     */
    public String getCredentialId() {
        return credentialId;
    }

    /**
     * @return o tipo da credencial.
     */
    public String getType() {
        return type;
    }

    /**
     * @return o DID do emissor.
     */
    public String getIssuerDID() {
        return issuerDID;
    }

    /**
     * @return o DID do titular.
     */
    public String getHolderDID() {
        return holderDID;
    }

    /**
     * @return a assinatura digital (Base64).
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @return a data de expiracao, ou {@code null}.
     */
    public String getExpiresAt() {
        return expiresAt;
    }

    public String getIssuedAt() { return issuedAt; }

    public void setCredentialId(String credentialId) { this.credentialId = credentialId; }
    public void setType(String type) { this.type = type; }
    public void setIssuerDID(String issuerDID) { this.issuerDID = issuerDID; }
    public void setHolderDID(String holderDID) { this.holderDID = holderDID; }
    public void setIssuedAt(String issuedAt) { this.issuedAt = issuedAt; }
    public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
    public void setSignature(String signature) { this.signature = signature; }

    /**
     * Calcula o hash SHA-256 da credencial (o que vai para a blockchain).
     *
     * <p>On-chain guardamos apenas este hash, nunca os dados completos.</p>
     *
     * @return o hash da representacao canonica, sem o campo signature
     */
    public String calcularHash() {
        return HashUtil.hash(CanonicalSerializer.paraJsonSemAssinatura(this));
    }

    @Override
    public String toString() {
        return "Credential{" + credentialId + ", type=" + type
                + ", issuer=" + issuerDID + ", holder=" + holderDID + '}';
    }
}
