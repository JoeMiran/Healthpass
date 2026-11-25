package pt.healthpass.config;

import java.util.Set;

/**
 * Configuracao global do HealthPass.
 *
 * <p>Centraliza os parametros do sistema para evitar valores "magicos"
 * espalhados pelo codigo:</p>
 * <ul>
 *   <li>Tamanho das chaves RSA.</li>
 *   <li>Portas dos nos da rede.</li>
 *   <li>Quorum do consenso.</li>
 *   <li>Caminhos dos ficheiros (cadeia, credenciais, chaves).</li>
 * </ul>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public final class HealthPassConfig {

    /** Classe de configuracao: nao deve ser instanciada. */
    private HealthPassConfig() {
        // Construtor privado.
    }

    /** Tamanho por omissao das chaves RSA, em bits. */
    public static final int TAMANHO_CHAVE_RSA = 3072;

    /** Porta do No A (Hospital A). */
    public static final int PORTA_NO_A = 5001;

    /** Porta do No B (Hospital B). */
    public static final int PORTA_NO_B = 5002;

    /** Porta do No C (Validador adicional). */
    public static final int PORTA_NO_C = 5003;

    /** Numero de validadores na configuracao de demonstracao. */
    public static final int NUM_VALIDADORES = 3;

    /**
     * Quorum do consenso: maioria estrita de 2 em 3.
     */
    public static final int QUORUM = 2;

    /** DIDs explicitamente confiáveis como emissores na demonstração local. */
    public static final Set<String> EMISSORES_DEMO_AUTORIZADOS = Set.of("did:healthpass:issuer-demo-01");

    /** Caminho do ficheiro da cadeia (persistencia). */
    public static final String FICHEIRO_CADEIA = "data/demo/chain.json";

    /** Diretorio das credenciais guardadas off-chain. */
    public static final String DIRETORIO_CREDENCIAIS = "data/demo/credentials";
}
