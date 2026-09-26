package ecosistema;

import java.util.ArrayList;
import java.util.Random;

// Nucleo de la simulacion: guarda las poblaciones, el clima y el turno actual.
public class Ecosistema {
    private ArrayList<Planta> plantas = new ArrayList<>();
    private ArrayList<Conejo> conejos = new ArrayList<>();
    private ArrayList<Lobo> lobos = new ArrayList<>();
    private ArrayList<Lobo> todosLosLobos = new ArrayList<>(); // vivos y muertos, para el reporte final

    private Clima climaActual;
    private int turnoActual;
    private int turnosTotales;
    private Random random = new Random();

    // Nombres: uno al azar de la lista + un numero para que no se repitan (ej: Helecho-3)
    private String[] nombresPlantas = {"Helecho", "Trebol", "Musgo", "Ortiga", "Cardo"};
    private String[] nombresConejos = {"Blas", "Luna", "Topo", "Rex", "Nube", "Maya", "Copo"};
    private String[] nombresLobos = {"Fang", "Sombra", "Garra", "Zeus", "Colmillo"};

    // Contadores para darle un nombre unico a cada entidad
    private int contadorPlanta;
    private int contadorConejo;
    private int contadorLobo;

    // Lobos agregados en toda la simulacion (maximo 5)
    private int lobosAgregados;

    // Estadisticas para el reporte final
    private int eventosTurno;
    private int maxEventos;
    private int turnoMayorActividad;
    private int nacimientosPlanta;
    private int muertesPlanta;
    private int nacimientosConejo;
    private int muertesConejo;
    private int muertesLobo;

    // (BONUS) Historial: cuantos habia de cada poblacion al final de cada turno
    private ArrayList<Integer> historialPlantas = new ArrayList<>();
    private ArrayList<Integer> historialConejos = new ArrayList<>();
    private ArrayList<Integer> historialLobos = new ArrayList<>();

    public Ecosistema(Clima climaInicial, int turnosTotales) {
        this.climaActual = climaInicial;
        this.turnosTotales = turnosTotales;
        this.turnoActual = 0;
    }

    // ---- Alta de entidades (SOBRECARGA: dos versiones de agregarEntidad) ----

    // Version 1: sin energia -> se elige una energia aleatoria entre 40 y 80
    public String agregarEntidad(String tipo) throws LimiteLobosException {
        double energia = 40 + random.nextInt(41);
        return agregarEntidad(tipo, energia);
    }

    // Version 2: con la energia inicial indicada. Devuelve el nombre de la entidad creada.
    public String agregarEntidad(String tipo, double energia) throws LimiteLobosException {
        if (tipo.equalsIgnoreCase("planta")) {
            int tamaño = 1 + random.nextInt(5);
            Planta nueva;
            // (BONUS) 1 de cada 5 plantas es venenosa. Va a la misma lista de plantas.
            if (random.nextInt(5) == 0) {
                nueva = new PlantaVenenosa(nuevoNombrePlanta(), energia, tamaño);
            } else {
                nueva = new Planta(nuevoNombrePlanta(), energia, tamaño);
            }
            plantas.add(nueva);
            return nueva.getNombre();
        }

        if (tipo.equalsIgnoreCase("conejo")) {
            Conejo nuevo = new Conejo(nuevoNombreConejo(), energia, 5, 2);
            conejos.add(nuevo);
            return nuevo.getNombre();
        }

        if (tipo.equalsIgnoreCase("lobo")) {
            if (lobosAgregados >= 5) {
                throw new LimiteLobosException("No se pueden agregar mas de 5 lobos en toda la simulacion.");
            }
            Lobo nuevo = new Lobo(nuevoNombreLobo(), energia, 8, 30);
            lobos.add(nuevo);
            todosLosLobos.add(nuevo);
            lobosAgregados++;
            return nuevo.getNombre();
        }

        return ""; // tipo desconocido: no se agrega nada
    }

    // ---- Turno ----

