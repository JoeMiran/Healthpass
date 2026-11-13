package pt.healthpass.gui;

import javax.swing.JFrame;

/**
 * Interface do Titular (Holder / carteira).
 *
 * <p>Permite ao titular das credenciais:</p>
 * <ul>
 *   <li>Consultar as credenciais guardadas.</li>
 *   <li>Visualizar os detalhes da credencial.</li>
 *   <li>Apresentar ou exportar uma credencial.</li>
 *   <li>Gerar uma representacao QR de demonstracao.</li>
 * </ul>
 *
 * <p><b>Regra:</b> sem logica criptografica nem de consenso; apenas le do
 * {@code storage.OffChainCredentialStore} e usa {@code CredentialPresentation}.</p>
 *
 * <p><b>Origem:</b> novo (reaproveita o estilo Swing de
 * {@code templarCoin.ui.TemplarCoin} da TemplarCoin).</p>
 *
 * @author HealthPass
 */
public class HolderWalletView extends JFrame {

    /**
     * Cria a janela da carteira do titular.
     */
    // TODO (Etapa 8): construir os componentes Swing e ligar aos servicos.
    public HolderWalletView() {
        setTitle("HealthPass - Titular");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
    }
}
