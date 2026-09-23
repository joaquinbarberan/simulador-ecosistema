package ecosistema;

import java.util.Scanner;

// Punto de entrada: configuracion, loop principal, intervencion y reporte final.
public class Main {

    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("===== SIMULADOR DE ECOSISTEMA =====");

        // ---- 1. Configuracion inicial (validada) ----
        int cantPlantas = leerEntero("Cantidad inicial de plantas", 5, 30);
        int cantConejos = leerEntero("Cantidad inicial de conejos", 2, 15);
        int cantLobos = leerEntero("Cantidad inicial de lobos", 1, 5);
        Clima clima = leerClima();
        int turnos = leerEntero("Cantidad de turnos", 10, 50);

        System.out.println();
        System.out.println("Resumen: " + cantPlantas + " plantas, " + cantConejos + " conejos, "
                + cantLobos + " lobos, clima " + clima + ", " + turnos + " turnos.");
        if (!confirmar("Confirmar configuracion?")) {
            System.out.println("Configuracion cancelada. Fin del programa.");
            return;
        }

        // Se crean las entidades iniciales con energia aleatoria (agregarEntidad sin energia)
        Ecosistema eco = new Ecosistema(clima, turnos);
        try {
            for (int i = 0; i < cantPlantas; i++) eco.agregarEntidad("planta");
            for (int i = 0; i < cantConejos; i++) eco.agregarEntidad("conejo");
            for (int i = 0; i < cantLobos; i++) eco.agregarEntidad("lobo");
        } catch (LimiteLobosException e) {
            System.out.println(e.getMessage());
        }

        eco.mostrarEstado();
        System.out.print(">>> Presione Enter para comenzar...");
        sc.nextLine();

        // ---- 2. Loop principal: sigue mientras queden turnos y no haya colapso ----
        while (eco.getTurnoActual() < turnos && !eco.ecosistemaColapsado()) {
            eco.procesarTurno();

            boolean sigue = eco.getTurnoActual() < turnos && !eco.ecosistemaColapsado();
            if (sigue) {
                System.out.print(">>> Presione Enter para continuar...");
                sc.nextLine();

                // ---- 3. Intervencion cada 3 turnos ----
                if (eco.getTurnoActual() % 3 == 0) {
                    intervenir(eco);
                }
            }
        }

        // ---- 4. Reporte final ----
        eco.generarReporteFinal();
    }

    // Menu de intervencion del jugador
    static void intervenir(Ecosistema eco) {
        System.out.println();
        System.out.println("=== INTERVENCION (cada 3 turnos) ===");
        System.out.println("1. Cambiar clima (actual: " + eco.getClima() + ")");
        System.out.println("2. Agregar entidad");
        System.out.println("3. Solo avanzar");
        int opcion = leerEntero("Opcion", 1, 3);

        if (opcion == 1) {
            Clima nuevo = leerClima();
            if (confirmar("Cambiar el clima a " + nuevo + "?")) {
                eco.cambiarClima(nuevo);
            } else {
                System.out.println("Accion cancelada.");
            }
        } else if (opcion == 2) {
            String tipo = leerTipo();
            boolean conEnergia = confirmar("Queres indicar la energia inicial?");
            int energia = 0;
            if (conEnergia) {
                energia = leerEntero("Energia inicial", 10, 100);
            }

            if (confirmar("Agregar " + tipo + "?")) {
                try {
                    String nombre;
                    // SOBRECARGA: se llama a una u otra version de agregarEntidad
                    if (conEnergia) {
                        nombre = eco.agregarEntidad(tipo, energia);
                    } else {
                        nombre = eco.agregarEntidad(tipo);
                    }
                    System.out.println("Se agrego '" + nombre + "' al ecosistema.");
                } catch (LimiteLobosException e) {
                    System.out.println("No se pudo agregar: " + e.getMessage());
                }
            } else {
                System.out.println("Accion cancelada.");
            }
        } else {
            System.out.println("Se avanza sin intervenir.");
        }
    }

    // ---- Lectura de datos por teclado ----

    // Lee un entero entre min y max; si es invalido lo vuelve a pedir
    static int leerEntero(String etiqueta, int min, int max) {
        while (true) {
            System.out.print(etiqueta + " (" + min + "-" + max + "): ");
            String linea = sc.nextLine().trim();
            try {
                int valor = Integer.parseInt(linea);
                if (valor >= min && valor <= max) {
                    return valor;
                }
                System.out.println("  Debe estar entre " + min + " y " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("  Ingrese un numero valido.");
            }
        }
    }

    static Clima leerClima() {
        System.out.println("Clima: 1. Soleado  2. Lluvioso  3. Sequia  4. Invierno");
        int opcion = leerEntero("Elija el clima", 1, 4);
        if (opcion == 1) return Clima.SOLEADO;
        if (opcion == 2) return Clima.LLUVIOSO;
        if (opcion == 3) return Clima.SEQUIA;
        return Clima.INVIERNO;
    }

    // Pide el tipo de entidad hasta que escriba planta, conejo o lobo
    static String leerTipo() {
        while (true) {
            System.out.print("Que entidad agregar? (planta/conejo/lobo): ");
            String tipo = sc.nextLine().trim().toLowerCase();
            if (tipo.equals("planta") || tipo.equals("conejo") || tipo.equals("lobo")) {
                return tipo;
            }
            System.out.println("  Escriba planta, conejo o lobo.");
        }
    }

    static boolean confirmar(String pregunta) {
        System.out.print(pregunta + " (s/n): ");
        return sc.nextLine().trim().equalsIgnoreCase("s");
    }
}
