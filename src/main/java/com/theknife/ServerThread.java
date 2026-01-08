package com.theknife;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
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

                        boolean ok = database.registerUser(nome, cognome, username, password, data_nascita, domicilio,
                                ruolo);

                        if (ok) {
                            out.writeObject(new ServerResponse("OK", "Registrazione completata"));
                            System.out.println("Registrazione completata!");
                        }

                        else {
                            out.writeObject(new ServerResponse("ERROR", "Errore nella registrazione"));
                            System.out.println("Errore nella registrazione");
                        }

                        break;
                    }

                    case "login": {
                        String username = (String) in.readObject();
                        String password = (String) in.readObject();

                        if (!database.validateUser(username, password)) {
                            out.writeObject(new ServerResponse("ERROR", "Credenziali errate"));
                            System.out.println("Credenziali errate");
                            break;
                        }

                        Utente u = database.getUserData(username);
                        out.writeObject(new ServerResponse("OK", u));
                        System.out.println("Login effettuato con successo!");
                        break;
                    }

                    case "updateUserInfo": {
                        String currentUsername = (String) in.readObject(); // username attuale (chiave per trovare l'utente)
                        String nome = (String) in.readObject();
                        String cognome = (String) in.readObject();
                        String dataNascita = (String) in.readObject();
                        String domicilio = (String) in.readObject();
                        String newUsername = (String) in.readObject(); // può essere uguale al currentUsername
                        String newPassword = (String) in.readObject();

                        if (database.updateUserInfo(currentUsername, nome, cognome, dataNascita, domicilio, newUsername,
                                newPassword)) {
                            out.writeObject(new ServerResponse("OK", "Dati utente aggiornati"));
                            System.out.println("Dati utente aggiornati con successo!");
                        } else {
                            out.writeObject(new ServerResponse("ERROR", "Errore nell'aggiornamento dei dati utente"));
                            System.out.println("Errore nell'aggiornamento dei dati utente");
                        }
                        out.flush();
                        break;
                    }

                    // RISTORANTI
                    case "addRestaurant": {
                        String nomeRist = (String) in.readObject();
                        String nazione = (String) in.readObject();
                        String citta = (String) in.readObject();
                        String indirizzo = (String) in.readObject();
                        double lat = (double) in.readObject();
                        double lon = (double) in.readObject();
                        boolean delivery = (boolean) in.readObject();
                        boolean prenotazione = (boolean) in.readObject();
                        String tipoCucina = (String) in.readObject();
                        String username_ristoratore = (String) in.readObject();
                        int prezzo = (int) in.readObject();

                        boolean ok = database.addRestaurant(nomeRist, nazione, citta, indirizzo, lat, lon,
                                delivery, prenotazione, tipoCucina, prezzo, username_ristoratore);

                        if (ok) {
                            out.writeObject(new ServerResponse("OK", "Ristorante aggiunto"));
                            System.out.println("Ristorante aggiunto con successo!");
                        } else {
                            out.writeObject(new ServerResponse("ERROR", "Errore nell'aggiunta del ristorante"));
                            System.out.println("Errore nell'aggiunta del ristorante");
                        }
                        break;
                    }

                    case "getMyRestaurants": {
                        String usernameRistoratore = (String) in.readObject();
                        List<Ristorante> lista = database.getMyRestaurants(usernameRistoratore);

                        out.writeObject(new ServerResponse("OK", lista));
                        System.out.println(
                                "Mostro i ristoranti del ristoratore " + usernameRistoratore + " sul client...");
                        break;
                    }

                    case "updateRestaurant": {
                        int id = (Integer) in.readObject();
                        String nome = (String) in.readObject();
                        String nazione = (String) in.readObject();
                        String citta = (String) in.readObject();
                        String indirizzo = (String) in.readObject();
                        double lat = (Double) in.readObject();
                        double lon = (Double) in.readObject();
                        boolean delivery = (Boolean) in.readObject();
                        boolean prenotazione = (Boolean) in.readObject();
                        String tipoCucina = (String) in.readObject();
                        int prezzo = (Integer) in.readObject();
                        boolean ok = database.updateRestaurant(
                                id, nome, nazione, citta, indirizzo,
                                lat, lon, delivery, prenotazione, tipoCucina, prezzo);

                        if (ok) {
                            out.writeObject(new ServerResponse("OK", "Ristorante aggiornato"));
                            System.out.println("Ristorante aggiornato con successo!");
                        } else {
                            out.writeObject(new ServerResponse("ERROR",
                                    "Errore nell'aggiornamento del ristorante (possibile duplicato)"));
                            System.out.println("Errore nell'aggiornamento del ristorante");
                        }
                        out.flush();
                        break;
                    }

                    case "deleteRestaurant": {
                        int id = (Integer) in.readObject();
                        boolean ok = database.deleteRestaurant(id);

                        if (ok) {
                            out.writeObject(new ServerResponse("OK", "Ristorante eliminato"));
                            System.out.println("Ristorante eliminato con successo!");
                        } else {
                            out.writeObject(new ServerResponse("ERROR", "Errore nell'eliminazione del ristorante"));
                            System.out.println("Errore nell'eliminazione del ristorante");
                        }
                        out.flush();
                        break;
                    }

                    /* // VECCHIO CODICE
                    case "searchRestaurants": {
                        String filtro = (String) in.readObject();
                        List<Ristorante> lista = database.searchRestaurants(filtro);

                        out.writeObject(new ServerResponse("OK", lista));
                        break;
                    }
                    */

                    case "searchRestaurants": {
                        String nome = (String) in.readObject();
                        String citta = (String) in.readObject();
                        String tipoCucina = (String) in.readObject();
                        Integer prezzoMin = (Integer) in.readObject();
                        Integer prezzoMax = (Integer) in.readObject();
                        Boolean delivery = (Boolean) in.readObject();
                        Boolean prenotazione = (Boolean) in.readObject();
                        // Manca ricerca per valutazione media stelle

                        List<Ristorante> lista = database.searchRestaurants(
                                nome, citta, tipoCucina, prezzoMin, prezzoMax, delivery, prenotazione);

                        out.writeObject(new ServerResponse("OK", lista));
                        System.out.println("Risultati della ricerca inviati al client. Numero ris: " + lista.size());
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

                    case "isOwnerOfRestaurant": {
                        String username = (String) in.readObject();
                        Integer idRistorante = (Integer) in.readObject();

                        boolean isOwner = database.isOwnerOfRestaurant(username, idRistorante);

                        out.writeObject(new ServerResponse("OK", isOwner));
                        out.flush();
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

                    case "addAnswer": {
                        String usernameOwner = (String) in.readObject();
                        int idRec = (int) in.readObject();
                        String risposta = (String) in.readObject();

                        database.addAnswer(usernameOwner, idRec, risposta);
                        out.writeObject(new ServerResponse("OK", "Risposta salvata"));
                        break;
                    }

                    case "deleteAnswer": {
                        String usernameOwner = (String) in.readObject();
                        int idRec = (int) in.readObject();

                        database.deleteAnswer(usernameOwner, idRec);
                        out.writeObject(new ServerResponse("OK", "Risposta eliminata"));
                        break;
                    }

                    case "hasReviewed": {
                        String username = (String) in.readObject();
                        int idRistorante = (int) in.readObject();

                        boolean reviewed = database.hasUserAlreadyReviewed(username, idRistorante);
                        out.writeObject(new ServerResponse("OK", reviewed));
                        break;
                    }

                    case "getMyReview": {
                        String username = (String) in.readObject();
                        int idRistorante = (int) in.readObject();

                        Recensione rec = database.getMyReview(username, idRistorante);
                        if (rec == null) {
                            out.writeObject(new ServerResponse("ERROR", "Recensione non trovata"));
                        } else {
                            out.writeObject(new ServerResponse("OK", rec));
                        }
                        break;
                    }

                    case "viewReviewUsernames": {
                        int idRistorante = (int) in.readObject();
                        List<String> usernames = database.getReviewUsernames(idRistorante);

                        out.writeObject(new ServerResponse("OK", usernames));
                        break;
                    }

                    case "getReviewsForOwner": {
                        String usernameRistoratore = (String) in.readObject();
                        List<Recensione> lista = database.getReviewsForOwner(usernameRistoratore);

                        out.writeObject(new ServerResponse("OK", lista));
                        break;
                    }

                    case "getMyReviews": {
                        String username = (String) in.readObject();
                        List<Recensione> lista = database.getMyReviews(username);

                        out.writeObject(new ServerResponse("OK", lista));
                        break;
                    }

                    case "getMyReviewRestaurantNames": {
                        String username = (String) in.readObject();
                        List<String> nomi = database.getMyReviewRestaurantNames(username);
                        
                        out.writeObject(new ServerResponse("OK", nomi));
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

                    case "isFavorite": {
                        String username = (String) in.readObject();
                        int idRistorante = (int) in.readObject();

                        boolean fav = database.isFavorite(username, idRistorante);
                        out.writeObject(new ServerResponse("OK", fav));
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
