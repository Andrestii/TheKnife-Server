package com.theknife;

import java.net.ServerSocket;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;

public class MenuServerController {

    Database database;
    public static final int PORT = 2345;
    private ServerSocket ss = null;

    @FXML
    TextField hostField, usernameField, passwordField;

    public void invioDatiDB(){
        String hostDB = hostField.getText();
        String usernameDB = usernameField.getText();
        String passwordDB = passwordField.getText();

        database = new Database(hostDB, usernameDB, passwordDB);
        
        /*
        try {
            ss = new ServerSocket(PORT);
            System.out.println("Server ready");
            while (true) {
                Socket cliSocket = ss.accept();
                System.out.println("Client connected: socket = " + cliSocket);
                new Thread(new ServerThread(cliSocket, database)).start();
                //cliSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ss != null) {
                    ss.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
       */
    }
    

    public static void main(String[] args) {
        
    }
}