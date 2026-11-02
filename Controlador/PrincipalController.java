package com.thegrid.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class PrincipalController {

    @FXML private Label lblConexion;
    @FXML private Button btnDesconectar;
    @FXML private TextField txtBuscarTabla;
    @FXML private ListView<String> lvTablas;
    @FXML private Label lblTotalTablas;
    @FXML private Label lblTablaSeleccionada;
    @FXML private Label lblInfoTabla;
    @FXML private Button btnDescargar;
    @FXML private TableView<Object> tvVistaPrevia;
    @FXML private ProgressBar pbDescarga;
    @FXML private Label lblEstadoDescarga;
    @FXML private Button btnAbrirCrud;
    @FXML private Button btnNavTablas;
    @FXML private Button btnNavCrud;
    @FXML private Button btnNavIntegrantes;

    @FXML
    private void initialize() {
    }

    @FXML
    private void handleDesconectar(ActionEvent event) {
    }

    @FXML
    private void handleBuscarTabla(KeyEvent event) {
    }

    @FXML
    private void handleSeleccionarTabla(MouseEvent event) {
    }

    @FXML
    private void handleDescargar(ActionEvent event) {
    }

    @FXML
    private void handleAbrirCrud(ActionEvent event) {
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
    private void handleTablaTecla(KeyEvent event) {
    }
}
