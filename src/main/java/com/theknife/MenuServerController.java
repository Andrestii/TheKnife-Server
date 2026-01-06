package com.theknife;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class MenuServerController {

    Database database;
    public static final int PORT = 2345;
    private ServerSocket ss = null;

    @FXML
    TextField hostField, usernameField, passwordField;

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