package ecosistema;

// Climas posibles del ecosistema y sus efectos. Cada clima guarda los modificadores que aplica cada turno:
 
public enum Clima {

    SOLEADO("Soleado", 1.5, 5, 0, 0.0, 12),
    LLUVIOSO("Lluvioso", 2.0, 3, -5, 0.0, 10),
    SEQUIA("Sequía", 0.5, -5, 0, 0.0, 5),
    INVIERNO("Invierno", 0.0, -8, 0, 0.20, 3);

    private final String etiqueta;
    private final double factorReproduccionPlanta;
    private final int modEnergiaConejo;
    private final int modEnergiaLobo;
    private final double bonusCazaLobo;
    private final int energiaFotosintesis;

    Clima(String etiqueta, double factorReproduccionPlanta, int modEnergiaConejo,
          int modEnergiaLobo, double bonusCazaLobo, int energiaFotosintesis) {
        this.etiqueta = etiqueta;
        this.factorReproduccionPlanta = factorReproduccionPlanta;
        this.modEnergiaConejo = modEnergiaConejo;
        this.modEnergiaLobo = modEnergiaLobo;
        this.bonusCazaLobo = bonusCazaLobo;
        this.energiaFotosintesis = energiaFotosintesis;
    }

    public int getEnergiaFotosintesis() {
        return energiaFotosintesis;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public double getFactorReproduccionPlanta() {
        return factorReproduccionPlanta;
    }

    public int getModEnergiaConejo() {
        return modEnergiaConejo;
    }

    public int getModEnergiaLobo() {
        return modEnergiaLobo;
    }

    public double getBonusCazaLobo() {
        return bonusCazaLobo;
    }

    // @return true si el clima permite que las plantas se reproduzcan
    public boolean permiteReproduccionPlantas() {
        return factorReproduccionPlanta > 0;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
