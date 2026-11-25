package pt.healthpass.ledger;

import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Objects;
import pt.healthpass.crypto.CanonicalSerializer;

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
    public List<Block> carregar() {
        Path caminho = Path.of(caminhoFicheiro);
        if (!Files.exists(caminho)) return new ArrayList<>();
        try {
            String json = Files.readString(caminho, StandardCharsets.UTF_8);
            List<Block> blocos = CanonicalSerializer.deJsonLista(json, Block.class);
            validarOuFalhar(blocos);
            return new ArrayList<>(blocos);
        } catch (IOException | RuntimeException e) {
            if (e instanceof IllegalStateException ise) throw ise;
            throw new IllegalStateException("Falha ao carregar cadeia de " + caminho, e);
        }
    }

    /**
     * Guarda a cadeia no ficheiro.
     *
     * @param cadeia lista de blocos a guardar
     */
    public void guardar(List<Block> cadeia) {
        Objects.requireNonNull(cadeia, "cadeia nao pode ser null");
        validarOuFalhar(cadeia);
        Path destino = Path.of(caminhoFicheiro).toAbsolutePath();
        Path diretorio = destino.getParent();
        Path temporario = null;
        try {
            if (diretorio != null) Files.createDirectories(diretorio);
            temporario = Files.createTempFile(diretorio, destino.getFileName().toString(), ".tmp");
            Files.writeString(temporario, CanonicalSerializer.paraJson(cadeia), StandardCharsets.UTF_8);
            try {
                Files.move(temporario, destino, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporario, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao guardar cadeia em " + destino, e);
        } finally {
            if (temporario != null) {
                try { Files.deleteIfExists(temporario); } catch (IOException ignored) { }
            }
        }
    }

    private static void validarOuFalhar(List<Block> blocos) {
        if (blocos == null || blocos.isEmpty()) {
            throw new IllegalArgumentException("A cadeia persistida deve conter bloco génesis");
        }
        Block genesis = Block.genesis();
        if (blocos.get(0) == null || !blocos.get(0).isValid()
                || !genesis.getHash().equals(blocos.get(0).getHash())
                || blocos.get(0).getHeight() != 0) {
            throw new IllegalArgumentException("Bloco génesis invalido");
        }
        for (int i = 1; i < blocos.size(); i++) {
            Block atual = blocos.get(i);
            Block anterior = blocos.get(i - 1);
            if (atual == null || !atual.isValid() || atual.getHeight() != i
                    || !anterior.getHash().equals(atual.getPreviousHash())) {
                throw new IllegalArgumentException("Cadeia invalida no bloco de altura " + i);
            }
        }
    }
}
