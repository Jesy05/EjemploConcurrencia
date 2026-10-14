package ni.edu.uam.ejemploconcurrencia;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;

import java.util.concurrent.Executor;

/** Una "descarga" simulada que corre en un hilo de fondo y expone progreso y estado a la UI. */
public class TareaItem {
    public enum Estado { EN_ESPERA, EN_EJECUCION, FINALIZADA, DETENIDA }

    private final int id;
    private final String nombre;
    private final int tamanoMb;
    private final long msPorPaso;
    private final DoubleProperty progreso = new SimpleDoubleProperty(0);
    private final StringProperty detalle = new SimpleStringProperty();
    private final ObjectProperty<Estado> estado = new SimpleObjectProperty<>(Estado.EN_ESPERA);
    private Task<Void> task;

    public TareaItem(int id, String nombre, int tamanoMb, long msPorPaso) {
        this.id = id;
        this.nombre = nombre;
        this.tamanoMb = tamanoMb;
        this.msPorPaso = msPorPaso;
        progreso.addListener((o, a, b) -> actualizarDetalle());
        estado.addListener((o, a, b) -> actualizarDetalle());
        actualizarDetalle();
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public Estado getEstado() { return estado.get(); }
    public ObjectProperty<Estado> estadoProperty() { return estado; }
    public DoubleProperty progresoProperty() { return progreso; }
    public StringProperty detalleProperty() { return detalle; }

    public boolean estaEjecutando() { return estado.get() == Estado.EN_EJECUCION; }

    private void actualizarDetalle() {
        double hecho = tamanoMb * progreso.get();
        double velocidad = 1000.0 / msPorPaso * tamanoMb / 100.0;   // MB/s
        detalle.set(switch (estado.get()) {
            case EN_ESPERA -> "En espera…";
            case EN_EJECUCION -> String.format("%.1f MB de %d MB · %.1f MB/s · %d s restantes",
                    hecho, tamanoMb, velocidad,
                    Math.max(1, Math.round((tamanoMb - hecho) / velocidad)));
            case FINALIZADA -> "Completada · " + tamanoMb + " MB";
            case DETENIDA -> "Cancelada";
        });
    }

    public void iniciar(Executor executor) {
        if (estado.get() == Estado.EN_EJECUCION || estado.get() == Estado.FINALIZADA) return;
        progreso.unbind();
        progreso.set(0);

        task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                final int pasos = 100;
                for (int i = 1; i <= pasos; i++) {
                    if (isCancelled()) return null;
                    Thread.sleep(msPorPaso);          // trabajo simulado
                    updateProgress(i, pasos);
                }
                return null;
            }
        };
        progreso.bind(task.progressProperty());
        estado.set(Estado.EN_EJECUCION);
        task.setOnSucceeded(e -> estado.set(Estado.FINALIZADA));
        task.setOnCancelled(e -> {
            progreso.unbind();
            estado.set(Estado.DETENIDA);
        });
        executor.execute(task);
    }

    public void detener() {
        if (estaEjecutando()) task.cancel();
    }
}
