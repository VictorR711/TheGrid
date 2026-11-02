package com.thegrid.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import modelo.ModelException;
import modelo.TableData;

public class CrudController {

    // Mismo estilo que los campos y etiquetas de los .fxml
    private static final String ESTILO_ETIQUETA = "-fx-text-fill: #6fb7c7; -fx-font-size: 11px;";
    private static final String ESTILO_CAMPO = "-fx-background-color: #08101c; -fx-text-fill: #ffffff; "
            + "-fx-border-color: #1aa7bd; -fx-border-width: 1; -fx-border-radius: 3; "
            + "-fx-background-radius: 3; -fx-padding: 7 9 7 9;";

    @FXML private Label lblTablaActual;
    @FXML private Label lblTotalRegistros;
    @FXML private TextField txtBuscar;
    @FXML private Button btnRefrescar;
    @FXML private TableView<Fila> tvRegistros;
    @FXML private VBox vbCampos;
    @FXML private Button btnNuevo;
    @FXML private Button btnGuardar;
    @FXML private Button btnActualizar;
    @FXML private Button btnEliminar;
    @FXML private Label lblEstado;
    @FXML private Button btnNavTablas;
    @FXML private Button btnNavCrud;
    @FXML private Button btnNavIntegrantes;

    /** Un TextField por columna: nombre de columna -> campo (en orden). */
    private final Map<String, TextField> campos = new LinkedHashMap<>();

    /** Todos los registros; la tabla de la vista puede estar filtrada. */
    private List<Fila> todasLasFilas = new ArrayList<>();

    private String tabla;

    @FXML
    private void initialize() {
        tabla = Sesion.tablaActual;

        if (tabla == null) {
            lblTablaActual.setText("");
            lblEstado.setText("Primero descarga una tabla en la pantalla Tablas.");
            btnRefrescar.setDisable(true);
            btnNuevo.setDisable(true);
            btnGuardar.setDisable(true);
            btnActualizar.setDisable(true);
            btnEliminar.setDisable(true);
            return;
        }

        lblTablaActual.setText(tabla);
        try {
            TableData datos = Sesion.modelo.getLocalTable(tabla);
            crearCampos(datos.getColumns());
            mostrarRegistros(datos);
        } catch (ModelException e) {
            lblEstado.setText(e.getMessage());
        }
    }

    // --------------------------- CRUD ---------------------------

    /** CREATE */
    @FXML
    private void handleGuardar(ActionEvent event) {
        Map<String, Object> valores = leerCampos();
        if (todosVacios(valores)) {
            lblEstado.setText("Llena al menos un campo.");
            return;
        }
        try {
            Sesion.modelo.insertRecord(tabla, valores);
            recargar();
            limpiarCampos();
            lblEstado.setText("Registro creado.");
        } catch (ModelException e) {
            lblEstado.setText(mensajeError(e));
        }
    }

    /** UPDATE */
    @FXML
    private void handleActualizar(ActionEvent event) {
        Fila fila = tvRegistros.getSelectionModel().getSelectedItem();
        if (fila == null) {
            lblEstado.setText("Selecciona el registro que quieres actualizar.");
            return;
        }
        try {
            Sesion.modelo.updateRecord(tabla, fila.getRowId(), leerCampos());
            recargar();
            lblEstado.setText("Registro actualizado.");
        } catch (ModelException e) {
            lblEstado.setText(mensajeError(e));
        }
    }

    /** DELETE */
    @FXML
    private void handleEliminar(ActionEvent event) {
        eliminar();
    }

    /** READ (vuelve a leer la tabla de SQLite) */
    @FXML
    private void handleRefrescar(ActionEvent event) {
        recargar();
        lblEstado.setText("Datos actualizados.");
    }

    @FXML
    private void handleNuevo(ActionEvent event) {
        limpiarCampos();
        lblEstado.setText("Llena los campos y presiona Crear. Si dejas vacío el id, se genera solo.");
    }

    // ------------------------ Selección y búsqueda ------------------------

    @FXML
    private void handleSeleccionarRegistro(MouseEvent event) {
        mostrarSeleccionEnCampos();
    }

    @FXML
    private void handleRegistroTecla(KeyEvent event) {
        if (event.getCode() == KeyCode.DELETE) {
            eliminar();
        } else {
            mostrarSeleccionEnCampos(); // flechas arriba/abajo
        }
    }

