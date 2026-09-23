# Simulador de Ecosistema

Simulación por turnos de un ecosistema, ejecutada en la terminal y desarrollada
en **Java** para la primera instancia evaluativa de **Interfaz Gráfica**.

El jugador configura un ecosistema inicial con **plantas**, **conejos** y
**lobos**, elige el clima y la cantidad de turnos, y observa cómo las poblaciones
interactúan turno a turno: las plantas se reproducen, los conejos comen y se
reproducen, y los lobos cazan. Cada 3 turnos puede intervenir. Al finalizar se
muestra un reporte completo de la simulación.

---

## Requisitos e instrucciones de ejecución

- **Java JDK 8 o superior** (desarrollado y probado con JDK 21).
- No usa librerías externas: solo la biblioteca estándar de Java
  (`java.util`).

### Opción A — Desde la terminal

Desde la carpeta raíz del proyecto (`SimuladorEcosistema/`):

```bash
# Compilar
javac -encoding UTF-8 -d bin src/ecosistema/*.java

# Ejecutar
java -cp bin ecosistema.Main
```

### Opción B — Desde NetBeans

1. `File > New Project > Java with Ant > Java Application`.
2. Copiar los archivos `.java` dentro del paquete `ecosistema` del proyecto
   nuevo (o crear el paquete `ecosistema` y arrastrar los archivos de `src/`).
3. Marcar `Main.java` como clase principal y ejecutar con **Run (F6)**.

> Nota: la salida usa acentos y símbolos UTF-8. Si en la consola se ven
> caracteres raros, verificar que la codificación del proyecto/terminal sea
> UTF-8.

---

## Estructura del proyecto

```
SimuladorEcosistema/
├── src/ecosistema/
│   ├── Entidad.java          (abstracta) clase base de todas las entidades
│   ├── Planta.java           extiende Entidad, implementa Reproducible
│   ├── PlantaVenenosa.java   (BONUS) extiende Planta, implementa Peligroso
│   ├── Animal.java           (abstracta) extiende Entidad, implementa Mortal
│   ├── Conejo.java           extiende Animal, implementa Reproducible
│   ├── Lobo.java             extiende Animal, implementa Peligroso
│   ├── Reproducible.java     (interface) reproducirse / puedeReproducirse
│   ├── Mortal.java           (interface) estaVivo / morir + verificarMuerte()
│   ├── Peligroso.java        (interface, BONUS) getNivelPeligro()
│   ├── Clima.java            (enum) Soleado / Lluvioso / Sequía / Invierno
│   ├── Ecosistema.java       núcleo: poblaciones, turnos, estadísticas, reporte
│   └── Main.java             configuración, loop principal e intervención
└── README.md
```

---

## Integrantes y rol de cada uno

| Integrante | Rol |
|------------|-----|
| *(completar)* | *(completar)* |
| *(completar)* | *(completar)* |
| *(completar)* | *(completar)* |

---

## Diseño de Programación Orientada a Objetos

### Jerarquía de herencia

```
Entidad (abstracta)
├── Planta  ──────────────► Reproducible
│   └── PlantaVenenosa ────► Peligroso            (BONUS)
└── Animal (abstracta) ────► Mortal
    ├── Conejo ────────────► Reproducible
    └── Lobo ──────────────► Peligroso            (BONUS)
```

- **`Entidad`** es la clase abstracta base. Define los atributos comunes
  (`nombre`, `energia`, `edad`, `viva`) con **encapsulamiento completo** (todos
  privados, con getters y setters validados: la energía nunca puede ser
  negativa), un constructor con parámetros, el método concreto compartido
  `envejecer()` y dos métodos abstractos: `actuar(Ecosistema)` y
  `mostrarEstado()`.
- **`Animal`** es la capa intermedia de herencia: aporta atributos propios
  (`velocidad`, `peso`) y el **método concreto compartido `moverse()`**, dejando
  abstracto `comer(Ecosistema)`.

### Interfaces

- **`Reproducible`** (la implementan `Planta` y `Conejo`): declara
  `reproducirse()` y `puedeReproducirse()`, y aporta el **método default**
  `intentarReproduccion()`. En `Ecosistema` se recorre un
  `ArrayList<Reproducible>` que contiene plantas y conejos, procesando la
  reproducción de ambos **en un mismo recorrido** → polimorfismo.
- **`Mortal`** (la implementan los animales): declara `estaVivo()` y `morir()`,
  y aporta el **método default** `verificarMuerte()` que centraliza la regla
  "sin energía, muere".
- **`Peligroso`** (BONUS, la implementan `Lobo` y `PlantaVenenosa`): declara
  `getNivelPeligro()`. En el reporte final los elementos peligrosos se listan
  ordenados por nivel.

