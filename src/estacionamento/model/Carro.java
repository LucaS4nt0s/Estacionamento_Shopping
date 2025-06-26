package estacionamento.model;

import estacionamento.util.TipoVaga;
import java.util.Random;

public class Carro implements Runnable {
    private String placa;
    private TipoVaga tipo;
    private Estacionamento estacionamento;
    private Vagas vagaAtual; // A vaga que o carro está ocupando

    public Carro(String placa, TipoVaga tipo, Estacionamento estacionamento) {
        this.placa = placa;
        this.tipo = tipo;
        this.estacionamento = estacionamento;
        this.vagaAtual = null; // Começa sem vaga
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
            System.out.println("Carro " + placa + " (" + tipo + ") iniciando sua jornada.");

            while (!Thread.currentThread().isInterrupted()) { // Loop contínuo para carros entrarem/saírem várias vezes
                // --- Ponto de Verificação de Pausa ---
                // O carro só vai tentar interagir com o estacionamento (entrar, sair) se não estiver pausado
                while (estacionamento.isSimulacaoPausada()) {
                    System.out.println("Carro " + placa + " PAUSADO. Aguardando retomada...");
                    Thread.sleep(500); // Carro dorme enquanto a simulação está pausada
                }
                // --- Fim da Verificação de Pausa ---

                // 1. Tentar entrar no estacionamento
                this.vagaAtual = estacionamento.entrar(this);

                if (this.vagaAtual != null) {
                    System.out.println("Carro " + placa + " estacionou na vaga " + vagaAtual.getId() + " (" + vagaAtual.getTipoVaga() + ").");

                    // 2. Simular tempo estacionado
                    Random random = new Random();
                    long tempoEstacionadoMs = 2000 + random.nextInt(8000); // Entre 2 a 10 segundos
                    System.out.println("Carro " + placa + " ficará estacionado por " + (tempoEstacionadoMs / 1000.0) + " segundos.");

                    // --- Ponto de Verificação de Pausa durante o estacionamento ---
                    long tempoDecorridoEstacionado = 0;
                    long inicioEstacionado = System.currentTimeMillis();
                    while (tempoDecorridoEstacionado < tempoEstacionadoMs) {
                        if (estacionamento.isSimulacaoPausada()) {
                            System.out.println("Carro " + placa + " em vaga " + vagaAtual.getId() + " PAUSADO.");
                            Thread.sleep(500); // Dorme enquanto pausado
                            inicioEstacionado = System.currentTimeMillis() - tempoDecorridoEstacionado; // Ajusta o início para continuar a contagem
                        } else {
                            // Se não pausado, espera por um curto período e atualiza o tempo decorrido
                            long sleepChunk = Math.min(500L, tempoEstacionadoMs - tempoDecorridoEstacionado); // Use L para long
                            Thread.sleep(sleepChunk);
                            tempoDecorridoEstacionado = System.currentTimeMillis() - inicioEstacionado;
                        }
                        if (Thread.currentThread().isInterrupted()) { // Verifica interrupção
                            throw new InterruptedException();
                        }
                    }
                    // --- Fim da Verificação de Pausa ---


                    // 3. Sair do estacionamento
                    estacionamento.sair(this.vagaAtual);
                    System.out.println("Carro " + placa + " saiu da vaga " + vagaAtual.getId() + ".");
                    this.vagaAtual = null; // Carro não está mais na vaga

                    // Opcional: tempo de "descanso" antes de tentar entrar novamente
                    Thread.sleep(1000 + random.nextInt(3000)); // Entre 1 e 4 segundos

                }
            }

        } catch (InterruptedException e) {
            System.out.println("Carro " + placa + " foi interrompido e encerrou sua jornada.");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            System.err.println("Erro inesperado para o carro " + placa + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

}