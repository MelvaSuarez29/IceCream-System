package com.heladeria.controllers;

import com.heladeria.dao.*;
import com.heladeria.models.*;
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
import javafx.stage.Stage;

import java.util.*;

public class NotaVentaFormController {

    @FXML private TextField clienteNombreField;
    @FXML private ComboBox<Usuario> clienteCombo;
    @FXML private CheckBox nuevoClienteCheck;

    @FXML private ComboBox<Sabor> saborCombo;
    @FXML private TextField cantidadField;
    @FXML private Button addDetalleBtn;

    @FXML private TableView<DetalleNotaVenta> detalleTable;
    @FXML private TableColumn<DetalleNotaVenta, String> colSabor;
    @FXML private TableColumn<DetalleNotaVenta, Integer> colCantidad;
    @FXML private TableColumn<DetalleNotaVenta, Double> colSubtotal;
    @FXML private Button removeDetalleBtn;

    @FXML private Label totalLabel;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Stage stage;
    private VendedorController vendedorController;
    private NotaVenta notaToEdit;
    private ObservableList<DetalleNotaVenta> detalleList = FXCollections.observableArrayList();
    private List<Sabor> saboresDisponibles = new ArrayList<>();
    private List<Usuario> clientesExistentes = new ArrayList<>();

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setVendedorController(VendedorController controller) {
        this.vendedorController = controller;
    }

    public void setNotaVenta(NotaVenta nota) {
        this.notaToEdit = nota;
    }

