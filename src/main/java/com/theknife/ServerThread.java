package com.theknife;

import java.io.*;
import java.util.*;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;


public class ServerThread implements Runnable {

    private Socket socket;
    private Database database;

    public ServerThread(Socket socket, Database database) {
        this.socket = socket;
        this.database = database;
    }

    @Override
    public void run() {
        try (
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ) 
            {
            while (true) {
                String command = (String) in.readObject();
                System.out.println("[SERVER] Ricevuto comando: " + command);

                switch (command) {
                    // UTENTI 
                    case "registerUser":
                        String nome = (String) in.readObject();
                        String cognome = (String) in.readObject();
                        String username = (String) in.readObject();
                        String password = (String) in.readObject();
                        String ruolo = (String) in.readObject();
                        String domicilio = (String) in.readObject();

                        boolean ok = database.registerUser(nome, cognome, username, password, ruolo, domicilio);
                        out.writeObject(ok);
                        break;

                    case "login":
                        String user = (String) in.readObject();
                        String pass = (String) in.readObject();
                        boolean isValid = database.validateUser(user, pass);
                        out.writeObject(isValid);
                        break;

                    // RISTORANTI 
                    case "addRestaurant":
                        String owner = (String) in.readObject();
                        String nomeRist = (String) in.readObject();
                        String nazione = (String) in.readObject();
                        String citta = (String) in.readObject();
                        String indirizzo = (String) in.readObject();
                        double lat = (double) in.readObject();
                        double lon = (double) in.readObject();
                        int prezzo = (int) in.readObject();
                        boolean delivery = (boolean) in.readObject();
                        boolean prenotazione = (boolean) in.readObject();
                        String tipoCucina = (String) in.readObject();

                        database.addRestaurant(owner, nomeRist, nazione, citta, indirizzo, lat, lon, prezzo, delivery, prenotazione, tipoCucina);
                        out.writeObject("OK");
                        break;

                    case "searchRestaurants":
                        String ricerca = (String) in.readObject();
                        ResultSet rs = database.searchRestaurants(ricerca);
                        out.writeObject(database.resultSetToList(rs));
                        break;

                    case "getRestaurantDetails":
                        int idR = (int) in.readObject();
                        out.writeObject(database.getRestaurantDetails(idR));
                        break;

                    // RECENSIONI
                    case "addReview":
                        int idRistorante = (int) in.readObject();
                        String recensore = (String) in.readObject();
                        int stelle = (int) in.readObject();
                        String testo = (String) in.readObject();

                        database.addReview(idRistorante, recensore, stelle, testo);
                        out.writeObject("OK");
                        break;

                    case "editReview":
                        int idRec = (int) in.readObject();
                        int nuoveStelle = (int) in.readObject();
                        String nuovoTesto = (String) in.readObject();

                        database.editReview(idRec, nuoveStelle, nuovoTesto);
                        out.writeObject("OK");
                        break;

                    case "deleteReview":
                        int idRecensione = (int) in.readObject();
                        database.deleteReview(idRecensione);
                        out.writeObject("OK");
                        break;

                    case "viewReviews":
                        int idRist = (int) in.readObject();
                        out.writeObject(database.getReviews(idRist));
                        break;

                    case "answerReview":
                        int idRev = (int) in.readObject();
                        String risposta = (String) in.readObject();
                        database.answerReview(idRev, risposta);
                        out.writeObject("OK");
                        break;

                    // PREFERITI
                    case "addFavorite":
                        String userFav = (String) in.readObject();
                        int idFav = (int) in.readObject();
                        database.addFavorite(userFav, idFav);
                        out.writeObject("OK");
                        break;

                    case "removeFavorite":
                        String usr = (String) in.readObject();
                        int idRemove = (int) in.readObject();
                        database.removeFavorite(usr, idRemove);
                        out.writeObject("OK");
                        break;

                    case "listFavorites":
                        String userF = (String) in.readObject();
                        out.writeObject(database.listFavorites(userF));
                        break;

                    default:
                        System.out.println("[SERVER] Comando sconosciuto");
                        out.writeObject("ERROR");
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace(); //Stampa la traccia dello stack (l’errore completo) a console
            System.out.println("[SERVER] Connessione chiusa: " + e.getMessage()); //restituisce una stringa con il messaggio dell’errore
        }
    }
}
