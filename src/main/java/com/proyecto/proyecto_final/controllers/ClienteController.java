package com.proyecto.proyecto_final.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ClienteController {

    @FXML private Label welcomeLabel;
    @FXML private TableView facturasTable;
    @FXML private TableColumn colFactFecha;
    @FXML private TableColumn colFactTotal;
    @FXML private TableView detalleTable;
    @FXML private TableColumn colDetSabor;
    @FXML private TableColumn colDetCantidad;
    @FXML private TableColumn colDetSubtotal;
    @FXML private Button logoutButton;

    @FXML
    private void initialize() {
    }
}