package pt.healthpass.app;

import java.awt.GraphicsEnvironment;
import java.util.Scanner;
import javax.swing.SwingUtilities;
import pt.healthpass.gui.HealthPassDashboard;

/** Arranque da demonstração HealthPass (interface gráfica ou terminal). */
public final class HealthPassApplication {
    private HealthPassApplication() { }

    public static void main(String[] args) {
        if (args.length > 0 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            System.out.println("HealthPass — demonstração local\nUso: mvn exec:java [-Dexec.args=--cli]\n"
                    + "Sem argumentos abre a interface gráfica; --cli abre o menu de terminal.");
            return;
        }
        if (args.length > 0 && "--cli".equals(args[0]) || GraphicsEnvironment.isHeadless()) {
            executarTerminal();
            return;
        }
        SwingUtilities.invokeLater(() -> new HealthPassDashboard().abrir());
    }

    private static void executarTerminal() {
        HealthPassDemo demo = null;
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("A iniciar três nós HealthPass locais. Aguarde...");
            demo = new HealthPassDemo();
            System.out.println("Rede pronta. DIDs e registo inicial já foram confirmados por quorum.");
            System.out.println("Só use dados fictícios. Comandos: emitir, apresentar, verificar, revogar, estado, sair");
            boolean continuar = true;
            while (continuar) {
                System.out.print("healthpass> ");
                if (!scanner.hasNextLine()) break;
                String comando = scanner.nextLine().trim().toLowerCase();
                try {
                    switch (comando) {
                        case "emitir" -> {
                            System.out.print("Código fictício da vacina: ");
                            String codigo = scanner.nextLine();
                            System.out.print("Data (AAAA-MM-DD, Enter = hoje): ");
                            String data = scanner.nextLine();
                            var credential = demo.emitir(codigo, data);
                            System.out.println("Credencial emitida e confirmada: " + credential.getCredentialId());
                        }
                        case "apresentar" -> System.out.println(demo.apresentar().paraJson());
                        case "verificar" -> System.out.println(demo.verificar());
                        case "revogar" -> { demo.revogar(); System.out.println("Revogação registada e confirmada."); }
                        case "estado" -> System.out.print(demo.estado());
                        case "sair", "exit", "quit" -> continuar = false;
                        case "" -> { }
                        default -> System.out.println("Comando desconhecido. Use: emitir, apresentar, verificar, revogar, estado, sair");
                    }
                } catch (RuntimeException e) {
                    System.out.println("Operação não concluída: " + e.getMessage());
                }
            }
        } catch (RuntimeException e) {
            System.err.println("Não foi possível iniciar a demonstração: " + e.getMessage());
        } finally {
            if (demo != null) demo.close();
            System.out.println("Nós parados. Os ficheiros da sessão foram preservados.");
        }
    }
}
