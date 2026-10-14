# Sesión 25 – Tareas concurrentes y Monitor de tareas

**Materia:** Programación de Aplicaciones de Escritorio

**Equipo**
- Gabriela Suyen Espinoza Rodríguez
- Fabiola Sarai Lanuza Paz
- Jesy Nicole González Jarquín
- José Cristo Carvallo Herrera

## 1. Tres aplicaciones con tareas concurrentes

| Aplicación | Operación en segundo plano |
|---|---|
| Microsoft Word | Autoguardado del documento y revisión ortográfica mientras el usuario escribe |
| Navegador (Chrome) | Descarga de archivos grandes mientras se sigue navegando |
| Windows Defender | Escaneo de archivos del disco sin bloquear el uso del equipo |

## 2. Boceto: Monitor de tareas

El boceto interactivo está en [`index.html`](index.html) (diapositiva 5). Incluye:

- **Iniciar:** lanza las tareas en hilos de fondo.
- **Estado:** tabla con progreso y estado de cada tarea (En espera, En ejecución, Finalizada, Detenida).
- **Detener:** cancela la tarea seleccionada (o todas si no hay selección).

## 3. Explicación breve

Las operaciones largas no deben ejecutarse en el hilo de la interfaz, porque la ventana se congelaría.
Se ejecutan en un hilo secundario y reportan su avance a la UI. En JavaFX esto se logra con `Task`,
`Thread` y `progressProperty().bind(...)`; la cancelación se pide con `task.cancel()`.

## Cómo presentar

Abrir `index.html` en el navegador. Navegar con las flechas ← → o los botones inferiores.
