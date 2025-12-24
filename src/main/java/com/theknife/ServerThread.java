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

                        if (!database.validateUser(user, pass)) {
                            out.writeObject(new ServerResponse("ERROR", "Credenziali errate"));
                            break;
                        }

                        Utente u = database.getUserData(user);
                        out.writeObject(new ServerResponse("OK", u));

                        break;
                    }

                    case "addUserDate": {
                        String username = (String) in.readObject();
                        String dataNascita = (String) in.readObject();

                        database.addUserDate(username, dataNascita);
                        out.writeObject(new ServerResponse("OK", "Data di nascita utente aggiunta"));
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

                        database.addRestaurant(id_ristoratore, nomeRist, nazione, citta, indirizzo,
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
                        int idRistorante = (int) in.readObject();
                        int idRecensore = (int) in.readObject();
                        int stelle = (int) in.readObject();
                        String testo = (String) in.readObject();

                        // controllo: esiste già una recensione di questo utente?
                        if (database.hasUserAlreadyReviewed(idRecensore, idRistorante)) {
                            out.writeObject(new ServerResponse("ERROR",
                                    "Hai già recensito questo ristorante"));
                            break;
                        }

                        database.addReview(idRistorante, idRecensore, stelle, testo);
                        out.writeObject(new ServerResponse("OK", "Recensione aggiunta"));
                        break;
                    }

                    case "editReview": {
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
                        out.writeObject(new ServerResponse("OK", "Recensione modificata"));
                        break;
                    }

                    case "deleteReview": {
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
                        String owner = (String) in.readObject();
                        int idRec = (int) in.readObject();
                        int idRist = (int) in.readObject();
                        String risposta = (String) in.readObject();

                        // controllo: è il proprietario?
                        if (!database.isOwnerOfRestaurant(owner, idRist)) {
                            out.writeObject(new ServerResponse("ERROR",
                                    "Non puoi rispondere a recensioni di ristoranti non tuoi"));
                            break;
                        }

                        database.answerReview(idRec, risposta);
                        out.writeObject(new ServerResponse("OK", "Risposta salvata"));
                        break;
                    }

                    case "getReviewsForOwner": {
                        int idRistoratore = (int) in.readObject();
                        List<Recensione> lista = database.getReviewsForOwner(idRistoratore);

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
                        int idUser = (int) in.readObject();
                        int idFav = (int) in.readObject();

                        database.addFavorite(idUser, idFav);
                        out.writeObject(new ServerResponse("OK", "Aggiunto ai preferiti"));
                        break;
                    }

                    case "removeFavorite": {
                        int idUser = (int) in.readObject();
                        int idFav = (int) in.readObject();

                        database.removeFavorite(idUser, idFav);
                        out.writeObject(new ServerResponse("OK", "Rimosso dai preferiti"));
                        break;
                    }

                    case "listFavorites": {
                        int idUser = (int) in.readObject();
                        List<Ristorante> list = database.listFavorites(idUser);

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
