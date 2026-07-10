module com.proyecto.proyecto_final {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.proyecto.proyecto_final to javafx.fxml;
    exports com.proyecto.proyecto_final;
}