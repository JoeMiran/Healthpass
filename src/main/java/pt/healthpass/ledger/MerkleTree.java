package pt.healthpass.ledger;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import pt.healthpass.crypto.HashUtil;

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
        Objects.requireNonNull(transacoes, "transacoes nao podem ser null");
        this.folhas = transacoes.stream()
                .map(t -> Objects.requireNonNull(t, "transacao nao pode ser null").calcularHash())
                .toList();
    }

    /**
     * Calcula a Merkle Root.
     *
     * <p>Casos a tratar (e a testar):</p>
     * <ul>
     *   <li>zero transacoes — SHA-256 da string vazia;</li>
     *   <li>uma transacao — a raiz e o hash dessa transacao;</li>
     *   <li>duas transacoes — combinar as duas;</li>
     *   <li>numero impar — duplicar a ultima folha.</li>
     * </ul>
     *
     * @return a Merkle Root em SHA-256 hexadecimal
     */
    public String getRoot() {
        return calcularRaiz(folhas);
    }

    /**
     * Verifica se uma transacao pertence a arvore (prova de inclusao).
     *
     * @param transacao transacao a procurar
     * @return {@code true} se a transacao pertencer a arvore
     */
    public boolean contem(Transaction transacao) {
        return transacao != null && folhas.contains(transacao.calcularHash());
    }

    /** Retorna os hashes irmãos necessários para provar a inclusão. */
    public List<String> provaInclusao(Transaction transacao) {
        if (transacao == null) return List.of();
        int indice = folhas.indexOf(transacao.calcularHash());
        if (indice < 0) return List.of();
        List<String> prova = new ArrayList<>();
        List<String> nivel = new ArrayList<>(folhas);
        int posicao = indice;
        while (nivel.size() > 1) {
            int irmao = (posicao % 2 == 0) ? Math.min(posicao + 1, nivel.size() - 1) : posicao - 1;
            prova.add(nivel.get(irmao));
            nivel = proximoNivel(nivel);
            posicao /= 2;
        }
        return List.copyOf(prova);
    }

    /** Verifica uma prova produzida por {@link #provaInclusao(Transaction)}. */
    public static boolean verificarProva(String hashFolha, int indice, int totalFolhas,
                                         List<String> prova, String raizEsperada) {
        if (hashFolha == null || raizEsperada == null || prova == null
                || totalFolhas < 1 || indice < 0 || indice >= totalFolhas) return false;
        String atual = hashFolha;
        int posicao = indice;
        int quantidade = totalFolhas;
        int nivelProva = 0;
        while (quantidade > 1) {
            if (nivelProva >= prova.size() || prova.get(nivelProva) == null) return false;
            String irmao = prova.get(nivelProva++);
            atual = (posicao % 2 == 0)
                    ? HashUtil.hash(atual + irmao)
                    : HashUtil.hash(irmao + atual);
            posicao /= 2;
            quantidade = (quantidade + 1) / 2;
        }
        return nivelProva == prova.size() && atual.equals(raizEsperada);
    }

    private static String calcularRaiz(List<String> folhas) {
        if (folhas.isEmpty()) return HashUtil.hash("");
        List<String> nivel = new ArrayList<>(folhas);
        while (nivel.size() > 1) nivel = proximoNivel(nivel);
        return nivel.get(0);
    }

    private static List<String> proximoNivel(List<String> nivel) {
        List<String> seguinte = new ArrayList<>((nivel.size() + 1) / 2);
        for (int i = 0; i < nivel.size(); i += 2) {
            String esquerda = nivel.get(i);
            String direita = i + 1 < nivel.size() ? nivel.get(i + 1) : esquerda;
            seguinte.add(HashUtil.hash(esquerda + direita));
        }
        return seguinte;
    }
}
