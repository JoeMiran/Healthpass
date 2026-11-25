package pt.healthpass.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.security.KeyPairGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class KeyStoreServiceTest {

    @TempDir Path tempDir;

    @Test
    void storesPublicKeyAndPasswordProtectedPrivateKey() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var pair = generator.generateKeyPair();
        var store = new KeyStoreService(tempDir);
        store.guardarChavePublica(pair.getPublic(), "issuer-demo");
        store.guardarChavePrivada(pair.getPrivate(), "issuer-demo", "correct horse battery staple");

        assertArrayEquals(pair.getPublic().getEncoded(), store.carregarChavePublica("issuer-demo").getEncoded());
        assertArrayEquals(pair.getPrivate().getEncoded(), store.carregarChavePrivada(
                "issuer-demo", "correct horse battery staple").getEncoded());
        assertThrows(IllegalStateException.class,
                () -> store.carregarChavePrivada("issuer-demo", "wrong password"));
    }
}
