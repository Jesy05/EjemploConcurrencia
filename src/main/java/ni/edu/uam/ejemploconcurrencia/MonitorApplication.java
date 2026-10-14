package ni.edu.uam.ejemploconcurrencia;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MonitorApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MonitorApplication.class.getResource("monitor-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(MonitorApplication.class.getResource("monitor.css").toExternalForm());
        stage.getIcons().add(new javafx.scene.image.Image(
                MonitorApplication.class.getResourceAsStream("/icons/chrome.png")));
        stage.setTitle("Descargas");
        stage.setScene(scene);
        stage.show();
    }
}
