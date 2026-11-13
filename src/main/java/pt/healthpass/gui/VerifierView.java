package pt.healthpass.gui;

import javax.swing.JFrame;

/**
 * Interface do Verificador.
 *
 * <p>Permite a um verificador (ex.: hospital, fronteira):</p>
 * <ul>
 *   <li>Importar uma credencial.</li>
 *   <li>Executar a verificacao.</li>
 *   <li>Consultar o estado na blockchain.</li>
 *   <li>Apresentar individualmente o resultado de cada verificacao.</li>
 *   <li>Mostrar o motivo de uma rejeicao.</li>
 * </ul>
 *
 * <p><b>Regra:</b> sem logica criptografica; deve chamar
 * {@code credential.CredentialVerifier} e mostrar o {@code Resultado} detalhado.</p>
 *
 * <p><b>Origem:</b> novo.</p>
 *
 * @author HealthPass
 */
public class VerifierView extends JFrame {

    /**
     * Cria a janela do verificador.
     */
    // TODO (Etapa 8): construir os componentes Swing e ligar aos servicos.
    public VerifierView() {
        setTitle("HealthPass - Verificador");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
    }
}
