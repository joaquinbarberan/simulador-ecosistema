package ecosistema;
public class Lobo extends Animal implements Peligroso {

    // Energía que gana el lobo por cada caza exitosa.
    private static final double ENERGIA_POR_CAZA = 30;

    // Contador de cacerías exitosas.
    private int exitosCaza;

    public Lobo(String nombre, double energia, int velocidad, double peso) {
        super(nombre, energia, velocidad, peso);
        this.exitosCaza = 0;
    }

    //En su turno el lobo intenta cazar un conejo vivo
    @Override
    public void actuar(Ecosistema eco) {
        comer(eco);
    }

    //Selecciona un conejo vivo al azar e intenta cazarlo. Si la caza es exitosa (probabilidad según su energía, más el bonus del clima), lo mata y gana energía.
     
    @Override
    public void comer(Ecosistema eco) {
        if (!estaVivo()) {
            return;
        }
        Conejo presa = eco.buscarConejoVivoAleatorio();
        if (presa == null) {
            eco.registrarEvento(getNombre() + " no encontró conejos para cazar");
            return;
        }
        double probabilidad = calcularProbabilidadCaza(eco);
        if (eco.getRandom().nextDouble() < probabilidad) {
            presa.morir();
            ganarEnergia(ENERGIA_POR_CAZA);
            exitosCaza++;
            eco.registrarEvento(getNombre() + " cazó a '" + presa.getNombre()
                    + "' (+" + (int) ENERGIA_POR_CAZA + " energía) [cacerías: "
                    + exitosCaza + "]");
        } else {
            eco.registrarEvento(getNombre() + " falló la caza de '"
                    + presa.getNombre() + "'");
        }
    }

    //Probabilidad de caza: crece con la energía del lobo (0.30 con energía 0, hasta un tope de 0.90) y le suma el bonus de caza del clima (ej. Invierno +20%). Nunca supera 0.95. @return probabilidad de éxito entre 0 y 0.95
    private double calcularProbabilidadCaza(Ecosistema eco) {
        double base = Math.min(0.90, 0.30 + getEnergia() / 200.0);
        double conBonus = base + eco.getClima().getBonusCazaLobo();
        return Math.min(0.95, conBonus);
    }

    @Override
    public void mostrarEstado() {
        System.out.printf("  Lobo %s | energía: %.0f | cacerías exitosas: %d%n",
                getNombre(), getEnergia(), exitosCaza);
    }

    // ---- Peligroso -------------------------------------------------------
    @Override
    public int getNivelPeligro() {
        return exitosCaza * 5 + (int) (getEnergia() / 10);
    }

    public int getExitosCaza() {
        return exitosCaza;
    }

    public void setExitosCaza(int exitosCaza) {
        // El contador de cacerias nunca puede ser negativo
        if (exitosCaza < 0) exitosCaza = 0;
        this.exitosCaza = exitosCaza;
    }
}
