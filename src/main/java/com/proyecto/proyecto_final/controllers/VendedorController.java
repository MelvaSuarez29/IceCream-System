package com.proyecto.proyecto_final.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class VendedorController {

    @FXML private TableView saboresTable;
    @FXML private TableColumn colSaborNombre;
    @FXML private TableColumn colSaborPrecio;
    @FXML private TableColumn colSaborStock;
    @FXML private TableView facturasTable;
    @FXML private TableColumn colFactFecha;
    @FXML private TableColumn colFactCliente;
    @FXML private TableColumn colFactTotal;
    @FXML private TableView detalleTable;
    @FXML private TableColumn colDetSabor;
    @FXML private TableColumn colDetCantidad;
    @FXML private TableColumn colDetSubtotal;
    @FXML private Button createFacturaBtn;
    @FXML private Button editFacturaBtn;
    @FXML private Button deleteFacturaBtn;
    @FXML private Button logoutButton;
    @FXML private Label clienteInfoLabel;

    @FXML
    private void initialize() {
    }
}