    @FXML
    private void initialize() {
        colSabor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getSaborNombre()));
        colCantidad.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getCantidad()).asObject());
        colSubtotal.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getSubtotal()).asObject());
        detalleTable.setItems(detalleList);

        nuevoClienteCheck.setOnAction(e -> {
            boolean nuevo = nuevoClienteCheck.isSelected();
            clienteCombo.setDisable(nuevo);
            clienteNombreField.setDisable(!nuevo);
            if (!nuevo) clienteNombreField.clear();
            if (nuevo) clienteCombo.getSelectionModel().clearSelection();
        });

        addDetalleBtn.setOnAction(e -> addDetalle());
        removeDetalleBtn.setOnAction(e -> {
            DetalleNotaVenta selected = detalleTable.getSelectionModel().getSelectedItem();
            if (selected != null) detalleList.remove(selected);
            updateTotal();
        });

        saveButton.setOnAction(e -> saveNotaVenta());
        cancelButton.setOnAction(e -> goBack());

        loadData();
    }

    private void loadData() {
        new Thread(() -> {
            try {
                saboresDisponibles = SaborDAO.getAllActivos();
                clientesExistentes = UsuarioDAO.getAllByRol("cliente");

                Platform.runLater(() -> {
                    saborCombo.getItems().setAll(saboresDisponibles);
                    clienteCombo.getItems().setAll(clientesExistentes);

                    if (notaToEdit != null) {
                        loadNotaToEdit();
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error al cargar datos: " + e.getMessage()));
            }
        }).start();
    }

    private void loadNotaToEdit() {
        new Thread(() -> {
            try {
                List<DetalleNotaVenta> detalles = DetalleNotaVentaDAO.getByNotaVentaId(notaToEdit.getId());
                Usuario cliente = UsuarioDAO.getById(notaToEdit.getClienteId());

                Platform.runLater(() -> {
                    if (cliente != null) {
                        clienteCombo.getSelectionModel().select(cliente);
                        clienteNombreField.setText(cliente.getNombre());
                    }
                    detalleList.setAll(detalles);
                    updateTotal();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showAlert("Error al cargar nota: " + e.getMessage()));
            }
        }).start();
    }

    private void addDetalle() {
        Sabor sabor = saborCombo.getSelectionModel().getSelectedItem();
        if (sabor == null) { showAlert("Seleccione un sabor"); return; }

        String cantText = cantidadField.getText().trim();
        if (cantText.isEmpty()) { showAlert("Ingrese una cantidad"); return; }

        try {
            int cantidad = Integer.parseInt(cantText);
            if (cantidad <= 0) { showAlert("La cantidad debe ser mayor a 0"); return; }

            if (cantidad > sabor.getStock() && notaToEdit == null) {
                showAlert("Stock insuficiente. Disponible: " + sabor.getStock());
                return;
            }

            double subtotal = cantidad * sabor.getPrecio();
            DetalleNotaVenta detalle = new DetalleNotaVenta();
            detalle.setSaborId(sabor.getId());
            detalle.setSaborNombre(sabor.getNombre());
            detalle.setCantidad(cantidad);
            detalle.setSubtotal(subtotal);

            detalleList.add(detalle);
            updateTotal();
            cantidadField.clear();
        } catch (NumberFormatException e) {
            showAlert("Cantidad inválida");
        }
    }

    private void updateTotal() {
        double total = detalleList.stream().mapToDouble(DetalleNotaVenta::getSubtotal).sum();
        totalLabel.setText(String.format("%.2f", total));
    }

    private void saveNotaVenta() {
        if (detalleList.isEmpty()) { showAlert("Agregue al menos un detalle"); return; }

        saveButton.setDisable(true);

        new Thread(() -> {
            try {
                String vendedorId = SessionManager.getInstance().getUsuarioActual().getId();
                String clienteId;
                String generatedPassword = null;

                if (nuevoClienteCheck.isSelected() || notaToEdit == null && clienteCombo.getSelectionModel().isEmpty()) {
                    String nombreCliente = clienteNombreField.getText().trim();
                    if (nombreCliente.isEmpty()) {
                        Platform.runLater(() -> {
                            showAlert("Ingrese el nombre del cliente");
                            saveButton.setDisable(false);
                        });
                        return;
                    }

                    String emailCliente = nombreCliente.toLowerCase().replaceAll("\\s+", "") + "@cliente.com";
                    Usuario existingCliente = UsuarioDAO.getByEmail(emailCliente);

                    if (existingCliente != null) {
                        clienteId = existingCliente.getId();
                        if (!existingCliente.getActivo()) {
                            UsuarioDAO.updateField(clienteId, "activo", true);
                        }
                    } else {
                        generatedPassword = PasswordUtil.generateRandomPassword(nombreCliente.replaceAll("\\s+", ""));
                        String hash = PasswordUtil.hash(generatedPassword);
                        Usuario nuevoCliente = new Usuario(nombreCliente, emailCliente, hash, "cliente");
                        Usuario created = UsuarioDAO.create(nuevoCliente);
                        clienteId = created.getId();
                    }
                } else {
                    Usuario selected = clienteCombo.getSelectionModel().getSelectedItem();
                    if (selected == null) {
                        Platform.runLater(() -> {
                            showAlert("Seleccione un cliente existente o marque 'Nuevo cliente'");
                            saveButton.setDisable(false);
                        });
                        return;
                    }
                    clienteId = selected.getId();
                    if (!selected.getActivo()) {
                        UsuarioDAO.updateField(clienteId, "activo", true);
                    }
                }

                if (notaToEdit == null) {
                    NotaVenta nota = new NotaVenta(clienteId, vendedorId);
                    NotaVenta created = NotaVentaDAO.create(nota);

                    if (created == null) {
                        Platform.runLater(() -> {
                            showAlert("Error al crear nota de venta");
                            saveButton.setDisable(false);
                        });
                        return;
                    }

                    double total = 0;
                    for (DetalleNotaVenta d : detalleList) {
                        d.setNotaVentaId(created.getId());
                        if (d.getId() == null) {
                            DetalleNotaVentaDAO.create(d);
                        }
                        SaborDAO.adjustStock(d.getSaborId(), -d.getCantidad());
                        total += d.getSubtotal();
                    }

                    NotaVentaDAO.updateTotal(created.getId(), total);
                } else {
                    List<DetalleNotaVenta> oldDetalles = DetalleNotaVentaDAO.getByNotaVentaId(notaToEdit.getId());

                    for (DetalleNotaVenta d : oldDetalles) {
                        SaborDAO.adjustStock(d.getSaborId(), d.getCantidad());
                    }

                    DetalleNotaVentaDAO.deleteByNotaVentaId(notaToEdit.getId());

                    double total = 0;
                    for (DetalleNotaVenta d : detalleList) {
                        d.setNotaVentaId(notaToEdit.getId());
                        DetalleNotaVentaDAO.create(d);
                        SaborDAO.adjustStock(d.getSaborId(), -d.getCantidad());
                        total += d.getSubtotal();
                    }

                    NotaVentaDAO.updateTotal(notaToEdit.getId(), total);
                }

                final String pwd = generatedPassword;
                Platform.runLater(() -> {
                    saveButton.setDisable(false);

                    if (pwd != null) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                        alert.setTitle("Cliente Nuevo");
                        alert.setHeaderText("Cliente creado exitosamente");
                        alert.setContentText("Contraseña generada para el cliente: " + pwd + "\n\nEntréguela al cliente para que pueda iniciar sesión.");
                        alert.showAndWait();
                    }

                    Alert success = new Alert(Alert.AlertType.INFORMATION);
                    success.setTitle("Nota de Venta Guardada");
                    success.setHeaderText("Nota de venta guardada exitosamente");
                    success.showAndWait();

                    goBack();
                    if (vendedorController != null) vendedorController.refreshData();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    showAlert("Error al guardar nota de venta: " + e.getMessage());
                    saveButton.setDisable(false);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    private void goBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/heladeria/views/vendedor_dashboard.fxml"));
            Parent root = loader.load();
            VendedorController controller = loader.getController();
            controller.setStage(stage);

            Scene scene = new Scene(root, 1000, 700);
            stage.setScene(scene);
            stage.setTitle("Heladería - Panel Vendedor");
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
