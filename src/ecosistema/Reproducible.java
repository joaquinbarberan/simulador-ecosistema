package ecosistema;

// La implementan las entidades que pueden reproducirse: Planta y Conejo.
public interface Reproducible {
    void reproducirse(Ecosistema eco);
    boolean puedeReproducirse(Ecosistema eco);

    // Metodo default: si puede reproducirse, lo hace.
    default void intentarReproduccion(Ecosistema eco) {
        if (puedeReproducirse(eco)) {
            reproducirse(eco);
        }
    }
}
