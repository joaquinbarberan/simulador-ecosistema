package ecosistema;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Núcleo de la simulación. Contiene las poblaciones (plantas, conejos y lobos),
 * el clima actual y el turno en curso. Orquesta el orden de acciones de cada
 * turno, mantiene las estadísticas y genera el reporte final.
 */
public class Ecosistema {

    /** Máximo de lobos que pueden existir en TODA la simulación. */
    public static final int MAX_LOBOS_TOTALES = 5;

    // ---- Poblaciones -----------------------------------------------------
    private final ArrayList<Planta> plantas;
    private final ArrayList<Conejo> conejos;
    private final ArrayList<Lobo> lobos;

    // Buffers para las crías nacidas durante un turno (evita ConcurrentModification)
    private final ArrayList<Planta> plantasNuevas;
    private final ArrayList<Conejo> conejosNuevas;

    // ---- Estado ----------------------------------------------------------
    private Clima climaActual;
    private int turnoActual;
    private final int turnosTotales;
    private final Random rnd;

    // ---- Contadores para nombres -----------------------------------------
    private int contadorPlanta;
    private int contadorConejo;
    private int contadorLobo;
    private int lobosCreadosTotal; // para respetar el máximo de 5

    // ---- Estadísticas ----------------------------------------------------
    private int eventosTurnoActual;
    private int nacimientosPlanta, muertesPlanta;
    private int nacimientosConejo, muertesConejo;
    private int muertesLobo; // los lobos no nacen (no se reproducen)

    // Historial de conteo poblacional turno a turno: {turno, plantas, conejos, lobos}
    private final List<int[]> historialConteos;
    // Actividad por turno: {turno, eventos}
    private final List<int[]> historialActividad;
    // Todos los lobos que existieron (para el "lobo con más cacerías")
    private final List<Lobo> historicoLobos;

    // Máximos y mínimos poblacionales, con el turno en que ocurrieron
    private int maxPlantas, turnoMaxPlantas, minPlantas, turnoMinPlantas;
    private int maxConejos, turnoMaxConejos, minConejos, turnoMinConejos;
    private int maxLobos, turnoMaxLobos, minLobos, turnoMinLobos;
    private boolean extremosInicializados;

    // Turno de mayor actividad
    private int turnoMayorActividad;
    private int maxEventos;

    private static final String[] NOMBRES_CONEJO = {
        "Blas", "Luna", "Rex", "Coco", "Nube", "Trueno", "Pelusa", "Copo",
        "Bruno", "Maya", "Toby", "Dali"
    };
    private static final String[] NOMBRES_LOBO = {
        "Fang", "Sombra", "Colmillo", "Garra", "Aullido", "Zeus"
    };

    /**
     * @param climaInicial  clima con el que arranca la simulación
     * @param turnosTotales cantidad de turnos configurada
     */
    public Ecosistema(Clima climaInicial, int turnosTotales) {
        this.plantas = new ArrayList<>();
        this.conejos = new ArrayList<>();
        this.lobos = new ArrayList<>();
        this.plantasNuevas = new ArrayList<>();
        this.conejosNuevas = new ArrayList<>();
        this.climaActual = climaInicial;
        this.turnosTotales = turnosTotales;
        this.turnoActual = 0;
        this.rnd = new Random();
        this.historialConteos = new ArrayList<>();
        this.historialActividad = new ArrayList<>();
        this.historicoLobos = new ArrayList<>();
    }

    // =====================================================================
    //  ALTA DE ENTIDADES  (incluye la SOBRECARGA de agregarEntidad)
    // =====================================================================

    /**
     * Agrega una nueva entidad del tipo indicado con una energía inicial
     * aleatoria dentro de un rango razonable.
     *
     * @param tipo "planta", "venenosa", "conejo" o "lobo"
     * @return true si se pudo agregar (los lobos pueden rechazarse por el tope)
     */
    public boolean agregarEntidad(String tipo) {
        double energiaAleatoria = 40 + rnd.nextInt(41); // 40-80
        return agregarEntidad(tipo, energiaAleatoria);
    }

