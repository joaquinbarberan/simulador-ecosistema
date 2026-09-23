package ecosistema;

import java.util.Scanner;

/**
 * Punto de entrada del Simulador de Ecosistema.
 *
 * Se encarga de la interacción por consola: configuración inicial validada,
 * loop principal turno a turno, sistema de intervención cada 3 turnos y reporte
 * final. Toda la lógica de la simulación vive en {@link Ecosistema} y las
 * entidades.
 */
public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("==================================================");
        System.out.println("            SIMULADOR DE ECOSISTEMA");
        System.out.println("==================================================");

        Ecosistema eco = configurar(sc);

        System.out.println();
        System.out.println("Configuración confirmada. ¡Comienza la simulación!");
        eco.registrarEstadoInicial();
        eco.mostrarEstado();

        // ---- Loop principal ----
        while (eco.getTurnoActual() < eco.getTurnosTotales() && !eco.ecosistemaColapsado()) {
            esperarEnter(sc);
            eco.procesarTurno();

            // Sistema de intervención: cada 3 turnos, si la simulación sigue
            if (eco.getTurnoActual() % 3 == 0
                    && eco.getTurnoActual() < eco.getTurnosTotales()
                    && !eco.ecosistemaColapsado()) {
                menuIntervencion(sc, eco);
            }
        }

        // ---- Reporte final ----
        eco.generarReporteFinal();
        System.out.println();
        System.out.println("Fin de la simulación. ¡Gracias por jugar!");
    }

    // =====================================================================
    //  CONFIGURACIÓN INICIAL
    // =====================================================================

    private static Ecosistema configurar(Scanner sc) {
        while (true) {
            System.out.println();
            System.out.println("----- CONFIGURACIÓN INICIAL -----");
            int cantPlantas = leerEntero(sc, "Cantidad inicial de plantas", 5, 30);
            int cantConejos = leerEntero(sc, "Cantidad inicial de conejos", 2, 15);
            int cantLobos = leerEntero(sc, "Cantidad inicial de lobos", 1, Ecosistema.MAX_LOBOS_TOTALES);
            Clima climaInicial = leerClima(sc);
            int turnos = leerEntero(sc, "Cantidad de turnos de la simulación", 10, 50);

            System.out.println();
            System.out.println("Resumen de la configuración:");
            System.out.println("  Plantas iniciales: " + cantPlantas);
            System.out.println("  Conejos iniciales: " + cantConejos);
            System.out.println("  Lobos iniciales:   " + cantLobos);
            System.out.println("  Clima inicial:     " + climaInicial);
            System.out.println("  Turnos totales:    " + turnos);

            if (!confirmar(sc, "¿Confirmar esta configuración?")) {
                System.out.println("Configuración descartada. Volvamos a empezar.");
                continue;
            }

            // Crear el ecosistema y sus entidades iniciales (energía aleatoria)
            Ecosistema eco = new Ecosistema(climaInicial, turnos);
            for (int i = 0; i < cantPlantas; i++) {
                eco.agregarEntidad("planta");
            }
            for (int i = 0; i < cantConejos; i++) {
                eco.agregarEntidad("conejo");
            }
            for (int i = 0; i < cantLobos; i++) {
                eco.agregarEntidad("lobo");
            }
            return eco;
        }
    }

    // =====================================================================
    //  SISTEMA DE INTERVENCIÓN (cada 3 turnos)
    // =====================================================================

    private static void menuIntervencion(Scanner sc, Ecosistema eco) {
        System.out.println();
        System.out.println("=== INTERVENCIÓN (cada 3 turnos) ===");
        System.out.println("1. Cambiar clima (actual: " + eco.getClima() + ")");
        System.out.println("2. Agregar entidad");
        System.out.println("3. Solo avanzar turno sin intervenir");
        int opcion = leerEntero(sc, "Opción", 1, 3);

        switch (opcion) {
            case 1:
                Clima nuevo = leerClima(sc);
                if (confirmar(sc, "¿Cambiar el clima a " + nuevo + "?")) {
                    eco.cambiarClima(nuevo);
                } else {
                    System.out.println("Cambio de clima cancelado.");
                }
                break;
            case 2:
                intervenirAgregarEntidad(sc, eco);
                break;
            default:
                System.out.println("Se avanza sin intervenir.");
        }
    }

    private static void intervenirAgregarEntidad(Scanner sc, Ecosistema eco) {
        System.out.println("¿Qué entidad agregar?");
        System.out.println("1. Planta");
        System.out.println("2. Conejo");
        System.out.println("3. Lobo" + (eco.puedeAgregarLobo()
                ? "" : " (NO disponible: se alcanzó el máximo de "
                + Ecosistema.MAX_LOBOS_TOTALES + " lobos)"));
        System.out.println("4. Planta venenosa");
        int tipoOp = leerEntero(sc, "Opción", 1, 4);

        String tipo;
        switch (tipoOp) {
            case 1: tipo = "planta"; break;
            case 2: tipo = "conejo"; break;
            case 3: tipo = "lobo"; break;
            default: tipo = "venenosa";
        }

        if (tipo.equals("lobo") && !eco.puedeAgregarLobo()) {
            System.out.println("No se puede agregar: ya se crearon los "
                    + Ecosistema.MAX_LOBOS_TOTALES + " lobos permitidos en toda la simulación.");
            return;
        }

        if (confirmar(sc, "¿Agregar una entidad de tipo '" + tipo + "'?")) {
            boolean ok = eco.agregarEntidad(tipo);
            if (ok) {
                System.out.println("Se agregó una nueva entidad de tipo '" + tipo + "' al ecosistema.");
            } else {
                System.out.println("No se pudo agregar la entidad (límite de lobos alcanzado).");
            }
        } else {
            System.out.println("Alta de entidad cancelada.");
        }
    }

    // =====================================================================
    //  UTILIDADES DE ENTRADA (Scanner con validación)
    // =====================================================================

    /**
     * Lee un entero dentro del rango [min, max]. Reintenta ante entradas
     * inválidas o fuera de rango.
     */
    private static int leerEntero(Scanner sc, String etiqueta, int min, int max) {
        while (true) {
            System.out.print(etiqueta + " (" + min + " - " + max + "): ");
            if (!sc.hasNextLine()) {
                return min; // fin de la entrada: valor por defecto seguro
            }
            String linea = sc.nextLine().trim();
            try {
                int valor = Integer.parseInt(linea);
                if (valor < min || valor > max) {
                    System.out.println("  -> El valor debe estar entre " + min + " y " + max + ".");
                    continue;
                }
                return valor;
            } catch (NumberFormatException e) {
                System.out.println("  -> Ingrese un número entero válido.");
            }
        }
    }

    private static Clima leerClima(Scanner sc) {
        System.out.println("Clima:");
        System.out.println("  1. Soleado");
        System.out.println("  2. Lluvioso");
        System.out.println("  3. Sequía");
        System.out.println("  4. Invierno");
        int op = leerEntero(sc, "Elija el clima", 1, 4);
        switch (op) {
            case 1: return Clima.SOLEADO;
            case 2: return Clima.LLUVIOSO;
            case 3: return Clima.SEQUIA;
            default: return Clima.INVIERNO;
        }
    }

    private static boolean confirmar(Scanner sc, String pregunta) {
        System.out.print(pregunta + " (s/n): ");
        if (!sc.hasNextLine()) {
            return true;
        }
        String r = sc.nextLine().trim().toLowerCase();
        return r.startsWith("s");
    }

    private static void esperarEnter(Scanner sc) {
        System.out.print(">>> Presione Enter para avanzar al siguiente turno...");
        if (sc.hasNextLine()) {
            sc.nextLine();
        }
    }
}
