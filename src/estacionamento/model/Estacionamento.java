package estacionamento.model;

import estacionamento.UI.EstacionamentoGUI;
import estacionamento.util.TipoVaga;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;


public class Estacionamento {
    private final Semaphore semaforoGeral;
    private final Semaphore semaforoIdoso;
    private final Semaphore semaforoPCD;

    private final List<Vagas> vagasGerais;
    private final List<Vagas> vagasIdosos;
    private final List<Vagas> vagasPCD;
    private final Queue<Carro> filaDeEspera;

    private int carrosSairam;
    private int carrosDesistiram;
    private int carrosQueEntraram; // Carros que realmente entraram no estacionamento

    private EstacionamentoGUI guiLogger;

    public Estacionamento(int numVagasGerais, int numVagasIdosos, int numVagasPCD, EstacionamentoGUI guiLogger) {
        this.semaforoGeral = new Semaphore(numVagasGerais);
        this.semaforoIdoso = new Semaphore(numVagasIdosos);
        this.semaforoPCD = new Semaphore(numVagasPCD);
        this.filaDeEspera = new LinkedList<>();

        this.vagasGerais = new ArrayList<>();
        for (int i = 1; i <= numVagasGerais; i++) {
            vagasGerais.add(new Vagas("G" + i, TipoVaga.GERAL));
        }

        this.vagasIdosos = new ArrayList<>();
        for (int i = 1; i <= numVagasIdosos; i++) {
            vagasIdosos.add(new Vagas("I" + i, TipoVaga.IDOSO));
        }

        this.vagasPCD = new ArrayList<>();
        for (int i = 1; i <= numVagasPCD; i++) {
            vagasPCD.add(new Vagas("P" + i, TipoVaga.PCD));
        }

        this.carrosSairam = 0;
        this.carrosDesistiram = 0;
        this.carrosQueEntraram = 0;
        this.guiLogger = guiLogger;
    }