    /**
     * SOBRECARGA: agrega una nueva entidad del tipo indicado con una energía
     * inicial específica.
     *
     * @param tipo          "planta", "venenosa", "conejo" o "lobo"
     * @param energiaInicial energía con la que nace la entidad
     * @return true si se pudo agregar
     */
    public boolean agregarEntidad(String tipo, double energiaInicial) {
        if (tipo == null) {
            return false;
        }
        switch (tipo.trim().toLowerCase()) {
            case "planta":
                plantas.add(new Planta(nombrarPlanta(), energiaInicial, 1 + rnd.nextInt(5)));
                return true;
            case "venenosa":
                plantas.add(new PlantaVenenosa(nombrarPlanta(), energiaInicial, 1 + rnd.nextInt(5)));
                return true;
            case "conejo":
                conejos.add(new Conejo(nombrarConejo(), energiaInicial,
                        3 + rnd.nextInt(6), 1.0 + rnd.nextDouble() * 2));
                return true;
            case "lobo":
                if (lobosCreadosTotal >= MAX_LOBOS_TOTALES) {
                    return false; // se alcanzó el máximo de lobos de toda la simulación
                }
                Lobo l = new Lobo(nombrarLobo(), energiaInicial,
                        5 + rnd.nextInt(6), 25.0 + rnd.nextDouble() * 15);
                lobos.add(l);
                historicoLobos.add(l);
                lobosCreadosTotal++;
                return true;
            default:
                return false;
        }
    }

    // =====================================================================
    //  MOTOR DEL TURNO
    // =====================================================================

    /**
     * Ejecuta el orden completo de acciones de un turno:
     * <ol>
     *   <li>Reproducción de plantas y conejos (recorrido polimórfico sobre
     *       {@code ArrayList<Reproducible>}).</li>
     *   <li>Los conejos buscan plantas y comen.</li>
     *   <li>Los lobos intentan cazar.</li>
     *   <li>Todas las entidades envejecen, gastan energía base y aplican el
     *       efecto del clima.</li>
     *   <li>Las entidades sin energía mueren.</li>
     *   <li>Se muestra el estado del ecosistema.</li>
     * </ol>
     */
    public void procesarTurno() {
        turnoActual++;
        eventosTurnoActual = 0;

        System.out.println();
        System.out.println("=== TURNO " + turnoActual + " | Clima: " + climaActual + " ===");
        System.out.printf("Plantas: %d  Conejos: %d  Lobos: %d%n",
                plantas.size(), conejos.size(), lobos.size());
        System.out.println("-- Eventos --");

        // 1. Reproducción: plantas y conejos en un mismo recorrido (polimorfismo)
        List<Reproducible> reproductores = new ArrayList<>();
        reproductores.addAll(plantas);
        reproductores.addAll(conejos);
        for (Reproducible r : reproductores) {
            r.intentarReproduccion(this);
        }
        integrarNacimientos();

        // 2. Los conejos buscan plantas cercanas y comen
        for (Conejo c : new ArrayList<>(conejos)) {
            if (c.estaVivo()) {
                c.comer(this);
            }
        }

        // 3. Los lobos intentan cazar
        for (Lobo l : new ArrayList<>(lobos)) {
            if (l.estaVivo()) {
                l.comer(this);
            }
        }

        // 4. Todas las entidades envejecen, gastan energía base y sufren el clima
        for (Entidad e : todasLasEntidades()) {
            e.envejecer();
            aplicarEfectoClima(e);
        }

        // 5. Las entidades sin energía mueren
        verificarMuertes();

        if (eventosTurnoActual == 0) {
            System.out.println("  (sin novedades este turno)");
        }

        // 6. Estado del ecosistema
        registrarActividad();
        actualizarExtremos();
        registrarHistorial();
        System.out.printf("Estado: Plantas: %d  Conejos: %d  Lobos: %d%n",
                plantas.size(), conejos.size(), lobos.size());
    }