    @FXML
    private void handleBuscar(KeyEvent event) {
        filtrar();
    }

    // ---------------------------- Navegación ----------------------------

    @FXML
    private void handleIrTablas(ActionEvent event) throws IOException {
        Sesion.cambiarVista(event, "/vistas/Principal.fxml");
    }

    @FXML
    private void handleIrCrud(ActionEvent event) {
        // ya estamos en CRUD
    }

    @FXML
    private void handleIrIntegrantes(ActionEvent event) throws IOException {
        Sesion.cambiarVista(event, "/vistas/Integrantes.fxml");
    }

    // ---------------------------- Auxiliares ----------------------------

    private void eliminar() {
        Fila fila = tvRegistros.getSelectionModel().getSelectedItem();
        if (fila == null) {
            lblEstado.setText("Selecciona el registro que quieres eliminar.");
            return;
        }

        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION, "¿Eliminar el registro seleccionado?");
        confirmar.setHeaderText(null);
        if (confirmar.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            Sesion.modelo.deleteRecord(tabla, fila.getRowId());
            recargar();
            limpiarCampos();
            lblEstado.setText("Registro eliminado.");
        } catch (ModelException e) {
            lblEstado.setText(e.getMessage());
        }
    }

    /** Crea un Label + TextField por cada columna de la tabla. */
    private void crearCampos(List<String> columnas) {
        vbCampos.getChildren().clear();
        campos.clear();
        for (String columna : columnas) {
            Label etiqueta = new Label(columna);
            etiqueta.setStyle(ESTILO_ETIQUETA);

            TextField campo = new TextField();
            campo.setStyle(ESTILO_CAMPO);

            vbCampos.getChildren().addAll(etiqueta, campo);
            campos.put(columna, campo);
        }
    }

    /** Copia los valores del registro seleccionado a los TextField. */
    private void mostrarSeleccionEnCampos() {
        Fila fila = tvRegistros.getSelectionModel().getSelectedItem();
        if (fila == null) {
            return;
        }
        int i = 0;
        for (TextField campo : campos.values()) {
            Object valor = fila.getValores().get(i);
            campo.setText(valor == null ? "" : valor.toString());
            i++;
        }
    }

    /** Lee los TextField y arma columna -> valor. Un campo vacío se guarda como NULL. */
    private Map<String, Object> leerCampos() {
        Map<String, Object> valores = new LinkedHashMap<>();
        for (Map.Entry<String, TextField> entrada : campos.entrySet()) {
            String texto = entrada.getValue().getText();
            valores.put(entrada.getKey(), texto.isEmpty() ? null : texto);
        }
        return valores;
    }

    /** Traduce el error de SQLite más común a algo que el usuario entienda. */
    private String mensajeError(ModelException e) {
        if (e.getMessage().contains("PRIMARY KEY")) {
            return "Ese id ya existe. Usa otro o déjalo vacío para que se genere solo.";
        }
        return e.getMessage();
    }

    private boolean todosVacios(Map<String, Object> valores) {
        for (Object valor : valores.values()) {
            if (valor != null) {
                return false;
            }
        }
        return true;
    }

    private void limpiarCampos() {
        tvRegistros.getSelectionModel().clearSelection();
        for (TextField campo : campos.values()) {
            campo.clear();
        }
    }

    private void recargar() {
        try {
            mostrarRegistros(Sesion.modelo.getLocalTable(tabla));
        } catch (ModelException e) {
            lblEstado.setText(e.getMessage());
        }
    }

    private void mostrarRegistros(TableData datos) {
        todasLasFilas = Sesion.llenarTabla(tvRegistros, datos);
        filtrar(); // respeta lo que haya en el buscador
    }

    /** Deja solo los registros que contienen el texto buscado en alguna columna. */
    private void filtrar() {
        String texto = txtBuscar.getText().toLowerCase();
        List<Fila> visibles = new ArrayList<>();
        for (Fila fila : todasLasFilas) {
            for (Object valor : fila.getValores()) {
                if (String.valueOf(valor).toLowerCase().contains(texto)) {
                    visibles.add(fila);
                    break;
                }
            }
        }
        tvRegistros.getItems().setAll(visibles);
        lblTotalRegistros.setText(visibles.size() + " registros");
    }
}
