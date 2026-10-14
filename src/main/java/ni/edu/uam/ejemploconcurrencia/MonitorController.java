package ni.edu.uam.ejemploconcurrencia;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MonitorController {
    private static final String[] NOMBRES = {
            "informe_final.pdf", "tutorial_javafx.mp4", "instalador_jdk21.exe",
            "presentacion_sesion25.pptx", "fotos_equipo.zip", "base_de_datos.sql"
    };
    private static final int[] TAMANOS_MB = {12, 180, 160, 35, 90, 48};

    @FXML private ListView<TareaItem> lista;
    @FXML private Label lblEstado;
    @FXML private javafx.scene.image.ImageView logo;

    private final ObservableList<TareaItem> tareas = FXCollections.observableArrayList();
    private final ExecutorService executor = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);   // no impide cerrar la aplicación
        return t;
    });
    private int contador = 0;

    @FXML
    private void initialize() {
        logo.setImage(new javafx.scene.image.Image(
                MonitorController.class.getResourceAsStream("/icons/chrome.png")));
        lista.setItems(tareas);
        lista.setCellFactory(lv -> new DescargaCell());
        lista.setPlaceholder(new Label("No hay descargas"));
        for (int i = 0; i < 4; i++) onNueva();
    }

    @FXML
    protected void onNueva() {
        int k = contador % NOMBRES.length;
        contador++;
        // tamaño y velocidad distintos para ver varias descargas a diferente ritmo
        tareas.add(new TareaItem(contador, NOMBRES[k], TAMANOS_MB[k], 30 + (k * 23) % 60));
        lblEstado.setText(tareas.size() + " descargas en la lista.");
    }

    @FXML
    protected void onIniciar() {
        tareas.forEach(t -> t.iniciar(executor));
        lblEstado.setText("Descargando en segundo plano… la ventana sigue respondiendo.");
    }

    @FXML
    protected void onDetener() {
        TareaItem sel = lista.getSelectionModel().getSelectedItem();
        if (sel != null) {
            sel.detener();
            lblEstado.setText("Descarga cancelada: " + sel.getNombre());
        } else {
            tareas.forEach(TareaItem::detener);
            lblEstado.setText("Todas las descargas canceladas.");
        }
    }

    /** Fila estilo "Descargas" de Chrome. */
    private class DescargaCell extends ListCell<TareaItem> {
        private final Label ext = new Label();
        private final StackPane icono = new StackPane(ext);
        private final Label nombre = new Label();
        private final Label detalle = new Label();
        private final ProgressBar barra = new ProgressBar(0);
        private final Button accion = new Button();
        private final HBox card;
        private TareaItem actual;

        DescargaCell() {
            icono.getStyleClass().add("file-icon");
            ext.getStyleClass().add("file-icon-text");
            nombre.getStyleClass().add("file-name");
            detalle.getStyleClass().add("file-detail");
            accion.getStyleClass().add("link");
            barra.setMaxWidth(Double.MAX_VALUE);
            barra.setPrefHeight(6);

            VBox centro = new VBox(6, nombre, barra, detalle);
            HBox.setHgrow(centro, Priority.ALWAYS);
            card = new HBox(14, icono, centro, accion);
            card.setAlignment(Pos.CENTER_LEFT);
            card.getStyleClass().add("card");
            card.setPadding(new Insets(14, 16, 14, 16));

            accion.setOnAction(e -> {
                if (actual == null) return;
                if (actual.estaEjecutando()) actual.detener();
                else actual.iniciar(executor);
            });

            // las celdas se reutilizan: un solo listener que mira el item actual
            estadoListener = (o, a, b) -> refrescar();
        }

        private final javafx.beans.value.ChangeListener<TareaItem.Estado> estadoListener;

        @Override
        protected void updateItem(TareaItem item, boolean empty) {
            super.updateItem(item, empty);
            if (actual != null) actual.estadoProperty().removeListener(estadoListener);
            barra.progressProperty().unbind();
            detalle.textProperty().unbind();

            if (empty || item == null) {
                actual = null;
                setGraphic(null);
                return;
            }
            actual = item;
            String n = item.getNombre();
            ext.setText(n.substring(n.lastIndexOf('.') + 1).toUpperCase());
            nombre.setText(n);
            barra.progressProperty().bind(item.progresoProperty());
            detalle.textProperty().bind(item.detalleProperty());
            item.estadoProperty().addListener(estadoListener);
            refrescar();
            setGraphic(card);
        }

        private void refrescar() {
            if (actual == null) return;
            detalle.getStyleClass().removeAll("done", "cancelled");
            switch (actual.getEstado()) {
                case EN_EJECUCION -> { accion.setText("Cancelar"); accion.setVisible(true); }
                case EN_ESPERA -> { accion.setText("Iniciar"); accion.setVisible(true); }
                case DETENIDA -> {
                    accion.setText("Reintentar");
                    accion.setVisible(true);
                    detalle.getStyleClass().add("cancelled");
                }
                case FINALIZADA -> {
                    accion.setVisible(false);
                    detalle.getStyleClass().add("done");
                }
            }
        }
    }
}
