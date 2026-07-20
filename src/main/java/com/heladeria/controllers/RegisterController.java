package com.heladeria.controllers;

import com.heladeria.dao.UsuarioDAO;
import com.heladeria.models.Usuario;
import com.heladeria.utils.PasswordUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class RegisterController {

    @FXML private TextField nombreField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button registerButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        registerButton.setOnAction(e -> handleRegister());
        cancelButton.setOnAction(e -> goBackToLogin());
    }

    private void handleRegister() {
        String nombre = nombreField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Todos los campos son obligatorios");
            return;
        }

        if (password.length() < 6) {
            showError("La contraseña debe tener al menos 6 caracteres");
            return;
        }

        if (!password.equals(confirm)) {
            showError("Las contraseñas no coinciden");
            return;
        }

        registerButton.setDisable(true);
        errorLabel.setVisible(false);

        new Thread(() -> {
            try {
                Usuario existing = UsuarioDAO.getByEmail(email);
                if (existing != null) {
                    Platform.runLater(() -> {
                        showError("El email ya está registrado");
                        registerButton.setDisable(false);
                    });
                    return;
                }

                String hash = PasswordUtil.hash(password);
                Usuario nuevo = new Usuario(nombre, email, hash, "vendedor");
                UsuarioDAO.create(nuevo);

                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Registro Exitoso");
                    alert.setHeaderText("Vendedor registrado correctamente");
                    alert.setContentText("Ya puedes iniciar sesión con tus credenciales.");
                    alert.showAndWait();
                    goBackToLogin();
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showError("Error al registrar: " + ex.getMessage());
                    registerButton.setDisable(false);
                });
            }
        }).start();
    }

    private void goBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/heladeria/views/login.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            controller.setStage(stage);

            Scene scene = new Scene(root, 450, 400);
            stage.setScene(scene);
            stage.setTitle("Heladería - Inicio de Sesión");
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Error al volver al login: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
