/**
 * Autori del progetto:
 *
 * - Lorenzo De Paoli
 *   Matricola: 753577
 *   Sede: VA
 *
 * - Andrea Onesti
 *   Matricola: 754771
 *   Sede: VA
 *
 * - Weili Wu
 *   Matricola: 752602
 *   Sede: VA
 */

package com.theknife;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Classe principale del server TheKnife.
 * Avvia l'applicazione JavaFX e carica la schermata iniziale.
 */
public class App extends Application {

    private static Scene scene;

    /**
     * Metodo di avvio dell'applicazione JavaFX.
     * Inizializza la scena principale e mostra lo stage.
     *
     * @param stage stage principale dell'applicazione
     * @throws IOException se il file FXML non viene caricato correttamente
     */
    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("Starting JavaFX app...");
        scene = new Scene(loadFXML("MenuServer"), 640, 480);
        System.out.println("Scene created.");
        stage.setScene(scene);
        stage.show();
        System.out.println("App started.");
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        System.out.println("Loading FXML: " + fxml);
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
            System.out.println("FXMLLoader created, resource: " + fxmlLoader.getLocation());
            Parent root = fxmlLoader.load();
            System.out.println("FXML loaded successfully.");
            return root;
        } catch (Exception e) {
            System.out.println("Error loading FXML: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch();
    }

}