    public void procesarTurno() {
        turnoActual++;
        eventosTurno = 0;
        System.out.println();
        System.out.println("=== TURNO " + turnoActual + " | Clima: " + climaActual + " ===");
        System.out.println("Plantas: " + plantas.size() + "  Conejos: " + conejos.size() + "  Lobos: " + lobos.size());
        System.out.println("-- Eventos --");

        // 1. Reproduccion: plantas y conejos en un mismo recorrido (POLIMORFISMO con Reproducible)
        ArrayList<Reproducible> reproducibles = new ArrayList<>();
        reproducibles.addAll(plantas);
        reproducibles.addAll(conejos);
        for (Reproducible r : reproducibles) {
            r.intentarReproduccion(this);
        }

        // 2. Los conejos buscan una planta y comen
        for (Conejo c : conejos) {
            c.comer(this);
        }

        // 3. Los lobos intentan cazar
        for (Lobo l : lobos) {
            l.actuar(this);
        }

        // 4. Todos envejecen y gastan energia base. Despues se aplica el efecto del clima.
        for (Planta p : plantas) {
            if (p.estaViva()) {
                p.envejecer();
                p.setEnergia(p.getEnergia() + 10); // la fotosintesis les devuelve energia
            }
        }
        for (Conejo c : conejos) {
            if (c.estaVivo()) {
                c.envejecer();
                c.setEnergia(c.getEnergia() + climaActual.getEnergiaConejo());
            }
        }
        for (Lobo l : lobos) {
            if (l.estaVivo()) {
                l.envejecer();
                l.setEnergia(l.getEnergia() + climaActual.getEnergiaLobo());
            }
        }

        // 5. Las entidades sin energia mueren (verificarMuerte es el metodo default de Mortal)
        for (Conejo c : conejos) {
            if (c.verificarMuerte()) {
                eventosTurno++;
            }
        }
        for (Lobo l : lobos) {
            if (l.verificarMuerte()) {
                eventosTurno++;
            }
        }
        for (Planta p : plantas) {
            if (p.estaViva() && p.getEnergia() <= 0) {
                p.setViva(false);
                registrarEvento("Planta " + p.getNombre() + " se seco");
            }
        }
        removerMuertos();

        if (eventosTurno == 0) {
            System.out.println("  (sin novedades)");
        }

        // Guardar el turno con mas eventos
        if (eventosTurno > maxEventos) {
            maxEventos = eventosTurno;
            turnoMayorActividad = turnoActual;
        }

        // (BONUS) Guardar como quedo cada poblacion en este turno
        historialPlantas.add(plantas.size());
        historialConejos.add(conejos.size());
        historialLobos.add(lobos.size());

        // 6. Estado del ecosistema al final del turno
        System.out.println("Estado: Plantas: " + plantas.size() + "  Conejos: " + conejos.size() + "  Lobos: " + lobos.size());
    }

    // Saca de las listas a las entidades muertas y cuenta las muertes.
    // Se recorre de atras para adelante para poder borrar sin saltear elementos.
    private void removerMuertos() {
        for (int i = plantas.size() - 1; i >= 0; i--) {
            if (!plantas.get(i).estaViva()) {
                plantas.remove(i);
                muertesPlanta++;
            }
        }
        for (int i = conejos.size() - 1; i >= 0; i--) {
            if (!conejos.get(i).estaVivo()) {
                conejos.remove(i);
                muertesConejo++;
            }
        }
        for (int i = lobos.size() - 1; i >= 0; i--) {
            if (!lobos.get(i).estaVivo()) {
                lobos.remove(i);
                muertesLobo++;
            }
        }
    }

    // ---- Metodos que usan las entidades ----

    // Imprime un evento del turno y lo cuenta
    public void registrarEvento(String mensaje) {
        System.out.println("  " + mensaje);
        eventosTurno++;
    }

    // Devuelve una planta viva al azar (o null si no hay ninguna)
    public Planta buscarPlantaViva() {
        ArrayList<Planta> vivas = new ArrayList<>();
        for (Planta p : plantas) {
            if (p.estaViva()) {
                vivas.add(p);
            }
        }
        if (vivas.isEmpty()) {
            return null;
        }
        return vivas.get(random.nextInt(vivas.size()));
    }

    // Devuelve un conejo vivo al azar (o null si no hay ninguno)
    public Conejo buscarConejoVivo() {
        ArrayList<Conejo> vivos = new ArrayList<>();
        for (Conejo c : conejos) {
            if (c.estaVivo()) {
                vivos.add(c);
            }
        }
        if (vivos.isEmpty()) {
            return null;
        }
        return vivos.get(random.nextInt(vivos.size()));
    }

