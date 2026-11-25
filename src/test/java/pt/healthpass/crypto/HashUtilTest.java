package pt.healthpass.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class HashUtilTest {

    @Test
    void sha256IsDeterministicAndUsesUtf8() {
        String text = "Saúde – vacinação";
        assertEquals(HashUtil.hash(text), HashUtil.hash(text));
        assertEquals(HashUtil.hash(text.getBytes(StandardCharsets.UTF_8)), HashUtil.hash(text));
        assertEquals(64, HashUtil.hash(text).length());
        assertEquals(HashUtil.hash("abc"), "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
    }

    @Test
    void alteredContentChangesHash() {
        assertNotEquals(HashUtil.hash("titular-á"), HashUtil.hash("titular-a"));
    }

    @Test
    void nullInputIsRejected() {
        assertThrows(NullPointerException.class, () -> HashUtil.hash((String) null));
        assertThrows(NullPointerException.class, () -> HashUtil.hash((byte[]) null));
    }
}
