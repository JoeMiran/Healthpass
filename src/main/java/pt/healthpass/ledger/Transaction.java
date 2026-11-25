package pt.healthpass.ledger;

import java.util.Objects;
import pt.healthpass.crypto.CanonicalSerializer;
import pt.healthpass.crypto.HashUtil;

/**
 * Transacao da blockchain do HealthPass.
 *
 * <p>Uma transacao representa uma <b>operacao da aplicacao</b> (e nao uma
 * transferencia de dinheiro). Campos previstos:</p>
 * <ul>
 *   <li>{@code type} — ver {@link TransactionType};</li>
 *   <li>{@code payload} — os dados da operacao em JSON (por exemplo, o hash da
 *       credencial ou a chave publica da identidade);</li>
 *   <li>{@code signature} — assinatura do autor da operacao;</li>
 *   <li>{@code timestamp} — momento da criacao.</li>
 * </ul>
 *
 * <p><b>Origem:</b> adaptada de {@code templarCoin.core.Transaction} da
 * TemplarCoin, removendo os campos monetarios {@code from/to/value}.</p>
 *
 * @author HealthPass
 */
public class Transaction {

    /** Tipo da operacao. */
    private TransactionType type;

    /** Conteudo da operacao (JSON). */
    private String payload;

    /** Assinatura do autor (Base64). */
    private String signature;

    /** Momento de criacao (epoch millis). */
    private long timestamp;

    /** Construtor vazio para serializacao JSON. */
    public Transaction() {
    }

    /**
     * Cria uma transacao.
     *
     * @param type tipo da operacao
     * @param payload conteudo da operacao em JSON
     * @param signature assinatura do autor
     */
    public Transaction(TransactionType type, String payload, String signature) {
        this(type, payload, signature, System.currentTimeMillis());
    }

    /** Construtor determinístico, útil para importar/registar transações. */
    public Transaction(TransactionType type, String payload, String signature, long timestamp) {
        this.type = type;
        this.payload = payload;
        this.signature = signature;
        this.timestamp = timestamp;
    }

    /**
     * @return o tipo da operacao.
     */
    public TransactionType getType() {
        return type;
    }

    /**
     * @return o conteudo da operacao (JSON).
     */
    public String getPayload() {
        return payload;
    }

    /**
     * @return a assinatura do autor.
     */
    public String getSignature() {
        return signature;
    }

    /**
     * @return o timestamp da criacao.
     */
    public long getTimestamp() {
        return timestamp;
    }

    public void setType(TransactionType type) { this.type = type; }
    public void setPayload(String payload) { this.payload = payload; }
    public void setSignature(String signature) { this.signature = signature; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    /**
     * Calcula o hash desta transacao (usado na Merkle Tree).
     *
     * @return o hash SHA-256 hexadecimal da transacao
     */
    public String calcularHash() {
        Objects.requireNonNull(type, "type da transacao nao pode ser null");
        Objects.requireNonNull(payload, "payload da transacao nao pode ser null");
        return HashUtil.hash(CanonicalSerializer.paraJson(this));
    }

    @Override
    public String toString() {
        return "Transaction{" + type + ", ts=" + timestamp + '}';
    }
}