    public int contarPlantas() {
        return plantas.size();
    }

    public int contarConejosVivos() {
        int vivos = 0;
        for (Conejo c : conejos) {
            if (c.estaVivo()) {
                vivos++;
            }
        }
        return vivos;
    }

    public void agregarPlanta(Planta p) { plantas.add(p); }
    public void agregarConejo(Conejo c) { conejos.add(c); }

    public void sumarNacimientoPlanta() { nacimientosPlanta++; }
    public void sumarNacimientoConejo() { nacimientosConejo++; }

    // Alias usados por Conejo/Lobo al reproducirse o cazar
    public String nombrarConejo() { return nuevoNombreConejo(); }
    public void registrarNacimiento(Conejo cria) {
        agregarConejo(cria);
        sumarNacimientoConejo();
    }
    public Conejo buscarConejoVivoAleatorio() { return buscarConejoVivo(); }

    public String nuevoNombrePlanta() {
        contadorPlanta++;
        return nombresPlantas[random.nextInt(nombresPlantas.length)] + "-" + contadorPlanta;
    }

    public String nuevoNombreConejo() {
        contadorConejo++;
        return nombresConejos[random.nextInt(nombresConejos.length)] + "-" + contadorConejo;
    }

    public String nuevoNombreLobo() {
        contadorLobo++;
        return nombresLobos[random.nextInt(nombresLobos.length)] + "-" + contadorLobo;
    }

    // ---- Estado y control ----

    public void mostrarEstado() {
        System.out.println();
        System.out.println("---- ESTADO DEL ECOSISTEMA ----");
        System.out.println("Turno: " + turnoActual + " / " + turnosTotales + " | Clima: " + climaActual);
        System.out.println("Plantas: " + plantas.size() + "  Conejos: " + conejos.size() + "  Lobos: " + lobos.size());
    }

    public void cambiarClima(Clima nuevo) {
        this.climaActual = nuevo;
        System.out.println("El clima cambio a: " + nuevo);
    }

    // Devuelve true si alguna poblacion llego a 0
    public boolean ecosistemaColapsado() {
        return plantas.isEmpty() || conejos.isEmpty() || lobos.isEmpty();
    }

    // ---- Reporte final ----

    public void generarReporteFinal() {
        System.out.println();
        System.out.println("========== REPORTE FINAL ==========");

        // Causa de fin
        if (ecosistemaColapsado()) {
            String extintas = "";
            if (plantas.isEmpty()) extintas = extintas + "plantas ";
            if (conejos.isEmpty()) extintas = extintas + "conejos ";
            if (lobos.isEmpty()) extintas = extintas + "lobos ";
            System.out.println("Causa de fin: colapso del ecosistema en el turno " + turnoActual
                    + " (se extinguieron: " + extintas.trim() + ")");
        } else {
            System.out.println("Causa de fin: se completaron los " + turnosTotales + " turnos.");
        }

        // Turno de mayor actividad
        System.out.println("Turno de mayor actividad: turno " + turnoMayorActividad + " (" + maxEventos + " eventos)");

        // Entidad mas longeva de cada tipo (entre las que siguen vivas)
        System.out.println();
        System.out.println("-- Mas longevas --");
        System.out.println("Planta: " + masLongeva(new ArrayList<Entidad>(plantas)));
        System.out.println("Conejo: " + masLongeva(new ArrayList<Entidad>(conejos)));
        System.out.println("Lobo:   " + masLongeva(new ArrayList<Entidad>(lobos)));

        // Lobo con mas cacerias (se cuentan tambien los que murieron)
        Lobo mejor = null;
        for (Lobo l : todosLosLobos) {
            if (mejor == null || l.getExitosCaza() > mejor.getExitosCaza()) {
                mejor = l;
            }
        }
        if (mejor != null) {
            System.out.println("Lobo con mas cacerias: " + mejor.getNombre() + " (" + mejor.getExitosCaza() + ")");
        }

        // Nacimientos y muertes por tipo
        System.out.println();
        System.out.println("-- Nacimientos y muertes --");
        System.out.println("Plantas -> nacimientos: " + nacimientosPlanta + " | muertes: " + muertesPlanta);
        System.out.println("Conejos -> nacimientos: " + nacimientosConejo + " | muertes: " + muertesConejo);
        System.out.println("Lobos   -> nacimientos: 0 (no se reproducen) | muertes: " + muertesLobo);

        // (BONUS) Estadisticas: turno de maximo y minimo de cada poblacion
        System.out.println();
        System.out.println("-- Historial de poblacion --");
        mostrarMaximoYMinimo("Plantas", historialPlantas);
        mostrarMaximoYMinimo("Conejos", historialConejos);
        mostrarMaximoYMinimo("Lobos", historialLobos);

        // (BONUS) Elementos peligrosos ordenados por nivel
        System.out.println();
        System.out.println("-- Elementos peligrosos (de mayor a menor) --");
        mostrarPeligrosos();

        // Estado final de cada sobreviviente (POLIMORFISMO: cada uno se muestra a su manera)
        System.out.println();
        System.out.println("-- Sobrevivientes --");
        ArrayList<Entidad> sobrevivientes = new ArrayList<>();
        sobrevivientes.addAll(plantas);
        sobrevivientes.addAll(conejos);
        sobrevivientes.addAll(lobos);
        if (sobrevivientes.isEmpty()) {
            System.out.println("  No quedo ninguna entidad viva.");
        }
        for (Entidad e : sobrevivientes) {
            e.mostrarEstado();
        }
        System.out.println("===================================");
    }

