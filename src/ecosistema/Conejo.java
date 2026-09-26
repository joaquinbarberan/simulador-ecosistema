package ecosistema;

//Conejo: animal herbívoro. Come plantas y se reproduce si tiene energía suficiente y hay al menos otro conejo vivo
public class Conejo extends Animal implements Reproducible {

    // Energía que pierde si no encuentra plantas para comer.
    private static final double PENALIDAD_SIN_COMIDA = 15;
    // Energía mínima para reproducirse.
    private static final double UMBRAL_REPRODUCCION = 60;
    // Energía que cuesta reproducirse.
    private static final double COSTO_REPRODUCCION = 30;
    // Umbral de energía por debajo del cual el conejo está en peligro.
    private static final double UMBRAL_PELIGRO = 20;
    // Probabilidad de que un conejo apto efectivamente tenga cría en el turno.
    private static final double PROB_REPRODUCCION = 0.40;

    public Conejo(String nombre, double energia, int velocidad, double peso) {
        super(nombre, energia, velocidad, peso);
    }

    @Override
    public void actuar(Ecosistema eco) {
        comer(eco);
        intentarReproduccion(eco);
    }

    // Busca una planta viva en el ecosistema y la come. Si no encuentra ninguna,
    // pierde energía. Nota: el conejo no puede distinguir una planta normal de una
    // venenosa antes de comerla (polimorfismo en serComida()).
    @Override
    public void comer(Ecosistema eco) {
        if (!estaVivo()) {
            return;
        }
        Planta objetivo = eco.buscarPlantaViva();
        if (objetivo == null) {
            gastarEnergia(PENALIDAD_SIN_COMIDA);
            eco.registrarEvento(getNombre() + " no encontró comida (-"
                    + (int) PENALIDAD_SIN_COMIDA + " energía)");
            return;
        }
        double valor = objetivo.serComida();
        if (valor >= 0) {
            ganarEnergia(valor);
            eco.registrarEvento(getNombre() + " comió '" + objetivo.getNombre()
                    + "' (+" + (int) valor + " energía)");
        } else {
            gastarEnergia(-valor);
            eco.registrarEvento(getNombre() + " se intoxicó con '"
                    + objetivo.getNombre() + "' (" + (int) valor + " energía)");
        }
    }

    @Override
    public void mostrarEstado() {
        String alerta = getEnergia() < UMBRAL_PELIGRO
                ? "  [PELIGRO: energia=" + (int) getEnergia() + "]"
                : "";
        System.out.printf("  Conejo %s | energía: %.0f%s%n",
                getNombre(), getEnergia(), alerta);
    }

    // ---- Reproducible ----------------------------------------------------

    @Override
    public boolean puedeReproducirse(Ecosistema eco) {
        return estaVivo()
                && getEnergia() > UMBRAL_REPRODUCCION
                && eco.contarConejosVivos() >= 2 // necesita al menos otro conejo
                && eco.getRandom().nextDouble() < PROB_REPRODUCCION;
    }

    @Override
    public void reproducirse(Ecosistema eco) {
        gastarEnergia(COSTO_REPRODUCCION);
        double energiaCria = 30 + eco.getRandom().nextInt(21); // 30-50
        int velocidadCria = 3 + eco.getRandom().nextInt(6); // 3-8
        double pesoCria = 1.0 + eco.getRandom().nextDouble() * 2; // 1-3 kg
        Conejo cria = new Conejo(eco.nombrarConejo(), energiaCria, velocidadCria, pesoCria);
        eco.registrarNacimiento(cria);
        eco.registrarEvento(getNombre() + " tuvo una cría -> nuevo conejo '"
                + cria.getNombre() + "' (energía: " + (int) energiaCria + ")");
    }
}
