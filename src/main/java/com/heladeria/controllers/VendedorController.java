package com.heladeria.controllers;

import com.heladeria.dao.DetalleNotaVentaDAO;
import com.heladeria.dao.NotaVentaDAO;
import com.heladeria.dao.SaborDAO;
import com.heladeria.dao.UsuarioDAO;
import com.heladeria.models.*;
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
import javafx.stage.Stage;

import java.util.List;

public class VendedorController {

    @FXML private TableView<Sabor> saboresTable;
    @FXML private TableColumn<Sabor, String> colSaborNombre;
    @FXML private TableColumn<Sabor, Double> colSaborPrecio;
    @FXML private TableColumn<Sabor, Integer> colSaborStock;

    @FXML private TableView<NotaVenta> facturasTable;
    @FXML private TableColumn<NotaVenta, String> colFactFecha;
    @FXML private TableColumn<NotaVenta, String> colFactCliente;
    @FXML private TableColumn<NotaVenta, Double> colFactTotal;

    @FXML private TableView<DetalleNotaVenta> detalleTable;
    @FXML private TableColumn<DetalleNotaVenta, String> colDetSabor;
    @FXML private TableColumn<DetalleNotaVenta, Integer> colDetCantidad;
    @FXML private TableColumn<DetalleNotaVenta, Double> colDetSubtotal;

    @FXML private Button createFacturaBtn;
    @FXML private Button editFacturaBtn;
    @FXML private Button deleteFacturaBtn;
    @FXML private Button logoutButton;

    @FXML private Label clienteInfoLabel;

    private Stage stage;
    private ObservableList<Sabor> saboresList = FXCollections.observableArrayList();
    private ObservableList<NotaVenta> facturasList = FXCollections.observableArrayList();
    private ObservableList<DetalleNotaVenta> detalleList = FXCollections.observableArrayList();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {
        colSaborNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNombre()));
        colSaborPrecio.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrecio()).asObject());
        colSaborStock.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getStock()).asObject());
        saboresTable.setItems(saboresList);

        colFactFecha.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFecha()));
        colFactCliente.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getClienteNombre()));
        colFactTotal.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());
        facturasTable.setItems(facturasList);

        colDetSabor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSaborNombre()));
        colDetCantidad.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCantidad()).asObject());
        colDetSubtotal.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getSubtotal()).asObject());
        detalleTable.setItems(detalleList);

        logoutButton.setOnAction(e -> handleLogout());
        createFacturaBtn.setOnAction(e -> openNotaVentaForm(null));
        editFacturaBtn.setOnAction(e -> {
            NotaVenta selected = facturasTable.getSelectionModel().getSelectedItem();
            if (selected != null) openNotaVentaForm(selected);
            else showAlert("Seleccione una nota de venta");
        });
        deleteFacturaBtn.setOnAction(e -> anularNotaVenta());

        facturasTable.getSelectionModel().selectedItemProperty().addListener((obs, old, nota) -> {
            if (nota != null) loadDetalle(nota);
        });

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                List<Sabor> sabores = SaborDAO.getAllActivos();
                String vendedorId = SessionManager.getInstance().getUsuarioActual().getId();
                List<NotaVenta> notas = NotaVentaDAO.getByVendedor(vendedorId);
                Platform.runLater(() -> {
                    saboresList.setAll(sabores);
                    facturasList.setAll(notas);
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error al cargar datos: " + e.getMessage()));
            }
        }).start();
    }

    private void loadDetalle(NotaVenta nota) {
        clienteInfoLabel.setText("Cliente: " + (nota.getClienteNombre() != null ? nota.getClienteNombre() : "N/A"));
        new Thread(() -> {
            try {
                List<DetalleNotaVenta> detalles = DetalleNotaVentaDAO.getByNotaVentaId(nota.getId());
                Platform.runLater(() -> detalleList.setAll(detalles));
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error al cargar detalle: " + e.getMessage()));
            }
        }).start();
    }

    private void openNotaVentaForm(NotaVenta nota) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/heladeria/views/nota_venta_form.fxml"));
            Parent root = loader.load();

            NotaVentaFormController controller = loader.getController();
            controller.setStage(stage);
            controller.setVendedorController(this);
            if (nota != null) controller.setNotaVenta(nota);

            Scene scene = new Scene(root, 700, 600);
            stage.setScene(scene);
            stage.setTitle(nota == null ? "Nueva Nota de Venta" : "Editar Nota de Venta");
            stage.centerOnScreen();
        } catch (Exception e) {
            showAlert("Error al abrir formulario: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void anularNotaVenta() {
        NotaVenta selected = facturasTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Seleccione una nota de venta"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Anular nota de venta #" + selected.getId().substring(0, 8) + "?");
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            new Thread(() -> {
                try {
                    List<DetalleNotaVenta> detalles = DetalleNotaVentaDAO.getByNotaVentaId(selected.getId());

                    for (DetalleNotaVenta d : detalles) {
                        SaborDAO.adjustStock(d.getSaborId(), d.getCantidad());
                    }

                    NotaVentaDAO.anular(selected.getId());

                    String clienteId = selected.getClienteId();
                    if (clienteId != null) {
                        long count = NotaVentaDAO.countActivasByCliente(clienteId);
                        if (count == 0) {
                            UsuarioDAO.updateField(clienteId, "activo", false);
                        }
                    }

                    Platform.runLater(() -> {
                        loadData();
                        detalleList.clear();
                        clienteInfoLabel.setText("");
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> showAlert("Error al anular nota de venta: " + e.getMessage()));
                }
            }).start();
        }
    }

    public void refreshData() {
        loadData();
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
