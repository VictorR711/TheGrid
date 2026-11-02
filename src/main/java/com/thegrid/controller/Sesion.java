package com.thegrid.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;
import modelo.DatabaseModel;
import modelo.TableData;

/**
 * Datos que comparten todas las pantallas.
 *
 * Cada vez que se cambia de pantalla JavaFX crea un controlador NUEVO,
 * por eso el modelo (y con él la conexión) se guarda aquí, en static,
 * para que todos usen el mismo.
 */
public class Sesion {

    /** El único modelo de toda la aplicación. */
    public static final DatabaseModel modelo = new DatabaseModel();

    public static String host;
    public static String usuario;

    /** Última tabla descargada; es la que se abre en el CRUD. */
    public static String tablaActual;

    /** Cambia la vista de la ventana actual. */
    public static void cambiarVista(ActionEvent event, String ruta) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                Sesion.class.getResource(ruta)
        );

        Parent root = loader.load();

        Stage stage = (Stage)
                ((Node) event.getSource())
                        .getScene()
                        .getWindow();

        stage.setScene(new Scene(root));
        stage.show();
    }

    /**
     * Muestra un TableData en un TableView. Las columnas se crean aquí
     * porque cada tabla de la base tiene columnas distintas.
     * Devuelve las filas por si la pantalla las necesita (ej. para buscar).
     */
    public static List<Fila> llenarTabla(TableView<Fila> tabla, TableData datos) {
        // Cada columna usa su propio ancho (si no caben, aparece scroll horizontal)
        tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        tabla.getColumns().clear();
        for (int i = 0; i < datos.getColumns().size(); i++) {
            final int indice = i;
            TableColumn<Fila, Object> columna = new TableColumn<>(datos.getColumns().get(i));
            // Qué valor muestra cada celda de esta columna
            columna.setCellValueFactory(celda ->
                    new SimpleObjectProperty<>(celda.getValue().getValores().get(indice)));
            columna.setPrefWidth(anchoColumna(datos, i));
            tabla.getColumns().add(columna);
        }

        List<Fila> filas = new ArrayList<>();
        for (int i = 0; i < datos.getRows().size(); i++) {
            filas.add(new Fila(datos.getRowIds().get(i), datos.getRows().get(i)));
        }
        tabla.setItems(FXCollections.observableArrayList(filas));
        return filas;
    }

    /** Ancho según el texto más largo de la columna (encabezado o valores). */
    private static double anchoColumna(TableData datos, int indice) {
        int letras = datos.getColumns().get(indice).length();
        for (List<Object> fila : datos.getRows()) {
            Object valor = fila.get(indice);
            if (valor != null) {
                letras = Math.max(letras, valor.toString().length());
            }
        }
        double ancho = letras * 8 + 24;          // ~8 px por letra en Consolas 13px + margen
        return Math.max(70, Math.min(ancho, 220)); // ni diminuta ni gigante
    }
}
