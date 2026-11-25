package pt.healthpass.ledger;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonIgnore;
import pt.healthpass.crypto.CanonicalSerializer;
import pt.healthpass.crypto.HashUtil;

/**
 * Bloco da blockchain do HealthPass.
 *
 * <p>Um bloco e um "contentor" de transacoes. Cada bloco guarda o hash do
 * bloco anterior, formando uma <b>cadeia</b>: alterar um bloco antigo
 * invalida todos os seguintes.</p>
 *
 * <p>Campos do cabeçalho e conteúdo:</p>
 * <ul>
 *   <li>{@code height} — posicao do bloco na cadeia (0 = génesis);</li>
 *   <li>{@code timestamp} — momento de criacao;</li>
 *   <li>{@code previousHash} — hash do bloco anterior;</li>
 *   <li>{@code merkleRoot} — raiz da Merkle Tree das transacoes;</li>
 *   <li>{@code transactions} — lista de transacoes;</li>
 *   <li>{@code validatorId} — DID do validador que propos o bloco;</li>
 *   <li>{@code hash} — hash do proprio bloco.</li>
 * </ul>
 *
 * <p><b>Como se calcula o hash?</b> A partir de uma serializacao
 * <b>deterministica</b> do cabecalho (height, timestamp, previousHash,
 * merkleRoot, validatorId) e do conteudo. A TemplarCoin usava a concatenacao
 * {@code previousHash + data + dificulty + nonce}; no HealthPass mantemos a
 * ideia de serializacao deterministica, mas adaptada aos novos campos. As
 * assinaturas de aprovacao (consenso) <b>nao</b> entram no hash, para evitar
 * dependencia circular.</p>
 *
 * <p><b>Origem:</b> adaptada de {@code core.Block} da TemplarCoin.</p>
 *
 * @author HealthPass
 */
public class Block {

    /** Posicao na cadeia (0 = bloco génesis). */
    private int height;

    /** Momento de criacao (epoch millis). */
    private long timestamp;

    /** Hash do bloco anterior. */
    private String previousHash;

    /** Raiz da Merkle Tree das transacoes deste bloco. */
    private String merkleRoot;

    /** Transacoes incluidas neste bloco. */
    private List<Transaction> transactions;

    /** DID do validador que propos este bloco. */
    private String validatorId;

    /** Hash deste bloco. */
    private String hash;

    /** Construtor vazio para serializacao JSON. */
    public Block() {
    }

    public Block(int height, long timestamp, String previousHash,
                 List<Transaction> transactions, String validatorId) {
        this.height = height;
        this.timestamp = timestamp;
        this.previousHash = Objects.requireNonNull(previousHash, "previousHash obrigatorio");
        this.transactions = new ArrayList<>(Objects.requireNonNull(transactions, "transactions obrigatorias"));
        this.validatorId = Objects.requireNonNull(validatorId, "validatorId obrigatorio");
        this.merkleRoot = new MerkleTree(this.transactions).getRoot();
        this.hash = calcularHash();
    }

    /** Génesis estável, idêntico em todos os nós que usem estas regras. */
    public static Block genesis() {
        return new Block(0, 0L, "0", List.of(), "GENESIS");
    }

    /**
     * @return a altura (indice) do bloco.
     */
    public int getHeight() {
        return height;
    }

    public long getTimestamp() { return timestamp; }

    /**
     * @return o hash do bloco anterior.
     */
    public String getPreviousHash() {
        return previousHash;
    }

    /**
     * @return a Merkle Root das transacoes.
     */
    public String getMerkleRoot() {
        return merkleRoot;
    }

    /**
     * @return a lista de transacoes.
     */
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * @return o DID do validador que propos o bloco.
     */
    public String getValidatorId() {
        return validatorId;
    }

    /**
     * @return o hash deste bloco.
     */
    public String getHash() {
        return hash;
    }

    /**
     * Calcula o hash deterministico deste bloco.
     *
     * @return o hash SHA-256 hexadecimal do bloco
     */
    public String calcularHash() {
        return HashUtil.hash(CanonicalSerializer.paraJson(new BlockHeader(
                height, timestamp, previousHash, merkleRoot, validatorId)));
    }

    /**
     * Verifica a integridade interna do bloco: o hash guardado corresponde ao
     * hash recalculado e a Merkle Root corresponde as transacoes.
     *
     * @return {@code true} se o bloco for internamente consistente
     */
    @JsonIgnore
    public boolean isValid() {
        if (height < 0 || previousHash == null || validatorId == null || transactions == null
                || merkleRoot == null || hash == null) return false;
        try {
            return merkleRoot.equals(new MerkleTree(transactions).getRoot())
                    && hash.equals(calcularHash());
        } catch (RuntimeException e) {
            return false;
        }
    }

    // Setters necessários à reconstrução explícita do formato JSON pelo Jackson.
    public void setHeight(int height) { this.height = height; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }
    public void setMerkleRoot(String merkleRoot) { this.merkleRoot = merkleRoot; }
    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }
    public void setValidatorId(String validatorId) { this.validatorId = validatorId; }
    public void setHash(String hash) { this.hash = hash; }

    private record BlockHeader(int height, long timestamp, String previousHash,
                               String merkleRoot, String validatorId) { }

    @Override
    public String toString() {
        return "Block{" + height + ", prev=" + previousHash + ", hash=" + hash + '}';
    }
}
