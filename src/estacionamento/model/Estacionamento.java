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
import java.util.concurrent.atomic.AtomicInteger;


public class Estacionamento {
    private final Semaphore semaforoGeral;
    private final Semaphore semaforoIdoso;
    private final Semaphore semaforoPCD;

    private final List<Vagas> vagasGerais;
    private final List<Vagas> vagasIdosos;
    private final List<Vagas> vagasPCD;

    // Múltiplas filas de espera, uma para cada tipo de carro para garantir que um carro geral não interfira na fila de idosos ou PCD.
    private final Queue<Carro> filaGeral;
    private final Queue<Carro> filaIdoso;
    private final Queue<Carro> filaPCD;

    private AtomicInteger carrosSairam;
    private AtomicInteger carrosDesistiram;
    private AtomicInteger carrosQueEntraram; // Carros que realmente entraram no estacionamento

    private EstacionamentoGUI guiLogger;

    public Estacionamento(int numVagasGerais, int numVagasIdosos, int numVagasPCD, EstacionamentoGUI guiLogger) {
        this.semaforoGeral = new Semaphore(numVagasGerais);
        this.semaforoIdoso = new Semaphore(numVagasIdosos);
        this.semaforoPCD = new Semaphore(numVagasPCD);
        this.filaGeral = new LinkedList<>();
        this.filaIdoso = new LinkedList<>();
        this.filaPCD = new LinkedList<>();

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

        this.carrosQueEntraram = new AtomicInteger(0);
        this.carrosSairam = new AtomicInteger(0);
        this.carrosDesistiram = new AtomicInteger(0);
        this.guiLogger = guiLogger;
    }

