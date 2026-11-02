package com.thegrid.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

public class CrudController {

    @FXML private Label lblTablaActual;
    @FXML private Label lblTotalRegistros;
    @FXML private TextField txtBuscar;
    @FXML private Button btnRefrescar;
    @FXML private TableView<Object> tvRegistros;
    @FXML private VBox vbCampos;
    @FXML private Button btnNuevo;
    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Label lblEstado;
    @FXML private Button btnNavTablas;
    @FXML private Button btnNavCrud;
    @FXML private Button btnNavIntegrantes;

    @FXML
    private void initialize() {
    }

    @FXML
    private void handleBuscar(KeyEvent event) {
    }

    @FXML
    private void handleRefrescar(ActionEvent event) {
    }

    @FXML
    private void handleSeleccionarRegistro(MouseEvent event) {
    }

    @FXML
    private void handleNuevo(ActionEvent event) {
    }

    @FXML
    private void handleGuardar(ActionEvent event) {
    }

    @FXML
    private void handleActualizar(ActionEvent event) {
    }

    @FXML
    private void handleEliminar(ActionEvent event) {
    }

    @FXML
    private void handleIrTablas(ActionEvent event) {
    }

    @FXML
    private void handleIrCrud(ActionEvent event) {
    }

    @FXML
    private void handleIrIntegrantes(ActionEvent event) {
    }

    @FXML
    private void handleRegistroTecla(KeyEvent event) {
    }
}