    /**
     * Aplica el modificador de energía por turno que impone el clima a conejos
     * y lobos (las plantas solo se ven afectadas en su reproducción).
     */
    private void aplicarEfectoClima(Entidad e) {
        if (e instanceof Conejo) {
            aplicarModificador(e, climaActual.getModEnergiaConejo());
        } else if (e instanceof Lobo) {
            aplicarModificador(e, climaActual.getModEnergiaLobo());
        } else if (e instanceof Planta) {
            // Las plantas son productoras: generan energía por fotosíntesis
            // según la luz que aporta el clima.
            e.ganarEnergia(climaActual.getEnergiaFotosintesis());
        }
    }

    private void aplicarModificador(Entidad e, int mod) {
        if (mod > 0) {
            e.ganarEnergia(mod);
        } else if (mod < 0) {
            e.gastarEnergia(-mod);
        }
    }

    /**
     * Verifica muertes por falta de energía (regla default de {@link Mortal}
     * para animales, y chequeo directo para plantas) y remueve del ecosistema a
     * todas las entidades muertas, contabilizándolas.
     */
    private void verificarMuertes() {
        // Animales: aprovechan el método default verificarMuerte() de Mortal
        for (Conejo c : conejos) {
            if (c.estaVivo()) {
                c.verificarMuerte();
            }
        }
        for (Lobo l : lobos) {
            if (l.estaVivo()) {
                l.verificarMuerte();
            }
        }
        // Plantas: sin energía se marchitan
        for (Planta p : plantas) {
            if (p.estaViva() && p.getEnergia() <= 0) {
                p.setViva(false);
                registrarEvento(p.getNombre() + " se marchitó");
            }
        }
        // Remoción y conteo de muertes
        muertesPlanta += removerMuertasPlantas();
        muertesConejo += removerMuertosConejos();
        muertesLobo += removerMuertosLobos();
    }

    private int removerMuertasPlantas() {
        int cont = 0;
        for (int i = plantas.size() - 1; i >= 0; i--) {
            if (!plantas.get(i).estaViva()) {
                plantas.remove(i);
                cont++;
            }
        }
        return cont;
    }

    private int removerMuertosConejos() {
        int cont = 0;
        for (int i = conejos.size() - 1; i >= 0; i--) {
            if (!conejos.get(i).estaVivo()) {
                conejos.remove(i);
                cont++;
            }
        }
        return cont;
    }

    private int removerMuertosLobos() {
        int cont = 0;
        for (int i = lobos.size() - 1; i >= 0; i--) {
            if (!lobos.get(i).estaVivo()) {
                lobos.remove(i);
                cont++;
            }
        }
        return cont;
    }

    /** Mueve las crías nacidas este turno a las poblaciones principales. */
    private void integrarNacimientos() {
        plantas.addAll(plantasNuevas);
        conejos.addAll(conejosNuevas);
        plantasNuevas.clear();
        conejosNuevas.clear();
    }

    // =====================================================================
    //  SERVICIOS QUE USAN LAS ENTIDADES
    // =====================================================================

    /**
     * Registra el nacimiento de una cría (por reproducción) en el buffer del
     * turno y actualiza el contador de nacimientos por tipo.
     */
    public void registrarNacimiento(Entidad cria) {
        if (cria instanceof Conejo) {
            conejosNuevas.add((Conejo) cria);
            nacimientosConejo++;
        } else if (cria instanceof Planta) {
            plantasNuevas.add((Planta) cria);
            nacimientosPlanta++;
        }
    }

    /** Imprime un evento del turno y lo contabiliza. */
    public void registrarEvento(String mensaje) {
        System.out.println("  " + mensaje);
        eventosTurnoActual++;
    }

