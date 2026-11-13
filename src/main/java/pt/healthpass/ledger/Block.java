package pt.healthpass.ledger;

import java.util.List;

/**
 * Bloco da blockchain do HealthPass.
 *
 * <p>Um bloco e um "contentor" de transacoes. Cada bloco guarda o hash do
 * bloco anterior, formando uma <b>cadeia</b>: alterar um bloco antigo
 * invalida todos os seguintes.</p>
 *
 * <p>Campos previstos (a completar na Etapa 5):</p>
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

    /**
     * @return a altura (indice) do bloco.
     */
    public int getHeight() {
        return height;
    }

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
     * @return o hash do bloco em Base64
     */
    // TODO (Etapa 5): serializar cabecalho + merkleRoot de forma deterministica.
    public String calcularHash() {
        throw new UnsupportedOperationException("A implementar na Etapa 5");
    }

    /**
     * Verifica a integridade interna do bloco: o hash guardado corresponde ao
     * hash recalculado e a Merkle Root corresponde as transacoes.
     *
     * @return {@code true} se o bloco for internamente consistente
     */
    // TODO (Etapa 5): recalcular hash e comparar; conferir Merkle Root.
    public boolean isValid() {
        throw new UnsupportedOperationException("A implementar na Etapa 5");
    }

    @Override
    public String toString() {
        return "Block{" + height + ", prev=" + previousHash + ", hash=" + hash + '}';
    }
}
