package com.thegrid;

import com.thegrid.controller.Sesion;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    public void start(Stage stage) throws IOException {

        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/vistas/Login.fxml")
        );

        Parent root = loader.load();

        Scene scene = new Scene(root);

        stage.setTitle("The Grid");
        stage.setScene(scene);
        stage.show();
    }

    /** Al cerrar la ventana se cierran el túnel SSH y las bases de datos. */
    public void stop() {
        Sesion.modelo.logout();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
