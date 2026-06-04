# Snake Race — ARSW Lab #2 (Java 21, Virtual Threads)

**Escuela Colombiana de Ingeniería – Arquitecturas de Software**  
Laboratorio de programación concurrente: condiciones de carrera, sincronización y colecciones seguras.

---

## Requisitos

- **JDK 21** (Temurin recomendado)
- **Maven 3.9+**
- SO: Windows, macOS o Linux

---

## Cómo ejecutar

```bash
mvn clean verify
mvn -q -DskipTests exec:java -Dsnakes=4
```

- `-Dsnakes=N` → inicia el juego con **N** serpientes (por defecto 2).
- **Controles**:
  - **Flechas**: serpiente **0** (Jugador 1).
  - **WASD**: serpiente **1** (si existe).
  - **Espacio** o botón **Action**: Pausar / Reanudar.

---

## Reglas del juego (resumen)

- **N serpientes** corren de forma autónoma (cada una en su propio hilo).
- **Ratones**: al comer uno, la serpiente **crece** y aparece un **nuevo obstáculo**.
- **Obstáculos**: si la cabeza entra en un obstáculo hay **rebote**.
- **Teletransportadores** (flechas rojas): entrar por uno te **saca por su par**.
- **Rayos (Turbo)**: al pisarlos, la serpiente obtiene **velocidad aumentada** temporal.
- Movimiento con **wrap-around** (el tablero “se repite” en los bordes).

---

## Arquitectura (carpetas)

```
co.eci.snake
├─ app/                 # Bootstrap de la aplicación (Main)
├─ core/                # Dominio: Board, Snake, Direction, Position
├─ core/engine/         # GameClock (ticks, Pausa/Reanudar)
├─ concurrency/         # SnakeRunner (lógica por serpiente con virtual threads)
└─ ui/legacy/           # UI estilo legado (Swing) con grilla y botón Action
```

---

# Actividades del laboratorio

## Parte I — (Calentamiento) `wait/notify` en un programa multi-hilo

