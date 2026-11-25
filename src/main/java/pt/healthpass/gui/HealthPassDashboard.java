package pt.healthpass.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import pt.healthpass.app.HealthPassDemo;
import pt.healthpass.credential.Credential;
import pt.healthpass.credential.CredentialVerifier;
import pt.healthpass.credential.VaccinationCredential;

/** Interface da demonstração HealthPass, organizada pelos papéis do enunciado. */
public final class HealthPassDashboard {
    private static final Color INK = new Color(24, 42, 58);
    private static final Color MUTED = new Color(103, 120, 134);
    private static final Color TEAL = new Color(14, 132, 126);
    private static final Color PALE = new Color(244, 248, 249);
    private static final Color LINE = new Color(224, 232, 235);
    private static final Color WHITE = Color.WHITE;

    private final JFrame frame = new JFrame("HealthPass | Saúde digital de confiança");
    private final JPanel pages = new JPanel(new CardLayout());
    private final JLabel pageTitle = new JLabel("Visão geral");
    private final JLabel networkBadge = pill("A iniciar rede…", new Color(234, 243, 242), TEAL);
    private final JLabel credentialBadge = pill("SEM CREDENCIAL", new Color(239, 242, 244), MUTED);
    private final JLabel homeCredential = new JLabel("Ainda não foi emitida");
    private final JLabel homeHeight = new JLabel("—");
    private final JTextArea audit = area(6);
    private final JTextArea presentation = area(14);
    private final JTextArea presentedInput = area(7);
    private final JTextArea networkDetails = area(12);
    private final JLabel[] checkValues = new JLabel[5];
    private final JTextField vaccineCode = new JTextField("VACINA-DEMO-A", 20);
    private final JTextField administrationDate = new JTextField(LocalDate.now().toString(), 12);
    private final JLabel issuerResult = new JLabel("Preencha os dados sintéticos para emitir uma credencial.");
    private final JLabel holderSummary = new JLabel("A carteira de demonstração está vazia.");
    private final JLabel verificationBanner = new JLabel("Apresente uma credencial para iniciar a verificação.");
    private HealthPassDemo demo;

