# Simulador de Ecosistema

Simulación por turnos de un ecosistema (plantas, conejos y lobos) que corre en la
terminal. Proyecto en Java para la primera instancia evaluativa de Interfaz Gráfica.

## Cómo ejecutar

**NetBeans:** *File → Open Project* y elegir esta carpeta (proyecto Java con Ant,
JDK 25). Ejecutar con *Run Project* (F6); la clase principal es `ecosistema.Main`.

**Consola / VS Code**, desde la carpeta del proyecto:

```bash
javac -encoding UTF-8 -d bin src/ecosistema/*.java
java -cp bin ecosistema.Main
```

No usa librerías externas. En `ejemplo_de_corrida.txt` hay una partida completa
de ejemplo.

## Estructura del proyecto

```
simulador-ecosistema/
├── src/ecosistema/     código fuente (paquete ecosistema)
├── nbproject/          configuración del proyecto NetBeans
├── build.xml           script de compilación de NetBeans (Ant)
└── ejemplo_de_corrida.txt
```

## Clases

- `Entidad` (abstracta): base de todas las entidades. Atributos `nombre`,
  `energia`, `edad`, `viva`; getters/setters validados (la energía nunca
  queda fuera de 0-100); método concreto `envejecer()` (+1 edad, −5 energía)
  y abstractos `actuar()` / `mostrarEstado()`.
- `Animal` (abstracta, extends Entidad, implements Mortal): capa intermedia.
  Atributos `velocidad`, `peso`; método concreto `moverse()`; abstracto
  `comer()`.
- `Planta` (extends Entidad, implements Reproducible): atributo `tamaño`
  (1-5, afecta la energía que da al ser comida).
- `PlantaVenenosa` (extends Planta, implements Peligroso) — bonus.
- `Conejo` (extends Animal, implements Reproducible).
- `Lobo` (extends Animal, implements Peligroso). No implementa
  `Reproducible`: los lobos no se reproducen en esta simulación.
- `Reproducible` (interface): `reproducirse`, `puedeReproducirse` y el
  default `intentarReproduccion`. La implementan `Planta` y `Conejo`.
- `Mortal` (interface): `estaVivo`, `morir` y el default `verificarMuerte`
  (mata a la entidad si su energía llega a 0). La implementan los animales
  (vía `Animal`).
- `Peligroso` (interface, bonus): `getNivelPeligro`. La implementan `Lobo` y
  `PlantaVenenosa`.
- `Clima` (enum): Soleado, Lluvioso, Sequía, Invierno, con los efectos de la
  consigna.
- `LimiteLobosException`: excepción propia para el máximo de 5 lobos en
  toda la simulación.
- `Ecosistema`: núcleo (poblaciones, `procesarTurno`, `agregarEntidad`
  sobrecargado, `cambiarClima`, `ecosistemaColapsado`,
  `generarReporteFinal`).
- `Main`: configuración con Scanner, loop principal e intervención.

## Reglas del turno

1. **Reproducción** (un solo recorrido de `ArrayList<Reproducible>` con
   plantas y conejos juntos, por polimorfismo):
   - Planta: necesita `30 / factor de reproducción del clima` de energía
     (Lluvioso 15, Soleado 20, Sequía 60; en Invierno no se reproducen).
     Máximo 60 plantas en simultáneo.
   - Conejo: energía > 60, al menos otro conejo vivo, y un 40% de
     probabilidad de que ocurra ese turno.
2. Los conejos comen una planta al azar (no distinguen si es venenosa
   antes de comerla). Si no hay ninguna, pierden 15 de energía.
3. Los lobos cazan un conejo al azar. Probabilidad = 30% + energía/200
   (+20% en Invierno), con tope de 95%.
4. Todos envejecen (−5 de energía); las plantas recuperan 10 por
   fotosíntesis y los animales reciben el efecto de energía del clima.
5. Muere quien se queda sin energía (`Mortal.verificarMuerte()`). La
   energía siempre está entre 0 y 100.

## Requisitos de POO cubiertos

- Herencia en 3 niveles: `Entidad → Animal → Conejo / Lobo`.
- Clase abstracta con métodos abstractos sobreescritos en cada subclase.
- Interfaces con métodos default (`Reproducible.intentarReproduccion`,
  `Mortal.verificarMuerte`).
- Polimorfismo: `ArrayList<Reproducible>` mezcla plantas y conejos en un
  mismo recorrido para la reproducción; `PlantaVenenosa` se guarda en el
  mismo `ArrayList<Planta>`; en el reporte final se muestra cada
  sobreviviente con `mostrarEstado()` desde un `ArrayList<Entidad>`.
- Encapsulamiento: todos los atributos privados, con getters y setters
  validados.
- Sobrecarga: `agregarEntidad(String)` (energía aleatoria) y
  `agregarEntidad(String, double)` (energía indicada).
- Excepción propia con try/catch: `LimiteLobosException`, al intentar
  superar los 5 lobos en toda la simulación (contando los iniciales).

## Bonus implementados

- **Planta Venenosa:** 1 de cada 5 plantas creadas es venenosa; el conejo
  no puede distinguirla de una planta normal antes de comerla, y en vez de
  nutrirlo le quita 30 de energía. Se guarda en el mismo `ArrayList<Planta>`
  que las plantas comunes (polimorfismo).
- **Estadísticas en tiempo real:** se guarda un historial de la población
  de cada tipo turno a turno, y al finalizar se informa en qué turno cada
  una tuvo su máximo y su mínimo.
- **Interface `Peligroso`:** la implementan `Lobo` y `PlantaVenenosa`; en
  el reporte final se listan todos los elementos peligrosos vivos,
  ordenados de mayor a menor nivel de peligro.

## Desafíos encontrados

- **Integrar el trabajo de las 4 personas:** cada uno programó su clase
  por separado y varios métodos quedaron con nombres distintos entre
  archivos (por ejemplo `Clima` exponía `getModEnergiaConejo()` pero
  `Ecosistema` esperaba `getEnergiaConejo()`, o `Mortal.verificarMuerte()`
  era `void` cuando `Ecosistema` necesitaba que devolviera `boolean`). Se
  resolvió revisando cada llamada y agregando los métodos/alias que
  faltaban sin tocar la lógica de nadie.
- **Balance de la simulación:** con los primeros valores el ecosistema
  colapsaba muy rápido. Se ajustó con un tope de energía (0-100), más
  energía de fotosíntesis para las plantas, y un máximo de 60 plantas en
  simultáneo para que no crecieran sin límite.
- **Borrar entidades muertas de una lista mientras se recorre:** se
  recorre de atrás para adelante con un `for` por índice para no saltear
  elementos al remover.
- **Contar las muertes como eventos del turno:** `verificarMuerte()`
  devuelve `true` cuando la entidad murió en ese llamado, así
  `Ecosistema` puede sumarla al conteo de eventos y detectar el turno de
  mayor actividad.

## Uso de IA


- **Joaquín:** *(completar)*
- **Fabri:** *(completar)*
- **Lucas:** *(completar)*
- **Licha:** *(completar)*


