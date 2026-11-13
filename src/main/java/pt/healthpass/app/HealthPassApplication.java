package pt.healthpass.app;

import pt.healthpass.config.HealthPassConfig;

/**
 * Ponto de entrada da aplicacao HealthPass.
 *
 * <p>Este e o {@code main} do projeto. Numa versao completa, devera:</p>
 * <ol>
 *   <li>Carregar a configuracao ({@link HealthPassConfig}).</li>
 *   <li>Inicializar a blockchain (ledger).</li>
 *   <li>Criar/carregar as identidades de demonstracao.</li>
 *   <li>Arrancar os nos da rede P2P.</li>
 *   <li>Abrir a interface adequada ao papel do utilizador.</li>
 * </ol>
 *
 * <p><b>Estado atual:</b> esqueleto inicial. O {@code main} apenas confirma que
 * o projeto arranca e lista o que falta implementar. Nao executa ainda nenhuma
 * funcionalidade de negocio.</p>
 *
 * <p><b>Origem:</b> novo ponto de entrada (a TemplarCoin arrancava por
 * {@code templarCoin.ui.TemplarLogin}).</p>
 *
 * @author HealthPass
 */
public final class HealthPassApplication {

    /** Classe de aplicacao: nao deve ser instanciada. */
    private HealthPassApplication() {
        // Construtor privado.
    }

    /**
     * Metodo principal.
     *
     * @param args argumentos de linha de comandos (nao usados por agora)
     */
    public static void main(String[] args) {
        System.out.println("HealthPass - esqueleto inicial");
        System.out.println("Configuracao: chaves RSA de "
                + HealthPassConfig.TAMANHO_CHAVE_RSA + " bits, quorum "
                + HealthPassConfig.QUORUM + " de " + HealthPassConfig.NUM_VALIDADORES);
        System.out.println("Etapas a implementar: ver docs/GUIA_EXECUCAO.md e os TODO no codigo.");
    }
}
