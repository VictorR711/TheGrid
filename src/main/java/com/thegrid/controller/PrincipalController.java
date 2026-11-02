package com.thegrid.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import modelo.ModelException;
import modelo.TableData;

/** Pantalla 3: lista de tablas del host y descarga a SQLite. */
public class PrincipalController {

    @FXML private Label lblConexion;
    @FXML private Button btnDesconectar;
    @FXML private TextField txtBuscarTabla;
    @FXML private ListView<String> lvTablas;
    @FXML private Label lblTotalTablas;
    @FXML private Label lblTablaSeleccionada;
    @FXML private Label lblInfoTabla;
    @FXML private Button btnDescargar;
    @FXML private TableView<Fila> tvVistaPrevia;
    @FXML private ProgressBar pbDescarga;
    @FXML private Label lblEstadoDescarga;
    @FXML private Button btnAbrirCrud;
    @FXML private Button btnNavTablas;
    @FXML private Button btnNavCrud;
    @FXML private Button btnNavIntegrantes;

    /** Todas las tablas del host; la lista de la vista puede estar filtrada. */
    private List<String> todasLasTablas = new ArrayList<>();

    @FXML
    private void initialize() {
        lblConexion.setText(Sesion.usuario + "@" + Sesion.host);

        try {
            todasLasTablas = Sesion.modelo.getRemoteTables();
            mostrarTablas(todasLasTablas);
        } catch (ModelException e) {
            lblEstadoDescarga.setText(e.getMessage());
        }

        // Al volver de otra pantalla, se muestra la última tabla descargada
        if (Sesion.tablaActual != null) {
            try {
                mostrarVistaPrevia(Sesion.modelo.getLocalTable(Sesion.tablaActual));
                btnAbrirCrud.setDisable(false);
            } catch (ModelException e) {
                lblEstadoDescarga.setText(e.getMessage());
            }
        }
    }

    @FXML
    private void handleDesconectar(ActionEvent event) throws IOException {
        Sesion.modelo.logout();
        Sesion.tablaActual = null;
        Sesion.cambiarVista(event, "/vistas/Login.fxml");
    }

    @FXML
    private void handleBuscarTabla(KeyEvent event) {
        String texto = txtBuscarTabla.getText().toLowerCase();
        List<String> filtradas = new ArrayList<>();
        for (String tabla : todasLasTablas) {
            if (tabla.toLowerCase().contains(texto)) {
                filtradas.add(tabla);
            }
        }
        mostrarTablas(filtradas);
    }

    @FXML
    private void handleSeleccionarTabla(MouseEvent event) {
        seleccionarTabla();
    }

    @FXML
    private void handleDescargar(ActionEvent event) {
        descargar();
    }

    @FXML
    private void handleAbrirCrud(ActionEvent event) throws IOException {
        Sesion.cambiarVista(event, "/vistas/Crud.fxml");
    }

    @FXML
    private void handleIrTablas(ActionEvent event) {
        // ya estamos en Tablas
    }

    @FXML
    private void handleIrCrud(ActionEvent event) throws IOException {
        Sesion.cambiarVista(event, "/vistas/Crud.fxml");
    }

    @FXML
    private void handleIrIntegrantes(ActionEvent event) throws IOException {
        Sesion.cambiarVista(event, "/vistas/Integrantes.fxml");
    }

    @FXML
    private void handleTablaTecla(KeyEvent event) {
        seleccionarTabla(); // flechas arriba/abajo
    }

    // ------------------------------------------------------------------

    private void mostrarTablas(List<String> tablas) {
        lvTablas.getItems().setAll(tablas);
        lblTotalTablas.setText(tablas.size() + " tablas");
    }

    /** Al seleccionar una tabla se descarga en ese momento. */
    private void seleccionarTabla() {
        String tabla = lvTablas.getSelectionModel().getSelectedItem();
        if (tabla == null || tabla.equals(lblTablaSeleccionada.getText())) {
            return; // nada seleccionado, o es la misma tabla que ya se muestra
        }
        descargar();
    }

    /** Copia la tabla seleccionada del host a la base SQLite local. */
    private void descargar() {
        String tabla = lvTablas.getSelectionModel().getSelectedItem();
        if (tabla == null) {
            return;
        }

        lblTablaSeleccionada.setText(tabla);
        lblEstadoDescarga.setText(""); // se quita el mensaje de la tabla anterior
        btnDescargar.setDisable(false);

        try {
            TableData datos = Sesion.modelo.downloadTable(tabla);
            Sesion.tablaActual = tabla;
            mostrarVistaPrevia(datos);
            pbDescarga.setProgress(1);
            lblEstadoDescarga.setText("Tabla " + tabla + " guardada en SQLite local.");
            btnAbrirCrud.setDisable(false);
        } catch (ModelException e) {
            tvVistaPrevia.getColumns().clear();
            tvVistaPrevia.getItems().clear();
            lblInfoTabla.setText("registros: -   columnas: -");
            pbDescarga.setProgress(0);
            lblEstadoDescarga.setText(e.getMessage());
            btnAbrirCrud.setDisable(true);
        }
    }

    private void mostrarVistaPrevia(TableData datos) {
        Sesion.llenarTabla(tvVistaPrevia, datos);
        lblTablaSeleccionada.setText(datos.getName());
        lblInfoTabla.setText("registros: " + datos.getRows().size()
                + "   columnas: " + datos.getColumns().size());
    }
}
