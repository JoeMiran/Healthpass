package pt.healthpass.app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class HealthPassDemoTest {
    @TempDir Path tempDir;

    @Test
    void fullDemoRegistersIssuesVerifiesRevokesAndReplicatesAcrossThreeNodes() {
        try (HealthPassDemo demo = new HealthPassDemo(tempDir)) {
            assertTrue(demo.verificarIdentidadesRegistadas());
            var credential = demo.emitir("VACINA-TESTE", "2026-11-25");
            var valid = demo.verificar();
            assertTrue(valid.tecnicamenteValida());
            assertTrue(demo.apresentar().paraJson().contains("VACINA-TESTE"));
            assertEquals(3, demo.alturas().size());
            assertEquals(2, demo.alturas().getFirst());
            assertEquals(credential.getCredentialId(), demo.ultimaCredencial().getCredentialId());

            demo.revogar();
            var revoked = demo.verificar();
            assertFalse(revoked.ativa());
            assertFalse(revoked.tecnicamenteValida());
            assertTrue(demo.alturas().stream().allMatch(height -> height == 3));
        }
    }
}
