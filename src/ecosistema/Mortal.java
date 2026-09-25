package ecosistema;

// La implementan los animales: pueden morir por falta de energia.
public interface Mortal {
    boolean estaVivo();
    void morir();
    double getEnergia();
    String getNombre();

    // Metodo default: si la energia llego a 0, la entidad muere.
    default void verificarMuerte() {
        if (estaVivo() && getEnergia() <= 0) {
            morir();
            System.out.println("  " + getNombre() + " murio de inanicion");
        }
    }
}
