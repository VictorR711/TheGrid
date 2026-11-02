package com.thegrid.controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import modelo.ModelException;

/** Pantalla 2: con el túnel ya abierto, conecta a MariaDB (usuario becario). */
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
        lblHostTunel.setText(Sesion.usuario + "@" + Sesion.host);

        // El modelo siempre se conecta a localhost:3306/its5a (lo que pide la tarea),
        // así que estos campos solo se muestran, no se editan.
        txtHostDB.setEditable(false);
        txtPuertoDB.setEditable(false);
        txtEsquema.setEditable(false);
        txtUsuarioDB.requestFocus();
    }

    @FXML
    private void handleConectarDB(ActionEvent event) throws IOException {
        String usuario = txtUsuarioDB.getText().trim();
        String password = txtPasswordDB.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            lblEstado.setText("Escribe usuario y contraseña de MariaDB.");
            return;
        }

        try {
            Sesion.modelo.connectDatabase(usuario, password);
        } catch (ModelException e) {
            lblEstado.setText(e.getMessage());
            return;
        }

        Sesion.cambiarVista(event, "/vistas/Principal.fxml");
    }

    @FXML
    private void handleCerrarTunel(ActionEvent event) throws IOException {
        Sesion.modelo.logout();
        Sesion.cambiarVista(event, "/vistas/Login.fxml");
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
