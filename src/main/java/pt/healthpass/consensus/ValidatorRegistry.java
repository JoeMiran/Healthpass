package pt.healthpass.consensus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Comparator;
import pt.healthpass.identity.DID;

/**
 * Registo de validadores autorizados.
 *
 * <p>Mantem a lista de validadores com permissao para votar. Só os DIDs aqui
 * presentes contam para o quorum; votos de outros DIDs sao rejeitados.</p>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public class ValidatorRegistry {

    /** Lista de validadores autorizados. */
    private final List<Validator> validadores = new ArrayList<>();

    /**
     * Adiciona um validador autorizado.
     *
     * @param validador validador a adicionar
     */
    public void adicionar(Validator validador) {
        Objects.requireNonNull(validador, "validador obrigatorio");
        if (ehValidador(validador.getDid())) throw new IllegalArgumentException("Validador duplicado: " + validador.getDid());
        validadores.add(validador);
        validadores.sort(Comparator.comparing(v -> v.getDid().toString()));
    }

    /**
     * Verifica se um DID pertence a um validador autorizado.
     *
     * @param did DID a verificar
     * @return {@code true} se for validador autorizado
     */
    public boolean ehValidador(DID did) {
        return did != null && validadores.stream().anyMatch(v -> v.getDid().equals(did));
    }

    /**
     * @return o numero de validadores autorizados.
     */
    public int tamanho() {
        return validadores.size();
    }

    /**
     * @return a lista de validadores autorizados.
     */
    public List<Validator> getValidadores() {
        return List.copyOf(validadores);
    }

    public Validator procurar(DID did) {
        if (did == null) return null;
        return validadores.stream().filter(v -> v.getDid().equals(did)).findFirst().orElse(null);
    }
}
