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
import java.util.Queue;

public class EstacionamentoGUI extends JFrame {

    private Estacionamento estacionamento;
    private ExecutorService executorService;

    // Componentes da UI
    private JSpinner txtVagasGerais, txtVagasIdosos, txtVagasPCD;
    private JSpinner txtCarrosGerais, txtCarrosIdosos, txtCarrosPCD;
    private JButton btnIniciar, btnPararExecucao;
    private JTextArea mainEventsLogArea;
    private JTextArea allEventsLogArea;

    private JLabel lblFilaGeral, lblFilaIdoso, lblFilaPCD;
    private JTextArea filaGeralArea, filaIdosoArea, filaPCDArea;

    private JLabel lblCarrosDesistiram;
    private JLabel lblCarrosQueEntraram;
    private JLabel lblCarrosSairam;

    // NOVO: Painel para a visualização das vagas
    private JPanel estacionamentoPanel;
    // Mapeamento de Vaga do modelo para seu VagaPanel visual
    private Map<Vagas, VagaPanel> vagaPanels;

    private volatile boolean simulacaoAtiva = false; // Flag para controlar o ciclo de vida das threads

    // --- Construtor do PainelControle ---
    public EstacionamentoGUI() {
        super("Controle de Estacionamento Inteligente");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null); // Centraliza a janela na tela
        setResizable(true); // Permite redimensionar a janela

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
        JPanel logContainerPanel = new JPanel(new GridLayout(2, 1, 5, 5)); // Layout para 2 logs
        logContainerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        mainEventsLogArea = new JTextArea(10, 30); // Eventos principais
        mainEventsLogArea.setEditable(false);
        JScrollPane mainScrollPane = new JScrollPane(mainEventsLogArea);
        mainScrollPane.setBorder(BorderFactory.createTitledBorder("Eventos Principais"));
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        logContainerPanel.add(mainScrollPane);

        allEventsLogArea = new JTextArea(10, 30); // Todos os eventos
        allEventsLogArea.setEditable(false);
        JScrollPane allScrollPane = new JScrollPane(allEventsLogArea);
        allScrollPane.setBorder(BorderFactory.createTitledBorder("Todos os Eventos"));
        allScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        allScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        logContainerPanel.add(allScrollPane);

        add(logContainerPanel, BorderLayout.WEST);