    // Devuelve el nombre y la edad de la entidad mas vieja de una lista
    private String masLongeva(ArrayList<Entidad> lista) {
        Entidad masVieja = null;
        for (Entidad e : lista) {
            if (masVieja == null || e.getEdad() > masVieja.getEdad()) {
                masVieja = e;
            }
        }
        if (masVieja == null) {
            return "no sobrevivio ninguna";
        }
        return masVieja.getNombre() + " (edad " + masVieja.getEdad() + ")";
    }

    // (BONUS) Busca en el historial el turno con mas y con menos individuos
    private void mostrarMaximoYMinimo(String nombre, ArrayList<Integer> historial) {
        if (historial.isEmpty()) {
            return;
        }
        int posMax = 0;
        int posMin = 0;
        for (int i = 1; i < historial.size(); i++) {
            if (historial.get(i) > historial.get(posMax)) posMax = i;
            if (historial.get(i) < historial.get(posMin)) posMin = i;
        }
        // La posicion 0 de la lista corresponde al turno 1
        System.out.println(nombre + ": maximo " + historial.get(posMax) + " en el turno " + (posMax + 1)
                + " | minimo " + historial.get(posMin) + " en el turno " + (posMin + 1));
    }

    // (BONUS) Junta los lobos y las plantas venenosas vivas y los ordena por nivel de peligro
    private void mostrarPeligrosos() {
        ArrayList<Peligroso> peligrosos = new ArrayList<>();
        for (Planta p : plantas) {
            if (p instanceof Peligroso) {
                peligrosos.add((Peligroso) p);
            }
        }
        for (Lobo l : lobos) {
            peligrosos.add(l);
        }

        if (peligrosos.isEmpty()) {
            System.out.println("  No quedan elementos peligrosos.");
            return;
        }

        // Ordenamiento burbuja: de mayor a menor nivel de peligro
        for (int i = 0; i < peligrosos.size() - 1; i++) {
            for (int j = 0; j < peligrosos.size() - 1 - i; j++) {
                if (peligrosos.get(j).getNivelPeligro() < peligrosos.get(j + 1).getNivelPeligro()) {
                    Peligroso aux = peligrosos.get(j);
                    peligrosos.set(j, peligrosos.get(j + 1));
                    peligrosos.set(j + 1, aux);
                }
            }
        }

        for (Peligroso p : peligrosos) {
            System.out.println("  " + p.getNombre() + " (nivel " + p.getNivelPeligro() + ")");
        }
    }

    // ---- Getters ----

    public Clima getClima() { return climaActual; }
    public Random getRandom() { return random; }
    public int getTurnoActual() { return turnoActual; }
    public int getTurnosTotales() { return turnosTotales; }
}