    public Vagas entrar(Carro carro) throws InterruptedException {
        System.out.println("Carro " + carro.getPlaca() + " (" + carro.getTipo() + ") tentando entrar.");

        Vagas vagaEncontrada = null;
        boolean adquiriuPermissao = false;

        // Tempo máximo de espera total para um carro (10 segundos)
        long tempoLimiteEsperaMs = 10 * 1000; // 10 segundos em milissegundos
        long inicioTentativa = System.currentTimeMillis();

        while (System.currentTimeMillis() - inicioTentativa < tempoLimiteEsperaMs && vagaEncontrada == null) {
            adquiriuPermissao = false; // Resetar para cada tentativa

            // 1. Tentar vaga do tipo específico do carro (PCD ou IDOSO) com timeout
            if (carro.getTipo() == TipoVaga.PCD) {
                if (semaforoPCD.tryAcquire(100, TimeUnit.MILLISECONDS)) { // Tenta por um curto período
                    adquiriuPermissao = true;
                    // Procura e ocupa a vaga se conseguiu a permissão
                    synchronized (this) { // Sincroniza o acesso à lista de vagas
                        vagaEncontrada = encontrarEocuparVagaLivre(vagasPCD, carro);
                    }
                    if (vagaEncontrada == null) { // Se não encontrou vaga física (erro de lógica ou corrida), libera a permissão
                        semaforoPCD.release();
                        adquiriuPermissao = false;
                    }
                }
            } else if (carro.getTipo() == TipoVaga.IDOSO) {
                if (semaforoIdoso.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                    adquiriuPermissao = true;
                    synchronized (this) {
                        vagaEncontrada = encontrarEocuparVagaLivre(vagasIdosos, carro);
                    }
                    if (vagaEncontrada == null) {
                        semaforoIdoso.release();
                        adquiriuPermissao = false;
                    }
                }
            }

            // 2. Se não conseguiu vaga específica, tentar vaga GERAL com timeout
            if (vagaEncontrada == null) { // Apenas tenta geral se ainda não encontrou
                if (semaforoGeral.tryAcquire(100, TimeUnit.MILLISECONDS)) {
                    adquiriuPermissao = true;
                    synchronized (this) {
                        vagaEncontrada = encontrarEocuparVagaLivre(vagasGerais, carro);
                    }
                    if (vagaEncontrada == null) {
                        semaforoGeral.release();
                        adquiriuPermissao = false;
                    }
                }
            }

            // Se ainda não encontrou vaga, adiciona à fila de espera (se já não estiver)
            // E faz o carro esperar um pouco antes de tentar de novo
            if (vagaEncontrada == null) {
                synchronized (filaDeEspera) {
                    if (!filaDeEspera.contains(carro)) {
                        filaDeEspera.add(carro);
                        System.out.println("Carro " + carro.getPlaca() + " entrou na fila de espera.");
                    }
                }
                // O carro espera um pouco antes da próxima tentativa no loop
                Thread.sleep(200); // Espera 200ms antes de tentar novamente (ajustável)
            }
        }

        // Fora do loop: verifica o resultado
        if (vagaEncontrada != null) {
            // Remove da fila de espera se estava nela
            synchronized (filaDeEspera) {
                filaDeEspera.remove(carro);
            }
            carrosQueEntraram++; // Carro realmente entrou
            return vagaEncontrada; // Carro conseguiu estacionar
        } else {
            System.out.println("Carro " + carro.getPlaca() + " desistiu de estacionar após " +
                    (System.currentTimeMillis() - inicioTentativa) / 1000.0 + " segundos.");
            carrosDesistiram++;
            synchronized (filaDeEspera) {
                filaDeEspera.remove(carro); // Remove da fila se desistiu
            }
            return null; // Carro não conseguiu estacionar
        }
    }
    /**
     * Método auxiliar para encontrar e ocupar a primeira vaga livre em uma lista.
     * Deve ser chamado APÓS a aquisição de um semáforo para garantir que há "espaço" disponível.
     */
    private synchronized Vagas encontrarEocuparVagaLivre(List<Vagas> vagas, Carro carro) {
        // Usamos Optional aqui para um tratamento mais elegante de "não encontrei" (Não lança Null Pointer Exception)
        Optional<Vagas> vagaLivre = vagas.stream()
                .filter(vaga -> !vaga.isOcupada())
                .findFirst();

        if (vagaLivre.isPresent()) {
            Vagas vaga = vagaLivre.get();
            vaga.ocupar(carro); // Método da classe Vaga que marca como ocupada
            return vaga;
        }
        return null; // Não deveria acontecer se o semáforo foi adquirido corretamente
    }

    public void sair(Vagas vaga) {
        if (vaga == null) {
            System.err.println("Erro: Tentando liberar uma vaga nula.");
            return;
        }

        // Libera a vaga física
        vaga.liberar(); // Chama o método da classe Vaga para desocupar

        // Libera a permissão no semáforo correspondente
        if (vagasPCD.contains(vaga)) { // Verifica se a vaga liberada é PCD
            semaforoPCD.release();
        } else if (vagasIdosos.contains(vaga)) { // Verifica se a vaga liberada é IDOSO
            semaforoIdoso.release();
        } else if (vagasGerais.contains(vaga)) { // Verifica se a vaga liberada é GERAL
            semaforoGeral.release();
        } else {
            System.err.println("Erro: Vaga " + vaga.getId() + " não encontrada em nenhuma lista de vagas.");
        }
        carrosSairam++; // Incrementa a estatística
    }


    public List<Vagas> getVagasGerais() { return vagasGerais; }
    public List<Vagas> getVagasIdosos() { return vagasIdosos; }
    public List<Vagas> getVagasPCD() { return vagasPCD; }
    public int getCarrosSairam() { return carrosSairam; }
    public int getCarrosDesistiram() { return carrosDesistiram; }
    public int getCarrosQueEntraram() { return carrosQueEntraram; }

    public Queue<Carro> getFilaDeEspera() {
        return filaDeEspera;
    }

    // Adicionar os getters para os semáforos
    public Semaphore getSemaforoGeral() { return semaforoGeral; }
    public Semaphore getSemaforoIdoso() { return semaforoIdoso; }
    public Semaphore getSemaforoPCD() { return semaforoPCD; }
}