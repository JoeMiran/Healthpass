package pt.healthpass.gui;

import javax.swing.JFrame;

/**
 * Interface do Administrador.
 *
 * <p>Permite observar o estado da rede:</p>
 * <ul>
 *   <li>Estado dos nos.</li>
 *   <li>Ligacoes P2P.</li>
 *   <li>Altura da cadeia.</li>
 *   <li>Ultimo hash e Merkle Root.</li>
 *   <li>Transacoes pendentes.</li>
 *   <li>Acompanhar a validacao e o consenso.</li>
 * </ul>
 *
 * <p><b>Regra:</b> apenas leitura e visualizacao; sem logica de consenso.</p>
 *
 * <p><b>Origem:</b> novo (reaproveita o explorador de blocos de
 * {@code templarCoin.ui.TemplarCoin} da TemplarCoin).</p>
 *
 * @author HealthPass
 */
public class AdminView extends JFrame {

    /**
     * Cria a janela do administrador.
     */
    // TODO (Etapa 8): construir os componentes Swing (explorador de cadeia).
    public AdminView() {
        setTitle("HealthPass - Administrador");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
    }
}