    /** @return una planta viva elegida al azar, o null si no hay ninguna */
    public Planta buscarPlantaViva() {
        List<Planta> vivas = new ArrayList<>();
        for (Planta p : plantas) {
            if (p.estaViva()) {
                vivas.add(p);
            }
        }
        if (vivas.isEmpty()) {
            return null;
        }
        return vivas.get(rnd.nextInt(vivas.size()));
    }

    /** @return un conejo vivo elegido al azar, o null si no hay ninguno */
    public Conejo buscarConejoVivoAleatorio() {
        List<Conejo> vivos = new ArrayList<>();
        for (Conejo c : conejos) {
            if (c.estaVivo()) {
                vivos.add(c);
            }
        }
        if (vivos.isEmpty()) {
            return null;
        }
        return vivos.get(rnd.nextInt(vivos.size()));
    }

    public int contarConejosVivos() {
        int cont = 0;
        for (Conejo c : conejos) {
            if (c.estaVivo()) {
                cont++;
            }
        }
        return cont;
    }

    private List<Entidad> todasLasEntidades() {
        List<Entidad> todas = new ArrayList<>();
        todas.addAll(plantas);
        todas.addAll(conejos);
        todas.addAll(lobos);
        return todas;
    }

    // ---- Generación de nombres únicos ------------------------------------

    public String nombrarPlanta() {
        return "Helecho-" + (++contadorPlanta);
    }

    public String nombrarConejo() {
        String base = NOMBRES_CONEJO[rnd.nextInt(NOMBRES_CONEJO.length)];
        return base + "-" + (++contadorConejo);
    }

    public String nombrarLobo() {
        String base = NOMBRES_LOBO[rnd.nextInt(NOMBRES_LOBO.length)];
        return base + "-" + (++contadorLobo);
    }

    // =====================================================================
    //  ESTADO / INTERVENCIÓN
    // =====================================================================

    /**
     * Muestra el conteo actual de cada entidad y el clima. También lista las
     * entidades vivas con su estado individual.
     */
    public void mostrarEstado() {
        System.out.println();
        System.out.println("---------- ESTADO DEL ECOSISTEMA ----------");
        System.out.println("Turno: " + turnoActual + " / " + turnosTotales
                + "   |   Clima: " + climaActual);
        System.out.printf("Plantas: %d   Conejos: %d   Lobos: %d%n",
                plantas.size(), conejos.size(), lobos.size());
        System.out.println("-------------------------------------------");
    }

    /** Cambia el clima actual del ecosistema. */
    public void cambiarClima(Clima nuevo) {
        if (nuevo != null) {
            this.climaActual = nuevo;
            System.out.println("El clima cambió a: " + nuevo);
        }
    }

    /** @return true si alguna población llegó a 0 (colapso del ecosistema) */
    public boolean ecosistemaColapsado() {
        return plantas.isEmpty() || conejos.isEmpty() || lobos.isEmpty();
    }

    // ---- Estadísticas internas -------------------------------------------

    private void registrarActividad() {
        historialActividad.add(new int[]{turnoActual, eventosTurnoActual});
        if (eventosTurnoActual > maxEventos) {
            maxEventos = eventosTurnoActual;
            turnoMayorActividad = turnoActual;
        }
    }

    private void registrarHistorial() {
        historialConteos.add(new int[]{turnoActual, plantas.size(),
            conejos.size(), lobos.size()});
    }

    /**
     * Actualiza los máximos y mínimos poblacionales con el conteo del turno
     * actual. Se usa tanto en la configuración inicial (turno 0) como después
     * de cada turno.
     */
    public void actualizarExtremos() {
        int p = plantas.size();
        int c = conejos.size();
        int l = lobos.size();
        if (!extremosInicializados) {
            maxPlantas = minPlantas = p;
            maxConejos = minConejos = c;
            maxLobos = minLobos = l;
            turnoMaxPlantas = turnoMinPlantas = turnoActual;
            turnoMaxConejos = turnoMinConejos = turnoActual;
            turnoMaxLobos = turnoMinLobos = turnoActual;
            extremosInicializados = true;
            return;
        }
        if (p > maxPlantas) { maxPlantas = p; turnoMaxPlantas = turnoActual; }
        if (p < minPlantas) { minPlantas = p; turnoMinPlantas = turnoActual; }
        if (c > maxConejos) { maxConejos = c; turnoMaxConejos = turnoActual; }
        if (c < minConejos) { minConejos = c; turnoMinConejos = turnoActual; }
        if (l > maxLobos) { maxLobos = l; turnoMaxLobos = turnoActual; }
        if (l < minLobos) { minLobos = l; turnoMinLobos = turnoActual; }
    }

