package pt.healthpass.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import pt.healthpass.crypto.HashUtil;

class MerkleTreeTest {

    private Transaction tx(String payload, long timestamp) {
        return new Transaction(TransactionType.IDENTITY_REGISTERED, payload, "sig", timestamp);
    }

    @Test
    void emptyAndSingleLeafRootsAreDefined() {
        assertEquals(HashUtil.hash(""), new MerkleTree(List.of()).getRoot());
        Transaction tx = tx("one", 1);
        assertEquals(tx.calcularHash(), new MerkleTree(List.of(tx)).getRoot());
    }

    @Test
    void oddLeafCountDuplicatesLastAndProofVerifies() {
        List<Transaction> transactions = List.of(tx("a", 1), tx("b", 2), tx("c", 3));
        MerkleTree tree = new MerkleTree(transactions);
        assertTrue(tree.contem(transactions.get(2)));
        assertTrue(MerkleTree.verificarProva(transactions.get(2).calcularHash(), 2,
                3, tree.provaInclusao(transactions.get(2)), tree.getRoot()));
        assertFalse(MerkleTree.verificarProva(tx("changed", 3).calcularHash(), 2,
                3, tree.provaInclusao(transactions.get(2)), tree.getRoot()));
    }
}
