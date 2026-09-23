# Simulador de Ecosistema

Simulación por turnos de un ecosistema (plantas, conejos y lobos) que corre en la
terminal. Proyecto en Java para la primera instancia evaluativa de Interfaz Gráfica.

## Cómo ejecutar

**NetBeans:** *File → Open Project* y elegir la carpeta `SimuladorEcosistema`
(es un proyecto Java with Ant, JDK 25). Ejecutar con *Run Project* (F6); la
clase principal es `ecosistema.Main`.

**Consola**, desde la carpeta `SimuladorEcosistema/`:

```bash
javac -encoding UTF-8 -d bin src/ecosistema/*.java
java -cp bin ecosistema.Main
```

No usa librerías externas. En `ejemplo_de_corrida.txt` hay una partida completa
de ejemplo.

## Estructura del proyecto

```
SimuladorEcosistema/
├── src/ecosistema/     código fuente (paquete ecosistema)
├── nbproject/          configuración del proyecto NetBeans
├── build.xml           script de compilación de NetBeans (Ant)
└── ejemplo_de_corrida.txt
```

## Clases

- `Entidad` (abstracta): base de todas las entidades. Atributos `nombre`,
  `energia`, `edad`, `viva`; getters/setters validados; método concreto
  `envejecer()` y abstractos `actuar()` / `mostrarEstado()`.
- `Animal` (abstracta, implements Mortal): capa intermedia. Atributos
  `velocidad`, `peso`; método concreto `moverse()`; abstracto `comer()`.
- `Planta` (extends Entidad, implements Reproducible).
- `PlantaVenenosa` (extends Planta, implements Peligroso) — bonus.
- `Conejo` (extends Animal, implements Reproducible).
- `Lobo` (extends Animal, implements Peligroso).
- `Reproducible` (interface): `reproducirse`, `puedeReproducirse` y el default
  `intentarReproduccion`.
- `Mortal` (interface): `estaVivo`, `morir` y el default `verificarMuerte`.
- `Peligroso` (interface): `getNivelPeligro` — bonus.
- `Clima` (enum): Soleado, Lluvioso, Sequía, Invierno, con sus efectos.
- `LimiteLobosException`: excepción propia para el máximo de 5 lobos.
- `Ecosistema`: núcleo (poblaciones, `procesarTurno`, `agregarEntidad`,
  `cambiarClima`, `ecosistemaColapsado`, `generarReporteFinal`).
- `Main`: configuración con Scanner, loop principal e intervención.

## Reglas del turno

1. Plantas y conejos intentan reproducirse (un solo recorrido de `ArrayList<Reproducible>`).
   - Planta: necesita `30 / factor del clima` de energía (Lluvioso 15, Soleado 20,
     Sequía 60, Invierno no se reproduce). Máximo 60 plantas.
   - Conejo: adulto (edad ≥ 2), energía > 60 y otro conejo vivo. Máximo 30 conejos.
2. Los conejos con hambre (energía < 80) comen una planta al azar. Si es venenosa
   pierden 30; si no hay plantas pierden 15.
3. Los lobos cazan un conejo al azar. Probabilidad = 30% + energía/200
   (+20% en Invierno), con tope de 95%.
4. Todos envejecen (−5 de energía); las plantas recuperan 10 por fotosíntesis y
   los animales reciben el efecto del clima.
5. Muere quien se queda sin energía. La energía siempre está entre 0 y 100.

## Requisitos de POO cubiertos

- Herencia en 3 niveles: `Entidad` -> `Animal` -> `Conejo` / `Lobo`.
- Clase abstracta con métodos abstractos sobreescritos en cada subclase.
- Interfaces con métodos default (`Reproducible`, `Mortal`).
- Polimorfismo: en `procesarTurno()` se recorre un `ArrayList<Reproducible>`
  con plantas y conejos; `PlantaVenenosa` se guarda en el mismo
  `ArrayList<Planta>`; en el reporte se muestra cada sobreviviente con
  `mostrarEstado()` desde un `ArrayList<Entidad>`.
- Encapsulamiento: atributos privados con getters/setters validados.
- Sobrecarga: `agregarEntidad(String)` y `agregarEntidad(String, double)`.
- Excepción propia con try/catch: `LimiteLobosException`.

## Bonus implementados

- Planta Venenosa (1 de cada 5 plantas creadas).
- Historial de población por turno, con el turno de máximo y mínimo de cada una.
- Interface `Peligroso`: en el reporte se listan lobos y plantas venenosas
  ordenados por nivel de peligro (ordenamiento burbuja).

## Integrantes y roles

| Integrante | Archivos a su cargo |
|------------|---------------------|
| Joaquín | `Ecosistema.java`, `Main.java`, `LimiteLobosException.java` (núcleo y loop) |
| Fabri | `Entidad.java`, `Animal.java`, `Reproducible.java`, `Mortal.java` |
| Lucas | `Planta.java`, `PlantaVenenosa.java`, `Peligroso.java` |
| Licha | `Conejo.java`, `Lobo.java`, `Clima.java` |

## Desafíos encontrados

- **Balance de la simulación:** con los primeros valores los conejos se
  multiplicaban cada turno, se comían todas las plantas y el ecosistema colapsaba
  en el turno 3, antes de la primera intervención. Se resolvió con un tope de
  energía (100), conejos que solo comen con hambre y se reproducen de adultos,
  un máximo de 30 conejos y más energía de fotosíntesis para las plantas.
- **Borrar muertos de una lista mientras se recorre:** se recorre de atrás para
  adelante con un `for` por índice.
- **Contar las muertes como eventos:** `verificarMuerte()` devuelve `true` si el
  animal murió, así `Ecosistema` puede sumarla al turno de mayor actividad.

## Uso de IA

*(Cada integrante completa su parte: links a las conversaciones completas o
capturas de los prompts, indicando de quién es cada una.)*

- **Joaquín:** *(completar)*
- **Fabri:** *(completar)*
- **Lucas:** *(completar)*
- **Licha:** *(completar)*
