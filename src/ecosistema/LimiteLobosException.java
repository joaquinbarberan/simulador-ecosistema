package ecosistema;

// Excepcion propia: se lanza al intentar agregar mas de 5 lobos en la simulacion.
public class LimiteLobosException extends Exception {
    public LimiteLobosException(String mensaje) {
        super(mensaje);
    }
}
