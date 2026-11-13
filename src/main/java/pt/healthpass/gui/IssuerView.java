package pt.healthpass.gui;

import javax.swing.JFrame;

/**
 * Interface do Emissor (Issuer).
 *
 * <p>Permite a uma instituicao emissora:</p>
 * <ul>
 *   <li>Criar uma credencial ficticia.</li>
 *   <li>Selecionar o titular.</li>
 *   <li>Assinar a credencial.</li>
 *   <li>Emitir a transacao.</li>
 *   <li>Consultar credenciais emitidas.</li>
 *   <li>Revogar uma credencial.</li>
 * </ul>
 *
 * <p><b>Regra:</b> esta interface nao contem logica criptografica nem de
 * consenso. Deve apenas chamar {@code credential.CredentialIssuer} e
 * {@code identity.IdentityService}.</p>
 *
 * <p><b>Origem:</b> novo (reaproveita o estilo Swing de
 * {@code digitalID.ui.DigitalIDui} da TemplarCoin).</p>
 *
 * @author HealthPass
 */
public class IssuerView extends JFrame {

    /**
     * Cria a janela do emissor.
     */
    // TODO (Etapa 8): construir os componentes Swing e ligar aos servicos.
    public IssuerView() {
        setTitle("HealthPass - Emissor");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 400);
    }
}
