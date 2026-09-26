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
    public void setNombre(String nombre) {
        // El nombre nunca puede quedar vacio: si lo mandan vacio, se conserva el anterior
        if (nombre != null && !nombre.isBlank()) {
            this.nombre = nombre;
        }
    }

    public double getEnergia() { return energia; }
    public void setEnergia(double energia) {
        // La energia siempre queda entre 0 y 100
        if (energia < 0) energia = 0;
        if (energia > 100) energia = 100;
        this.energia = energia;
    }

    // Resta energia (usa setEnergia, que ya valida el rango 0-100)
    public void gastarEnergia(double cantidad) {
        setEnergia(this.energia - cantidad);
    }

    // Suma energia (usa setEnergia, que ya valida el rango 0-100)
    public void ganarEnergia(double cantidad) {
        setEnergia(this.energia + cantidad);
    }

    public int getEdad() { return edad; }
    public void setEdad(int edad) {
        // La edad nunca puede ser negativa
        if (edad < 0) edad = 0;
        this.edad = edad;
    }

    public boolean estaViva() { return viva; }
    public void setViva(boolean viva) { this.viva = viva; }
}
