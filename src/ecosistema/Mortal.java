package ecosistema;

// La implementan los animales: pueden morir por falta de energia.
public interface Mortal {
    boolean estaVivo();
    void morir();
    double getEnergia();
    String getNombre();

    // Metodo default: si la energia llego a 0, la entidad muere.
    // Devuelve true si la entidad murio en este llamado (para que Ecosistema pueda contarlo como evento).
    default boolean verificarMuerte() {
        if (estaVivo() && getEnergia() <= 0) {
            morir();
            System.out.println("  " + getNombre() + " murio de inanicion");
            return true;
        }
        return false;
    }
}
