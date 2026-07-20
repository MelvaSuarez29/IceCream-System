module com.heladeria {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;
    requires bcrypt;
    requires java.net.http;

    exports com.heladeria to javafx.graphics;
    opens com.heladeria to javafx.fxml;
    opens com.heladeria.controllers to javafx.fxml;
    opens com.heladeria.models to com.google.gson;
}