1. Toma el programa [**PrimeFinder**](https://github.com/ARSW-ECI/wait-notify-excercise).
2. Modifícalo para que **cada _t_ milisegundos**:
   - Se **pausen** todos los hilos trabajadores.
   - Se **muestre** cuántos números primos se han encontrado.
   - El programa **espere ENTER** para **reanudar**.
3. La sincronización debe usar **`synchronized`**, **`wait()`**, **`notify()` / `notifyAll()`** sobre el **mismo monitor** (sin _busy-waiting_).
4. Entrega en el reporte de laboratorio **las observaciones y/o comentarios** explicando tu diseño de sincronización (qué lock, qué condición, cómo evitas _lost wakeups_).

> Objetivo didáctico: practicar suspensión/continuación **sin** espera activa y consolidar el modelo de monitores en Java.

---

## Parte II — SnakeRace concurrente (núcleo del laboratorio)

### 1) Análisis de concurrencia

- Explica **cómo** el código usa hilos para dar autonomía a cada serpiente.
- **Identifica** y documenta en **`el reporte de laboratorio`**:
  - Posibles **condiciones de carrera**.
  - **Colecciones** o estructuras **no seguras** en contexto concurrente.
  - Ocurrencias de **espera activa** (busy-wait) o de sincronización innecesaria.

### 2) Correcciones mínimas y regiones críticas

- **Elimina** esperas activas reemplazándolas por **señales** / **estados** o mecanismos de la librería de concurrencia.
- Protege **solo** las **regiones críticas estrictamente necesarias** (evita bloqueos amplios).
- Justifica en **`el reporte de laboratorio`** cada cambio: cuál era el riesgo y cómo lo resuelves.

### 3) Control de ejecución seguro (UI)

- Implementa la **UI** con **Iniciar / Pausar / Reanudar** (ya existe el botón _Action_ y el reloj `GameClock`).
- Al **Pausar**, muestra de forma **consistente** (sin _tearing_):
  - La **serpiente viva más larga**.
  - La **peor serpiente** (la que **primero murió**).
- Considera que la suspensión **no es instantánea**; coordina para que el estado mostrado no quede “a medias”.

### 4) Robustez bajo carga

- Ejecuta con **N alto** (`-Dsnakes=20` o más) y/o aumenta la velocidad.
- El juego **no debe romperse**: sin `ConcurrentModificationException`, sin lecturas inconsistentes, sin _deadlocks_.
- Si habilitas **teleports** y **turbo**, verifica que las reglas no introduzcan carreras.

> Entregables detallados más abajo.

---

## Entregables

1. **Código fuente** funcionando en **Java 21**.
2. Todo de manera clara en **`**el reporte de laboratorio**`** con:
   - Data races encontradas y su solución.
   - Colecciones mal usadas y cómo se protegieron (o sustituyeron).
   - Esperas activas eliminadas y mecanismo utilizado.
   - Regiones críticas definidas y justificación de su **alcance mínimo**.
3. UI con **Iniciar / Pausar / Reanudar** y estadísticas solicitadas al pausar.

---

## Criterios de evaluación (10)

- (3) **Concurrencia correcta**: sin data races; sincronización bien localizada.
- (2) **Pausa/Reanudar**: consistencia visual y de estado.
- (2) **Robustez**: corre **con N alto** y sin excepciones de concurrencia.
- (1.5) **Calidad**: estructura clara, nombres, comentarios; sin _code smells_ obvios.
- (1.5) **Documentación**: **`reporte de laboratorio`** claro, reproducible;

---

## Tips y configuración útil

- **Número de serpientes**: `-Dsnakes=N` al ejecutar.
- **Tamaño del tablero**: cambiar el constructor `new Board(width, height)`.
- **Teleports / Turbo**: editar `Board.java` (métodos de inicialización y reglas en `step(...)`).
- **Velocidad**: ajustar `GameClock` (tick) o el `sleep` del `SnakeRunner` (incluye modo turbo).

---

## Cómo correr pruebas

```bash
mvn clean verify
```

Incluye compilación y ejecución de pruebas JUnit. Si tienes análisis estático, ejecútalo en `verify` o `site` según tu `pom.xml`.

---

## Reporte de laboratorio - Parte II

### 1) Análisis de concurrencia

Al iniciar el juego el main utiliza el método launch de SnakeApp, el cual crea una instancia de SnakeApp y que delega su manejo al hilo Event Dispatch Thread de Swing.
Al crear la instancia de SnakeApp, las sentencias:
```
    var exec = Executors.newVirtualThreadPerTaskExecutor();
    snakes.forEach(s -> exec.submit(new SnakeRunner(s, board)));
```
Crean un `Executor` que delega la creación de un Thread por cada una de las tareas que se enlisten. Luego, por cada serpiente en el arreglo `snakes` crea
una instancia de `SnakeRunner` (que implementa runnable) y luego `Executor` creará un **virtual thread** por cada runnable enlistado
y en cada hilo se ejecutará el método `run()` de manera paralela a los demás.

Ahora bien, con respecto a `Snake` y `SnakeRunner` (estado y actualización estado/movimiento) el método `snapshot()` le permite a SnakeApp pintar el estado de la serpiente
y el método `run()` de `SnakeRunner` utiliza el método `step()` (de la clase `Board`), donde se utiliza el método `advance()` de `Snake` lo cual cambia el estado de la
"serpiente", este hecho puede generar un **race condition**, ya que si en simultáneo con la ejecución del método `run()`, se está cambiando el estado de `Snake`, y el EDT
utiliza el método `snapshot()` pues, la serpiente que se pinta puede tener un estado inconsistente o incluso lanzar un `ConcurrentModificationException`.

Con respecto a `Board` el método `randomEmpty()` cómo se utiliza en el método `step()`, que aplica `synchronized` para hacer **lock** sobre el `Board` y esto
también bloquea este método. De todas formas el único otro método que utiliza el `randomEmpty()` es el constructor de la clase y como no hay hilos de `SnakeRunner`
para ese momento no hay posibilidad de que haya **race condition**. Por otro lado, los métodos:
```
    public synchronized Set<Position> mice()
    public synchronized Set<Position> obstacles()
    public synchronized Set<Position> turbo()
    public synchronized Map<Position, Position> teleports()
```
Al ser llamados por `SnakeApp` para pintar sus elementos los llamados generan el **lock** sobre el objeto `Board`; pero, cómo estos llamados
son sentencias en secuencia se da que `SnakeApp` pinta los **ratones** en un tiempo t, luego los obstaculos de un tiempo t+1 y el turbo en el tiempo t+2.
Esto es un desface y muestra el tablero con estos elementos en tiempos distintos.

Con respecto al `GameClock`  los métodos `pause()` y `resume()` solo cambian el atributo `state` no está suspendiendo el `scheduler`. Esto es **busy-wait**, puesto que
el scheduler va a seguir ejecutando la tarea que se le asignó en el método `start()`; aunque el juego diga en su estado que está pausado. La tarea es:

````
        () -> {
        if (state.get() == GameState.RUNNING) tick.run()
        }
````

Finalmente, `SnakeRunner` ejecuta run() siempre y cuando su hilo no esté interrumpido; sin embargo, no exite mecanismo alguno para que este sepa cuando el juego está pausado.
Así pues, `SnakeApp` no pinta más el estado; pero, el movimiento de las serpientes sigue ejecutandose, lo que genera también una inconsistencia
entre lo que muestra la **UI** y el estado real del juego.

### 2) Correcciones mínimas y regiones críticas

+ Para resolver el problema de la pausa y que los hilos de cada `SnakeRunner` puedan pausarse con el juego aplique el patron **monitor** para poder sincronizar todos los hilos
de las serpientes. Cree la clase `GameMonitor` que es la clase que los hilos de las serpientes verifican para saber si el juego está en pausa o no.
Se modificó `SnakeApp` donde se instancia un único `GameMonitor` y se le pasa a todos los `SnakeRunner` para que en el método `run()` hagan un `pauseMonitor.checkPause()` y
así suspenderse todos y al reanudar el juego con un `notifyAll()` todo los hilos se reactivaran. En `SnakeApp` el método `togglePause()` cambia el estado del monitor `monitor.pause()` y
`monitor.resume()`

+ Para resolver el problema de **race condition** donde el EDT podia utilizar el método `snapshot()` mientras otro hilo ejecutaba el `advance()` de la `Snake` simplemente
agregamos a ambos método la palabra reservada `synchronized` que bloquea la snake apenas un hilo ejecuta alguno de los dos y haciendo esperar a los hilos que requieran utilizar
a la serpiente.

+ Para la corrección de la desincronización en el tiempo en que `SnakeApp` pintaba los **ratones**, **turbos**, etc. cree una clase que llamada `BoardSnapshot` que es la
foto completa de todos los elementos del tablero en un momento y para poder tomarla en la clase `Board` en vez de usar los metodos `mice()`, `obstacles()` cree el
método `snapshot()` (que aplica `synchronized`) y que devuelve el BoardSnapshot para que `SnakeApp` construya una imagen consistente. Además, envia copias defensivas de los arreglos;
es decir, que no se envian los arreglos originales los del objeto `Board`.

+ Aquí reutilicé el monitor que cree que ya maneja la pausa. En el GameClock agregué el atributo `pauseMonitor` y ahora en el método de `start` la tarea que se le asigna al
  `scheduler` utiliza el método `checkPaused()` para saber si debe suspenderse; además, el único monitor que utilizan todos los `SnakeRunner` es el que se le envia a
  `GameClock`.  También, algo a mencionar es que quitamos el condicional que estaba antes `if (state.get() == GameState.RUNNING)` y quedo de esta manera:

````
       scheduler.scheduleAtFixedRate(() -> {
          try {
              pauseMonitor.checkPaused();
              tick.run();
          } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
          }
      }, 0, periodMillis, TimeUnit.MILLISECONDS);
