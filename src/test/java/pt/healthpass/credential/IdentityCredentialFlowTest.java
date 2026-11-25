package pt.healthpass.credential;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pt.healthpass.identity.DID;
import pt.healthpass.identity.Identity;
import pt.healthpass.identity.IdentityRole;
import pt.healthpass.identity.IdentityService;
import pt.healthpass.ledger.Blockchain;
import pt.healthpass.ledger.ChainRepository;
import pt.healthpass.ledger.TransactionType;
import pt.healthpass.storage.OffChainCredentialStore;

class IdentityCredentialFlowTest {

    @TempDir Path tempDir;

    @Test
    void registersIdentitiesIssuesStoresAndVerifiesCredential() throws Exception {
        Fixture f = fixture("valid");
        VaccinationCredential credential = credential(f.issuerDid, f.holderDid, "cred-valid-1");
        CredentialIssuer issuer = new CredentialIssuer(f.issuerDid, f.issuerKeys.getPrivate(),
                f.blockchain, f.identities, f.store);
        issuer.emitir(credential);

        assertNotNull(f.store.carregar("cred-valid-1"));
        assertTrue(f.blockchain.getPendentes().stream().anyMatch(t -> t.getType() == TransactionType.CREDENTIAL_ISSUED));
        String onChainPayload = f.blockchain.getPendentes().stream()
                .filter(t -> t.getType() == TransactionType.CREDENTIAL_ISSUED).findFirst().orElseThrow().getPayload();
        assertFalse(onChainPayload.contains("vaccineCode"));
        assertFalse(onChainPayload.contains("VACCINE-SYNTHETIC"));

        f.blockchain.adicionarBloco(f.blockchain.criarBlocoPendente("validator-demo"));
        CredentialVerifier verifier = new CredentialVerifier(f.blockchain, f.identities);
        CredentialVerifier.Resultado resultado = verifier.verificar(f.store.carregar("cred-valid-1"));
        assertTrue(resultado.assinaturaValida());
        assertTrue(resultado.hashCorresponde());
        assertTrue(resultado.emissorAutorizado());
        assertTrue(resultado.ativa());
        assertTrue(resultado.dentroValidade());
        assertTrue(resultado.tecnicamenteValida());

        VaccinationCredential adulterada = (VaccinationCredential) f.store.carregar("cred-valid-1");
        adulterada.setVaccineCode("VACCINE-ALTERED");
        CredentialVerifier.Resultado rejeitada = verifier.verificar(adulterada);
        assertFalse(rejeitada.assinaturaValida());
        assertFalse(rejeitada.hashCorresponde());
        assertFalse(rejeitada.tecnicamenteValida());
    }

    @Test
    void revocationBecomesVisibleAfterItsTransactionIsCommitted() throws Exception {
        Fixture f = fixture("revoke");
        CredentialIssuer issuer = new CredentialIssuer(f.issuerDid, f.issuerKeys.getPrivate(),
                f.blockchain, f.identities, f.store);
        VaccinationCredential credential = credential(f.issuerDid, f.holderDid, "cred-revoke-1");
        issuer.emitir(credential);
        f.blockchain.adicionarBloco(f.blockchain.criarBlocoPendente("validator-demo"));
        CredentialVerifier verifier = new CredentialVerifier(f.blockchain, f.identities);
        assertTrue(verifier.verificar(credential).tecnicamenteValida());

        issuer.revogar("cred-revoke-1");
        assertTrue(verifier.verificar(credential).ativa(), "revogação pendente ainda não é estado confirmado");
        f.blockchain.adicionarBloco(f.blockchain.criarBlocoPendente("validator-demo"));
        assertFalse(verifier.verificar(credential).ativa());
        assertFalse(verifier.verificar(credential).tecnicamenteValida());
    }

    @Test
    void rejectsIdentityPrivateKeyMismatchAndInvalidDid() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair expected = generator.generateKeyPair();
        KeyPair wrong = generator.generateKeyPair();
        Blockchain chain = new Blockchain(new ChainRepository(tempDir.resolve("identity.json").toString()));
        chain.inicializar();
        IdentityService identities = new IdentityService(chain);
        Identity identity = new Identity(new DID("issuer-mismatch"), IdentityRole.ISSUER, expected.getPublic());
        assertThrows(IllegalArgumentException.class, () -> identities.registar(identity, wrong.getPrivate()));
        assertNull(identities.procurar(identity.getDid()));
        assertThrows(IllegalArgumentException.class, () -> DID.parse("not-a-did"));
    }

    @Test
    void issuerMustBeOnTheConfiguredTrustList() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keys = generator.generateKeyPair();
        DID untrustedDid = new DID("issuer-unlisted");
        Blockchain chain = new Blockchain(new ChainRepository(tempDir.resolve("untrusted.json").toString()));
        chain.inicializar();
        IdentityService identities = new IdentityService(chain);
        identities.registar(new Identity(untrustedDid, IdentityRole.ISSUER, keys.getPublic()), keys.getPrivate());
        chain.adicionarBloco(chain.criarBlocoPendente("bootstrap-validator"));
        assertFalse(identities.ehEmissorAutorizado(untrustedDid));
        var issuer = new CredentialIssuer(untrustedDid, keys.getPrivate(), chain, identities,
                new OffChainCredentialStore(tempDir.resolve("untrusted-credentials")));
        assertThrows(IllegalStateException.class, () -> issuer.emitir(
                credential(untrustedDid, new DID("holder-demo"), "cred-untrusted")));
    }

    private Fixture fixture(String name) throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair issuerKeys = generator.generateKeyPair();
        KeyPair holderKeys = generator.generateKeyPair();
        DID issuerDid = new DID("issuer-demo-01");
        DID holderDid = new DID("holder-" + name);
        Blockchain chain = new Blockchain(new ChainRepository(tempDir.resolve(name + "-chain.json").toString()));
        chain.inicializar();
        IdentityService identities = new IdentityService(chain);
        identities.registar(new Identity(issuerDid, IdentityRole.ISSUER, issuerKeys.getPublic()), issuerKeys.getPrivate());
        identities.registar(new Identity(holderDid, IdentityRole.HOLDER, holderKeys.getPublic()), holderKeys.getPrivate());
        chain.adicionarBloco(chain.criarBlocoPendente("bootstrap-validator"));
        OffChainCredentialStore store = new OffChainCredentialStore(tempDir.resolve(name + "-credentials"));
        return new Fixture(chain, identities, store, issuerDid, holderDid, issuerKeys);
    }

    private static VaccinationCredential credential(DID issuer, DID holder, String id) {
        return new VaccinationCredential(id, issuer.toString(), holder.toString(),
                Instant.now().minusSeconds(30).toString(), Instant.now().plusSeconds(3600).toString(),
                "VACCINE-SYNTHETIC", "2026-11-20");
    }

    private record Fixture(Blockchain blockchain, IdentityService identities, OffChainCredentialStore store,
                           DID issuerDid, DID holderDid, KeyPair issuerKeys) { }
}
