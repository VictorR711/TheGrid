package com.thegrid.controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import modelo.ModelException;

/** Pantalla 1: abre el túnel SSH. */
public class LoginController {

    @FXML private ComboBox<String> cbHost;
    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnConectar;
    @FXML private Label lblEstado;

    @FXML
    private void initialize() {
        // Si ya hubo una sesión (ej. después de "Desconectar") se rellena el usuario
        if (Sesion.usuario != null) {
            txtUsuario.setText(Sesion.usuario);
        }
    }

    @FXML
    private void handleConectar(ActionEvent event) throws IOException {
        // El ComboBox es editable: lo escrito está en su editor
        String host = cbHost.getEditor().getText().trim();
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        if (host.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
            lblEstado.setText("Escribe host, usuario y contraseña.");
            return;
        }

        try {
            Sesion.modelo.openTunnel(host, usuario, password);
        } catch (ModelException e) {
            lblEstado.setText(e.getMessage());
            return;
        }

        Sesion.host = host;
        Sesion.usuario = usuario;
        Sesion.cambiarVista(event, "/vistas/ConexionDB.fxml");
    }

    @FXML
    private void handleHostSeleccionado(ActionEvent event) {
        txtUsuario.requestFocus();
    }

    @FXML
    private void handleUsuarioEnter(ActionEvent event) {
        txtPassword.requestFocus();
    }
}
