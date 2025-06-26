package estacionamento.model;
import estacionamento.UI.EstacionamentoGUI;
import estacionamento.util.TipoVaga;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

public class Estacionamento {
    private final Semaphore semaforoGeral;
    private final Semaphore semaforoIdoso;
    private final Semaphore semaforoPCD;

    private final List<Vagas> vagasGerais;
    private final List<Vagas> vagasIdosos;
    private final List<Vagas> vagasPCD;

    private int carrosSairam;
    private int carrosDesistiram;
    private int carrosQueEntraram;

    public Estacionamento(int numVagasGerais, int numVagasIdosos, int numVagasPCD) {
        this.semaforoGeral = new Semaphore(numVagasGerais);
        this.semaforoIdoso = new Semaphore(numVagasIdosos);
        this.semaforoPCD = new Semaphore(numVagasPCD);

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
    }

    public Vagas entrar(Carro carro) throws InterruptedException {
        // Mensagem inicial para rastreamento
        System.out.println("Carro " + carro.getPlaca() + " (" + carro.getTipo() + ") tentando entrar.");

        // 1. Tentar vaga do tipo específico do carro (PCD ou IDOSO)
        if (carro.getTipo() == TipoVaga.PCD) {
            if (semaforoPCD.tryAcquire()) { // Tenta adquirir a permissão sem bloquear indefinidamente
                Vagas vagaEncontrada = encontrarEocuparVagaLivre(vagasPCD, carro);
                if (vagaEncontrada != null) {
                    return vagaEncontrada;
                    carrosQueEntraram++;
                } else {
                    // Deveria encontrar uma vaga se o semáforo deu permissão, mas é bom verificar
                    semaforoPCD.release(); // Libera a permissão se não encontrou vaga física (erro de lógica)
                }
            }
        } else if (carro.getTipo() == TipoVaga.IDOSO) {
            if (semaforoIdoso.tryAcquire()) {
                Vagas vagaEncontrada = encontrarEocuparVagaLivre(vagasIdosos, carro);
                if (vagaEncontrada != null) {
                    return vagaEncontrada;
                    carrosQueEntraram++;
                } else {
                    semaforoIdoso.release();
                }
            }
        }

        // 2. Tentar vaga GERAL (para qualquer carro, incluindo PCD e IDOSO se não acharam suas vagas)
        // Isso é feito após tentar a vaga específica para respeitar a prioridade
        if (semaforoGeral.tryAcquire()) {
            Vagas vagaEncontrada = encontrarEocuparVagaLivre(vagasGerais, carro);
            if (vagaEncontrada != null) {
                return vagaEncontrada;
                carrosQueEntraram++; // Incrementa a contagem de carros que entraram
            } else {
                semaforoGeral.release();
            }
        }

        // Se chegou até aqui, não conseguiu nenhuma vaga.
        System.out.println("Carro " + carro.getPlaca() + " desistiu de estacionar. Nenhuma vaga disponível.");
        carrosDesistiram++;
        return null; // Carro não conseguiu estacionar
    }

    /**
     * Método auxiliar para encontrar e ocupar a primeira vaga livre em uma lista.
     * Deve ser chamado APÓS a aquisição de um semáforo para garantir que há "espaço" disponível.
     */
    private synchronized Vagas encontrarEocuparVagaLivre(List<Vagas> vagas, Carro carro) {
        // Usamos Optional aqui para um tratamento mais elegante de "não encontrei"
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

    // Dentro da classe Estacionamento.java

    // ... (atributos, construtor e método entrar)

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



    public List<Vagas> getVagasIdosos() { return vagasIdosos; }
    public List<Vagas> getVagasPCD() { return vagasPCD; }
    public int getCarrosSairam() { return carrosSairam; }
    public int getCarrosDesistiram() { return carrosDesistiram; }
}