````

### 3) Control de ejecución seguro (UI)

+ En `Snake` agregamos el atributo `alive` y `deathTime` para saber si la serpiente murio; Además,
usamos el método `isAlive()` y `deathTime()` para dar a concer estos datos.
+ En `Board` agregamos el **enum** `KILLED` para que el método step devuelva esta respuesta dado
el caso en que la serpiente colisione con otro. Además, adecuamos el método para evaluar la colisión.
Y la parte que evalua la colisión es:

````
      boolean itDied = false;
      for (Snake otherSnake : allSnakes) {
          //la otra serpiente es válida para chocar
          if (otherSnake != snake && otherSnake.isAlive()) {
              //Colisión
              if (otherSnake.snapshot().contains(next)) {
                  snake.kill();
                  itDied = true;
              }
          }
      }
````
En este revisa que el movimiento caiga en la posición de una serpiente que esté viva.

+ En `SnakeRunner` pase el arreglo de serpientes por el constructor para que este se lo pase al tablero cada vez que ejecute el método `step()` para que ahora haga lo mismo;
pero, que revise si hay colisión con alguna serpiente viva. Finalmente, si `step()` retorna `KILLED` se termina el hilo con `return`.
+ En `SnakeApp` agregue `startTime` para medir el tiempo de en que inicio la creación del juego y restarselo al tiempo de las serpientes que mueren y así medir el tiempo más corto
que una serpiente vivio. El método `togglePause()` se encarga de medir lo anterior y de determinar también la serpiente más larga y los resultados los muestra haciendo uso de JOptionPane.
Por último, si la serpiente muere esta ya no será más pintada.

### 4) Robustez bajo carga

Al ejecutar el juego con 20 serpientes

![iniciar el juego](/img_1.png)

Luego todas las serpientes se crearon y conforma avanzaba el juego iban mueriendo

![desarrollo del juego](/img_2.png)

Finalmente, al pausar el juego se muestran las estádisticas solicitadas

![pausar el juego](/img.png)


---
## Créditos

Este laboratorio es una adaptación modernizada del ejercicio **SnakeRace** de ARSW. El enunciado de actividades se conserva para mantener los objetivos pedagógicos del curso.

**Base construida por el Ing. Javier Toquica.**
