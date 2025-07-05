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
        setLocationRelativeTo(null); // Centraliza a janela na tela
        setResizable(true); // Permite redimensionar a janela

        mapaVagaParaPanel = new HashMap<>();

        // --- Painel de Configuração de Vagas e Carros (topPanel) ---
        JPanel configPanel = new JPanel(new GridLayout(2, 1, 2, 10)); // Com espaçamento
        configPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 0, 50)); // Padding

        // --- Painel de Configuração de Vagas (configVagasPanel) ---
        JPanel configVagasPanel = new JPanel(new GridBagLayout());
        configVagasPanel.setBorder(BorderFactory.createTitledBorder("Quantidade de Vagas"));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5); // Espaçamento entre componentes
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0; // posição 0, 0 na "matriz" dos elementos
        gbc.anchor = GridBagConstraints.EAST; // Alinha à direita
        configVagasPanel.add(new JLabel("Vagas Gerais:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0; // Muda para 1, 0
        txtVagasGerais = new JSpinner(new SpinnerNumberModel(20, 1, 1000, 1));
        configVagasPanel.add(txtVagasGerais, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0; // Muda para 2, 0
        gbc.anchor = GridBagConstraints.EAST; // Alinha à direita
        configVagasPanel.add(new JLabel("Vagas Idosos:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 0; // Muda para 3, 0
        txtVagasIdosos = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
        configVagasPanel.add(txtVagasIdosos, gbc);

        gbc.gridx = 4;
        gbc.gridy = 0; // Muda para 4, 0
        gbc.anchor = GridBagConstraints.EAST; // Alinha à direita
        configVagasPanel.add(new JLabel("Vagas PCD:"), gbc);

        gbc.gridx = 5;
        gbc.gridy = 0; // Muda para 5, 0
        txtVagasPCD = new JSpinner(new SpinnerNumberModel(5, 1, 1000, 1));
        configVagasPanel.add(txtVagasPCD, gbc);

        // Adiciona o painel de configuração de vagas ao painel principal de configuração
        configPanel.add(configVagasPanel);

        // --- Painel de Configuração de Carros (configCarrosPanel) ---
        JPanel configCarrosPanel = new JPanel(new GridBagLayout());
        configCarrosPanel.setBorder(BorderFactory.createTitledBorder("Quantidade de Carros"));
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5); // Espaçamento entre componentes
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0; // posição 0, 0 na "matriz" dos elementos
        gbc.anchor = GridBagConstraints.EAST; // Alinha à direita
        configCarrosPanel.add(new JLabel("Carros Gerais:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0; // Muda para 1, 0
        txtCarrosGerais = new JSpinner(new SpinnerNumberModel(30, 1, 1000, 1));
        configCarrosPanel.add(txtCarrosGerais, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0; // Muda para 2, 0
        gbc.anchor = GridBagConstraints.EAST; // Alinha à direita
        configCarrosPanel.add(new JLabel("Carros Idosos:"), gbc);

        gbc.gridx = 3;
        gbc.gridy = 0; // Muda para 3, 0
        txtCarrosIdosos = new JSpinner(new SpinnerNumberModel(15, 1, 1000, 1));
        configCarrosPanel.add(txtCarrosIdosos, gbc);

        gbc.gridx = 4;
        gbc.gridy = 0; // Muda para 4, 0
        gbc.anchor = GridBagConstraints.EAST; // Alinha à direita
        configCarrosPanel.add(new JLabel("Carros PCD:"), gbc);

        gbc.gridx = 5;
        gbc.gridy = 0; // Muda para 5, 0
        txtCarrosPCD = new JSpinner(new SpinnerNumberModel(10, 1, 1000, 1));
        configCarrosPanel.add(txtCarrosPCD, gbc);

        // Adiciona o painel de configuração de carros ao painel principal de configuração
        configPanel.add(configCarrosPanel);

        // Adiciona o painel de configuração ao JFrame, na região superior
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
        estacionamentoPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 10, 10));
        estacionamentoPanel.setBorder(BorderFactory.createTitledBorder("Visualização das Vagas"));
        JScrollPane vagasScrollPane = new JScrollPane(estacionamentoPanel);
        vagasScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        vagasScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
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

        // --- Métodos de Controle da Simulação ---
        btnIniciar.setEnabled(true);
        btnPararExecucao.setEnabled(false);

        // --- Ações dos Botões ---
        btnIniciar.addActionListener((ActionEvent e) -> {
            if (estacionamento != null) {
                encerrarSimulacao();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
            iniciarSimulacao(e);
        });

        btnPararExecucao.addActionListener(e -> {
            if (estacionamento != null) {
                encerrarSimulacao();
            } else {
                log("Nenhuma simulação em andamento.");
            }
        });

        setVisible(true);
    }

    // --- Iniciar a simulação ---
    private void iniciarSimulacao(ActionEvent e) {
            int numVagasGerais = (Integer) txtVagasGerais.getValue();
            int numVagasIdosos = (Integer) txtVagasIdosos.getValue();
            int numVagasPCD = (Integer) txtVagasPCD.getValue();

            estacionamento = new Estacionamento(numVagasGerais, numVagasIdosos, numVagasPCD);
            log("Simulação iniciada com " + numVagasGerais + " vagas gerais, " +
                    numVagasIdosos + " de idosos e " + numVagasPCD + " de PCD.");

            // NOVO: Inicializar os VagaPanels
            estacionamentoPanel.removeAll(); // Limpa painéis anteriores
            mapaVagaParaPanel.clear(); // Limpa o mapa

            // Adicionar Vagas Gerais
            for (Vagas vaga : estacionamento.getVagasGerais()) {
                VagaPanel vagaPanel = new VagaPanel(vaga);
                mapaVagaParaPanel.put(vaga, vagaPanel);
                estacionamentoPanel.add(vagaPanel);
            }
            // Adicionar Vagas Idosos
            for (Vagas vaga : estacionamento.getVagasIdosos()) {
                VagaPanel vagaPanel = new VagaPanel(vaga);
                mapaVagaParaPanel.put(vaga, vagaPanel);
                estacionamentoPanel.add(vagaPanel);
            }
            // Adicionar Vagas PCD
            for (Vagas vaga : estacionamento.getVagasPCD()) {
                VagaPanel vagaPanel = new VagaPanel(vaga);
                mapaVagaParaPanel.put(vaga, vagaPanel);
                estacionamentoPanel.add(vagaPanel);
            }
            estacionamentoPanel.revalidate(); // Revalida o layout para exibir as novas vagas
            estacionamentoPanel.repaint(); // Repinta

            // Resetar executor (não precisamos de threadsDosCarros agora)
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdownNow();
            }
            executorService = Executors.newCachedThreadPool();
            adicionarNovosCarros(null); // Adiciona carros iniciais

            btnIniciar.setEnabled(false);
            btnPararExecucao.setEnabled(true);

            // Iniciar o monitoramento da UI (agora também atualiza os VagaPanels)
            iniciarMonitoramentoUI();
    }

    private void adicionarNovosCarros(ActionEvent e) {
        if (estacionamento == null) {
            log("Inicie a simulação antes de adicionar carros.");
            return;
        }
        try {
            int carrosGerais = (Integer) txtCarrosGerais.getValue();
            int carrosIdosos = (Integer) txtCarrosIdosos.getValue();
            int carrosPCD = (Integer) txtCarrosPCD.getValue();

            if (carrosGerais < 0 || carrosIdosos < 0 || carrosPCD < 0) {
                JOptionPane.showMessageDialog(this, "Número de carros não pode ser negativo.", "Erro de Entrada", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Random random = new Random();

            for (int i = 0; i < carrosGerais; i++) {
                Carro carro = new Carro("CAR-" + System.nanoTime(), TipoVaga.GERAL, estacionamento);
                executorService.execute(carro);
                Thread.sleep(random.nextInt(200));
            }
            for (int i = 0; i < carrosIdosos; i++) {
                Carro carro = new Carro("IDOSO-" + System.nanoTime(), TipoVaga.IDOSO, estacionamento);
                executorService.execute(carro);
                Thread.sleep(random.nextInt(200));
            }
            for (int i = 0; i < carrosPCD; i++) {
                Carro carro = new Carro("PCD-" + System.nanoTime(), TipoVaga.PCD, estacionamento);
                executorService.execute(carro);
                Thread.sleep(random.nextInt(200));
            }
            log("Adicionados novos carros: " + carrosGerais + " gerais, " + carrosIdosos + " idosos, " + carrosPCD + " PCD.");

        } catch (InterruptedException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao adicionar carros: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            Thread.currentThread().interrupt();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Ocorreu um erro ao adicionar carros: " + ex.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void encerrarSimulacao() {
        if (executorService != null) {
            executorService.shutdownNow();
            try {
                if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                    log("Algumas threads não terminaram a tempo.");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (estacionamento != null) {
            // Se houver carros na fila de espera que desistiram ao encerrar
            synchronized (estacionamento.getFilaDeEspera()) {
                int desistiramAoEncerrar = estacionamento.getFilaDeEspera().size();
                // Assumindo que Estacionamento possui um método para incrementar carros desistiram
                // Ou você pode apenas logar a quantidade aqui
                // for (Carro c : estacionamento.getFilaDeEspera()) { estacionamento.incrementCarrosDesistiram(); }
                estacionamento.getFilaDeEspera().clear();
                if (desistiramAoEncerrar > 0) {
                    log("Total de " + desistiramAoEncerrar + " carros na fila desistiram ao encerrar a simulação.");
                }
            }
        }
        estacionamento = null;

        log("Simulação finalizada.");

        estacionamentoPanel.removeAll();
        mapaVagaParaPanel.clear();
        estacionamentoPanel.revalidate();
        estacionamentoPanel.repaint();

        btnIniciar.setEnabled(true);
        btnPararExecucao.setEnabled(false);

        lblFilaEspera.setText("Carros na Fila de Espera: --");
        filaEsperaArea.setText("");
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> logArea.append(message + "\n"));
    }

    // --- Monitoramento da UI (atualizado para VagaPanels) ---
    private void iniciarMonitoramentoUI() {
        new Thread(() -> {
            while (estacionamento != null && !executorService.isShutdown()) {
                try {
                    SwingUtilities.invokeLater(() -> {
                        if (estacionamento != null) {
                            for (Vagas vaga : estacionamento.getVagasGerais()) {
                                if (mapaVagaParaPanel.containsKey(vaga)) {
                                    mapaVagaParaPanel.get(vaga).atualizarEstado();
                                }
                            }
                            for (Vagas vaga : estacionamento.getVagasIdosos()) {
                                if (mapaVagaParaPanel.containsKey(vaga)) {
                                    mapaVagaParaPanel.get(vaga).atualizarEstado();
                                }
                            }
                            for (Vagas vaga : estacionamento.getVagasPCD()) {
                                if (mapaVagaParaPanel.containsKey(vaga)) {
                                    mapaVagaParaPanel.get(vaga).atualizarEstado();
                                }
                            }

                            synchronized (estacionamento.getFilaDeEspera()) {
                                lblFilaEspera.setText("Carros na Fila de Espera: " + estacionamento.getFilaDeEspera().size());
                                StringBuilder filaDetalhada = new StringBuilder();
                                for (Carro c : estacionamento.getFilaDeEspera()) {
                                    filaDetalhada.append(c.getPlaca()).append(" (").append(c.getTipo().name().charAt(0)).append(")\n");
                                }
                                filaEsperaArea.setText(filaDetalhada.toString());
                            }
                        }
                    });
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    System.out.println("Monitoramento da UI interrompido.");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "MonitoramentoUI-Thread").start();
    }
}