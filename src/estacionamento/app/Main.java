package estacionamento.app;
import estacionamento.UI.EstacionamentoGUI;
import estacionamento.model.Carro;
import estacionamento.model.Vagas;
import estacionamento.util.TipoVaga;
import estacionamento.model.Estacionamento;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit; // Para o Thread.sleep
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
