package pt.healthpass.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CanonicalSerializerTest {

    @Test
    void sortsPropertiesAndPreservesUtf8Text() {
        String json = CanonicalSerializer.paraJson(new Sample("ação", "demo"));
        assertEquals("{\"alpha\":\"ação\",\"zulu\":\"demo\"}", json);
        assertEquals(json, CanonicalSerializer.paraJson(new Sample("ação", "demo")));
    }

    @Test
    void credentialSigningRepresentationOmitsOnlySignature() {
        String json = CanonicalSerializer.paraJsonSemAssinatura(
                new SignedSample("cred-1", "conteúdo-á", "assinatura-secreta"));
        assertTrue(json.contains("\"credentialId\":\"cred-1\""));
        assertTrue(json.contains("conteúdo-á"));
        assertFalse(json.contains("signature"));
        assertFalse(json.contains("assinatura-secreta"));
        assertEquals(json, CanonicalSerializer.paraJsonSemAssinatura(
                new SignedSample("cred-1", "conteúdo-á", "assinatura-diferente")));
    }

    public static class Sample {
        private final String zulu;
        private final String alpha;

        Sample(String alpha, String zulu) {
            this.alpha = alpha;
            this.zulu = zulu;
        }

        public String getZulu() { return zulu; }
        public String getAlpha() { return alpha; }
    }

    public static class SignedSample {
        private final String credentialId;
        private final String payload;
        private final String signature;

        SignedSample(String credentialId, String payload, String signature) {
            this.credentialId = credentialId;
            this.payload = payload;
            this.signature = signature;
        }

        public String getCredentialId() { return credentialId; }
        public String getPayload() { return payload; }
        public String getSignature() { return signature; }
    }
}
