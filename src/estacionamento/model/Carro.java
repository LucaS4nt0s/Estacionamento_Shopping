package estacionamento.model;

import estacionamento.util.TipoVaga;
import java.util.Random;
import estacionamento.UI.EstacionamentoGUI;

public class Carro implements Runnable {
    private String placa;
    private TipoVaga tipo;
    private Estacionamento estacionamento;
    private Vagas vagaAtual; // A vaga que o carro está ocupando
    private EstacionamentoGUI guiLogger;

    public Carro(String placa, TipoVaga tipo, Estacionamento estacionamento, EstacionamentoGUI guiLogger) {
        this.placa = placa;
        this.tipo = tipo;
        this.estacionamento = estacionamento;
        this.vagaAtual = null; // Começa sem vaga
        this.guiLogger = guiLogger;
    }

    public String getPlaca() {
        return placa;
    }

    public TipoVaga getTipo() {
        return tipo;
    }

    public Vagas getVagaAtual() {
        return vagaAtual;
    }

    @Override
    public void run() {
        try {
            guiLogger.logAllEvents("Carro " + placa + " (" + tipo + ") iniciando sua jornada.");

            while (!Thread.currentThread().isInterrupted()) {
                // 1. Tentar entrar no estacionamento
                this.vagaAtual = estacionamento.entrar(this);

                if (this.vagaAtual != null) {
                    guiLogger.logAllEvents("Carro " + placa + " estacionou na vaga " + vagaAtual.getId() + " (" + vagaAtual.getTipoVaga() + ").");

                    // 2. Simular tempo estacionado
                    Random random = new Random();
                    long tempoEstacionadoMs = 10000 + random.nextInt(20000); // Entre 10 a 20 segundos
                    guiLogger.logAllEvents("Carro " + placa + " ficará estacionado por " + (tempoEstacionadoMs / 1000.0) + " segundos.");

                    // --- Ponto de Verificação de Pausa durante o estacionamento ---
                    long tempoDecorridoEstacionado = 0;
                    long inicioEstacionado = System.currentTimeMillis();
                    while (tempoDecorridoEstacionado < tempoEstacionadoMs) {
                        long sleepChunk = Math.min(500L, tempoEstacionadoMs - tempoDecorridoEstacionado); // Use L para long
                        Thread.sleep(sleepChunk);
                        tempoDecorridoEstacionado = System.currentTimeMillis() - inicioEstacionado;

                        if (Thread.currentThread().isInterrupted()) { // Verifica interrupção
                            throw new InterruptedException();
                        }
                    }

                    // 3. Sair do estacionamento
                    estacionamento.sair(this.vagaAtual);
                    guiLogger.logAllEvents("Carro " + placa + " saiu da vaga " + vagaAtual.getId() + ".");
                    this.vagaAtual = null; // Carro não está mais na vaga

                    // Opcional: tempo de "descanso" antes de tentar entrar novamente
                    Thread.sleep(1000 + random.nextInt(3000)); // Entre 1 e 4 segundos

                }
            }

        } catch (InterruptedException e) {
            guiLogger.logAllEvents("Carro " + placa + " foi interrompido e encerrou sua jornada.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Erro inesperado para o carro " + placa + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

}