    /**
     * Registra el estado inicial (turno 0) en las estadísticas, antes de que
     * empiece el loop de simulación.
     */
    public void registrarEstadoInicial() {
        actualizarExtremos();
        registrarHistorial();
    }

    // =====================================================================
    //  REPORTE FINAL
    // =====================================================================

    /**
     * Imprime el reporte completo de la simulación: causa de fin, actividad,
     * longevidad, cacerías, nacimientos/muertes, historial y elementos
     * peligrosos.
     */
    public void generarReporteFinal() {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("            REPORTE FINAL DE LA SIMULACIÓN");
        System.out.println("==================================================");

        // Causa de fin
        System.out.println();
        System.out.println("Causa de fin:");
        if (ecosistemaColapsado()) {
            System.out.println("  Colapso del ecosistema en el turno " + turnoActual + ".");
            System.out.println("  Población(es) extinta(s): " + poblacionesExtintas());
        } else {
            System.out.println("  Se completaron los " + turnosTotales + " turnos configurados.");
        }

        // Actividad
        System.out.println();
        System.out.println("Turno de mayor actividad:");
        if (turnoMayorActividad > 0) {
            System.out.println("  Turno " + turnoMayorActividad + " con " + maxEventos + " eventos.");
        } else {
            System.out.println("  No se registró actividad.");
        }

        // Entidad más longeva de cada tipo
        System.out.println();
        System.out.println("Entidad más longeva de cada tipo (mayor edad al finalizar):");
        System.out.println("  Planta: " + descripcionMasLongeva(plantas));
        System.out.println("  Conejo: " + descripcionMasLongeva(conejos));
        System.out.println("  Lobo:   " + descripcionMasLongeva(lobos));

        // Lobo con más cacerías
        System.out.println();
        System.out.println("Lobo con más cacerías exitosas:");
        Lobo mejorCazador = loboConMasCacerias();
        if (mejorCazador != null && mejorCazador.getExitosCaza() > 0) {
            System.out.println("  " + mejorCazador.getNombre() + " con "
                    + mejorCazador.getExitosCaza() + " cacerías.");
        } else {
            System.out.println("  Ningún lobo logró cazar.");
        }

        // Nacimientos y muertes por tipo
        System.out.println();
        System.out.println("Nacimientos y muertes por tipo (toda la simulación):");
        System.out.printf("  Plantas -> nacimientos: %d | muertes: %d%n", nacimientosPlanta, muertesPlanta);
        System.out.printf("  Conejos -> nacimientos: %d | muertes: %d%n", nacimientosConejo, muertesConejo);
        System.out.printf("  Lobos   -> nacimientos: %d | muertes: %d%n", 0, muertesLobo);

        // BONUS: máximos y mínimos poblacionales con su turno
        System.out.println();
        System.out.println("Máximos y mínimos poblacionales:");
        System.out.printf("  Plantas -> máx %d (turno %d) | mín %d (turno %d)%n",
                maxPlantas, turnoMaxPlantas, minPlantas, turnoMinPlantas);
        System.out.printf("  Conejos -> máx %d (turno %d) | mín %d (turno %d)%n",
                maxConejos, turnoMaxConejos, minConejos, turnoMinConejos);
        System.out.printf("  Lobos   -> máx %d (turno %d) | mín %d (turno %d)%n",
                maxLobos, turnoMaxLobos, minLobos, turnoMinLobos);

        // BONUS: elementos peligrosos ordenados por nivel de peligro
        System.out.println();
        System.out.println("Elementos peligrosos presentes (ordenados por nivel):");
        List<Peligroso> peligrosos = recolectarPeligrosos();
        if (peligrosos.isEmpty()) {
            System.out.println("  No quedan elementos peligrosos en el ecosistema.");
        } else {
            ordenarPorPeligroDesc(peligrosos);
            for (Peligroso pel : peligrosos) {
                String nombre = (pel instanceof Entidad) ? ((Entidad) pel).getNombre() : "?";
                System.out.println("  " + nombre + " (nivel " + pel.getNivelPeligro() + ")");
            }
        }

        // BONUS: historial de conteo poblacional turno a turno
        System.out.println();
        System.out.println("Historial poblacional (turno: plantas / conejos / lobos):");
        for (int[] fila : historialConteos) {
            System.out.printf("  T%-2d: %d / %d / %d%n", fila[0], fila[1], fila[2], fila[3]);
        }

        System.out.println();
        System.out.println("==================================================");
    }

