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
    private final boolean cicloUnico;

    public Carro(String placa, TipoVaga tipo, Estacionamento estacionamento, EstacionamentoGUI guiLogger, boolean cicloUnico) {
        this.placa = placa;
        this.tipo = tipo;
        this.estacionamento = estacionamento;
        this.vagaAtual = null; // Começa sem vaga
        this.guiLogger = guiLogger;
        this.cicloUnico = cicloUnico;
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

            if (cicloUnico) {
                // Executa o ciclo apenas uma vez
                executarCicloDeVida();
            } else {
                // Comportamento original: executa em loop
                while (!Thread.currentThread().isInterrupted()) {
                    executarCicloDeVida();
                    // Opcional: tempo de "descanso" antes de tentar entrar novamente
                    Thread.sleep(1000 + new Random().nextInt(3000)); // Entre 1 e 4 segundos
                }
            }
        } catch (InterruptedException e) {
            guiLogger.logAllEvents("Carro " + placa + " foi interrompido e encerrou sua jornada.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Erro inesperado para o carro " + placa + ": " + e.getMessage());
            e.printStackTrace();
        }
        // Se for ciclo único, a thread terminará naturalmente aqui.
        // Se for loop, só termina com interrupção.
        guiLogger.logAllEvents("Carro " + placa + " finalizou sua thread.");
    }

    private void executarCicloDeVida() throws InterruptedException {
        // 1. Tentar entrar no estacionamento
        this.vagaAtual = estacionamento.entrar(this);

        // Se conseguiu uma vaga, fica um tempo e depois sai.
        if (this.vagaAtual != null) {
            guiLogger.logMainEvent("Carro " + placa + " estacionou na vaga " + vagaAtual.getId() + ".");
            guiLogger.logAllEvents("Carro " + placa + " estacionou na vaga " + vagaAtual.getId() + " (" + vagaAtual.getTipoVaga() + ").");

            // 2. Simular tempo estacionado
            Random random = new Random();
            long tempoEstacionadoMs = 10000 + random.nextInt(20000); // Entre 10 a 20 segundos
            guiLogger.logAllEvents("Carro " + placa + " ficará estacionado por " + (tempoEstacionadoMs / 1000.0) + " segundos.");

            Thread.sleep(tempoEstacionadoMs); // Simples sleep para o ciclo único

            // 3. Sair do estacionamento
            estacionamento.sair(this.vagaAtual);
            guiLogger.logMainEvent("Carro " + placa + " saiu da vaga " + this.vagaAtual.getId() + ".");
            guiLogger.logAllEvents("Carro " + placa + " saiu da vaga " + this.vagaAtual.getId() + ".");
            this.vagaAtual = null; // Carro não está mais na vaga
        }
        // Se não conseguiu vaga (desistiu), o método simplesmente termina e a thread encerra (no modo ciclo único).
    }

}