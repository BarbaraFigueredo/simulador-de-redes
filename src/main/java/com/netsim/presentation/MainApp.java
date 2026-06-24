package com.netsim.presentation;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Ponto de entrada da aplicação JavaFX.
 *
 * A classe main() está separada em Launcher.java para evitar erros
 * do classloader ao executar o JAR sem o module-path configurado.
 */
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/netsim/fxml/main.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1280, 750);
        scene.getStylesheets().add(
                getClass().getResource("/com/netsim/css/dark-theme.css").toExternalForm());

        primaryStage.setTitle("NetSim Java — Simulador de Redes de Computadores");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}