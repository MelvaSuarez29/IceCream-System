package com.heladeria.controllers;

import com.heladeria.dao.SaborDAO;
import com.heladeria.dao.UsuarioDAO;
import com.heladeria.models.Sabor;
import com.heladeria.models.Usuario;
import com.heladeria.utils.PasswordUtil;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class AdminController {

    @FXML private TabPane tabPane;

    @FXML private TableView<Sabor> saboresTable;
    @FXML private TableColumn<Sabor, String> colSaborNombre;
    @FXML private TableColumn<Sabor, String> colSaborDesc;
    @FXML private TableColumn<Sabor, Double> colSaborPrecio;
    @FXML private TableColumn<Sabor, Integer> colSaborStock;
    @FXML private TableColumn<Sabor, String> colSaborActivo;
    @FXML private Button addSaborBtn;
    @FXML private Button editSaborBtn;
    @FXML private Button toggleSaborBtn;
    @FXML private Button deleteSaborBtn;

    @FXML private TableView<Sabor> stockTable;
    @FXML private TableColumn<Sabor, String> colStockNombre;
    @FXML private TableColumn<Sabor, Integer> colStockActual;
    @FXML private TextField stockCantidadField;
    @FXML private Button updateStockBtn;

    @FXML private TableView<Usuario> vendedoresTable;
    @FXML private TableColumn<Usuario, String> colVendNombre;
    @FXML private TableColumn<Usuario, String> colVendEmail;
    @FXML private TableColumn<Usuario, String> colVendActivo;
    @FXML private Button toggleVendedorBtn;
    @FXML private Button deleteVendedorBtn;

    @FXML private Button logoutButton;

    private Stage stage;
    private ObservableList<Sabor> saboresList = FXCollections.observableArrayList();
    private ObservableList<Usuario> vendedoresList = FXCollections.observableArrayList();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        setupSaboresTable();
        setupStockTable();
        setupVendedoresTable();

        logoutButton.setOnAction(e -> handleLogout());
        addSaborBtn.setOnAction(e -> showSaborForm(null));
        editSaborBtn.setOnAction(e -> {
            Sabor selected = saboresTable.getSelectionModel().getSelectedItem();
            if (selected != null) showSaborForm(selected);
            else showAlert("Seleccione un sabor");
        });
        toggleSaborBtn.setOnAction(e -> toggleSabor());
        deleteSaborBtn.setOnAction(e -> deleteSabor());
        updateStockBtn.setOnAction(e -> updateStock());

        toggleVendedorBtn.setOnAction(e -> toggleVendedor());
        deleteVendedorBtn.setOnAction(e -> deleteVendedor());

        loadData();
    }

    private void setupSaboresTable() {
        colSaborNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colSaborDesc.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getDescripcion()));
        colSaborPrecio.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrecio()).asObject());
        colSaborStock.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getStock()).asObject());
        colSaborActivo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getActivo() ? "Sí" : "No"));
        saboresTable.setItems(saboresList);
    }

    private void setupStockTable() {
        colStockNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colStockActual.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getStock()).asObject());
        stockTable.setItems(saboresList);
    }

    private void setupVendedoresTable() {
        colVendNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colVendEmail.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getEmail()));
        colVendActivo.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getActivo() ? "Activo" : "Inactivo"));
        vendedoresTable.setItems(vendedoresList);
    }

    private void loadData() {
        new Thread(() -> {
            try {
                List<Sabor> sabores = SaborDAO.getAll();
                List<Usuario> vendedores = UsuarioDAO.getAllByRol("vendedor");
                Platform.runLater(() -> {
                    saboresList.setAll(sabores);
                    vendedoresList.setAll(vendedores);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error al cargar datos: " + e.getMessage()));
            }
        }).start();
    }

    private void showSaborForm(Sabor sabor) {
        Dialog<Sabor> dialog = new Dialog<>();
        dialog.setTitle(sabor == null ? "Nuevo Sabor" : "Editar Sabor");

        TextField nombreField = new TextField(sabor != null ? sabor.getNombre() : "");
        TextField descField = new TextField(sabor != null ? sabor.getDescripcion() : "");
        TextField precioField = new TextField(sabor != null ? String.valueOf(sabor.getPrecio()) : "");
        TextField stockField = new TextField(sabor != null ? String.valueOf(sabor.getStock()) : "0");

        dialog.getDialogPane().setContent(new javafx.scene.layout.VBox(10,
                new Label("Nombre:"), nombreField,
                new Label("Descripción:"), descField,
                new Label("Precio:"), precioField,
                new Label("Stock inicial (solo para nuevo):"), stockField
        ));

        ButtonType saveBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn == saveBtn) {
                try {
                    Sabor result = sabor != null ? sabor : new Sabor();
                    result.setNombre(nombreField.getText());
                    result.setDescripcion(descField.getText());
                    result.setPrecio(Double.parseDouble(precioField.getText()));
                    if (sabor == null) result.setStock(Integer.parseInt(stockField.getText()));
                    return result;
                } catch (NumberFormatException e) {
                    showAlert("Precio y stock deben ser números válidos");
                    return null;
                }
            }
            return null;
        });

        Optional<Sabor> result = dialog.showAndWait();
        result.ifPresent(s -> {
            new Thread(() -> {
                try {
                    if (sabor == null) {
                        SaborDAO.create(s);
                    } else {
                        SaborDAO.update(sabor.getId(), s);
                    }
                    Platform.runLater(this::loadData);
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Error al guardar sabor: " + e.getMessage()));
                }
            }).start();
        });
    }

    private void toggleSabor() {
        Sabor selected = saboresTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Seleccione un sabor"); return; }

        new Thread(() -> {
            try {
                SaborDAO.updateField(selected.getId(), "activo", !selected.getActivo());
                Platform.runLater(this::loadData);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void deleteSabor() {
        Sabor selected = saboresTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Seleccione un sabor"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar sabor '" + selected.getNombre() + "'?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    SaborDAO.delete(selected.getId());
                    Platform.runLater(this::loadData);
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Error al eliminar: " + e.getMessage()));
                }
            }).start();
        }
    }

    private void updateStock() {
        Sabor selected = stockTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Seleccione un sabor"); return; }

        String input = stockCantidadField.getText().trim();
        if (input.isEmpty()) { showAlert("Ingrese una cantidad"); return; }

        try {
            int newStock = Integer.parseInt(input);
            new Thread(() -> {
                try {
                    SaborDAO.updateStock(selected.getId(), newStock);
                    Platform.runLater(() -> {
                        loadData();
                        stockCantidadField.clear();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Error al actualizar stock: " + e.getMessage()));
                }
            }).start();
        } catch (NumberFormatException e) {
            showAlert("Cantidad inválida");
        }
    }

    private void toggleVendedor() {
        Usuario selected = vendedoresTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Seleccione un vendedor"); return; }

        new Thread(() -> {
            try {
                UsuarioDAO.updateField(selected.getId(), "activo", !selected.getActivo());
                Platform.runLater(this::loadData);
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error: " + e.getMessage()));
            }
        }).start();
    }

    private void deleteVendedor() {
        Usuario selected = vendedoresTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Seleccione un vendedor"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar vendedor '" + selected.getNombre() + "'?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    UsuarioDAO.delete(selected.getId());
                    Platform.runLater(this::loadData);
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Error al eliminar: " + e.getMessage()));
                }
            }).start();
        }
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