        // --- Painel de Visualização do Estacionamento (central) ---
        estacionamentoPanel = new JPanel();
        estacionamentoPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 10, 10));
        estacionamentoPanel.setBorder(BorderFactory.createTitledBorder("Visualização das Vagas"));
        JScrollPane vagasScrollPane = new JScrollPane(estacionamentoPanel);
        vagasScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        vagasScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        add(vagasScrollPane, BorderLayout.CENTER);

        // --- Painel de Status (lado direito) ---
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS)); // Para empilhar labels verticalmente
        statusPanel.setBorder(BorderFactory.createTitledBorder("Status Geral"));
        statusPanel.add(Box.createVerticalStrut(5)); // Espaçamento

        JPanel filasPanel = new JPanel(new GridLayout(3, 2)); // 3 filas, Label + TextArea
        filasPanel.setBorder(BorderFactory.createTitledBorder("Filas de Espera"));

        lblFilaGeral = new JLabel("Fila Geral: 0");
        filaGeralArea = new JTextArea(3, 20); // Menor, pois agora são 3
        filaGeralArea.setEditable(false);
        filasPanel.add(lblFilaGeral);
        filasPanel.add(new JScrollPane(filaGeralArea));

        lblFilaIdoso = new JLabel("Fila Idoso: 0");
        filaIdosoArea = new JTextArea(3, 20);
        filaIdosoArea.setEditable(false);
        filasPanel.add(lblFilaIdoso);
        filasPanel.add(new JScrollPane(filaIdosoArea));

        lblFilaPCD = new JLabel("Fila PCD: 0");
        filaPCDArea = new JTextArea(3, 20);
        filaPCDArea.setEditable(false);
        filasPanel.add(lblFilaPCD);
        filasPanel.add(new JScrollPane(filaPCDArea));
        statusPanel.add(filasPanel);

        lblCarrosDesistiram = new JLabel("Carros que Desistiram: --", SwingConstants.CENTER); // Novo Label
        lblCarrosDesistiram.setAlignmentX(Component.LEFT_ALIGNMENT); // Centraliza o texto
        lblCarrosDesistiram.setFont(new Font("Arial", Font.BOLD, 14));
        statusPanel.add(lblCarrosDesistiram);
        statusPanel.add(Box.createVerticalStrut(10)); // Mais espaçamento

        lblCarrosQueEntraram = new JLabel("Carros que Entraram: --", SwingConstants.CENTER); // Novo Label
        lblCarrosQueEntraram.setAlignmentX(Component.LEFT_ALIGNMENT); // Centraliza o texto
        lblCarrosQueEntraram.setFont(new Font("Arial", Font.BOLD, 14));
        statusPanel.add(lblCarrosQueEntraram);
        statusPanel.add(Box.createVerticalStrut(10)); // Mais espaçamento

        lblCarrosSairam = new JLabel("Carros que Sairam: --", SwingConstants.CENTER); // Novo Label
        lblCarrosSairam.setAlignmentX(Component.LEFT_ALIGNMENT); // Centraliza o texto
        lblCarrosSairam.setFont(new Font("Arial", Font.BOLD, 14));
        statusPanel.add(lblCarrosSairam);
        statusPanel.add(Box.createVerticalStrut(10)); // Mais espaçamento

        add(statusPanel, BorderLayout.EAST);

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

    // --- Métodos de Log (PÚBLICOS para serem chamados por Estacionamento e Carro) ---
    public void logMainEvent(String message) {
        SwingUtilities.invokeLater(() -> {
            mainEventsLogArea.append(message + "\n");
            mainEventsLogArea.setCaretPosition(mainEventsLogArea.getDocument().getLength()); // Rola para o final
        });
    }

    public void logAllEvents(String message) {
        SwingUtilities.invokeLater(() -> {
            allEventsLogArea.append(message + "\n");
            allEventsLogArea.setCaretPosition(allEventsLogArea.getDocument().getLength()); // Rola para o final
        });
    }

    // --- Iniciar a simulação ---
    private void iniciarSimulacao(ActionEvent e) {

        if (simulacaoAtiva) {
            JOptionPane.showMessageDialog(this, "A simulação já está em andamento.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int numVagasGerais = (Integer) txtVagasGerais.getValue();
        int numVagasIdosos = (Integer) txtVagasIdosos.getValue();
        int numVagasPCD = (Integer) txtVagasPCD.getValue();

        estacionamento = new Estacionamento(numVagasGerais, numVagasIdosos, numVagasPCD, this);
        logMainEvent("Simulação iniciada com " + numVagasGerais + " vagas gerais, " +
                numVagasIdosos + " de idosos e " + numVagasPCD + " de PCD.");
        logAllEvents("Simulação iniciada com " + numVagasGerais + " vagas gerais, " +
                numVagasIdosos + " de idosos e " + numVagasPCD + " de PCD.");
        vagaPanels = new HashMap<>(); // Inicializa o mapa de VagaPanels

        // Configura o painel de visualização das vagas
        estacionamentoPanel.setLayout(new WrapLayout(FlowLayout.LEFT, 10, 10));

        // Adiciona VagaPanels para vagas gerais
        for (Vagas vaga : estacionamento.getVagasGerais()) {
            VagaPanel panel = new VagaPanel(vaga);
            vagaPanels.put(vaga, panel);
            estacionamentoPanel.add(panel);
        }
        // Adiciona VagaPanels para vagas idosos
        for (Vagas vaga : estacionamento.getVagasIdosos()) {
            VagaPanel panel = new VagaPanel(vaga);
            vagaPanels.put(vaga, panel);
            estacionamentoPanel.add(panel);
        }
        // Adiciona VagaPanels para vagas PCD
        for (Vagas vaga : estacionamento.getVagasPCD()) {
            VagaPanel panel = new VagaPanel(vaga);
            vagaPanels.put(vaga, panel);
            estacionamentoPanel.add(panel);
        }
        estacionamentoPanel.revalidate();
        estacionamentoPanel.repaint();

        simulacaoAtiva = true;
        btnIniciar.setEnabled(false);
        btnPararExecucao.setEnabled(true);

        // Iniciar monitoramento da UI (em nova Thread)
        iniciarMonitoramentoUI();

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
                Carro carro = new Carro("CARRO-" + (i + 1), TipoVaga.GERAL, estacionamento, this);
                executorService.execute(carro);
                Thread.sleep(random.nextInt(200));
            }
            for (int i = 0; i < carrosIdosos; i++) {
                Carro carro = new Carro("IDOSO-" + (i + 1), TipoVaga.IDOSO, estacionamento, this);
                executorService.execute(carro);
                Thread.sleep(random.nextInt(200));
            }
            for (int i = 0; i < carrosPCD; i++) {
                Carro carro = new Carro("PCD-" + (i + 1), TipoVaga.PCD, estacionamento, this);
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
        simulacaoAtiva = false; // Define a flag como false primeiro
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
            synchronized (estacionamento.getFilaIdoso()) {
                int desistiramAoEncerrar = estacionamento.getFilaIdoso().size();
                // Assumindo que Estacionamento possui um método para incrementar carros desistiram
                // Ou você pode apenas logar a quantidade aqui
                // for (Carro c : estacionamento.getFilaDeEspera()) { estacionamento.incrementCarrosDesistiram(); }
                estacionamento.getFilaIdoso().clear();
                if (desistiramAoEncerrar > 0) {
                    log("Total de " + desistiramAoEncerrar + " carros Idosos na fila desistiram ao encerrar a simulação.");
                }
            }

            synchronized (estacionamento.getFilaPCD()) {
                int desistiramAoEncerrar = estacionamento.getFilaPCD().size();
                estacionamento.getFilaPCD().clear();
                if (desistiramAoEncerrar > 0) {
                    log("Total de " + desistiramAoEncerrar + " carros PCD na fila desistiram ao encerrar a simulação.");
                }
            }

            synchronized (estacionamento.getFilaGeral()) {
                int desistiramAoEncerrar = estacionamento.getFilaGeral().size();
                estacionamento.getFilaGeral().clear();
                if (desistiramAoEncerrar > 0) {
                    log("Total de " + desistiramAoEncerrar + " carros Gerais na fila desistiram ao encerrar a simulação.");
                }
            }

        }
        estacionamento = null;

        log("Simulação finalizada.");

        estacionamentoPanel.removeAll();
        vagaPanels.clear();
        estacionamentoPanel.revalidate();
        estacionamentoPanel.repaint();

        btnIniciar.setEnabled(true);
        btnPararExecucao.setEnabled(false);

        lblFilaGeral.setText("Fila Geral: --");
        filaGeralArea.setText("");
        lblFilaIdoso.setText("Fila Idoso: --"); // Redefine os rótulos de fila específicos
        filaIdosoArea.setText("");
        lblFilaPCD.setText("Fila PCD: --");
        filaPCDArea.setText("");

    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> mainEventsLogArea.append(message + "\n"));
    }

    // --- Monitoramento da UI (atualizado para VagaPanels) ---
    private void iniciarMonitoramentoUI() {
        new Thread(() -> {
            while (estacionamento != null && !executorService.isShutdown()) {
                try {
                    SwingUtilities.invokeLater(() -> {
                        if (estacionamento != null) {
                            for (Vagas vaga : estacionamento.getVagasGerais()) {
                                if (vagaPanels.containsKey(vaga)) {
                                    vagaPanels.get(vaga).atualizarEstado();
                                }
                            }
                            for (Vagas vaga : estacionamento.getVagasIdosos()) {
                                if (vagaPanels.containsKey(vaga)) {
                                    vagaPanels.get(vaga).atualizarEstado();
                                }
                            }
                            for (Vagas vaga : estacionamento.getVagasPCD()) {
                                if (vagaPanels.containsKey(vaga)) {
                                    vagaPanels.get(vaga).atualizarEstado();
                                }
                            }

                            synchronized (estacionamento.getFilaIdoso()) {
                                lblFilaIdoso.setText("Carros na Fila de Espera: " + estacionamento.getFilaIdoso().size());
                                StringBuilder filaDetalhada = new StringBuilder();
                                for (Carro c : estacionamento.getFilaIdoso()) {
                                    filaDetalhada.append(c.getPlaca()).append(" (").append(c.getTipo().name().charAt(0)).append(")\n");
                                }
                                filaIdosoArea.setText(filaDetalhada.toString());
                            }
                            synchronized (estacionamento.getFilaPCD()) {
                                lblFilaPCD.setText("Carros na Fila de Espera: " + estacionamento.getFilaPCD().size());
                                StringBuilder filaDetalhada = new StringBuilder();
                                for (Carro c : estacionamento.getFilaPCD()) {
                                    filaDetalhada.append(c.getPlaca()).append(" (").append(c.getTipo().name().charAt(0)).append(")\n");
                                }
                                filaPCDArea.setText(filaDetalhada.toString());
                            }
                            synchronized (estacionamento.getFilaGeral()) {
                                lblFilaGeral.setText("Carros na Fila de Espera: " + estacionamento.getFilaGeral().size());
                                StringBuilder filaDetalhada = new StringBuilder();
                                for (Carro c : estacionamento.getFilaGeral()) {
                                    filaDetalhada.append(c.getPlaca()).append(" (").append(c.getTipo().name().charAt(0)).append(")\n");
                                }
                                filaGeralArea.setText(filaDetalhada.toString());
                            }

                            lblCarrosDesistiram.setText("Carros que Desistiram: " + estacionamento.getCarrosDesistiram());
                            lblCarrosQueEntraram.setText("Carros que Entraram: " + estacionamento.getCarrosQueEntraram());
                            lblCarrosSairam.setText("Carros que Sairam: " + estacionamento.getCarrosSairam());
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