package pt.healthpass.credential;

/** Formatos mínimos publicados on-chain; nunca contêm o corpo clínico. */
final class CredentialTransactionData {
    private CredentialTransactionData() { }

    record Issuance(String credentialId, String issuerDID, String holderDID, String credentialHash) { }
    record Revocation(String credentialId, String issuerDID, long revokedAt) { }
}