    public void abrir() {
        configurarEstilo();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(920, 680));
        frame.setSize(1120, 780);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.add(criarSidebar(), BorderLayout.WEST);
        frame.add(criarWorkspace(), BorderLayout.CENTER);
        frame.setVisible(true);
        new Thread(() -> {
            try {
                demo = new HealthPassDemo();
                SwingUtilities.invokeLater(() -> {
                    networkBadge.setText("REDE LOCAL ATIVA · 3 NÓS");
                    log("Sessão iniciada. Identidades fictícias do emissor e titular registadas na blockchain.");
                    atualizarRede();
                    frame.setTitle("HealthPass | Saúde digital de confiança");
                });
            } catch (RuntimeException e) {
                SwingUtilities.invokeLater(() -> networkBadge.setText("FALHA AO INICIAR A REDE"));
                SwingUtilities.invokeLater(() -> log("Falha ao iniciar: " + e.getMessage()));
            }
        }, "healthpass-startup").start();
    }

    private JPanel criarSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setPreferredSize(new Dimension(228, 0));
        side.setBackground(INK);
        side.setBorder(new EmptyBorder(22, 15, 18, 15));
        JPanel top = new JPanel(); top.setOpaque(false); top.setLayout(new javax.swing.BoxLayout(top, javax.swing.BoxLayout.Y_AXIS));
        JLabel brand = new JLabel("✚  HealthPass"); brand.setForeground(WHITE); brand.setFont(new Font("SansSerif", Font.BOLD, 22));
        JLabel sub = label("IDENTIDADE DIGITAL DE SAÚDE", new Color(171, 194, 201), 10, Font.BOLD);
        top.add(brand); top.add(Box.gap(7)); top.add(sub); top.add(Box.gap(34));
        JLabel navLabel = label("ESPAÇO DE DEMONSTRAÇÃO", new Color(139, 163, 174), 10, Font.BOLD);
        top.add(navLabel); top.add(Box.gap(10));
        top.add(nav("Visão geral", "overview")); top.add(nav("Emissor · Hospital A", "issuer"));
        top.add(nav("Carteira · Titular", "holder")); top.add(nav("Verificação · Hospital B", "verifier"));
        top.add(nav("Rede e validadores", "network"));
        JPanel bottom = new JPanel(); bottom.setOpaque(false); bottom.setLayout(new javax.swing.BoxLayout(bottom, javax.swing.BoxLayout.Y_AXIS));
        bottom.add(label("AMBIENTE ACADÉMICO", new Color(171, 194, 201), 10, Font.BOLD));
        bottom.add(Box.gap(8)); bottom.add(label("Dados exclusivamente sintéticos", WHITE, 12, Font.PLAIN));
        bottom.add(Box.gap(5)); bottom.add(label("A blockchain guarda provas técnicas.", new Color(171, 194, 201), 11, Font.PLAIN));
        side.add(top, BorderLayout.NORTH); side.add(bottom, BorderLayout.SOUTH);
        return side;
    }

    private JButton nav(String text, String page) {
        JButton b = new JButton(text); b.setHorizontalAlignment(JButton.LEFT); b.setForeground(new Color(225, 236, 239));
        b.setFont(new Font("SansSerif", Font.PLAIN, 13)); b.setBackground(INK); b.setBorder(new EmptyBorder(12, 10, 12, 8));
        b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        b.addActionListener(e -> showPage(page)); return b;
    }

    private JPanel criarWorkspace() {
        JPanel root = new JPanel(new BorderLayout()); root.setBackground(PALE);
        JPanel header = new JPanel(new BorderLayout()); header.setBackground(WHITE); header.setBorder(new EmptyBorder(16, 28, 16, 28));
        header.add(pageTitle, BorderLayout.WEST); pageTitle.setFont(new Font("SansSerif", Font.BOLD, 20)); pageTitle.setForeground(INK);
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); headerRight.setOpaque(false);
        headerRight.add(pill("MODO DEMONSTRAÇÃO", new Color(237, 244, 247), new Color(72, 105, 121))); headerRight.add(networkBadge);
        header.add(headerRight, BorderLayout.EAST); header.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,LINE), new EmptyBorder(16,28,16,28)));
        pages.setBackground(PALE); pages.setBorder(new EmptyBorder(24, 28, 22, 28));
        pages.add(criarOverview(), "overview"); pages.add(criarIssuer(), "issuer"); pages.add(criarHolder(), "holder");
        pages.add(criarVerifier(), "verifier"); pages.add(criarNetwork(), "network");
        root.add(header, BorderLayout.NORTH); root.add(pages, BorderLayout.CENTER); return root;
    }

    private JPanel criarOverview() {
        JPanel p = vertical();
        p.add(hero("Credenciais de saúde, com confiança verificável", "Emita, apresente e verifique uma credencial fictícia através de uma rede permissionada."));
        JPanel stats = new JPanel(new GridLayout(1, 3, 14, 0)); stats.setOpaque(false);
        stats.add(stat("CREDENCIAL DA SESSÃO", homeCredential)); stats.add(stat("ESTADO ATUAL", credentialBadge)); stats.add(stat("ALTURA DA CADEIA", homeHeight)); p.add(stats);
        JPanel flow = card(); flow.setLayout(new BorderLayout(8, 8)); flow.add(sectionTitle("Percurso da credencial", "Cada papel tem uma etapa própria do fluxo do trabalho."), BorderLayout.NORTH);
        JPanel steps = new JPanel(new GridLayout(1, 4, 12, 0)); steps.setOpaque(false);
        steps.add(step("01", "Hospital A", "Emite e assina", "issuer")); steps.add(step("02", "Titular", "Guarda e apresenta", "holder"));
        steps.add(step("03", "Hospital B", "Verifica a prova", "verifier")); steps.add(step("04", "Validadores", "Replicam o registo", "network"));
        flow.add(steps, BorderLayout.CENTER); p.add(flow);
        JPanel logCard = card(); logCard.setLayout(new BorderLayout(8, 10)); logCard.add(sectionTitle("Atividade da sessão", "Registo das operações executadas nesta demonstração."), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(audit); sp.setBorder(BorderFactory.createLineBorder(LINE)); logCard.add(sp, BorderLayout.CENTER); p.add(logCard);
        return p;
    }

    private JPanel criarIssuer() {
        JPanel p = vertical(); p.add(hero("Emissão de credencial", "Hospital A · entidade emissora autorizada na rede de demonstração."));
        JPanel details = card(); details.setLayout(new BorderLayout(12, 12)); details.add(sectionTitle("Nova credencial de vacinação", "Use apenas valores fictícios. A chave privada do emissor assina a credencial localmente."), BorderLayout.NORTH);
        JPanel form = new JPanel(new GridLayout(2, 2, 12, 8)); form.setOpaque(false);
        form.add(field("Código fictício da vacina", vaccineCode)); form.add(field("Data de administração · AAAA-MM-DD", administrationDate));
        form.add(readOnlyField("DID do emissor", HealthPassDemo.ISSUER_DID.toString())); form.add(readOnlyField("DID do titular", HealthPassDemo.HOLDER_DID.toString()));
        details.add(form, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0)); actions.setOpaque(false);
        JButton issue = primary("Emitir e confirmar na rede"); issue.addActionListener(e -> executar("Emissão", () -> {
            Credential c = demo.emitir(vaccineCode.getText(), administrationDate.getText());
            return "Credencial emitida e confirmada: " + c.getCredentialId();
        }, this::afterIssue));
        JButton revoke = secondary("Revogar credencial"); revoke.addActionListener(e -> executar("Revogação", () -> { demo.revogar(); return "Credencial revogada e registada na rede."; }, result -> refreshAll()));
        actions.add(issue); actions.add(revoke); details.add(actions, BorderLayout.SOUTH); p.add(details);
        JPanel result = card(); result.setLayout(new BorderLayout()); result.add(sectionTitle("Resultado da emissão", "A credencial completa fica no armazenamento local off-chain."), BorderLayout.NORTH);
        issuerResult.setForeground(MUTED); issuerResult.setBorder(new EmptyBorder(12, 0, 4, 0)); result.add(issuerResult, BorderLayout.CENTER); p.add(result);
        p.add(note("Registo on-chain: identificador, DIDs, hash e evento técnico. O conteúdo completo não é escrito na blockchain.")); return p;
    }

    private JPanel criarHolder() {
        JPanel p = vertical(); p.add(hero("Carteira do titular", "O titular controla a credencial e decide quando a apresenta ao verificador."));
        JPanel summary = card(); summary.setLayout(new BorderLayout(8, 10)); summary.add(sectionTitle("Credencial guardada localmente", "Carteira de demonstração · armazenamento off-chain nesta sessão."), BorderLayout.NORTH);
        holderSummary.setFont(new Font("SansSerif", Font.PLAIN, 14)); holderSummary.setForeground(INK); summary.add(holderSummary, BorderLayout.CENTER);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0)); buttons.setOpaque(false);
        JButton present = primary("Preparar apresentação"); present.addActionListener(e -> executar("Apresentação", () -> demo.apresentar().paraJson(), json -> { presentation.setText(json); log("Titular preparou a apresentação da credencial."); }));
        JButton copy = secondary("Copiar JSON"); copy.addActionListener(e -> { if (presentation.getText().isBlank()) return; java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(presentation.getText()), null); log("JSON da apresentação copiado para a área de transferência."); });
        buttons.add(present); buttons.add(copy); summary.add(buttons, BorderLayout.SOUTH); p.add(summary);
        JPanel json = card(); json.setLayout(new BorderLayout(8, 8)); json.add(sectionTitle("Apresentação partilhável", "Representação JSON da credencial assinada; nesta versão permanece na mesma aplicação."), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(presentation); sp.setBorder(BorderFactory.createLineBorder(LINE)); json.add(sp, BorderLayout.CENTER); p.add(json); return p;
    }

    private JPanel criarVerifier() {
        JPanel p = vertical(); p.add(hero("Verificação da credencial", "Hospital B · validação técnica independente do papel emissor."));
        JPanel received = card(); received.setLayout(new BorderLayout(8, 10)); received.add(sectionTitle("Credencial recebida", "Cole aqui o JSON preparado pela carteira do titular."), BorderLayout.NORTH);
        JScrollPane inputScroll = new JScrollPane(presentedInput); inputScroll.setBorder(BorderFactory.createLineBorder(LINE)); received.add(inputScroll, BorderLayout.CENTER);
        JButton paste = secondary("Colar da área de transferência"); paste.addActionListener(e -> { try { presentedInput.setText((String)java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().getData(java.awt.datatransfer.DataFlavor.stringFlavor)); } catch (Exception ex) { log("Não foi possível ler texto da área de transferência."); } });
        JPanel pasteRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)); pasteRow.setOpaque(false); pasteRow.add(paste); received.add(pasteRow, BorderLayout.SOUTH); p.add(received);
        JPanel result = card(); result.setLayout(new BorderLayout(8, 12)); result.add(sectionTitle("Resultado da análise", "A credencial só é aceite quando todas as verificações obrigatórias passam."), BorderLayout.NORTH);
        JPanel banner = new JPanel(new BorderLayout()); banner.setBackground(new Color(237, 244, 247)); banner.setBorder(new EmptyBorder(14, 16, 14, 16));
        verificationBanner.setFont(new Font("SansSerif", Font.BOLD, 16)); verificationBanner.setForeground(INK); banner.add(verificationBanner);
        String[] names = {"Assinatura do emissor", "Hash corresponde ao registo", "Emissor autorizado", "Credencial ativa", "Dentro do prazo de validade"};
        JPanel checks = new JPanel(new GridLayout(5, 1, 0, 5)); checks.setOpaque(false);
        for (int i=0;i<names.length;i++) { JPanel row = new JPanel(new BorderLayout()); row.setOpaque(false); row.add(label("✓   " + names[i], INK, 13, Font.PLAIN), BorderLayout.WEST); checkValues[i] = label("Por verificar", MUTED, 12, Font.BOLD); row.add(checkValues[i], BorderLayout.EAST); checks.add(row); }
        JPanel center = new JPanel(new BorderLayout(0, 14)); center.setOpaque(false); center.add(banner, BorderLayout.NORTH); center.add(checks, BorderLayout.CENTER); result.add(center, BorderLayout.CENTER);
        JButton verify = primary("Verificar apresentação recebida"); verify.addActionListener(e -> executar("Verificação", () -> {
            JsonNode root = new ObjectMapper().readTree(presentedInput.getText());
            JsonNode credentialNode = root.has("credential") ? root.get("credential") : root;
            if (credentialNode == null || credentialNode.isNull()) throw new IllegalArgumentException("O JSON não contém uma credencial.");
            Credential presented = new ObjectMapper().treeToValue(credentialNode, VaccinationCredential.class);
            CredentialVerifier.Resultado r = demo.verificar(presented);
            return (r.tecnicamenteValida() ? "VÁLIDA" : "REJEITADA") + "|" + r.assinaturaValida() + "|" + r.hashCorresponde() + "|" + r.emissorAutorizado() + "|" + r.ativa() + "|" + r.dentroValidade();
        }, this::showVerification));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); actions.add(verify); result.add(actions, BorderLayout.SOUTH); p.add(result);
        p.add(note("O resultado confirma propriedades criptográficas e o estado registado. Não constitui validação clínica.")); return p;
    }

    private JPanel criarNetwork() {
        JPanel p = vertical(); p.add(hero("Rede e validadores", "Monitorização técnica das réplicas, dos blocos e da convergência P2P."));
        JPanel summary = new JPanel(new GridLayout(1, 3, 14, 0)); summary.setOpaque(false);
        summary.add(stat("VALIDADORES", label("3 nós TCP", INK, 20, Font.BOLD))); summary.add(stat("CONSENSO", label("PoA · quorum 2/3", INK, 17, Font.BOLD))); summary.add(stat("ALTURA ATUAL", homeHeight)); p.add(summary);
        JPanel details = card(); details.setLayout(new BorderLayout(8, 10)); details.add(sectionTitle("Estado das réplicas", "Cada nó mantém a sua cadeia persistida e participa na confirmação dos blocos."), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(networkDetails); sp.setBorder(BorderFactory.createLineBorder(LINE)); details.add(sp, BorderLayout.CENTER);
        JButton refresh = secondary("Atualizar estado dos nós"); refresh.addActionListener(e -> atualizarRede()); JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false); actions.add(refresh); details.add(actions, BorderLayout.SOUTH); p.add(details);
        p.add(note("Na GUI, os três nós são endpoints locais executados na mesma máquina e JVM. Consulte o relatório para as limitações da prova de conceito.")); return p;
    }

    private void showVerification(String result) {
        String[] values = result.split("\\|"); boolean valid = "VÁLIDA".equals(values[0]);
        verificationBanner.setText("●   CREDENCIAL " + values[0]); verificationBanner.setForeground(valid ? new Color(17, 112, 82) : new Color(170, 61, 57));
        for (int i=0;i<5;i++) { boolean passed = Boolean.parseBoolean(values[i+1]); checkValues[i].setText(passed ? "CONFIRMADO" : "FALHOU"); checkValues[i].setForeground(passed ? new Color(17,112,82) : new Color(170,61,57)); }
        log("Verificação técnica concluída: " + values[0] + ".");
    }

    private void afterIssue(String message) {
        issuerResult.setText("Emitida e confirmada · " + message.substring(message.indexOf(':') + 1).trim());
        refreshAll();
    }

    private void refreshAll() {
        atualizarRede();
        Credential c = demo == null ? null : demo.ultimaCredencial();
        if (c != null) {
            homeCredential.setText(shortId(c.getCredentialId()));
            boolean active = demo.verificar().ativa();
            credentialBadge.setText(active ? "ATIVA" : "REVOGADA"); credentialBadge.setForeground(active ? new Color(17,112,82) : new Color(170,61,57));
            holderSummary.setText("Vacinação de demonstração · " + (c instanceof VaccinationCredential v ? v.getVaccineCode() : c.getType()) + " · administrada em " + ((VaccinationCredential)c).getAdministrationDate() + " · emissor Hospital A");
        }
    }

    private void atualizarRede() {
        if (demo == null) return;
        String state = demo.estado(); networkDetails.setText(state); networkDetails.setCaretPosition(0);
        var heights = demo.alturas(); homeHeight.setText(heights.isEmpty() ? "—" : String.valueOf(heights.get(0)));
        boolean converged = heights.stream().distinct().count() == 1;
        networkBadge.setText(converged ? "REDE CONVERGENTE · 3 NÓS" : "A SINCRONIZAR RÉPLICAS");
        if (demo.ultimaCredencial() == null) { credentialBadge.setText("SEM CREDENCIAL"); }
    }

    private void executar(String operation, java.util.concurrent.Callable<String> action, java.util.function.Consumer<String> onSuccess) {
        if (demo == null) { log("A rede ainda está a iniciar."); return; }
        networkBadge.setText(operation.toUpperCase() + " EM CURSO…");
        new Thread(() -> {
            try { String result = action.call(); SwingUtilities.invokeLater(() -> { log(result); onSuccess.accept(result); networkBadge.setText("REDE LOCAL ATIVA · 3 NÓS"); }); }
            catch (Exception e) { SwingUtilities.invokeLater(() -> { log(operation + " falhou: " + e.getMessage()); networkBadge.setText("OPERAÇÃO NÃO CONCLUÍDA"); }); }
        }, "healthpass-action").start();
    }

    private void log(String text) { audit.append("• " + text + "\n\n"); audit.setCaretPosition(audit.getDocument().getLength()); }
    private void showPage(String key) {
        String[] titles = {"overview|Visão geral", "issuer|Emissor · Hospital A", "holder|Carteira · Titular", "verifier|Verificação · Hospital B", "network|Rede e validadores"};
        for (String entry : titles) if (entry.startsWith(key + "|")) pageTitle.setText(entry.substring(entry.indexOf('|') + 1));
        ((CardLayout) pages.getLayout()).show(pages, key);
    }

    private static JPanel hero(String title, String description) { JPanel p = new JPanel(); p.setOpaque(false); p.setLayout(new javax.swing.BoxLayout(p, javax.swing.BoxLayout.Y_AXIS)); p.add(label(title, INK, 23, Font.BOLD)); p.add(Box.gap(6)); p.add(label(description, MUTED, 13, Font.PLAIN)); p.setBorder(new EmptyBorder(0, 0, 3, 0)); return p; }
    private static JPanel vertical() { JPanel p = new JPanel(); p.setOpaque(false); p.setLayout(new javax.swing.BoxLayout(p, javax.swing.BoxLayout.Y_AXIS)); return p; }
    private static JPanel card() { JPanel p = new JPanel(); p.setBackground(WHITE); p.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), new EmptyBorder(17, 18, 17, 18))); return p; }
    private static JPanel stat(String caption, Component value) { JPanel p = card(); p.setLayout(new javax.swing.BoxLayout(p, javax.swing.BoxLayout.Y_AXIS)); p.add(label(caption, MUTED, 10, Font.BOLD)); p.add(Box.gap(12)); if (value instanceof JLabel l) { l.setFont(new Font("SansSerif", Font.BOLD, 18)); l.setForeground(INK); } p.add(value); return p; }
    private static JPanel sectionTitle(String title, String sub) { JPanel p = new JPanel(); p.setOpaque(false); p.setLayout(new javax.swing.BoxLayout(p, javax.swing.BoxLayout.Y_AXIS)); p.add(label(title, INK, 15, Font.BOLD)); p.add(Box.gap(5)); p.add(label(sub, MUTED, 12, Font.PLAIN)); return p; }
    private static JPanel field(String name, JTextField input) { JPanel p = new JPanel(new BorderLayout(0, 6)); p.setOpaque(false); p.add(label(name, MUTED, 11, Font.BOLD), BorderLayout.NORTH); input.setFont(new Font("SansSerif", Font.PLAIN, 13)); input.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(LINE), new EmptyBorder(8, 9, 8, 9))); p.add(input, BorderLayout.CENTER); return p; }
    private static JPanel readOnlyField(String name, String value) { JPanel p = new JPanel(new BorderLayout(0, 6)); p.setOpaque(false); p.add(label(name, MUTED, 11, Font.BOLD), BorderLayout.NORTH); JLabel v=label(value, INK, 11, Font.PLAIN); v.setBorder(new EmptyBorder(9, 0, 8, 0)); p.add(v); return p; }
    private JPanel step(String n, String title, String text, String page) { JPanel p = card(); p.setLayout(new javax.swing.BoxLayout(p, javax.swing.BoxLayout.Y_AXIS)); p.add(label(n, TEAL, 11, Font.BOLD)); p.add(Box.gap(11)); p.add(label(title, INK, 14, Font.BOLD)); p.add(Box.gap(4)); p.add(label(text, MUTED, 12, Font.PLAIN)); p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); p.addMouseListener(new java.awt.event.MouseAdapter(){@Override public void mouseClicked(java.awt.event.MouseEvent e){showPage(page);}}); return p; }
    private static JPanel note(String text) { JPanel p = new JPanel(new BorderLayout()); p.setBackground(new Color(235, 243, 245)); p.setBorder(new EmptyBorder(12, 14, 12, 14)); p.add(label("ⓘ  " + text, new Color(72, 101, 113), 11, Font.PLAIN)); return p; }
    private static JLabel pill(String text, Color bg, Color fg) { JLabel l = label(text, fg, 10, Font.BOLD); l.setOpaque(true); l.setBackground(bg); l.setBorder(new EmptyBorder(7, 10, 7, 10)); return l; }
    private static JLabel label(String text, Color color, int size, int style) { JLabel l = new JLabel(text); l.setForeground(color); l.setFont(new Font("SansSerif", style, size)); return l; }
    private static JButton primary(String text) { JButton b = button(text, TEAL, WHITE); return b; }
    private static JButton secondary(String text) { return button(text, WHITE, INK); }
    private static JButton button(String text, Color bg, Color fg) { JButton b = new JButton(text); b.setBackground(bg); b.setForeground(fg); b.setFont(new Font("SansSerif", Font.BOLD, 12)); b.setFocusPainted(false); b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); b.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(bg == WHITE ? LINE : bg), new EmptyBorder(9, 14, 9, 14))); return b; }
    private static JTextArea area(int rows) { JTextArea a = new JTextArea(rows, 60); a.setEditable(false); a.setLineWrap(true); a.setWrapStyleWord(true); a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11)); a.setForeground(INK); a.setBackground(new Color(251, 253, 253)); a.setBorder(new EmptyBorder(10, 11, 10, 11)); return a; }
    private static String shortId(String id) { return id == null ? "—" : id.length() > 22 ? id.substring(0, 19) + "…" : id; }
    private static void configurarEstilo() { UIManager.put("Panel.background", PALE); UIManager.put("Label.foreground", INK); }
    private static final class Box { static Component gap(int height) { return javax.swing.Box.createRigidArea(new Dimension(1,height)); } }
}
