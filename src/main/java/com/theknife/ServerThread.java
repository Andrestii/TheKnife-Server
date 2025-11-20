package com.theknife;

import theknifeserver.*;
import java.io.*;
import java.net.Socket;
import java.util.List;

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
        ) {
            while (true) {

                String command = (String) in.readObject();
                System.out.println("[SERVER] Ricevuto comando: " + command);

                switch (command) {

                    //  UTENTI
                    case "registerUser": {
                        String nome = (String) in.readObject();
                        String cognome = (String) in.readObject();
                        String username = (String) in.readObject();
                        String password = (String) in.readObject();
                        String ruolo = (String) in.readObject();
                        String domicilio = (String) in.readObject();

                        boolean ok = database.registerUser(nome, cognome, username, password, ruolo, domicilio);

                        if (ok)
                            out.writeObject(new ServerResponse("OK", "Registrazione completata"));
                        else
                            out.writeObject(new ServerResponse("ERROR", "Errore nella registrazione"));
                        break;
                    }

                    case "login": {
                        String user = (String) in.readObject();
                        String pass = (String) in.readObject();

                        boolean isValid = database.validateUser(user, pass);

                        if (isValid)
                            out.writeObject(new ServerResponse("OK", new Utente(user, "?", "?")));
                        else
                            out.writeObject(new ServerResponse("ERROR", "Credenziali errate"));
                        break;
                    }

                    //  RISTORANTI
                    case "addRestaurant": {
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

                        database.addRestaurant(owner, nomeRist, nazione, citta, indirizzo,
                                lat, lon, prezzo, delivery, prenotazione, tipoCucina);

                        out.writeObject(new ServerResponse("OK", "Ristorante aggiunto"));
                        break;
                    }

                    case "searchRestaurants": {
                        String filtro = (String) in.readObject();
                        List<Ristorante> lista = database.searchRestaurants(filtro);

                        out.writeObject(new ServerResponse("OK", lista));
                        break;
                    }

                    case "getRestaurantDetails": {
                        int id = (int) in.readObject();
                        Ristorante r = database.getRestaurantDetails(id);

                        if (r != null)
                            out.writeObject(new ServerResponse("OK", r));
                        else
                            out.writeObject(new ServerResponse("ERROR", "Ristorante non trovato"));

                        break;
                    }

                    //  RECENSIONI
                    case "addReview": {
                        int idRistorante = (int) in.readObject();
                        String recensore = (String) in.readObject();
                        int stelle = (int) in.readObject();
                        String testo = (String) in.readObject();

                        database.addReview(idRistorante, recensore, stelle, testo);
                        out.writeObject(new ServerResponse("OK", "Recensione aggiunta"));
                        break;
                    }

                    case "editReview": {
                        int idRec = (int) in.readObject();
                        int nuoveStelle = (int) in.readObject();
                        String nuovoTesto = (String) in.readObject();

                        database.editReview(idRec, nuoveStelle, nuovoTesto);
                        out.writeObject(new ServerResponse("OK", "Recensione modificata"));
                        break;
                    }

                    case "deleteReview": {
                        int idRec = (int) in.readObject();
                        database.deleteReview(idRec);
                        out.writeObject(new ServerResponse("OK", "Recensione eliminata"));
                        break;
                    }

                    case "viewReviews": {
                        int idR = (int) in.readObject();

                        List<Recensione> recensioni = database.getReviews(idR);
                        out.writeObject(new ServerResponse("OK", recensioni));
                        break;
                    }

                    case "answerReview": {
                        int idRec = (int) in.readObject();
                        String risposta = (String) in.readObject();

                        database.answerReview(idRec, risposta);
                        out.writeObject(new ServerResponse("OK", "Risposta salvata"));
                        break;
                    }

                    //  PREFERITI
                    case "addFavorite": {
                        String userFav = (String) in.readObject();
                        int idFav = (int) in.readObject();

                        database.addFavorite(userFav, idFav);
                        out.writeObject(new ServerResponse("OK", "Aggiunto ai preferiti"));
                        break;
                    }

                    case "removeFavorite": {
                        String userFav2 = (String) in.readObject();
                        int idRemove = (int) in.readObject();

                        database.removeFavorite(userFav2, idRemove);
                        out.writeObject(new ServerResponse("OK", "Rimosso dai preferiti"));
                        break;
                    }

                    case "listFavorites": {
                        String userFav3 = (String) in.readObject();

                        List<Ristorante> list = database.listFavorites(userFav3);
                        out.writeObject(new ServerResponse("OK", list));
                        break;
                    }


                    // DEFAULT
                    default:
                        out.writeObject(new ServerResponse("ERROR", "Comando non riconosciuto"));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[SERVER] Connessione chiusa: " + e.getMessage());
        }
    }
}
