package ecosistema;

// Capa intermedia: cualquier animal del ecosistema. Puede morir (Mortal).
public abstract class Animal extends Entidad implements Mortal {
    private int velocidad;
    private double peso;

    public Animal(String nombre, double energia, int velocidad, double peso) {
        super(nombre, energia);
        this.velocidad = velocidad;
        this.peso = peso;
    }

    // Como se alimenta cada animal (lo define cada subclase)
    public abstract void comer(Ecosistema eco);

    // Metodo concreto compartido
    public void moverse() {
        System.out.println("  " + getNombre() + " se desplazo buscando alimento");
    }

    @Override
    public boolean estaVivo() { return estaViva(); }

    @Override
    public void morir() {
        setViva(false);
        setEnergia(0);
    }

    public int getVelocidad() { return velocidad; }
    public double getPeso() { return peso; }
}
