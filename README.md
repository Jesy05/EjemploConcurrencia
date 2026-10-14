# Sesión 25 · Investigación — Tareas concurrentes y Monitor de tareas

**Materia:** Programación de Aplicaciones de Escritorio

**Equipo**
- Gabriela Suyen Espinoza Rodríguez
- Fabiola Sarai Lanuza Paz
- Jesy Nicole González Jarquín
- José Cristo Carvallo Herrera

---

## 1. Tres aplicaciones que usan tareas concurrentes

| # | Aplicación | Qué hace en paralelo |
|---|------------|----------------------|
| 1 | **Microsoft Word** | El usuario escribe mientras la app autoguarda y revisa la ortografía |
| 2 | **Navegador (Chrome)** | Descarga archivos grandes mientras se sigue navegando y viendo video |
| 3 | **Antivirus (Windows Defender)** | Escanea el disco sin impedir que se use la computadora |

## 2. ¿Qué operación ocurriría en segundo plano?

| Aplicación | Primer plano (lo que ve el usuario) | Segundo plano (hilo aparte) | Por qué |
|------------|-------------------------------------|-----------------------------|---------|
| Word | Escribir y dar formato | Autoguardado periódico y corrector ortográfico | Guardar en disco es lento y no debe interrumpir la escritura |
| Chrome | Navegar y leer páginas | Descargar un archivo y actualizar su progreso | La red es lenta e impredecible; la pestaña debe seguir respondiendo |
| Windows Defender | Trabajar con otros programas | Revisar miles de archivos y comparar firmas de malware | El análisis completo tarda minutos u horas |

En los tres casos el patrón es el mismo: **iniciar → reportar estado → poder detener**.

## 3. Boceto: interfaz “Monitor de tareas” (Iniciar, Estado y Detener)

```
┌───────────────────────────────────────────────────────────────────────┐
│ [Chrome]  Descargas        [Nueva descarga] [Iniciar todas] [Cancelar]│
├───────────────────────────────────────────────────────────────────────┤
│ ┌───────────────────────────────────────────────────────────────────┐ │
│ │ [PDF]  informe_final.pdf                                          │ │
│ │        ███████████████████████████████████████████░░░  (Estado)   │ │
│ │        Completada · 12 MB                                         │ │
│ └───────────────────────────────────────────────────────────────────┘ │
│ ┌───────────────────────────────────────────────────────────────────┐ │
│ │ [MP4]  tutorial_javafx.mp4                          [Cancelar]    │ │
│ │        ██████████████░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░               │ │
│ │        63.2 MB de 180 MB · 3.1 MB/s · 38 s restantes   (Estado)   │ │
│ └───────────────────────────────────────────────────────────────────┘ │
│ ┌───────────────────────────────────────────────────────────────────┐ │
│ │ [EXE]  instalador_jdk21.exe                         [Reintentar]  │ │
│ │        Cancelada                                       (Estado)   │ │
│ └───────────────────────────────────────────────────────────────────┘ │
├───────────────────────────────────────────────────────────────────────┤
│ Descargando en segundo plano… la ventana sigue respondiendo.          │
└───────────────────────────────────────────────────────────────────────┘
```

| Control | Función |
|---------|---------|
| **Iniciar todas** / **Iniciar** | Lanza cada tarea en un hilo de fondo |
| **Estado** | Barra de progreso y texto: En espera, En ejecución (MB, velocidad, tiempo restante), Completada o Cancelada |
| **Cancelar** / **Detener** | Cancela la tarea seleccionada (o todas si no hay selección) |

## 4. Explicación breve

Una operación larga no se debe ejecutar en el hilo de la interfaz, porque la ventana se congelaría
(“No responde”). Por eso cada tarea corre en un **hilo secundario** y va reportando su avance a la UI.
La ventana nunca se bloquea y el usuario puede iniciar, ver el estado o detener cualquier tarea.

En JavaFX se implementa con:

- `Task<Void>`: representa el trabajo en segundo plano.
- `updateProgress(...)` + `progressProperty().bind(...)`: la barra se actualiza sola.
- `ExecutorService` con hilos *daemon*: ejecuta las tareas sin impedir cerrar la app.
- `task.cancel()` + `isCancelled()`: detiene la tarea de forma ordenada.

## 5. Implementación en este repositorio

La app JavaFX **Descargas** simula varias descargas simultáneas como en Chrome.

| Archivo | Rol |
|---------|-----|
| `TareaItem.java` | Tarea de fondo (`Task`) con progreso y estado |
| `MonitorController.java` | Botones y lista de descargas |
| `monitor-view.fxml` | Vista |
| `monitor.css` | Estilo tipo Chrome |

Ejecutar: `Launcher.main` en IntelliJ o `./mvnw javafx:run`.
