package pt.healthpass.ledger;

import java.util.List;

/**
 * Repositorio da cadeia (persistencia).
 *
 * <p>Responsavel por guardar e carregar a blockchain de um ficheiro local.</p>
 *
 * <p><b>Diferenca face a TemplarCoin:</b> no original a persistencia estava
 * <b>dentro</b> da classe {@code BlockChain}, que escrevia diretamente o
 * ficheiro {@code blockchain.bc} com serializacao Java. Aqui separamos essa
 * responsabilidade numa classe propria (principio da responsabilidade unica) e
 * usaremos um formato explicito (JSON) em vez de serializacao Java insegura.</p>
 *
 * <p><b>Origem:</b> novo (extraido de {@code core.BlockChain} da TemplarCoin).</p>
 *
 * @author HealthPass
 */
public class ChainRepository {

    /** Caminho do ficheiro onde a cadeia e guardada. */
    private final String caminhoFicheiro;

    /**
     * @param caminhoFicheiro caminho do ficheiro da cadeia (ex.: data/demo/chain.json)
     */
    public ChainRepository(String caminhoFicheiro) {
        this.caminhoFicheiro = caminhoFicheiro;
    }

    /**
     * Carrega a cadeia do ficheiro.
     *
     * @return a lista de blocos guardada, ou uma lista vazia se o ficheiro nao existir
     */
    // TODO (Etapa 5): ler JSON do ficheiro e reconstruir a lista de Block.
    public List<Block> carregar() {
        throw new UnsupportedOperationException("A implementar na Etapa 5");
    }

    /**
     * Guarda a cadeia no ficheiro.
     *
     * @param cadeia lista de blocos a guardar
     */
    // TODO (Etapa 5): escrever a lista de Block em JSON no ficheiro.
    public void guardar(List<Block> cadeia) {
        throw new UnsupportedOperationException("A implementar na Etapa 5");
    }
}
