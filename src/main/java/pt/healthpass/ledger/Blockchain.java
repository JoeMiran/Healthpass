package pt.healthpass.ledger;

import java.util.ArrayList;
import java.util.List;

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
    // TODO (Etapa 5): criar bloco génesis sem valor monetario.
    public void inicializar() {
        throw new UnsupportedOperationException("A implementar na Etapa 5");
    }

    /**
     * Adiciona uma transacao a pool de pendentes.
     *
     * @param transacao transacao a adicionar
     */
    // TODO (Etapa 5): validar e adicionar a pendentes.
    public void adicionarTransacao(Transaction transacao) {
        throw new UnsupportedOperationException("A implementar na Etapa 5");
    }

    /**
     * Adiciona um bloco ja aprovado pelo consenso.
     *
     * @param bloco bloco a adicionar
     */
    // TODO (Etapa 5): validar (previousHash, Merkle Root) e acrescentar a cadeia.
    public void adicionarBloco(Block bloco) {
        throw new UnsupportedOperationException("A implementar na Etapa 5");
    }

    /**
     * Valida a integridade de toda a cadeia.
     *
     * <p>Para cada bloco: confirmar o hash e confirmar que o seu
     * {@code previousHash} corresponde ao hash do bloco anterior.</p>
     *
     * @return {@code true} se a cadeia estiver integra
     */
    // TODO (Etapa 5): percorrer a cadeia e validar encadeamento.
    public boolean validarCadeia() {
        throw new UnsupportedOperationException("A implementar na Etapa 5");
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
        return cadeia;
    }

    /**
     * @return a lista de transacoes pendentes.
     */
    public List<Transaction> getPendentes() {
        return pendentes;
    }

    /**
     * @return a altura atual da cadeia (numero de blocos - 1).
     */
    public int getAltura() {
        return cadeia.size() - 1;
    }
}
