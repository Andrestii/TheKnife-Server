package com.theknife;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Controller JavaFX della schermata di avvio del server.
 * <p>
 * Consente di inserire i parametri di connessione al database e avvia il server
 * in ascolto sulla porta {@link #PORT}. Per ogni client connesso viene creato
 * un thread dedicato ({@link ServerThread}) che gestisce la comunicazione e
 * invoca le operazioni sul {@link Database}.
 * </p>
 */
public class MenuServerController {

    Database database;
    public static final int PORT = 2345;
    private ServerSocket ss = null;

    @FXML
    TextField hostField, usernameField, passwordField;

    /**
     * Legge i parametri di connessione al database dai campi della GUI,
     * inizializza l'istanza {@link Database} e avvia il loop del server in un
     * thread daemon.
     * <p>
     * Il server ascolta sulla porta {@link #PORT} e, per ogni nuova connessione,
     * crea un nuovo {@link ServerThread} dedicato al client.
     * </p>
     */
    public void invioDatiDB() {
        String hostDB = hostField.getText();
        String usernameDB = usernameField.getText();
        String passwordDB = passwordField.getText();
        database = new Database(hostDB, usernameDB, passwordDB);

        Thread serverLoop = new Thread(() -> {
            try (ServerSocket ss = new ServerSocket(PORT)) {
                System.out.println("Server ready");
                while (true) {
                    Socket cliSocket = ss.accept();
                    System.out.println("Client connected: socket = " + cliSocket);
                    new Thread(new ServerThread(cliSocket, database)).start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverLoop.setDaemon(true);
        serverLoop.start();
    }

    public static void main(String[] args) {

    }
}