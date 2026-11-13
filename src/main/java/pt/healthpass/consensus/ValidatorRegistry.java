package pt.healthpass.consensus;

import java.util.ArrayList;
import java.util.List;
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
        validadores.add(validador);
    }

    /**
     * Verifica se um DID pertence a um validador autorizado.
     *
     * @param did DID a verificar
     * @return {@code true} se for validador autorizado
     */
    // TODO (Etapa 7): comparar com a lista de validadores.
    public boolean ehValidador(DID did) {
        throw new UnsupportedOperationException("A implementar na Etapa 7");
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
        return validadores;
    }
}
