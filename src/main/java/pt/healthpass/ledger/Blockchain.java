package pt.healthpass.ledger;

import java.util.ArrayList;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

/**
 * Blockchain do HealthPass.
 *
 * <p>Mantem a cadeia de blocos e uma <b>pool de transacoes pendentes</b>
 * (transacoes ainda nao incluidas em nenhum bloco). Quando o consenso aprova um
 * novo bloco, as transacoes pendentes passam para esse bloco.</p>
 *
 * <p>Responsabilidades:</p>
 * <ul>
 *   <li>Guardar/carregar a cadeia (delegando em {@link ChainRepository}).</li>
 *   <li>Aceitar novas transacoes na pool de pendentes.</li>
 *   <li>Validar a integridade de toda a cadeia (encadeamento dos hashes).</li>
 *   <li>Adicionar blocos aprovados pelo consenso.</li>
 * </ul>
 *
 * <p><b>Origem:</b> adaptada de {@code core.BlockChain} da TemplarCoin. As
 * principais mudancas sao: remocao do bloco génesis monetario; adicao da pool
 * de pendentes; validacao encadeada; e delegacao da persistencia.</p>
 *
 * @author HealthPass
 */
public class Blockchain {

    /** Lista de blocos que formam a cadeia. */
    private final List<Block> cadeia = new ArrayList<>();

    /** Transacoes recebidas mas ainda nao incluidas num bloco. */
    private final List<Transaction> pendentes = new ArrayList<>();

    /** Repositorio usado para persistir a cadeia. */
    private final ChainRepository repositorio;

    /**
     * @param repositorio repositorio onde a cadeia e guardada/carregada
     */
    public Blockchain(ChainRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * Inicializa a cadeia, criando o bloco génesis se estiver vazia.
     *
     * <p>Nota: o génesis do HealthPass nao cria moedas (ao contrario da
     * TemplarCoin). Serve apenas para iniciar a cadeia.</p>
     */
    public synchronized void inicializar() {
        List<Block> guardados = repositorio.carregar();
        if (guardados.isEmpty()) {
            cadeia.clear();
            cadeia.add(Block.genesis());
            repositorio.guardar(cadeia);
            return;
        }
        cadeia.clear();
        cadeia.addAll(guardados);
        if (!validarCadeia()) throw new IllegalStateException("Cadeia carregada invalida");
    }

    /**
     * Adiciona uma transacao a pool de pendentes.
     *
     * @param transacao transacao a adicionar
     */
    public synchronized void adicionarTransacao(Transaction transacao) {
        Objects.requireNonNull(transacao, "transacao nao pode ser null");
        transacao.calcularHash(); // valida os campos obrigatórios
        String hash = transacao.calcularHash();
        boolean existeConfirmada = cadeia.stream().flatMap(b -> b.getTransactions().stream())
                .anyMatch(t -> t.calcularHash().equals(hash));
        boolean existePendente = pendentes.stream().anyMatch(t -> t.calcularHash().equals(hash));
        if (!existeConfirmada && !existePendente) pendentes.add(transacao);
    }

    /**
     * Adiciona um bloco ja aprovado pelo consenso.
     *
     * @param bloco bloco a adicionar
     */
    public synchronized void adicionarBloco(Block bloco) {
        Objects.requireNonNull(bloco, "bloco nao pode ser null");
        if (cadeia.isEmpty()) throw new IllegalStateException("Inicialize a cadeia antes de adicionar blocos");
        Block anterior = getUltimoBloco();
        if (!bloco.isValid() || bloco.getHeight() != cadeia.size()
                || !anterior.getHash().equals(bloco.getPreviousHash())) {
            throw new IllegalArgumentException("Bloco invalido ou fora de sequência");
        }
        List<Block> novaCadeia = new ArrayList<>(cadeia);
        novaCadeia.add(bloco);
        repositorio.guardar(novaCadeia);
        cadeia.add(bloco);
        Set<String> incluidos = new HashSet<>();
        bloco.getTransactions().forEach(t -> incluidos.add(t.calcularHash()));
        pendentes.removeIf(t -> incluidos.contains(t.calcularHash()));
    }

    /** Monta um bloco candidato com as transações atualmente pendentes. */
    public synchronized Block criarBlocoPendente(String validatorId) {
        if (cadeia.isEmpty()) throw new IllegalStateException("Inicialize a cadeia primeiro");
        Block anterior = getUltimoBloco();
        return new Block(cadeia.size(), System.currentTimeMillis(), anterior.getHash(),
                new ArrayList<>(pendentes), validatorId);
    }

    /**
     * Valida a integridade de toda a cadeia.
     *
     * <p>Para cada bloco: confirmar o hash e confirmar que o seu
     * {@code previousHash} corresponde ao hash do bloco anterior.</p>
     *
     * @return {@code true} se a cadeia estiver integra
     */
    public boolean validarCadeia() {
        if (cadeia.isEmpty() || cadeia.get(0) == null) return false;
        Block esperadoGenesis = Block.genesis();
        Block genesis = cadeia.get(0);
        if (!genesis.isValid() || genesis.getHeight() != 0
                || !esperadoGenesis.getHash().equals(genesis.getHash())) return false;
        for (int i = 1; i < cadeia.size(); i++) {
            Block atual = cadeia.get(i);
            Block anterior = cadeia.get(i - 1);
            if (atual == null || !atual.isValid() || atual.getHeight() != i
                    || !anterior.getHash().equals(atual.getPreviousHash())) return false;
        }
        return true;
    }

    /**
     * @return o ultimo bloco da cadeia.
     */
    public Block getUltimoBloco() {
        return cadeia.isEmpty() ? null : cadeia.get(cadeia.size() - 1);
    }

    /**
     * @return a lista de blocos da cadeia.
     */
    public List<Block> getCadeia() {
        return java.util.Collections.unmodifiableList(cadeia);
    }

    /**
     * @return a lista de transacoes pendentes.
     */
    public List<Transaction> getPendentes() {
        return java.util.Collections.unmodifiableList(pendentes);
    }

    /**
     * @return a altura atual da cadeia (numero de blocos - 1).
     */
    public int getAltura() {
        return cadeia.size() - 1;
    }
}