### Requisitos de POO cubiertos

| Requisito | Dónde |
|-----------|-------|
| Entidad abstracta con métodos abstractos correctamente sobreescritos | `Entidad` → `actuar()` / `mostrarEstado()` en cada subclase |
| Animal como capa intermedia con método concreto compartido | `Animal.moverse()` |
| `Reproducible` implementada por Planta y Conejo, usada con polimorfismo | `Ecosistema.procesarTurno()` recorre `ArrayList<Reproducible>` |
| `Mortal` implementada por los animales, con método default aprovechado | `Mortal.verificarMuerte()` |
| Encapsulamiento completo (atributos privados, getters/setters validados) | todas las clases |
| Sobrecarga (al menos un método con 2 versiones) | `Ecosistema.agregarEntidad(String)` y `agregarEntidad(String, double)` |
| Polimorfismo | `Reproducible`, `serComida()` (planta normal vs venenosa), lista de `Peligroso` |

---

## Mecánica del juego

### Configuración inicial (con validación por `Scanner`)

- Cantidad de plantas (5–30), conejos (2–15), lobos (1–5).
- Clima inicial: Soleado, Lluvioso, Sequía o Invierno.
- Cantidad de turnos (10–50).
- Se confirma la configuración antes de iniciar; las entidades se crean con
  energía aleatoria dentro de rangos razonables.

### Orden de cada turno

1. **Reproducción** de plantas y conejos (recorrido polimórfico sobre
   `ArrayList<Reproducible>`).
2. Los **conejos** buscan una planta y comen (si no hay, pierden 15 de energía).
3. Los **lobos** intentan cazar un conejo (la probabilidad de éxito **aumenta
   con la energía** del lobo — no es fija — y suma el bonus del clima).
4. Todas las entidades **envejecen** y gastan energía base; se aplica el
   **efecto del clima**.
5. Las entidades **sin energía mueren**.
6. Se muestra el **estado** del ecosistema (conteos y eventos del turno).

### Clima y sus efectos

| Clima | Plantas (reproducción) | Conejos (energía/turno) | Lobos |
|-------|------------------------|--------------------------|-------|
| Soleado | x1.5 | +5 | sin cambio |
| Lluvioso | x2 | +3 | −5 energía/turno |
| Sequía | x0.5 | −5 energía/turno | sin cambio |
| Invierno | no se reproducen | −8 energía/turno | caza con +20% de éxito |

### Intervención del jugador (cada 3 turnos)

- Cambiar el clima.
- Agregar una entidad (planta, conejo, lobo o —BONUS— planta venenosa).
  Se valida que **no puedan existir más de 5 lobos en toda la simulación**.
- Solo avanzar sin intervenir.

Toda acción de intervención se confirma antes de ejecutarse.

### Condiciones de fin

- Se alcanzó el número de turnos configurado, **o**
- Se extinguió alguna población (colapso del ecosistema).

Al finalizar se muestra un **reporte final** con: causa de fin, turno de mayor
actividad, entidad más longeva de cada tipo, lobo con más cacerías, total de
nacimientos y muertes por tipo, y (BONUS) los máximos/mínimos poblacionales con
su turno, el historial poblacional turno a turno y los elementos peligrosos
ordenados por nivel.

---

## Desafíos encontrados y decisiones de diseño

- **Sostenibilidad del ecosistema.** Como todas las entidades gastan energía
  base por existir pero las plantas no comen, se modeló la **fotosíntesis**:
  las plantas generan energía por turno según el clima (son productoras). Sin
  esto, las plantas estaban condenadas a extinguirse siempre.
- **Crecimiento sin control.** Para evitar que las plantas crecieran de forma
  exponencial, se agregó una **capacidad de carga** (la probabilidad de
  reproducción de la planta baja a medida que hay más plantas).
- **Equilibrio depredador-presa.** La reproducción de los conejos incorpora una
  probabilidad, de modo que las poblaciones oscilen en lugar de explotar. Aun
  así, el ecosistema es **frágil**: algunas partidas colapsan y otras completan
  todos los turnos, lo cual es un comportamiento esperado.
- **Modificación concurrente de listas.** Las crías nacidas durante un turno se
  acumulan en un buffer y se integran al final de la fase de reproducción para
  no modificar las listas mientras se recorren.
- **Polimorfismo en `serComida()`.** El conejo no puede distinguir una planta
  normal de una venenosa: llama a `serComida()` sin saber el tipo real; la
  planta venenosa devuelve un valor nutritivo negativo (intoxicación).

---

## Uso de IA / herramientas externas

*(Completar según la documentación individual: links a las conversaciones o
capturas de los prompts utilizados por cada integrante, tal como pide la
consigna.)*
