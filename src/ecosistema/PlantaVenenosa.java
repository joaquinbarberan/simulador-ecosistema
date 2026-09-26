package ecosistema;

// Planta venenosa (bonus): el conejo no puede distinguirla de una planta normal antes de comerla.
// Se guarda en el mismo ArrayList<Planta> que las plantas comunes (polimorfismo).
public class PlantaVenenosa extends Planta implements Peligroso {

    public PlantaVenenosa(String nombre, double energia, int tamaño) {
        super(nombre, energia, tamaño);
    }

<<<<<<< HEAD
    // En vez de nutrir, el conejo pierde energia (siempre 30, sin importar el tamaño).
=======
    // En vez de nutrir, el conejo pierde energia (siempre 30, sin importar el
    // tamaño).
>>>>>>> 9bf6ec525988e3caec5c6655a89975a9b9360ded
    @Override
    public double serComida() {
        setEnergia(0);
        setViva(false);
        return -30;
    }

    @Override
    public int getNivelPeligro() {
        return 30;
    }
}