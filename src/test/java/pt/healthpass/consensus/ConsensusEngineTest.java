package pt.healthpass.consensus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pt.healthpass.identity.DID;
import pt.healthpass.ledger.Block;
import pt.healthpass.ledger.Blockchain;
import pt.healthpass.ledger.ChainRepository;
import pt.healthpass.ledger.Transaction;
import pt.healthpass.ledger.TransactionType;

class ConsensusEngineTest {

    @TempDir Path tempDir;

    @Test
    void twoOfThreeValidVotesAcceptAndPersistBlock() throws Exception {
        Fixture f = fixture("accept");
        f.blockchain.adicionarTransacao(new Transaction(TransactionType.IDENTITY_REGISTERED,
                "{\"did\":\"did:healthpass:demo\"}", "demo-signature", 10));
        BlockProposal proposal = f.engine.propor(f.blockchain);
        assertEquals(f.ids.get(0), proposal.getProponente());
        assertTrue(f.engine.validarProposta(proposal, f.blockchain));

        ValidatorVote first = f.engine.votar(proposal, f.blockchain, f.ids.get(0), f.keys.get(0).getPrivate());
        ConsensusCertificate oneVote = new ConsensusCertificate(proposal.getBloco().getHash(), List.of(first));
        assertFalse(f.engine.temQuorum(oneVote));
        assertThrows(IllegalArgumentException.class,
                () -> f.engine.aceitar(proposal, oneVote, f.blockchain));
        assertEquals(0, f.blockchain.getAltura());

        ValidatorVote second = f.engine.votar(proposal, f.blockchain, f.ids.get(1), f.keys.get(1).getPrivate());
        ConsensusCertificate certificate = new ConsensusCertificate(proposal.getBloco().getHash(), List.of(first, second));
        assertEquals(2, certificate.contarAprovacoes(f.registry));
        f.engine.aceitar(proposal, certificate, f.blockchain);
        assertEquals(1, f.blockchain.getAltura());
        assertTrue(f.blockchain.validarCadeia());

        Blockchain reloaded = new Blockchain(new ChainRepository(f.chainFile.toString()));
        reloaded.inicializar();
        assertEquals(proposal.getBloco().getHash(), reloaded.getUltimoBloco().getHash());
    }

    @Test
    void duplicateInvalidAndUnauthorizedVotesDoNotCount() throws Exception {
        Fixture f = fixture("votes");
        BlockProposal proposal = f.engine.propor(f.blockchain);
        ValidatorVote good = f.engine.votar(proposal, f.blockchain, f.ids.get(0), f.keys.get(0).getPrivate());
        ValidatorVote duplicate = f.engine.votar(proposal, f.blockchain, f.ids.get(0), f.keys.get(0).getPrivate());
        ValidatorVote wrongTarget = ValidatorVote.assinar(f.ids.get(1), "different-hash", true,
                f.keys.get(1).getPrivate());
        ValidatorVote badSignature = new ValidatorVote(f.ids.get(1), proposal.getBloco().getHash(),
                true, "not-a-valid-base64-signature");
        ConsensusCertificate alteredSignature = new ConsensusCertificate(proposal.getBloco().getHash(),
                List.of(good, badSignature));
        assertEquals(1, alteredSignature.contarAprovacoes(f.registry));
        DID unknown = new DID("validator-outsider");
        ValidatorVote outsider = ValidatorVote.assinar(unknown, proposal.getBloco().getHash(), true,
                f.keys.get(2).getPrivate());
        ConsensusCertificate certificate = new ConsensusCertificate(proposal.getBloco().getHash(),
                List.of(good, duplicate, wrongTarget, outsider));
        assertEquals(0, certificate.contarAprovacoes(f.registry),
                "duplicados anulam o par, alvo divergente e identidade não autorizada não contam");
        assertFalse(f.engine.temQuorum(certificate));
    }

    @Test
    void rejectsBlockWithWrongParentOrUnexpectedProposer() throws Exception {
        Fixture f = fixture("invalid-block");
        Block validlyHashedButWrongParent = new Block(1, 20, "wrong-parent", List.of(), f.ids.get(0).toString());
        BlockProposal wrongParent = new BlockProposal(validlyHashedButWrongParent, f.ids.get(0));
        assertFalse(f.engine.validarProposta(wrongParent, f.blockchain));

        Block rightParentWrongProposer = new Block(1, 21, f.blockchain.getUltimoBloco().getHash(),
                List.of(), f.ids.get(1).toString());
        BlockProposal wrongProposer = new BlockProposal(rightParentWrongProposer, f.ids.get(1));
        assertFalse(f.engine.validarProposta(wrongProposer, f.blockchain));
        assertThrows(IllegalArgumentException.class,
                () -> f.engine.aceitar(wrongProposer, new ConsensusCertificate(
                        rightParentWrongProposer.getHash(), List.of()), f.blockchain));
    }

    private Fixture fixture(String name) throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        List<KeyPair> keys = List.of(generator.generateKeyPair(), generator.generateKeyPair(), generator.generateKeyPair());
        List<DID> ids = List.of(new DID("validator-a"), new DID("validator-b"), new DID("validator-c"));
        ValidatorRegistry registry = new ValidatorRegistry();
        for (int i = 0; i < ids.size(); i++) registry.adicionar(new Validator(ids.get(i), keys.get(i).getPublic()));
        Path chainFile = tempDir.resolve(name + ".json");
        Blockchain chain = new Blockchain(new ChainRepository(chainFile.toString()));
        chain.inicializar();
        return new Fixture(chain, registry, new ConsensusEngine(registry, 2), ids, keys, chainFile);
    }

    private record Fixture(Blockchain blockchain, ValidatorRegistry registry, ConsensusEngine engine,
                           List<DID> ids, List<KeyPair> keys, Path chainFile) { }
}
