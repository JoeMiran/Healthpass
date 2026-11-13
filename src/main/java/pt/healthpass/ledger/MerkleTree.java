package pt.healthpass.ledger;

import java.util.List;

/**
 * Arvore de Merkle.
 *
 * <p><b>O que e e para que serve?</b><br>
 * A Merkle Tree resume uma lista de transacoes num unico hash — a
 * <b>Merkle Root</b>. O bloco guarda apenas essa raiz. Assim, qualquer pessoa
 * pode confirmar que uma transacao pertence ao bloco sem precisar de todas as
 * outras (prova de inclusao).</p>
 *
 * <p><b>Como se constroi:</b></p>
 * <ol>
 *   <li>Calcula-se o hash de cada transacao (as "folhas").</li>
 *   <li>Combinam-se os hashes dois a dois (hash(hashA + hashB)).</li>
 *   <li>Repete-se ate restar um unico hash: a Merkle Root.</li>
 * </ol>
 *
 * <p><b>Regra para numero impar de transacoes:</b> duplicar a ultima folha
 * antes de combinar. Esta regra tem de ser <b>deterministica</b> (igual em
 * todos os nos) para que a Merkle Root seja sempre a mesma.</p>
 *
 * <p><b>Origem:</b> novo — na TemplarCoin nao existia Merkle Tree.</p>
 *
 * @author HealthPass
 */
public class MerkleTree {

    /** Lista de hashes (folhas) da arvore. */
    private final List<String> folhas;

    /**
     * @param transacoes transacoes a resumir na arvore
     */
    public MerkleTree(List<Transaction> transacoes) {
        // TODO (Etapa 5): converter cada transacao no seu hash.
        this.folhas = null;
    }

    /**
     * Calcula a Merkle Root.
     *
     * <p>Casos a tratar (e a testar):</p>
     * <ul>
     *   <li>zero transacoes — devolver um valor fixo/documentado;</li>
     *   <li>uma transacao — a raiz e o hash dessa transacao;</li>
     *   <li>duas transacoes — combinar as duas;</li>
     *   <li>numero impar — duplicar a ultima folha.</li>
     * </ul>
     *
     * @return a Merkle Root em Base64
     */
    // TODO (Etapa 5): implementar o algoritmo descrito acima.
    public String getRoot() {
        throw new UnsupportedOperationException("A implementar na Etapa 5");
    }

    /**
     * Verifica se uma transacao pertence a arvore (prova de inclusao).
     *
     * @param transacao transacao a procurar
     * @return {@code true} se a transacao pertencer a arvore
     */
    // TODO (Etapa 5): implementar a prova de inclusao.
    public boolean contem(Transaction transacao) {
        throw new UnsupportedOperationException("A implementar na Etapa 5");
    }
}
