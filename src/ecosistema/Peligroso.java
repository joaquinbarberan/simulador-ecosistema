package ecosistema;

// Interface para las entidades peligrosas del ecosistema (bonus).
// La implementan Lobo y PlantaVenenosa.
public interface Peligroso {
    String getNombre();
    int getNivelPeligro();
}