    public Vagas entrar(Carro carro) throws InterruptedException {
        guiLogger.logAllEvents("Carro " + carro.getPlaca() + " (" + carro.getTipo() + ") tentando entrar.");

        Vagas vagaEncontrada = null;
        long tempoLimiteEsperaMs = 60 * 1000; // 1 minuto em milissegundos
        long inicioTentativa = System.currentTimeMillis();

        Queue<Carro> minhaFila;
        Semaphore meuSemaforoEspecifico = null;
        List<Vagas> minhasVagasEspecificas = null;

        // Atribui o carro à fila e semáforo corretos com base no tipo de vaga
        switch (carro.getTipo()) {
            case PCD:
                minhaFila = filaPCD;
                meuSemaforoEspecifico = semaforoPCD;
                minhasVagasEspecificas = vagasPCD;
                break;
            case IDOSO:
                minhaFila = filaIdoso;
                meuSemaforoEspecifico = semaforoIdoso;
                minhasVagasEspecificas = vagasIdosos;
                break;
            case GERAL:
            default:
                minhaFila = filaGeral;
                meuSemaforoEspecifico = semaforoGeral;
                minhasVagasEspecificas = vagasGerais;
                break;
        }

        // Sincroniza o acesso à fila de espera para evitar problemas de concorrência
        synchronized (minhaFila) {
            // Adiciona o carro à sua fila específica se ainda não estiver lá
            if (!minhaFila.contains(carro)) {
                minhaFila.add(carro);
                guiLogger.logAllEvents("Carro " + carro.getPlaca() + " entrou na fila de espera " + carro.getTipo() + ".");
            }

            // Loop para tentar adquirir uma vaga ou desistir
            while (vagaEncontrada == null) {
                long tempoDecorrido = System.currentTimeMillis() - inicioTentativa;
                long tempoRestante = tempoLimiteEsperaMs - tempoDecorrido;

                // Se o tempo limite de espera foi atingido, o carro desiste.
                if (tempoRestante <= 0) {
                    break; // Sai do loop se o tempo limite foi atingido, irá desistir em um if/else no fim da função
                }

                // Se o carro não é o primeiro da fila, ele deve esperar sua vez.
                // Isso garante que o carro na frente da fila tem prioridade para tentar.
                if (minhaFila.peek() != carro) { // se o carro não é o primeiro da fila entra no wait
                    try {
                        // O carro "dorme" e espera ser notificado ou por um pequeno timeout
                        // para reavaliar se é a sua vez ou se o tempo limite foi atingido.
                        minhaFila.wait(tempoRestante); // Espera sua vez ou até o tempo limite
                        continue; // Volta para o início do while loop para reavaliar
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }

                // *** Se o código chegou aqui, significa que minhaFila.peek() == carro ***
                // É a vez deste carro tentar adquirir uma vaga.
                boolean adquiriuPermissao = false;

                // 1. Tentar vaga do tipo específico (PCD ou IDOSO)
                if (carro.getTipo() == TipoVaga.PCD || carro.getTipo() == TipoVaga.IDOSO) {
                    if (meuSemaforoEspecifico.tryAcquire()) { // Tenta adquirir sem timeout aqui, o wait/notify gerencia a espera
                        adquiriuPermissao = true;
                        synchronized (this) { // Sincroniza o acesso à lista de vagas físicas
                            vagaEncontrada = encontrarEocuparVagaLivre(minhasVagasEspecificas, carro);
                        }
                        if (vagaEncontrada == null) { // Permissão adquirida, mas vaga física não encontrada (erro de lógica ou corrida)
                            meuSemaforoEspecifico.release(); // Libera a permissão se não conseguiu ocupar a vaga física
                            adquiriuPermissao = false;
                        }
                    }
                }

                // 2. Tentar vaga GERAL
                // Carros GERAIS só tentam vagas gerais.
                // Carros PCD/IDOSO tentam vagas gerais SE não conseguiram as específicas.
                if (vagaEncontrada == null && !adquiriuPermissao) {
                    if (semaforoGeral.tryAcquire()) {
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

                // Se, após todas as tentativas elegíveis, ainda não conseguiu uma vaga
                if (vagaEncontrada == null && !adquiriuPermissao) {
                    try {
                        minhaFila.wait(tempoRestante); // Espera novamente na sua fila
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            } // Fim do while loop de espera

            // Fora do loop: remove o carro da fila, independentemente de ter conseguido vaga ou desistido.
            // Isso é feito dentro do bloco sincronizado para evitar problemas de concorrência na fila.
            minhaFila.remove(carro);
        } // Fim do synchronized (filaDeEspera)

        // Verifica o resultado final após o loop de espera
        if (vagaEncontrada != null) {
            carrosQueEntraram.incrementAndGet(); // Incrementa atomicamente
            return vagaEncontrada; // Carro conseguiu estacionar
        } else {
            guiLogger.logAllEvents("Carro " + carro.getPlaca() + " desistiu de estacionar após " +
                    (System.currentTimeMillis() - inicioTentativa) / 1000.0 + " segundos.");
            carrosDesistiram.incrementAndGet(); // Incrementa atomicamente
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

        Carro carroSaindo = vaga.getCarroEstacionado();
        guiLogger.logAllEvents("Carro " + carroSaindo.getPlaca() + " saindo da vaga " + vaga.getId() + " (" + vaga.getTipoVaga() + ").");

        // Libera a vaga física
        vaga.liberar(); // Chama o método da classe Vaga para desocupar

        // Libera a permissão no semáforo correspondente
        if (vagasPCD.contains(vaga)) {
            semaforoPCD.release();
            // Notifica apenas a fila PCD
            synchronized (filaPCD) {
                filaPCD.notifyAll();
            }
        } else if (vagasIdosos.contains(vaga)) {
            semaforoIdoso.release();
            // Notifica apenas a fila IDOSO
            synchronized (filaIdoso) {
                filaIdoso.notifyAll();
            }
        } else if (vagasGerais.contains(vaga)) {
            semaforoGeral.release();
            // Uma vaga geral liberada pode ser usada por qualquer tipo de carro
            // Então, notifica todas as filas
            synchronized (filaGeral) {
                filaGeral.notifyAll();
            }
            synchronized (filaIdoso) {
                filaIdoso.notifyAll();
            }
            synchronized (filaPCD) {
                filaPCD.notifyAll();
            }
        } else {
            System.err.println("Erro: Vaga " + vaga.getId() + " não encontrada em nenhuma lista de vagas.");
        }
        carrosSairam.incrementAndGet();
    }

    public List<Vagas> getVagasGerais() { return vagasGerais; }
    public List<Vagas> getVagasIdosos() { return vagasIdosos; }
    public List<Vagas> getVagasPCD() { return vagasPCD; }
    public int getCarrosSairam() { return carrosSairam.get(); }
    public int getCarrosDesistiram() { return carrosDesistiram.get(); }
    public int getCarrosQueEntraram() { return carrosQueEntraram.get(); }

    public Queue<Carro> getFilaGeral() { return filaGeral; }
    public Queue<Carro> getFilaIdoso() { return filaIdoso; }
    public Queue<Carro> getFilaPCD() { return filaPCD; }

    // Adicionar os getters para os semáforos
    public Semaphore getSemaforoGeral() { return semaforoGeral; }
    public Semaphore getSemaforoIdoso() { return semaforoIdoso; }
    public Semaphore getSemaforoPCD() { return semaforoPCD; }
}