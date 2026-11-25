package pt.healthpass.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BlockchainPersistenceTest {

    @TempDir Path tempDir;

    @Test
    void createsTwoBlocksAndReloadsValidChain() {
        Path chainPath = tempDir.resolve("chain.json");
        Blockchain original = new Blockchain(new ChainRepository(chainPath.toString()));
        original.inicializar();
        String genesisHash = original.getUltimoBloco().getHash();
        original.adicionarTransacao(tx("identity-1", 1));
        original.adicionarBloco(original.criarBlocoPendente("did:healthpass:validator-a"));
        original.adicionarTransacao(tx("identity-2", 2));
        original.adicionarBloco(original.criarBlocoPendente("did:healthpass:validator-b"));

        Blockchain reiniciada = new Blockchain(new ChainRepository(chainPath.toString()));
        reiniciada.inicializar();
        assertEquals(2, reiniciada.getAltura());
        assertEquals(genesisHash, reiniciada.getCadeia().get(0).getHash());
        assertTrue(reiniciada.validarCadeia());
        assertEquals(original.getUltimoBloco().getHash(), reiniciada.getUltimoBloco().getHash());
        assertTrue(reiniciada.getPendentes().isEmpty());
    }

    @Test
    void corruptedTransactionIsRejectedWhenLoading() throws Exception {
        Path chainPath = tempDir.resolve("corrupt.json");
        Blockchain blockchain = new Blockchain(new ChainRepository(chainPath.toString()));
        blockchain.inicializar();
        blockchain.adicionarTransacao(tx("payload-original", 1));
        blockchain.adicionarBloco(blockchain.criarBlocoPendente("validator"));

        String json = Files.readString(chainPath);
        Files.writeString(chainPath, json.replace("payload-original", "payload-alterado"));
        assertThrows(IllegalStateException.class,
                () -> new ChainRepository(chainPath.toString()).carregar());
    }

    @Test
    void corruptedBlockHashIsRejected() throws Exception {
        Path chainPath = tempDir.resolve("corrupt-hash.json");
        Blockchain blockchain = new Blockchain(new ChainRepository(chainPath.toString()));
        blockchain.inicializar();
        String json = Files.readString(chainPath);
        Files.writeString(chainPath, json.replace(blockchain.getUltimoBloco().getHash(), "bad-hash"));
        assertThrows(IllegalStateException.class,
                () -> new ChainRepository(chainPath.toString()).carregar());
    }

    @Test
    void rejectsBrokenPreviousHashOnAppend() {
        Blockchain blockchain = new Blockchain(new ChainRepository(tempDir.resolve("append.json").toString()));
        blockchain.inicializar();
        Block good = new Block(1, 10, blockchain.getUltimoBloco().getHash(),
                java.util.List.of(), "validator");
        good.setPreviousHash("wrong-parent");
        assertFalse(good.isValid()); // a mudança em campo do cabeçalho invalida o hash guardado
        assertThrows(IllegalArgumentException.class, () -> blockchain.adicionarBloco(good));
    }

    private static Transaction tx(String payload, long timestamp) {
        return new Transaction(TransactionType.IDENTITY_REGISTERED, payload, "sig", timestamp);
    }
}
