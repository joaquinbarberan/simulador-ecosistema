package ecosistema;

// Planta: sirve de alimento a los conejos y se reproduce si tiene energia y el clima lo permite.
public class Planta extends Entidad implements Reproducible {
    private int tamaño; // 1 a 5: afecta cuanta energia da al ser comida

    public Planta(String nombre, double energia, int tamaño) {
        super(nombre, energia);
        setTamaño(tamaño);
    }

    @Override
    public void actuar(Ecosistema eco) {
        intentarReproduccion(eco);
    }

    @Override
    public void mostrarEstado() {
        System.out.println("  Planta " + getNombre() + " | tamaño: " + tamaño
                + " | energia: " + (int) getEnergia());
    }

    // La planta es comida: su energia se reduce al minimo y retorna el valor nutritivo (tamaño * 10).
    // PlantaVenenosa sobreescribe este metodo para devolver un valor negativo en vez de nutrir.
    public double serComida() {
        setEnergia(0);
        setViva(false);
        return tamaño * 10;
    }

    @Override
    public boolean puedeReproducirse(Ecosistema eco) {
        if (eco.contarPlantas() >= 60) return false; // no hay mas espacio para plantas
        double factor = eco.getClima().getFactorPlanta();
        if (factor <= 0) return false;            // en Invierno no se reproducen
        double energiaNecesaria = 30 / factor;    // con mejor clima, es mas facil
        return getEnergia() >= energiaNecesaria;
    }

    @Override
    public void reproducirse(Ecosistema eco) {
        setEnergia(getEnergia() - 20);
        Planta cria = new Planta(eco.nuevoNombrePlanta(), 25, tamaño);
        eco.agregarPlanta(cria);
        eco.registrarEvento("Planta " + getNombre() + " se reprodujo -> nueva planta " + cria.getNombre()
                + " (energia: " + (int) cria.getEnergia() + ")");
        eco.sumarNacimientoPlanta();
    }

    public int getTamaño() { return tamaño; }
    public void setTamaño(int tamaño) {
        // El tamaño siempre queda entre 1 y 5
        if (tamaño < 1) tamaño = 1;
        if (tamaño > 5) tamaño = 5;
        this.tamaño = tamaño;
    }
}