    private String poblacionesExtintas() {
        List<String> extintas = new ArrayList<>();
        if (plantas.isEmpty()) extintas.add("plantas");
        if (conejos.isEmpty()) extintas.add("conejos");
        if (lobos.isEmpty()) extintas.add("lobos");
        return String.join(", ", extintas);
    }

    private String descripcionMasLongeva(List<? extends Entidad> lista) {
        Entidad masLongeva = null;
        for (Entidad e : lista) {
            if (masLongeva == null || e.getEdad() > masLongeva.getEdad()) {
                masLongeva = e;
            }
        }
        if (masLongeva == null) {
            return "ninguna sobrevivió";
        }
        return masLongeva.getNombre() + " (edad " + masLongeva.getEdad() + ")";
    }

    private Lobo loboConMasCacerias() {
        Lobo mejor = null;
        for (Lobo l : historicoLobos) {
            if (mejor == null || l.getExitosCaza() > mejor.getExitosCaza()) {
                mejor = l;
            }
        }
        return mejor;
    }

    private List<Peligroso> recolectarPeligrosos() {
        List<Peligroso> peligrosos = new ArrayList<>();
        for (Planta p : plantas) {
            if (p instanceof Peligroso) {
                peligrosos.add((Peligroso) p);
            }
        }
        for (Lobo l : lobos) {
            peligrosos.add(l); // Lobo siempre es Peligroso
        }
        return peligrosos;
    }

    /** Ordena de mayor a menor nivel de peligro (selection sort simple). */
    private void ordenarPorPeligroDesc(List<Peligroso> lista) {
        for (int i = 0; i < lista.size() - 1; i++) {
            int idxMax = i;
            for (int j = i + 1; j < lista.size(); j++) {
                if (lista.get(j).getNivelPeligro() > lista.get(idxMax).getNivelPeligro()) {
                    idxMax = j;
                }
            }
            if (idxMax != i) {
                Peligroso tmp = lista.get(i);
                lista.set(i, lista.get(idxMax));
                lista.set(idxMax, tmp);
            }
        }
    }

    // ---- Getters ---------------------------------------------------------

    public Clima getClima() {
        return climaActual;
    }

    public Random getRandom() {
        return rnd;
    }

    public int getTurnoActual() {
        return turnoActual;
    }

    public int getTurnosTotales() {
        return turnosTotales;
    }

    public int getCantidadPlantas() {
        return plantas.size();
    }

    public int getCantidadConejos() {
        return conejos.size();
    }

    public int getCantidadLobos() {
        return lobos.size();
    }

    public int getLobosCreadosTotal() {
        return lobosCreadosTotal;
    }

    public boolean puedeAgregarLobo() {
        return lobosCreadosTotal < MAX_LOBOS_TOTALES;
    }
}
