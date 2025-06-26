package estacionamento.UI;

import javax.swing.*;

public class EstacionamentoGUI extends JFrame{
    public EstacionamentoGUI() {
        setTitle("Estacionamento");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Aqui você pode adicionar componentes à interface gráfica
        JLabel label = new JLabel("Bem-vindo ao Estacionamento!");
        add(label);

        setVisible(true);
    }

}
