package com.theknife;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

import theknifeserver.Recensione;
import theknifeserver.Ristorante;
import theknifeserver.ServerResponse;
import theknifeserver.Utente;

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

                    // UTENTI
                    
                    case "registerUser": {
                        String nome = (String) in.readObject();
                        String cognome = (String) in.readObject();
                        String username = (String) in.readObject();
                        String password = (String) in.readObject();
                        String data_nascita = (String) in.readObject();
                        String domicilio = (String) in.readObject();
                        String ruolo = (String) in.readObject();

                        boolean ok = database.registerUser(nome, cognome, username, password, data_nascita, domicilio, ruolo);

                        if (ok)
                            out.writeObject(new ServerResponse("OK", "Registrazione completata"));
                        else
                            out.writeObject(new ServerResponse("ERROR", "Errore nella registrazione"));

                        break;
                    }

                    case "login": {
                        String username = (String) in.readObject();
                        String password = (String) in.readObject();

                        if (!database.validateUser(username, password)) {
                            out.writeObject(new ServerResponse("ERROR", "Credenziali errate"));
                            break;
                        }

                        Utente u = database.getUserData(username);
                        out.writeObject(new ServerResponse("OK", u));

                        break;
                    }

                    case "modifyUserNomeCognome": {
                        String username = (String) in.readObject();
                        String nome = (String) in.readObject();
                        String cognome = (String) in.readObject();

                        database.modifyUserNomeCognome(username, nome, cognome);
                        out.writeObject(new ServerResponse("OK", "Nome e cognome utente aggiornati"));
                        break;
                    }

                    case "modifyUserDate": {
                        String username = (String) in.readObject();
                        String dataNascita = (String) in.readObject();

                        database.modifyUserDate(username, dataNascita);
                        out.writeObject(new ServerResponse("OK", "Data di nascita utente aggiornata"));
                        break;
                    }

                    case "modifyUserDomicilio": {
                        String username = (String) in.readObject();
                        String domicilio = (String) in.readObject();

                        database.modifyUserDomicilio(username, domicilio);
                        out.writeObject(new ServerResponse("OK", "Domicilio utente aggiornato"));
                        break;
                    }

                    case "modifyUserUsername": {
                        String username = (String) in.readObject();
                        String newUsername = (String) in.readObject();

                        database.modifyUserUsername(username, newUsername);
                        out.writeObject(new ServerResponse("OK", "Username utente aggiornato"));
                        break;
                    }

                    case "modifyUserPassword": {
                        String username = (String) in.readObject();
                        String password = (String) in.readObject();

                        database.modifyUserPassword(username, password);
                        out.writeObject(new ServerResponse("OK", "Password utente aggiornata"));
                        break;
                    }
                    
                    // RISTORANTI
                    
                    case "addRestaurant": {
                        int id_ristoratore = (int) in.readObject();
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

                        boolean ok = database.addRestaurant(id_ristoratore, nomeRist, nazione, citta, indirizzo,
                                lat, lon, prezzo, delivery, prenotazione, tipoCucina);

                        if (ok)
                            out.writeObject(new ServerResponse("OK", "Ristorante aggiunto"));
                        else
                            out.writeObject(new ServerResponse("ERROR", "Errore nell'aggiunta del ristorante"));

                        break;
                    }

                    case "searchRestaurants": {
                        String filtro = (String) in.readObject();
                        List<Ristorante> lista = database.searchRestaurants(filtro);

                        out.writeObject(new ServerResponse("OK", lista));
                        break;
                    }

                    case "searchRestaurantsAdvanced": {
                        String citta = (String) in.readObject();
                        String tipoCucina = (String) in.readObject();
                        Integer prezzoMin = (Integer) in.readObject();
                        Integer prezzoMax = (Integer) in.readObject();
                        Boolean delivery = (Boolean) in.readObject();
                        Boolean prenotazione = (Boolean) in.readObject();

                        List<Ristorante> lista = database.searchRestaurantsAdvanced(
                                citta, tipoCucina, prezzoMin, prezzoMax, delivery, prenotazione
                        );

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


                    
                    // RECENSIONI
                    
                    case "addReview": {
                        String username = (String) in.readObject();
                        int idRistorante = (int) in.readObject();
                        int stelle = (int) in.readObject();
                        String testo = (String) in.readObject();

                        // Controllo se esiste già una recensione di questo utente
                        if (database.hasUserAlreadyReviewed(username, idRistorante)) {
                            out.writeObject(new ServerResponse("ERROR", "Hai già recensito questo ristorante"));
                            break;
                        }

                        database.addReview(idRistorante, username, stelle, testo);
                        out.writeObject(new ServerResponse("OK", "Recensione aggiunta"));
                        break;
                    }

                    case "editReview": {
                        /* 
                        // Codice vecchio Weili
                        int idRec = (int) in.readObject();
                        int idUtente = (int) in.readObject();
                        int nuoveStelle = (int) in.readObject();
                        String nuovoTesto = (String) in.readObject();

                        // controllo autore → NON può modificare rec altrui
                        List<Recensione> recensioni = database.getReviewsForOwner(idUtente);
                        boolean isAuthor = false;

                        for (Recensione r : recensioni) {
                            if (r.getId() == idRec && r.getIdUtente() == idUtente) {
                                isAuthor = true;
                                break;
                            }
                        }

                        if (!isAuthor) {
                            out.writeObject(new ServerResponse("ERROR", "Non puoi modificare recensioni altrui"));
                            break;
                        }

                        database.editReview(idRec, nuoveStelle, nuovoTesto);
                        */

                        String username = (String) in.readObject();
                        int idRistorante = (int) in.readObject();
                        int newStelle = (int) in.readObject();
                        String newTesto = (String) in.readObject();

                        // Controllo se l'utente ha effettivamente già recensito questo ristorante
                        if (!database.hasUserAlreadyReviewed(username, idRistorante)) {
                            out.writeObject(new ServerResponse("ERROR", "Non hai ancora recensito questo ristorante"));
                            break;
                        }

                        database.editReview(idRistorante, username, newStelle, newTesto);
                        out.writeObject(new ServerResponse("OK", "Recensione modificata"));
                        break;
                    }

                    case "deleteReview": {
                        /*
                        // Codice vecchio Weili
                        int idRec = (int) in.readObject();
                        int idUtente = (int) in.readObject();

                        // controllo se l'autore e' il ristoratore
                        List<Recensione> recensioni = database.getReviewsForOwner(idUtente);
                        boolean isAuthor = false;

                        for (Recensione r : recensioni) {
                            if (r.getId() == idRec && r.getIdUtente() == idUtente) {
                                isAuthor = true;
                                break;
                            }
                        }

                        if (!isAuthor) {
                            out.writeObject(new ServerResponse("ERROR", "Non puoi eliminare recensioni altrui"));
                            break;
                        }

                        database.deleteReview(idRec);
                        */

                        String username = (String) in.readObject();
                        int idRistorante = (int) in.readObject();

                        // Controllo se l'utente ha effettivamente già recensito questo ristorante
                        if (!database.hasUserAlreadyReviewed(username, idRistorante)) {
                            out.writeObject(new ServerResponse("ERROR", "Non hai ancora recensito questo ristorante"));
                            break;
                        }

                        database.deleteReview(username, idRistorante);
                        out.writeObject(new ServerResponse("OK", "Recensione eliminata"));
                        break;
                    }

                    case "viewReviews": {
                        int idRistorante = (int) in.readObject();
                        List<Recensione> recensioni = database.getReviews(idRistorante);

                        out.writeObject(new ServerResponse("OK", recensioni));
                        break;
                    }

                    case "answerReview": { // Solo il proprietario del ristorante può rispondere
                        String usernameRistoratore = (String) in.readObject();
                        String usernameCliente = (String) in.readObject(); // Username del cliente che ha scritto la recensione
                        int idRistorante = (int) in.readObject();
                        String risposta = (String) in.readObject();

                        // Controllo se è il proprietario del ristorante
                        if (!database.isOwnerOfRestaurant(usernameRistoratore, idRistorante)) {
                            out.writeObject(new ServerResponse("ERROR", "Non puoi rispondere a recensioni di ristoranti non tuoi"));
                            break;
                        }

                        database.answerReview(usernameCliente, idRistorante, risposta);
                        out.writeObject(new ServerResponse("OK", "Risposta salvata"));
                        break;
                    }

                    case "getReviewsForOwner": {
                        String usernameRistoratore = (String) in.readObject();
                        List<Recensione> lista = database.getReviewsForOwner(usernameRistoratore);

                        out.writeObject(new ServerResponse("OK", lista));
                        break;
                    }

                    case "getRestaurantSummary": {
                        int idRistoratore = (int) in.readObject();
                        List<?> summary = database.getRestaurantSummary(idRistoratore);

                        out.writeObject(new ServerResponse("OK", summary));
                        break;
                    }


                    
                    // PREFERITI
                    
                    case "addFavorite": {
                        String username = (String) in.readObject();
                        int idFav = (int) in.readObject();

                        database.addFavorite(username, idFav);
                        out.writeObject(new ServerResponse("OK", "Aggiunto ai preferiti"));
                        break;
                    }

                    case "removeFavorite": {
                        String username = (String) in.readObject();
                        int idFav = (int) in.readObject();

                        database.removeFavorite(username, idFav);
                        out.writeObject(new ServerResponse("OK", "Rimosso dai preferiti"));
                        break;
                    }

                    case "listFavorites": {
                        String username = (String) in.readObject();
                        List<Ristorante> list = database.listFavorites(username);

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
