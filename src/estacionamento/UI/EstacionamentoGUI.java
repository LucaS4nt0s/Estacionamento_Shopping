package estacionamento.UI;

import estacionamento.model.Carro;
import estacionamento.model.Estacionamento;
import estacionamento.util.TipoVaga;
import estacionamento.model.Vagas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EstacionamentoGUI extends JFrame {

    private Estacionamento estacionamento;
    private ExecutorService executorService;

    // Componentes da UI
    private JSpinner txtVagasGerais, txtVagasIdosos, txtVagasPCD;
    private JSpinner txtCarrosGerais, txtCarrosIdosos, txtCarrosPCD;
    private JButton btnIniciar, btnPararExecucao;
    private JTextArea logArea;
    private JLabel lblFilaEspera;
    private JTextArea filaEsperaArea; // Referência direta para a área de texto da fila de espera detalhada

    // NOVO: Painel para a visualização das vagas
    private JPanel estacionamentoPanel;
    // Mapeamento de Vaga do modelo para seu VagaPanel visual
    private Map<Vagas, VagaPanel> mapaVagaParaPanel;

    // --- Construtor do PainelControle (atualizado) ---
    public EstacionamentoGUI() {
        super("Controle de Estacionamento Inteligente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        setVisible(true);

        mapaVagaParaPanel = new HashMap<>();

        // --- Painel de Configuração de Vagas e Carros (topPanel) ---
        JPanel configPanel = new JPanel(new GridLayout(2, 1, 2, 10)); // Com espaçamento
        configPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 0, 50)); // Padding

        JPanel configVagasPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        configVagasPanel.setBorder(BorderFactory.createTitledBorder("Quantidade de Vagas"));
        configVagasPanel.add(new JLabel("Vagas Gerais:"));
        txtVagasGerais = new JSpinner(new SpinnerNumberModel(20, 1, 1000, 1));
        configVagasPanel.add(txtVagasGerais);
        configVagasPanel.add(new JLabel("Vagas Idosos:"));
        txtVagasIdosos = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
        configVagasPanel.add(txtVagasIdosos);
        configVagasPanel.add(new JLabel("Vagas PCD:"));
        txtVagasPCD = new JSpinner(new SpinnerNumberModel(5, 1, 1000, 1));
        configVagasPanel.add(txtVagasPCD);
        configPanel.add(configVagasPanel);

        JPanel configCarrosPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        configCarrosPanel.setBorder(BorderFactory.createTitledBorder("Quantidade de Carros"));
        configCarrosPanel.add(new JLabel("Carros Gerais:"));
        txtCarrosGerais = new JSpinner(new SpinnerNumberModel(30, 1, 1000, 1));
        configCarrosPanel.add(txtCarrosGerais);
        configCarrosPanel.add(new JLabel("Carros Idosos:"));
        txtCarrosIdosos = new JSpinner(new SpinnerNumberModel(15, 1, 1000, 1));
        configCarrosPanel.add(txtCarrosIdosos);
        configCarrosPanel.add(new JLabel("Carros PCD:"));
        txtCarrosPCD = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
        configCarrosPanel.add(txtCarrosPCD);
        configPanel.add(configCarrosPanel);

        add(configPanel, BorderLayout.NORTH);

        // --- Painel de Botões de Controle (controlPanel) ---
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Centralizado com espaçamento
        btnIniciar = new JButton("Iniciar Simulação");
        btnPararExecucao = new JButton("Parar Execução");

        controlPanel.add(btnIniciar);
        controlPanel.add(btnPararExecucao);
        add(controlPanel, BorderLayout.SOUTH); // Colocar botões abaixo do log

        // --- Área de Log (lado esquerdo) ---
        logArea = new JTextArea(15, 30); // Ajustar tamanho
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Log da Simulação"));
        add(scrollPane, BorderLayout.WEST);

        // --- Painel de Visualização do Estacionamento (central) ---
        estacionamentoPanel = new JPanel();
        estacionamentoPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Layout flexível para as vagas
        estacionamentoPanel.setBorder(BorderFactory.createTitledBorder("Visualização das Vagas"));
        JScrollPane vagasScrollPane = new JScrollPane(estacionamentoPanel); // Adiciona scroll se muitas vagas
        add(vagasScrollPane, BorderLayout.CENTER);

        // --- Painel de Status (lado direito) ---
        JPanel statusPanel = new JPanel(new BorderLayout()); // Usar BorderLayout para o status e fila
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status Geral"));
        lblFilaEspera = new JLabel("Carros na Fila de Espera: --", SwingConstants.CENTER);
        lblFilaEspera.setFont(new Font("Arial", Font.BOLD, 14));
        statusPanel.add(lblFilaEspera, BorderLayout.NORTH); // Fila no topo do painel de status


        // Uma área para exibir a fila de espera detalhada
        filaEsperaArea = new JTextArea(5, 20); // Referência direta
        filaEsperaArea.setEditable(false);
        JScrollPane filaScrollPane = new JScrollPane(filaEsperaArea);
        filaScrollPane.setBorder(BorderFactory.createTitledBorder("Fila de Espera Detalhada"));
        statusPanel.add(filaScrollPane, BorderLayout.CENTER);


        add(statusPanel, BorderLayout.EAST); // Painel de status à direita

//        // Desabilitar botões inicialmente
//        btnPausar.setEnabled(false);
//        btnRetomar.setEnabled(false);
//        btnAdicionarCarros.setEnabled(false);
//        btnEncerrar.setEnabled(false);
//
//        // --- Ações dos Botões ---
//        btnIniciar.addActionListener(this::iniciarSimulacao);
//        btnPausar.addActionListener(e -> pausarSimulacao());
//        btnRetomar.addActionListener(e -> retomarSimulacao());
//        btnAdicionarCarros.addActionListener(this::adicionarNovosCarros);
//        btnEncerrar.addActionListener(e -> encerrarSimulacao());
//
//        setVisible(true);
//    }
//
//    // --- Métodos de Controle da Simulação (atualizados) ---
//
//    private void iniciarSimulacao(ActionEvent e) {
//        // ... (Mesma lógica de antes para encerrar simulação anterior) ...
//        if (estacionamento != null) {
//            encerrarSimulacao();
//            try { Thread.sleep(500); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
//        }
//
//        try {
//            int numVagasGerais = Integer.parseInt(txtVagasGerais.getText());
//            int numVagasIdosos = Integer.parseInt(txtVagasIdosos.getText());
//            int numVagasPCD = Integer.parseInt(txtVagasPCD.getText());
//
//            estacionamento = new Estacionamento(numVagasGerais, numVagasIdosos, numVagasPCD);
//            log("Simulação iniciada com " + numVagasGerais + " vagas gerais, " +
//                    numVagasIdosos + " de idosos e " + numVagasPCD + " de PCD.");
//
//            // NOVO: Inicializar os VagaPanels
//            estacionamentoPanel.removeAll(); // Limpa painéis anteriores
//            mapaVagaParaPanel.clear(); // Limpa o mapa
//
//            // Adicionar Vagas Gerais
//            for (Vagas vaga : estacionamento.getVagasGerais()) {
//                VagaPanel vagaPanel = new VagaPanel(vaga);
//                mapaVagaParaPanel.put(vaga, vagaPanel);
//                estacionamentoPanel.add(vagaPanel);
//            }
//            // Adicionar Vagas Idosos
//            for (Vagas vaga : estacionamento.getVagasIdosos()) {
//                VagaPanel vagaPanel = new VagaPanel(vaga);
//                mapaVagaParaPanel.put(vaga, vagaPanel);
//                estacionamentoPanel.add(vagaPanel);
//            }
//            // Adicionar Vagas PCD
//            for (Vagas vaga : estacionamento.getVagasPCD()) {
//                VagaPanel vagaPanel = new VagaPanel(vaga);
//                mapaVagaParaPanel.put(vaga, vagaPanel);
//                estacionamentoPanel.add(vagaPanel);
//            }
//            estacionamentoPanel.revalidate(); // Revalida o layout para exibir as novas vagas
//            estacionamentoPanel.repaint(); // Repinta
//
//            // Resetar executor (não precisamos de threadsDosCarros agora)
//            if (executorService != null && !executorService.isShutdown()) {
//                executorService.shutdownNow();
//            }
//            executorService = Executors.newCachedThreadPool();
//
//            // Gerar carros iniciais
//            adicionarNovosCarros(null);
//
//            btnIniciar.setEnabled(false);
//            btnPausar.setEnabled(true);
//            btnRetomar.setEnabled(false);
//            btnAdicionarCarros.setEnabled(true);
//            btnEncerrar.setEnabled(true);
//
//            // Iniciar o monitoramento da UI (agora também atualiza os VagaPanels)
//            iniciarMonitoramentoUI();
//
//        } catch (NumberFormatException ex) {
//            JOptionPane.showMessageDialog(this, "Por favor, insira números válidos para as vagas.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
//        }
//    }
//
//    private void adicionarNovosCarros(ActionEvent e) {
//        if (estacionamento == null) {
//            log("Inicie a simulação antes de adicionar carros.");
//            return;
//        }
//        try {
//            int carrosGerais = Integer.parseInt(txtCarrosGerais.getText());
//            int carrosIdosos = Integer.parseInt(txtCarrosIdosos.getText());
//            int carrosPCD = Integer.parseInt(txtCarrosPCD.getText());
//
//            Random random = new Random();
//
//            for (int i = 0; i < carrosGerais; i++) {
//                Carro carro = new Carro("CAR-" + System.nanoTime(), TipoVaga.GERAL, estacionamento);
//                // Não adicionamos mais a threadsDosCarros
//                executorService.execute(carro); // Executa a tarefa no pool
//                Thread.sleep(random.nextInt(200)); // Pequena pausa
//            }
//            for (int i = 0; i < carrosIdosos; i++) {
//                Carro carro = new Carro("IDOSO-" + System.nanoTime(), TipoVaga.IDOSO, estacionamento);
//                executorService.execute(carro);
//                Thread.sleep(random.nextInt(200));
//            }
//            for (int i = 0; i < carrosPCD; i++) {
//                Carro carro = new Carro("PCD-" + System.nanoTime(), TipoVaga.PCD, estacionamento);
//                executorService.execute(carro);
//                Thread.sleep(random.nextInt(200));
//            }
//            log("Adicionados novos carros: " + carrosGerais + " gerais, " + carrosIdosos + " idosos, " + carrosPCD + " PCD.");
//
//        } catch (NumberFormatException | InterruptedException ex) {
//            JOptionPane.showMessageDialog(this, "Erro ao adicionar carros: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
//            Thread.currentThread().interrupt(); // Re-interrompe se for InterruptedException
//        }
//    }
//
//    private void pausarSimulacao() {
//        estacionamento.setSimulacaoPausada(true); // Atualiza a flag no objeto Estacionamento
//        btnPausar.setEnabled(false);
//        btnRetomar.setEnabled(true);
//        log("Simulação pausada.");
//    }
//
//    private void retomarSimulacao() {
//        estacionamento.setSimulacaoPausada(false); // Atualiza a flag no objeto Estacionamento
//        btnPausar.setEnabled(true);
//        btnRetomar.setEnabled(false);
//        log("Simulação retomada.");
//    }
//
//    private void encerrarSimulacao() {
//        if (executorService != null) {
//            executorService.shutdownNow(); // Tenta parar todas as threads imediatamente
//            try {
//                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) { // Espera um pouco
//                    log("Algumas threads não terminaram a tempo.");
//                }
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//        estacionamento = null; // Limpa a referência do estacionamento
//        // Não precisamos mais de threadsDosCarros, o ExecutorService gerencia
//
//        log("Simulação encerrada.");
//
//        // Limpar e remover VagaPanels
//        estacionamentoPanel.removeAll();
//        mapaVagaParaPanel.clear();
//        estacionamentoPanel.revalidate();
//        estacionamentoPanel.repaint();
//
//        btnIniciar.setEnabled(true);
//        btnPausar.setEnabled(false);
//        btnRetomar.setEnabled(false);
//        btnAdicionarCarros.setEnabled(false);
//        btnEncerrar.setEnabled(false);
//
//        // Resetar labels de status
//        lblFilaEspera.setText("Carros na Fila de Espera: --");
//        filaEsperaArea.setText(""); // Limpa a área de texto da fila detalhada
//    }
//
//    private void log(String message) {
//        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
//    }
//
//    // --- Monitoramento da UI (atualizado para VagaPanels) ---
//    private void iniciarMonitoramentoUI() {
//        new Thread(() -> {
//            while (estacionamento != null && !executorService.isShutdown()) {
//                try {
//                    SwingUtilities.invokeLater(() -> {
//                        if (estacionamento != null) {
//                            // Atualizar cada VagaPanel individualmente
//                            for (Vagas vaga : estacionamento.getVagasGerais()) {
//                                mapaVagaParaPanel.get(vaga).atualizarEstado();
//                            }
//                            for (Vagas vaga : estacionamento.getVagasIdosos()) {
//                                mapaVagaParaPanel.get(vaga).atualizarEstado();
//                            }
//                            for (Vagas vaga : estacionamento.getVagasPCD()) {
//                                mapaVagaParaPanel.get(vaga).atualizarEstado();
//                            }
//
//                            // Atualizar a label da fila de espera
//                            synchronized (estacionamento.getFilaDeEspera()) {
//                                lblFilaEspera.setText("Carros na Fila de Espera: " + estacionamento.getFilaDeEspera().size());
//                                // Atualizar a área de texto da fila detalhada
//                                StringBuilder filaDetalhada = new StringBuilder();
//                                for (Carro c : estacionamento.getFilaDeEspera()) {
//                                    filaDetalhada.append(c.getPlaca()).append(" (").append(c.getTipo().name().charAt(0)).append(")\n");
//                                }
//                                filaEsperaArea.setText(filaDetalhada.toString()); // Usando a referência direta
//                            }
//                        }
//                    });
//                    TimeUnit.SECONDS.sleep(1); // Atualiza a cada 1 segundo
//                } catch (InterruptedException e) {
//                    System.out.println("Monitoramento da UI interrompido.");
//                    Thread.currentThread().interrupt();
//                    break;
//                }
//            }
//        }, "MonitoramentoUI-Thread").start();
//    }

//    // --- Método main para iniciar a aplicação Swing (já existente) ---
//    public static void main(String[] args) {
//        SwingUtilities.invokeLater(EstacionamentoGUI::new);
//    }
    }
}