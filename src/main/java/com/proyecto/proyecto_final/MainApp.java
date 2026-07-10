package com.proyecto.proyecto_final;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/proyecto/proyecto_final/login.fxml"));
        Scene scene = new Scene(loader.load(), 450, 400);

        com.proyecto.proyecto_final.controllers.LoginController controller = loader.getController();

        stage.setTitle("Heladería - Inicio de Sesión");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
}
