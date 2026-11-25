package pt.healthpass.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import pt.healthpass.app.HealthPassDemo;

/** Interface simples de demonstração para emissor, titular, verificador e rede. */
public final class HealthPassDashboard {
    private final JFrame frame = new JFrame("HealthPass — identidade digital de saúde");
    private final JTextArea output = new JTextArea(12, 80);
    private final JLabel status = new JLabel("A preparar a rede local de três nós…");
    private final JTextField vaccineCode = new JTextField("VACINA-DEMO-A", 22);
    private final JTextField date = new JTextField(java.time.LocalDate.now().toString(), 12);
    private HealthPassDemo demo;

    public void abrir() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(860, 600);
        frame.setLocationRelativeTo(null);
        frame.add(status, BorderLayout.NORTH);
        output.setEditable(false);
        output.setLineWrap(true);
        frame.add(new JScrollPane(output), BorderLayout.CENTER);
        frame.setVisible(true);
        new Thread(() -> {
            try {
                demo = new HealthPassDemo();
                SwingUtilities.invokeLater(() -> {
                    status.setText("Rede pronta: 3 nós TCP locais, consenso PoA simplificado (quorum 2/3).");
                    frame.add(criarAbas(), BorderLayout.SOUTH);
                    frame.revalidate();
                    log("Sessão criada em: " + demo.getSessionDirectory());
                    log("O registo do emissor e do titular já foi incluído na blockchain.");
                    log(demo.estado());
                });
            } catch (RuntimeException e) {
                SwingUtilities.invokeLater(() -> status.setText("Falha ao iniciar: " + e.getMessage()));
            }
        }, "healthpass-startup").start();
    }

    private JTabbedPane criarAbas() {
        JTabbedPane tabs = new JTabbedPane();
        JPanel issuer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        issuer.add(new JLabel("Código fictício:")); issuer.add(vaccineCode);
        issuer.add(new JLabel("Data:")); issuer.add(date);
        JButton issue = new JButton("Emitir e confirmar");
        issue.addActionListener(e -> executar("Emissão", () -> {
            var credential = demo.emitir(vaccineCode.getText(), date.getText());
            return "Credencial emitida: " + credential.getCredentialId() + "\n" + demo.estado();
        }));
        JButton revoke = new JButton("Revogar última");
        revoke.addActionListener(e -> executar("Revogação", () -> { demo.revogar(); return "Credencial revogada.\n" + demo.estado(); }));
        issuer.add(issue); issuer.add(revoke);
        tabs.addTab("Emissor", issuer);

        JPanel holder = new JPanel(new BorderLayout());
        JButton present = new JButton("Mostrar apresentação da credencial");
        present.addActionListener(e -> executar("Apresentação", () -> demo.apresentar().paraJson()));
        holder.add(new JLabel("A credencial completa fica no armazenamento local off-chain."), BorderLayout.NORTH);
        holder.add(present, BorderLayout.CENTER);
        tabs.addTab("Titular", holder);

        JPanel verifier = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton verify = new JButton("Verificar última credencial");
        verify.addActionListener(e -> executar("Verificação", () -> {
            var result = demo.verificar();
            return "Resultado técnico: " + (result.tecnicamenteValida() ? "VÁLIDA" : "REJEITADA")
                    + "\nAssinatura válida: " + result.assinaturaValida()
                    + "\nHash corresponde: " + result.hashCorresponde()
                    + "\nEmissor autorizado: " + result.emissorAutorizado()
                    + "\nCredencial ativa: " + result.ativa()
                    + "\nDentro da validade: " + result.dentroValidade();
        }));
        verifier.add(verify);
        tabs.addTab("Verificador", verifier);

        JPanel network = new JPanel(new BorderLayout());
        JButton refresh = new JButton("Atualizar estado dos nós");
        refresh.addActionListener(e -> log(demo.estado()));
        network.add(refresh, BorderLayout.NORTH);
        tabs.addTab("Rede", network);
        return tabs;
    }

    private void executar(String operacao, java.util.concurrent.Callable<String> action) {
        status.setText(operacao + " em curso…");
        new Thread(() -> {
            try {
                String result = action.call();
                SwingUtilities.invokeLater(() -> { log(result); status.setText(operacao + " concluída."); });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> { log(operacao + " falhou: " + e.getMessage()); status.setText("Operação não concluída."); });
            }
        }, "healthpass-action").start();
    }

    private void log(String text) {
        output.append(text + "\n\n");
        output.setCaretPosition(output.getDocument().getLength());
    }
}
