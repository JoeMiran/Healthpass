package pt.healthpass.crypto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.json.JsonMapper;
import java.util.Objects;

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

    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
            .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
            .serializationInclusion(JsonInclude.Include.ALWAYS)
            .build();

    /** Classe utilitaria: nao deve ser instanciada. */
    private CanonicalSerializer() {
        // Construtor privado.
    }

    /**
     * Converte um objeto para a sua representacao canonica em JSON.
     *
     * <p>Ordena propriedades e chaves de mapas alfabeticamente, inclui campos
     * nulos e não escreve espaços supérfluos. A codificação dos bytes para hash
     * é UTF-8.</p>
     * <ul>
     *   <li>Ordem de campos <b>deterministica</b> (ex.: alfabetica).</li>
     *   <li>Sem espacos nem quebras de linha desnecessarios.</li>
     *   <li>Codificacao UTF-8.</li>
     * </ul>
     *
     * @param objeto objeto a serializar
     * @return o JSON canonico
     */
    public static String paraJson(Object objeto) {
        Objects.requireNonNull(objeto, "objeto nao pode ser null");
        try {
            return MAPPER.writeValueAsString(objeto);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Nao foi possivel serializar objeto para JSON", e);
        }
    }

    /**
     * Serializa um objeto removendo a propriedade JSON {@code signature}.
     * Usado para assinar credenciais sem dependência circular.
     */
    public static String paraJsonSemAssinatura(Object objeto) {
        Objects.requireNonNull(objeto, "objeto nao pode ser null");
        var arvore = MAPPER.valueToTree(objeto);
        if (arvore.isObject()) {
            ((com.fasterxml.jackson.databind.node.ObjectNode) arvore).remove("signature");
        }
        try {
            return MAPPER.writeValueAsString(arvore);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Nao foi possivel serializar objeto para JSON", e);
        }
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
    public static <T> T deJson(String json, Class<T> tipo) {
        Objects.requireNonNull(json, "json nao pode ser null");
        Objects.requireNonNull(tipo, "tipo nao pode ser null");
        try {
            return MAPPER.readValue(json, tipo);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON invalido para " + tipo.getSimpleName(), e);
        }
    }

    /** Reconstrói uma lista JSON tipada sem ativar desserialização polimórfica. */
    public static <T> java.util.List<T> deJsonLista(String json, Class<T> tipoElemento) {
        Objects.requireNonNull(json, "json nao pode ser null");
        Objects.requireNonNull(tipoElemento, "tipoElemento nao pode ser null");
        try {
            var tipoLista = TypeFactory.defaultInstance()
                    .constructCollectionType(java.util.List.class, tipoElemento);
            return MAPPER.readValue(json, tipoLista);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON invalido para lista de "
                    + tipoElemento.getSimpleName(), e);
        }
    }
}
