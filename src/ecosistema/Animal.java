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
    public void setVelocidad(int velocidad) {
        // La velocidad no puede ser negativa
        if (velocidad < 0) velocidad = 0;
        this.velocidad = velocidad;
    }

    public double getPeso() { return peso; }
    public void setPeso(double peso) {
        // El peso no puede ser negativo ni cero
        if (peso <= 0) peso = 0.1;
        this.peso = peso;
    }
}
