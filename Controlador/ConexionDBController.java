package com.thegrid.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ConexionDBController {

    @FXML private Label lblEstadoTunel;
    @FXML private Label lblHostTunel;
    @FXML private TextField txtHostDB;
    @FXML private TextField txtPuertoDB;
    @FXML private TextField txtEsquema;
    @FXML private TextField txtUsuarioDB;
    @FXML private PasswordField txtPasswordDB;
    @FXML private Button btnConectarDB;
    @FXML private Button btnCerrarTunel;
    @FXML private Label lblEstado;

    @FXML
    private void initialize() {
    }

    @FXML
    private void handleConectarDB(ActionEvent event) {
    }

    @FXML
    private void handleCerrarTunel(ActionEvent event) {
    }

    @FXML
    private void handleHostDBEnter(ActionEvent event) {
        txtPuertoDB.requestFocus();
    }

    @FXML
    private void handlePuertoDBEnter(ActionEvent event) {
        txtEsquema.requestFocus();
    }

    @FXML
    private void handleEsquemaEnter(ActionEvent event) {
        txtUsuarioDB.requestFocus();
    }

    @FXML
    private void handleUsuarioDBEnter(ActionEvent event) {
        txtPasswordDB.requestFocus();
    }
}
