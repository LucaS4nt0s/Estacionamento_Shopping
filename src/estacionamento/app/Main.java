package estacionamento.app;
import estacionamento.UI.EstacionamentoGUI;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new EstacionamentoGUI();
            }
        });
    }
}
