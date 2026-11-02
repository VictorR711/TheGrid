package com.thegrid.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML private ComboBox<String> cbHost;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnConectar;
    @FXML private Label lblEstado;

    @FXML
    private void initialize() {
    }

    @FXML
    private void handleConectar(ActionEvent event) {
    }

    @FXML
    private void handleHostSeleccionado(ActionEvent event) {
    }

    @FXML
    private void handleUsuarioEnter(ActionEvent event) {
        txtPassword.requestFocus();
    }
}
