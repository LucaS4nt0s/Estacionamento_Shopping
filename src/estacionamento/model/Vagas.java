package estacionamento.model;
import estacionamento.util.TipoVaga;

public class Vagas {
    private String id;
    private TipoVaga tipoVaga;
    private boolean ocupada;
    private Carro carroEstacionado;

    public Vagas(String id, TipoVaga tipoVaga) {
        this.id = id;
        this.tipoVaga = tipoVaga;
        this.ocupada = false;
        this.carroEstacionado = null;
    }

    public String getId() {
        return id;
    }

    public TipoVaga getTipoVaga() {
        return tipoVaga;
    }

    public boolean isOcupada() {
        return ocupada;
    }

    public void setOcupada(boolean ocupada) {
        this.ocupada = ocupada;
    }

    public Carro getCarroEstacionado() {
        return carroEstacionado;
    }

    public synchronized void ocupar(Carro carro) {
        if (!this.ocupada) {
            this.ocupada = true;
            this.carroEstacionado = carro;
            System.out.println("Vaga " + id + " (" + tipoVaga + ") ocupada pelo carro " + carro.getPlaca());
        }
    }

    public synchronized void liberar() {
        if (this.ocupada) {
            System.out.println("Vaga " + id + " (" + tipoVaga + ") liberada pelo carro " + carroEstacionado.getPlaca());
            this.ocupada = false;
            this.carroEstacionado = null;
        }
    }

    @Override
    public String toString() {
        return "Vaga{" +
                "id='" + id + '\'' +
                ", tipo=" + tipoVaga +
                ", ocupada=" + ocupada +
                ", carro=" + (carroEstacionado != null ? carroEstacionado.getPlaca() : "Nenhum") +
                '}';
    }
}
