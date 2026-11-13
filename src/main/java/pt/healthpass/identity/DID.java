package pt.healthpass.identity;

import java.util.Objects;

/**
 * Identificador Descentralizado (DID).
 *
 * <p><b>O que e um DID?</b><br>
 * Um DID e um identificador unico, controlado pelo proprio dono e independente
 * de uma autoridade central. No HealthPass usamos um formato proprio de
 * demonstracao:</p>
 *
 * <pre>
 *   did:healthpass:issuer-demo-01
 *   did:healthpass:holder-demo-07
 *   did:healthpass:validator-01
 * </pre>
 *
 * <p><b>Regra importante:</b> o DID <b>nunca</b> contem dados pessoais (nome,
 * numero de utente, etc.). E apenas uma referencia tecnica.</p>
 *
 * <p><b>Origem:</b> novo. Na TemplarCoin a identidade era referida pelo nome do
 * utilizador, o que nao e adequado.</p>
 *
 * @author HealthPass
 */
public final class DID {

    /** Prefixo obrigatorio de todos os DIDs do HealthPass. */
    public static final String PREFIXO = "did:healthpass:";

    /** Parte especifica do DID (sem o prefixo), por exemplo "issuer-demo-01". */
    private final String identificador;

    /**
     * Cria um DID a partir da sua parte especifica.
     *
     * @param identificador parte especifica (nao pode ser vazia)
     */
    public DID(String identificador) {
        // Validacao de parametros: o identificador e obrigatorio.
        this.identificador = Objects.requireNonNull(identificador, "identificador nao pode ser nulo");
    }

    /**
     * Cria um DID a partir de uma string completa no formato
     * {@code did:healthpass:...}.
     *
     * @param did completo
     * @return o DID correspondente
     */
    // TODO (Etapa 6): validar o prefixo e extrair a parte especifica.
    public static DID parse(String did) {
        throw new UnsupportedOperationException("A implementar na Etapa 6");
    }

    /**
     * @return a parte especifica do DID (sem o prefixo).
     */
    public String getIdentificador() {
        return identificador;
    }

    /**
     * @return o DID completo, por exemplo {@code did:healthpass:issuer-demo-01}.
     */
    @Override
    public String toString() {
        return PREFIXO + identificador;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DID)) {
            return false;
        }
        return identificador.equals(((DID) o).identificador);
    }

    @Override
    public int hashCode() {
        return identificador.hashCode();
    }
}
