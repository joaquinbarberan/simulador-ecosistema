package ecosistema;

// Clase base abstracta de todas las entidades del ecosistema (plantas y animales).
public abstract class Entidad {
    private String nombre;
    private double energia;
    private int edad;
    private boolean viva;

    public Entidad(String nombre, double energia) {
        this.nombre = nombre;
        setEnergia(energia); // valida que no sea negativa
        this.edad = 0;
        this.viva = true;
    }

    // Metodos abstractos: cada subclase los implementa a su manera
    public abstract void actuar(Ecosistema eco);
    public abstract void mostrarEstado();

    // Metodo concreto compartido: envejece y gasta energia base por existir
    public void envejecer() {
        this.edad++;
        setEnergia(this.energia - 5);
    }

    // Getters y setters con validacion
    public String getNombre() { return nombre; }

    public double getEnergia() { return energia; }
    public void setEnergia(double energia) {
        // La energia nunca puede ser negativa: si baja de 0, se lleva a 0
        if (energia < 0) energia = 0;
        this.energia = energia;
    }

    public int getEdad() { return edad; }

    public boolean estaViva() { return viva; }
    public void setViva(boolean viva) { this.viva = viva; }
}
