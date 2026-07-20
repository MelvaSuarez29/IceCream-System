package com.heladeria;

import com.heladeria.utils.SetupUtil;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        try {
            SetupUtil.seedAdmin();
        } catch (Exception e) {
            System.err.println("No se pudo verificar/crear admin: " + e.getMessage());
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/heladeria/views/login.fxml"));
        Scene scene = new Scene(loader.load(), 450, 400);

        com.heladeria.controllers.LoginController controller = loader.getController();
        controller.setStage(stage);

        stage.setTitle("Heladería - Inicio de Sesión");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.show();
    }
}
