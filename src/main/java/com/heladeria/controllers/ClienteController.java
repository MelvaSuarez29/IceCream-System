package com.heladeria.controllers;

import com.heladeria.dao.DetalleNotaVentaDAO;
import com.heladeria.dao.NotaVentaDAO;
import com.heladeria.models.DetalleNotaVenta;
import com.heladeria.models.NotaVenta;
import com.heladeria.models.Usuario;
import com.heladeria.utils.SessionManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

public class ClienteController {

    @FXML private Label welcomeLabel;
    @FXML private TableView<NotaVenta> facturasTable;
    @FXML private TableColumn<NotaVenta, String> colFactFecha;
    @FXML private TableColumn<NotaVenta, Double> colFactTotal;

    @FXML private TableView<DetalleNotaVenta> detalleTable;
    @FXML private TableColumn<DetalleNotaVenta, String> colDetSabor;
    @FXML private TableColumn<DetalleNotaVenta, Integer> colDetCantidad;
    @FXML private TableColumn<DetalleNotaVenta, Double> colDetSubtotal;

    @FXML private Button logoutButton;
    @FXML private Button descargarBtn;

    private Stage stage;
    private ObservableList<NotaVenta> facturasList = FXCollections.observableArrayList();
    private ObservableList<DetalleNotaVenta> detalleList = FXCollections.observableArrayList();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        Usuario user = SessionManager.getInstance().getUsuarioActual();
        if (user != null) {
            welcomeLabel.setText("Bienvenido, " + user.getNombre());
        }

        colFactFecha.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFecha()));
        colFactTotal.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());
        facturasTable.setItems(facturasList);

        colDetSabor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSaborNombre()));
        colDetCantidad.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCantidad()).asObject());
        colDetSubtotal.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getSubtotal()).asObject());
        detalleTable.setItems(detalleList);

        logoutButton.setOnAction(e -> handleLogout());
        descargarBtn.setOnAction(e -> descargarNota());

        facturasTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nota) -> {
            descargarBtn.setDisable(nota == null);
            if (nota != null) loadDetalle(nota);
        });

        loadNotas();
    }

    private void loadNotas() {
        new Thread(() -> {
            try {
                String clienteId = SessionManager.getInstance().getUsuarioActual().getId();
                List<NotaVenta> notas = NotaVentaDAO.getByCliente(clienteId);
                Platform.runLater(() -> facturasList.setAll(notas));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error al cargar notas de venta: " + e.getMessage()));
            }
        }).start();
    }

    private void loadDetalle(NotaVenta nota) {
        new Thread(() -> {
            try {
                List<DetalleNotaVenta> detalles = DetalleNotaVentaDAO.getByNotaVentaId(nota.getId());
                Platform.runLater(() -> detalleList.setAll(detalles));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error al cargar detalle: " + e.getMessage()));
            }
        }).start();
    }

    private void descargarNota() {
        NotaVenta nota = facturasTable.getSelectionModel().getSelectedItem();
        if (nota == null || detalleList.isEmpty()) return;

        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar Nota de Venta");
        fc.setInitialFileName("nota_" + nota.getId().substring(0, 8) + ".txt");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivo de texto", "*.txt"));
        File file = fc.showSaveDialog(stage);
        if (file == null) return;

        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("          NOTA DE VENTA\n");
        sb.append("========================================\n");
        sb.append("ID:      #").append(nota.getId().substring(0, 8)).append("\n");
        sb.append("Fecha:   ").append(nota.getFecha()).append("\n");
        sb.append("Cliente: ").append(nota.getClienteNombre()).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("DETALLE\n");
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-20s %6s %10s\n", "Sabor", "Cant.", "Subtotal"));
        for (DetalleNotaVenta d : detalleList) {
            sb.append(String.format("%-20s %6d %10.2f\n",
                    d.getSaborNombre(), d.getCantidad(), d.getSubtotal()));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("%-27s %10.2f\n", "TOTAL:", nota.getTotal()));
        sb.append("========================================\n");

        new Thread(() -> {
            try {
                Files.writeString(file.toPath(), sb.toString());
                Platform.runLater(() -> showAlert("Nota descargada exitosamente"));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error al guardar: " + e.getMessage()));
            }
        }).start();
    }

    private void handleLogout() {
        SessionManager.getInstance().logout();
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
            e.printStackTrace();
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg);
        alert.showAndWait();
    }
}
