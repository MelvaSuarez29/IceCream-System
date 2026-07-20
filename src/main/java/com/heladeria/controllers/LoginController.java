package com.heladeria.controllers;

import com.heladeria.dao.UsuarioDAO;
import com.heladeria.models.Usuario;
import com.heladeria.utils.SessionManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button registerButton;
    @FXML private Label errorLabel;

    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
        loginButton.setOnAction(e -> handleLogin());
        registerButton.setOnAction(e -> openRegister());

        emailField.setOnAction(e -> passwordField.requestFocus());
        passwordField.setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Por favor ingrese email y contraseña");
            return;
        }

        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        new Thread(() -> {
            try {
                Usuario user = UsuarioDAO.authenticate(email, password);
                if (user == null) {
                    Platform.runLater(() -> {
                        showError("Credenciales inválidas o usuario inactivo");
                        loginButton.setDisable(false);
                    });
                    return;
                }

                SessionManager.getInstance().login(user);

                Platform.runLater(() -> {
                    loginButton.setDisable(false);
                    navigateToDashboard(user);
                });
            } catch (Exception ex) {
                Platform.runLater(() -> {
                    showError("Error de conexión: " + ex.getMessage());
                    loginButton.setDisable(false);
                });
            }
        }).start();
    }

    private void navigateToDashboard(Usuario user) {
        try {
            String fxmlFile;
            switch (user.getRol()) {
                case "admin":
                    fxmlFile = "/com/heladeria/views/admin_dashboard.fxml";
                    break;
                case "vendedor":
                    fxmlFile = "/com/heladeria/views/vendedor_dashboard.fxml";
                    break;
                case "cliente":
                    fxmlFile = "/com/heladeria/views/cliente_dashboard.fxml";
                    break;
                default:
                    showError("Rol desconocido: " + user.getRol());
                    return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof AdminController) {
                ((AdminController) controller).setStage(stage);
            } else if (controller instanceof VendedorController) {
                ((VendedorController) controller).setStage(stage);
            } else if (controller instanceof ClienteController) {
                ((ClienteController) controller).setStage(stage);
            }

            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setTitle("Heladería - " + user.getNombre() + " (" + user.getRol() + ")");
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Error al cargar el dashboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void openRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/heladeria/views/register.fxml"));
            Parent root = loader.load();

            RegisterController controller = loader.getController();
            controller.setStage(stage);

            Scene scene = new Scene(root, 500, 500);
            stage.setScene(scene);
            stage.setTitle("Registro de Vendedor");
            stage.centerOnScreen();
        } catch (Exception e) {
            showError("Error al abrir registro: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
