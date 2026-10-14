module ni.edu.uam.ejemploconcurrencia {
    requires javafx.controls;
    requires javafx.fxml;


    opens ni.edu.uam.ejemploconcurrencia to javafx.fxml;
    exports ni.edu.uam.ejemploconcurrencia;
}