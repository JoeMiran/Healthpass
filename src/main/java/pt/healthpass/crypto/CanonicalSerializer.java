package pt.healthpass.crypto;

/**
 * Serializador canonico em JSON.
 *
 * <p><b>Porque precisamos disto?</b><br>
 * Para assinar e verificar uma credencial e preciso transformar o objeto num
 * texto <b>exatamente igual</b> nas duas pontas (emissor e verificador). Se a
 * ordem dos campos, os espacos ou a codificacao mudarem, a assinatura deixa de
 * bater certo. A esse texto chama-se <b>representacao canonica</b>.</p>
 *
 * <p>A TemplarCoin usava serializacao Java
 * ({@code ObjectOutputStream}/{@code ObjectInputStream}). Isso e
 * <b>inseguro</b>: ler objetos Java arbitrarios de uma fonte externa pode
 * executar codigo malicioso. Por isso, no HealthPass usamos <b>JSON</b>
 * (biblioteca Jackson), que e explicito e validavel.</p>
 *
 * <p>A assinatura deve ser calculada sobre esta representacao canonica
 * <b>sem incluir o proprio campo da assinatura</b> (para nao criar uma
 * dependencia circular).</p>
 *
 * <p><b>Origem:</b> substitui {@code utils.Conversion} da TemplarCoin.</p>
 *
 * @author HealthPass
 */
public final class CanonicalSerializer {

    /** Classe utilitaria: nao deve ser instanciada. */
    private CanonicalSerializer() {
        // Construtor privado.
    }

    /**
     * Converte um objeto para a sua representacao canonica em JSON.
     *
     * <p>Requisitos a garantir na implementacao (Etapa 5):</p>
     * <ul>
     *   <li>Ordem de campos <b>deterministica</b> (ex.: alfabetica).</li>
     *   <li>Sem espacos nem quebras de linha desnecessarios.</li>
     *   <li>Codificacao UTF-8.</li>
     * </ul>
     *
     * @param objeto objeto a serializar
     * @return o JSON canonico
     */
    // TODO (Etapa 5): implementar com Jackson (ObjectMapper) e ordenacao de campos.
    public static String paraJson(Object objeto) {
        throw new UnsupportedOperationException("A implementar na Etapa 5");
    }

    /**
     * Reconstroi um objeto a partir do seu JSON.
     *
     * <p>Deve validar o tipo esperado antes de devolver, para evitar
     * desserializacao de conteudo inesperado.</p>
     *
     * @param json texto JSON
     * @param tipo classe do objeto esperado
     * @param <T> tipo do objeto
     * @return o objeto reconstruido
     */
    // TODO (Etapa 5): implementar com ObjectMapper.readValue + validacao.
    public static <T> T deJson(String json, Class<T> tipo) {
        throw new UnsupportedOperationException("A implementar na Etapa 5");
    }
}
