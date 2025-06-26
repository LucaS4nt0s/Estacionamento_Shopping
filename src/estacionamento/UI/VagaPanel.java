package estacionamento.UI;

import estacionamento.model.Vagas;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;


public class VagaPanel extends JPanel {
    private Vagas vaga;
    private JLabel infoLabel;
    private static final Color COR_LIVRE = new Color(144, 238, 144); // Verde claro
    private static final Color COR_OCUPADA = new Color(255, 99, 71); // Vermelho tomate
    private static final Color COR_TIPO = new Color(220, 220, 220); // Cinza claro para o tipo de vaga

    public VagaPanel(Vagas vaga) {
        this.vaga = vaga;
        setLayout(new BorderLayout()); // Para organizar o label
        setPreferredSize(new Dimension(100, 60)); // Tamanho preferencial para cada vaga

        Border lineBorder = BorderFactory.createLineBorder(Color.DARK_GRAY, 1);
        Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        setBorder(BorderFactory.createCompoundBorder(lineBorder, emptyBorder)); // Borda visível

        // Label principal para as informações da vaga
        infoLabel = new JLabel("", SwingConstants.CENTER);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 12));
        add(infoLabel, BorderLayout.CENTER);

        // Label para o tipo da vaga (PCD, IDOSO, GERAL) na parte superior
        JLabel tipoLabel = new JLabel(vaga.getTipoVaga().name(), SwingConstants.CENTER);
        tipoLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        tipoLabel.setOpaque(true); // Para pintar o background
        tipoLabel.setBackground(COR_TIPO);
        add(tipoLabel, BorderLayout.NORTH);

        atualizarEstado(); // Define o estado inicial
    }

    // Método para atualizar a cor e o texto do painel da vaga
    public void atualizarEstado() {
        if (vaga.isOcupada()) {
            setBackground(COR_OCUPADA);
            infoLabel.setText(vaga.getId() + "<br>" + vaga.getCarroEstacionado().getPlaca());
            infoLabel.setForeground(Color.WHITE); // Texto branco para contraste
        } else {
            setBackground(COR_LIVRE);
            infoLabel.setText(vaga.getId() + "<br>Livre");
            infoLabel.setForeground(Color.BLACK); // Texto preto
        }
        // Permite HTML no JLabel para quebra de linha
        infoLabel.setText("<html>" + infoLabel.getText() + "</html>");
        repaint(); // Força a repintura do componente
    }

    public Vagas getVaga() {
        return vaga;
    }
}