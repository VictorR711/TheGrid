package com.thegrid.controller;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class IntegrantesController {

    @FXML private Label lblIntegrante1;
    @FXML private Label lblIntegrante2;
    @FXML private Label lblIntegrante3;
    @FXML private Label lblIntegrante4;
    @FXML private Button btnNavTablas;
    @FXML private Button btnNavCrud;
    @FXML private Button btnNavIntegrantes;

    @FXML
    private void initialize() {
        // Los nombres ya vienen escritos en Integrantes.fxml
    }

    @FXML
    private void handleIrTablas(ActionEvent event) throws IOException {
        Sesion.cambiarVista(event, "/vistas/Principal.fxml");
    }

    @FXML
    private void handleIrCrud(ActionEvent event) throws IOException {
        Sesion.cambiarVista(event, "/vistas/Crud.fxml");
    }

    @FXML
    private void handleIrIntegrantes(ActionEvent event) {
        // ya estamos en Integrantes
    }
}
