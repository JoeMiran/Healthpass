package pt.healthpass.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class TransactionTest {

    @Test
    void hashIsDeterministicForSameTransaction() {
        var tx1 = new Transaction(TransactionType.CREDENTIAL_ISSUED,
                "{\"credentialId\":\"demo-á\"}", "sig-demo", 123456L);
        var tx2 = new Transaction(TransactionType.CREDENTIAL_ISSUED,
                "{\"credentialId\":\"demo-á\"}", "sig-demo", 123456L);
        assertEquals(tx1.calcularHash(), tx2.calcularHash());
    }

    @Test
    void payloadChangeChangesTransactionHash() {
        var tx1 = new Transaction(TransactionType.CREDENTIAL_ISSUED, "payload-á", "sig", 123L);
        var tx2 = new Transaction(TransactionType.CREDENTIAL_ISSUED, "payload-a", "sig", 123L);
        assertNotEquals(tx1.calcularHash(), tx2.calcularHash());
    }
}
