package com.theknife;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Gestisce l'accesso al database del server TheKnife.
 * Fornisce metodi per la gestione di utenti, ristoranti,
 * recensioni e preferiti tramite query SQL su database PostgreSQL.
 */
public class Database {

    private Connection connection;

    /**
     * Inizializza la connessione al database PostgreSQL.
     *
     * @param dbName     nome del database
     * @param usernameDB username per l'autenticazione al DB
     * @param passwordDB password per l'autenticazione al DB
     */
    public Database(String dbName, String usernameDB, String passwordDB) {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/" + dbName,
                    usernameDB,
                    passwordDB);
            System.out.println("[DB] Connessione avvenuta con successo!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[DB] Errore connessione: " + e.getMessage());
        }
    }

    // UTENTI
    /**
     * Registra un nuovo utente nel sistema.
     *
     * @param nome         nome dell'utente
     * @param cognome      cognome dell'utente
     * @param username     username scelto
     * @param password     password in chiaro (verrà cifrata)
     * @param data_nascita data di nascita (formato yyyy-MM-dd)
     * @param domicilio    domicilio dell'utente
     * @param ruolo        ruolo dell'utente (cliente o ristoratore)
     * @return true se la registrazione va a buon fine, false altrimenti
     */
    public boolean registerUser(String nome, String cognome, String username,
            String password, String data_nascita, String domicilio, String ruolo) {
        try {
            if (!isUsernameFree(username)) { // Se username esiste già
                System.out.println("[DB] Errore registerUser: Username già esistente");
                return false;
            }

            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO utenti(nome, cognome, username, password, data_nascita, domicilio, ruolo) VALUES (?, ?, ?, ?, ?, ?, ?)");
            ps.setString(1, nome);
            ps.setString(2, cognome);
            ps.setString(3, username);
            ps.setString(4, PasswordUtil.hashPassword(password));
            // data_nascita (colonna DATE)
            if (data_nascita == null || data_nascita.isBlank()) {
                ps.setNull(5, Types.DATE);
            } else {
                try {
                    LocalDate ld = LocalDate.parse(data_nascita.trim()); // yyyy-MM-dd
                    ps.setDate(5, Date.valueOf(ld));
                } catch (DateTimeParseException ex) {
                    System.out.println("[DB] Errore registerUser: data_nascita non valida: " + data_nascita);
                    return false;
                }
            }
            ps.setString(6, domicilio);
            ps.setString(7, ruolo.toLowerCase());

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore registerUser: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica la validità delle credenziali di accesso di un utente.
     *
     * @param username username dell'utente
     * @param password password in chiaro inserita
     * @return true se le credenziali sono corrette, false altrimenti
     */
    public boolean validateUser(String username, String password) { // Per login
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT password FROM utenti WHERE username=?");
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return false; // username non esiste

            String storedHash = rs.getString("password");
            return PasswordUtil.verifyPassword(password, storedHash);
            // return true; // PER TESTING SENZA PASSWORD

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore validateUser: " + e.getMessage());
            return false;
        }
    }

    /**
     * Aggiorna i dati personali di un utente.
     *
     * @param currentUsername username attuale dell'utente
     * @param nome            nuovo nome
     * @param cognome         nuovo cognome
     * @param dataNascita     nuova data di nascita
     * @param domicilio       nuovo domicilio
     * @param newUsername     nuovo username
     * @param newPassword     nuova password (opzionale)
     * @return true se l'aggiornamento va a buon fine, false altrimenti
     */
    public boolean updateUserInfo(String currentUsername, String nome, String cognome, String dataNascita,
            String domicilio, String newUsername, String newPassword) {
        try {
            // Controlla se il nuovo username è diverso e già esistente
            if (newUsername != null && !newUsername.equals(currentUsername)) {
                if (!isUsernameFree(newUsername)) {
                    System.out.println("[DB] updateUserInfo: newUsername già esistente");
                    return false;
                }
            }

            boolean changePassword = (newPassword != null && !newPassword.isBlank());
            String sql;

            if (changePassword) {
                sql = "UPDATE utenti SET nome=?, cognome=?, data_nascita=?, domicilio=?, username=?, password=? WHERE username=?";
            } else {
                sql = "UPDATE utenti SET nome=?, cognome=?, data_nascita=?, domicilio=?, username=? WHERE username=?";
            }

            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, nome);
            ps.setString(2, cognome);
            if (dataNascita == null || dataNascita.isBlank()) {
                ps.setNull(3, Types.DATE);
            } else {
                LocalDate ld = LocalDate.parse(dataNascita);
                ps.setDate(3, Date.valueOf(ld));
            }
            ps.setString(4, domicilio);
            ps.setString(5, newUsername);
            if (changePassword) {
                ps.setString(6, PasswordUtil.hashPassword(newPassword));
                ps.setString(7, currentUsername);
            } else {
                ps.setString(6, currentUsername);
            }

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[DB] Errore updateUserInfo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Recupera i dati di un utente registrato.
     *
     * @param username username dell'utente
     * @return oggetto {@link Utente} con i dati dell'utente, oppure null se non
     *         trovato
     */
    public Utente getUserData(String username) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT nome, cognome, data_nascita, domicilio, username, password, ruolo FROM utenti WHERE username=?");
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;

            return new Utente(
                    rs.getString("nome"),
                    rs.getString("cognome"),
                    rs.getString("data_nascita"),
                    rs.getString("domicilio"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("ruolo"));

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore getUserData: " + e.getMessage());
            return null;
        }
    }

    // RISTORANTI
    /**
     * Aggiunge un nuovo ristorante associato a un ristoratore.
     *
     * @param nome                 nome del ristorante
     * @param nazione              nazione
     * @param citta                città
     * @param indirizzo            indirizzo
     * @param lat                  latitudine
     * @param lon                  longitudine
     * @param delivery             disponibilità servizio delivery
     * @param prenotazione         disponibilità prenotazione online
     * @param tipoCucina           tipologia di cucina
     * @param prezzo               prezzo medio
     * @param username_ristoratore username del ristoratore
     * @return true se il ristorante viene aggiunto correttamente
     */
    public boolean addRestaurant(String nome, String nazione, String citta, String indirizzo,
            double lat, double lon, boolean delivery, boolean prenotazione,
            String tipoCucina, int prezzo, String username_ristoratore) {

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO ristoranti(nome, nazione, citta, indirizzo, latitudine, longitudine, delivery, prenotazione, tipo_cucina, id_ristoratore, prezzo)"
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, (SELECT id FROM utenti WHERE username = ?), ?)")) {

            ps.setString(1, nome);
            ps.setString(2, nazione);
            ps.setString(3, citta);
            ps.setString(4, indirizzo);
            ps.setDouble(5, lat);
            ps.setDouble(6, lon);
            ps.setBoolean(7, delivery);
            ps.setBoolean(8, prenotazione);
            ps.setString(9, tipoCucina);

            // qui prima username (per la subquery), poi prezzo
            ps.setString(10, username_ristoratore);
            ps.setInt(11, prezzo);

            int rows = ps.executeUpdate();
            return rows == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore addRestaurant: " + e.getMessage());
            return false;
        }
    }

    /**
     * Restituisce l'elenco dei ristoranti di un ristoratore.
     *
     * @param usernameRistoratore username del ristoratore
     * @return lista dei ristoranti gestiti
     */
    public List<Ristorante> getMyRestaurants(String usernameRistoratore) {
        List<Ristorante> lista = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM ristoranti WHERE id_ristoratore=(SELECT id FROM utenti WHERE username=?)");
            ps.setString(1, usernameRistoratore);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(buildRestaurantFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Aggiorna i dati di un ristorante esistente.
     *
     * @param id           identificativo del ristorante
     * @param nome         nuovo nome
     * @param nazione      nuova nazione
     * @param citta        nuova città
     * @param indirizzo    nuovo indirizzo
     * @param lat          nuova latitudine
     * @param lon          nuova longitudine
     * @param delivery     nuovo stato delivery
     * @param prenotazione nuovo stato prenotazione
     * @param tipoCucina   nuova tipologia di cucina
     * @param prezzo       nuovo prezzo medio
     * @return true se l'aggiornamento va a buon fine
     */
    public boolean updateRestaurant(int id, String nome, String nazione, String citta, String indirizzo, double lat,
            double lon,
            boolean delivery, boolean prenotazione, String tipoCucina, int prezzo) {
        try {
            // Controllo se esiste un duplicato con stesso nome, città e indirizzo, ma non
            // lo stesso id
            PreparedStatement check = connection.prepareStatement(
                    "SELECT id FROM ristoranti WHERE LOWER(TRIM(nome)) = LOWER(TRIM(?)) AND LOWER(TRIM(citta)) = LOWER(TRIM(?)) AND LOWER(TRIM(indirizzo)) = LOWER(TRIM(?)) AND id <> ?");
            check.setString(1, nome);
            check.setString(2, citta);
            check.setString(3, indirizzo);
            check.setInt(4, id);

            ResultSet rs = check.executeQuery();
            if (rs.next()) {
                System.out.println("[DB] Update bloccato: duplicato trovato con stesso nome, città e indirizzo");
                return false;
            }

            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE ristoranti SET nome=?, nazione=?, citta=?, indirizzo=?, latitudine=?, longitudine=?, delivery=?, prenotazione=?, tipo_cucina=?, prezzo=? WHERE id=?");
            ps.setString(1, nome);
            ps.setString(2, nazione);
            ps.setString(3, citta);
            ps.setString(4, indirizzo);
            ps.setDouble(5, lat);
            ps.setDouble(6, lon);
            ps.setBoolean(7, delivery);
            ps.setBoolean(8, prenotazione);
            ps.setString(9, tipoCucina);
            ps.setInt(10, prezzo);
            ps.setInt(11, id);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore updateRestaurant: " + e.getMessage());
            return false;
        }
    }

    /**
     * Elimina un ristorante dal sistema.
     *
     * @param id identificativo del ristorante
     * @return true se l'eliminazione va a buon fine
     */
    public boolean deleteRestaurant(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM ristoranti WHERE id=?");
            ps.setInt(1, id);

            return ps.executeUpdate() == 1;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore deleteRestaurant: " + e.getMessage());
            return false;
        }
    }

    /**
     * Effettua una ricerca avanzata di ristoranti in base a più filtri.
     *
     * @param nome         nome del ristorante
     * @param citta        città
     * @param tipoCucina   tipologia di cucina
     * @param prezzoMin    prezzo minimo
     * @param prezzoMax    prezzo massimo
     * @param delivery     filtro delivery
     * @param prenotazione filtro prenotazione
     * @param votoMin      voto medio minimo
     * @return lista dei ristoranti che soddisfano i criteri
     */
    public List<Ristorante> searchRestaurants(
            String nome,
            String citta,
            String tipoCucina,
            Integer prezzoMin,
            Integer prezzoMax,
            Boolean delivery,
            Boolean prenotazione,
            Double votoMin) {

        List<Ristorante> lista = new ArrayList<>();

        String query = "SELECT r.* " +
                "FROM ristoranti r " +
                "LEFT JOIN ( " +
                "   SELECT id_ristorante, AVG(stelle) AS media " +
                "   FROM recensioni " +
                "   GROUP BY id_ristorante " +
                ") a ON a.id_ristorante = r.id " +
                "WHERE 1=1 ";

        if (nome != null)
            query += " AND LOWER(r.nome) LIKE LOWER(?) ";
        if (citta != null)
            query += " AND LOWER(r.citta) LIKE LOWER(?) ";
        if (tipoCucina != null)
            query += " AND LOWER(r.tipo_cucina) = LOWER(?) ";
        if (prezzoMin != null)
            query += " AND r.prezzo >= ? ";
        if (prezzoMax != null)
            query += " AND r.prezzo <= ? ";
        if (delivery != null)
            query += " AND r.delivery = ? ";
        if (prenotazione != null)
            query += " AND r.prenotazione = ? ";
        if (votoMin != null)
            query += " AND COALESCE(a.media, 0) >= ? ";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            int idx = 1;

            if (nome != null)
                ps.setString(idx++, "%" + nome + "%");
            if (citta != null)
                ps.setString(idx++, "%" + citta + "%");
            if (tipoCucina != null)
                ps.setString(idx++, tipoCucina);
            if (prezzoMin != null)
                ps.setInt(idx++, prezzoMin);
            if (prezzoMax != null)
                ps.setInt(idx++, prezzoMax);
            if (delivery != null)
                ps.setBoolean(idx++, delivery);
            if (prenotazione != null)
                ps.setBoolean(idx++, prenotazione);
            if (votoMin != null)
                ps.setDouble(idx++, votoMin);

            ResultSet rs = ps.executeQuery();
            while (rs.next())
                lista.add(buildRestaurantFromResultSet(rs));

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore searchRestaurantsAdvanced: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Recupera i dettagli completi di un ristorante.
     *
     * @param id identificativo del ristorante
     * @return oggetto {@link Ristorante} oppure null se non trovato
     */
    public Ristorante getRestaurantDetails(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM ristoranti WHERE id = ?");
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;

            return buildRestaurantFromResultSet(rs);

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private Ristorante buildRestaurantFromResultSet(ResultSet rs) throws SQLException {
        return new Ristorante(
                rs.getInt("id"),
                rs.getString("nome"),
                rs.getString("nazione"),
                rs.getString("citta"),
                rs.getString("indirizzo"),
                rs.getDouble("latitudine"),
                rs.getDouble("longitudine"),
                rs.getInt("prezzo"),
                rs.getBoolean("delivery"),
                rs.getBoolean("prenotazione"),
                rs.getString("tipo_cucina"),
                rs.getString("id_ristoratore"));
    }

    /**
     * Calcola la valutazione media di un ristorante.
     *
     * @param idRistorante identificativo del ristorante
     * @return media delle stelle
     */
    public double getRestaurantAvgRating(int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT COALESCE(AVG(stelle), 0) AS media " +
                            "FROM recensioni " +
                            "WHERE id_ristorante = ?");
            ps.setInt(1, idRistorante);

            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getDouble("media");
            return 0.0;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore getRestaurantAvgRating: " + e.getMessage());
            return 0.0;
        }
    }

    // CONTROLLI E SUPPORTO
    /**
     * Verifica se uno username è disponibile (non presente in tabella utenti).
     *
     * @param username username da controllare
     * @return true se lo username non è presente nel database, false altrimenti
     */
    public boolean isUsernameFree(String username) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT 1 FROM utenti WHERE username=?");
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            return !rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore isUsernameFree: " + e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se un utente (ristoratore) è proprietario di un determinato
     * ristorante.
     *
     * @param username     username del ristoratore
     * @param idRistorante identificativo del ristorante
     * @return true se l'utente risulta proprietario del ristorante, false
     *         altrimenti
     */
    public boolean isOwnerOfRestaurant(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT 1 FROM ristoranti WHERE id=? AND id_ristoratore=(SELECT id FROM utenti WHERE username=?)");
            ps.setInt(1, idRistorante);
            ps.setString(2, username);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore isOwnerOfRestaurant: " + e.getMessage());
            return false;
        }
    }

    /**
     * Controlla se un utente ha già inserito una recensione per un determinato
     * ristorante.
     *
     * @param username     username dell'utente
     * @param idRistorante identificativo del ristorante
     * @return true se esiste già una recensione dell'utente per quel ristorante,
     *         false altrimenti
     */
    public boolean hasUserAlreadyReviewed(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT 1 FROM recensioni WHERE id_utente=(SELECT id FROM utenti WHERE username=?) AND id_ristorante=?");
            ps.setString(1, username);
            ps.setInt(2, idRistorante);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore hasUserAlreadyReviewed: " + e.getMessage());
            return false;
        }
    }

    // RECENSIONI
    /**
     * Aggiunge una nuova recensione a un ristorante.
     *
     * @param idRistorante identificativo del ristorante
     * @param username     username dell'utente
     * @param stelle       valutazione (1–5)
     * @param testo        testo della recensione
     */
    public void addReview(int idRistorante, String username, int stelle, String testo) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO recensioni(id_ristorante, id_utente, stelle, testo) VALUES (?, (SELECT id FROM utenti WHERE username=?), ?, ?)");

            ps.setInt(1, idRistorante);
            ps.setString(2, username);
            ps.setInt(3, stelle);
            ps.setString(4, testo);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore addReview: " + e.getMessage());
        }
    }

    /**
     * Modifica una recensione esistente.
     *
     * @param idRistorante identificativo del ristorante
     * @param username     username dell'utente
     * @param stelle       nuova valutazione
     * @param testo        nuovo testo
     */
    public void editReview(int idRistorante, String username, int stelle, String testo) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE recensioni SET stelle=?, testo=? WHERE id_utente=(SELECT id FROM utenti WHERE username=?) AND id_ristorante=?");
            ps.setInt(1, stelle);
            ps.setString(2, testo);
            ps.setString(3, username);
            ps.setInt(4, idRistorante);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore editReview: " + e.getMessage());
        }
    }

    /**
     * Elimina una recensione effettuata da un utente.
     *
     * @param username     username dell'utente
     * @param idRistorante identificativo del ristorante
     */
    public void deleteReview(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM recensioni WHERE id_utente=(SELECT id FROM utenti WHERE username=?) AND id_ristorante=?");
            ps.setString(1, username);
            ps.setInt(2, idRistorante);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore deleteReview: " + e.getMessage());
        }
    }

    /**
     * Restituisce tutte le recensioni di un ristorante.
     *
     * @param idRistorante identificativo del ristorante
     * @return lista delle recensioni
     */
    public List<Recensione> getReviews(int idRistorante) {
        List<Recensione> lista = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM recensioni WHERE id_ristorante=? ORDER BY id DESC");
            ps.setInt(1, idRistorante);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Recensione r = new Recensione(
                        rs.getInt("id"),
                        rs.getInt("id_utente"),
                        rs.getInt("stelle"),
                        rs.getString("testo"),
                        rs.getString("risposta"));
                r.setIdRistorante(rs.getInt("id_ristorante"));
                lista.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Restituisce tutte le recensioni ricevute dai ristoranti di un ristoratore.
     *
     * @param usernameRistoratore username del ristoratore
     * @return lista delle recensioni
     */
    public List<Recensione> getReviewsForOwner(String usernameRistoratore) {
        List<Recensione> lista = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT rec.id, rec.id_ristorante, rec.id_utente, rec.stelle, rec.testo, rec.risposta " +
                            "FROM recensioni rec " +
                            "JOIN ristoranti r ON r.id = rec.id_ristorante " +
                            "WHERE r.id_ristoratore=(SELECT id FROM utenti WHERE username=?) " +
                            "ORDER BY rec.id DESC");
            ps.setString(1, usernameRistoratore);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Recensione rec = new Recensione(
                        rs.getInt("id"),
                        rs.getInt("id_utente"),
                        rs.getInt("stelle"),
                        rs.getString("testo"),
                        rs.getString("risposta"));
                rec.setIdRistorante(rs.getInt("id_ristorante"));
                lista.add(rec);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore getReviewsForOwner: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Restituisce tutte le recensioni scritte da un utente.
     *
     * @param username username dell'utente
     * @return lista delle recensioni
     */
    public List<Recensione> getMyReviews(String username) {
        List<Recensione> lista = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT rec.id, rec.id_ristorante, rec.id_utente, rec.stelle, rec.testo, rec.risposta " +
                            "FROM recensioni rec " +
                            "WHERE rec.id_utente=(SELECT id FROM utenti WHERE username=?) " +
                            "ORDER BY rec.id DESC");
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Recensione r = new Recensione(
                        rs.getInt("id"),
                        rs.getInt("id_utente"),
                        rs.getInt("stelle"),
                        rs.getString("testo"),
                        rs.getString("risposta"));
                r.setIdRistorante(rs.getInt("id_ristorante"));
                lista.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore getMyReviews: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Aggiunge (o aggiorna) la risposta del ristoratore a una recensione.
     * L'operazione è consentita solo se il ristoratore è proprietario del
     * ristorante
     * associato alla recensione.
     *
     * @param usernameOwner username del ristoratore proprietario
     * @param idRecensione  identificativo della recensione
     * @param risposta      testo della risposta
     */
    public void addAnswer(String usernameOwner, int idRecensione, String risposta) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE recensioni rec " +
                            "SET risposta=? " +
                            "FROM ristoranti r " +
                            "WHERE rec.id=? AND r.id=rec.id_ristorante " +
                            "AND r.id_ristoratore=(SELECT id FROM utenti WHERE username=?)");
            ps.setString(1, risposta);
            ps.setInt(2, idRecensione);
            ps.setString(3, usernameOwner);
            int updated = ps.executeUpdate();
            if (updated == 0)
                System.out.println("[DB] addAnswerByReviewId: nessun record aggiornato (non owner o id errato)");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore addAnswerByReviewId: " + e.getMessage());
        }
    }

    /**
     * Elimina la risposta del ristoratore da una recensione (impostandola a NULL).
     * L'operazione è consentita solo se il ristoratore è proprietario del
     * ristorante
     * associato alla recensione.
     *
     * @param usernameOwner username del ristoratore proprietario
     * @param idRecensione  identificativo della recensione
     */
    public void deleteAnswer(String usernameOwner, int idRecensione) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE recensioni rec " +
                            "SET risposta=NULL " +
                            "FROM ristoranti r " +
                            "WHERE rec.id=? AND r.id=rec.id_ristorante " +
                            "AND r.id_ristoratore=(SELECT id FROM utenti WHERE username=?)");
            ps.setInt(1, idRecensione);
            ps.setString(2, usernameOwner);
            int updated = ps.executeUpdate();
            if (updated == 0)
                System.out.println("[DB] deleteAnswerByReviewId: nessun record aggiornato (non owner o id errato)");
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore deleteAnswerByReviewId: " + e.getMessage());
        }
    }

    /**
     * Recupera la recensione di un utente per uno specifico ristorante.
     *
     * @param username     username dell'utente
     * @param idRistorante identificativo del ristorante
     * @return recensione trovata oppure null
     */
    public Recensione getMyReview(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM recensioni " +
                            "WHERE id_utente = (SELECT id FROM utenti WHERE username=?) " +
                            "AND id_ristorante=? " +
                            "LIMIT 1;");
            ps.setString(1, username);
            ps.setInt(2, idRistorante);

            ResultSet rs = ps.executeQuery();
            if (!rs.next())
                return null;

            Recensione r = new Recensione(
                    rs.getInt("id"),
                    rs.getInt("id_utente"),
                    rs.getInt("stelle"),
                    rs.getString("testo"),
                    rs.getString("risposta"));
            r.setIdRistorante(rs.getInt("id_ristorante"));
            return r;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore getMyReview: " + e.getMessage());
            return null;
        }
    }

    /**
     * Restituisce la lista degli username degli utenti che hanno recensito un
     * ristorante.
     *
     * @param idRistorante identificativo del ristorante
     * @return lista di username ordinata per recensioni più recenti
     */
    public List<String> getReviewUsernames(int idRistorante) {
        List<String> lista = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT u.username " +
                            "FROM recensioni rec " +
                            "JOIN utenti u ON u.id = rec.id_utente " +
                            "WHERE rec.id_ristorante = ? " +
                            "ORDER BY rec.id DESC");
            ps.setInt(1, idRistorante);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(rs.getString("username"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore getReviewUsernames: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Restituisce i nomi dei ristoranti che sono stati recensiti da un utente.
     *
     * @param username username dell'utente
     * @return lista dei nomi dei ristoranti recensiti
     */
    public List<String> getMyReviewRestaurantNames(String username) {
        List<String> lista = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT r.nome " +
                            "FROM recensioni rec " +
                            "JOIN ristoranti r ON r.id = rec.id_ristorante " +
                            "WHERE rec.id_utente=(SELECT id FROM utenti WHERE username=?) " +
                            "ORDER BY rec.id DESC");
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(rs.getString("nome"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore getMyReviewRestaurantNames: " + e.getMessage());
        }

        return lista;
    }

    // RIEPILOGO RISTORATORE
    /**
     * Restituisce un riepilogo dei ristoranti di un ristoratore con statistiche
     * aggregate.
     * Per ogni ristorante vengono calcolati media delle stelle e numero di
     * recensioni.
     *
     * @param ownerId identificativo del ristoratore (id in tabella utenti)
     * @return lista di righe (mappa chiave/valore) contenenti id, nome,
     *         media_stelle e num_recensioni
     */
    public List<HashMap<String, Object>> getRestaurantSummary(int ownerId) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT r.id, r.nome, " +
                            "       COALESCE(AVG(rec.stelle),0) AS media_stelle, " +
                            "       COUNT(rec.id) AS num_recensioni " +
                            "FROM ristoranti r " +
                            "LEFT JOIN recensioni rec ON rec.id_ristorante = r.id " +
                            "WHERE r.id_ristoratore=? " +
                            "GROUP BY r.id");

            ps.setInt(1, ownerId);
            ResultSet rs = ps.executeQuery();
            return resultSetToList(rs);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore getRestaurantSummary: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // PREFERITI
    /**
     * Aggiunge un ristorante ai preferiti di un utente.
     *
     * @param username     username dell'utente
     * @param idRistorante identificativo del ristorante
     */
    public void addFavorite(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO preferiti (id_utente, id_ristorante) " +
                            "SELECT u.id, ? " +
                            "FROM utenti u " +
                            "WHERE u.username = ? " +
                            "ON CONFLICT (id_utente, id_ristorante) DO NOTHING;");
            ps.setInt(1, idRistorante);
            ps.setString(2, username);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore addFavorite: " + e.getMessage());
        }
    }

    /**
     * Rimuove un ristorante dai preferiti di un utente.
     *
     * @param username     username dell'utente
     * @param idRistorante identificativo del ristorante
     */
    public void removeFavorite(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM preferiti " +
                            "WHERE id_utente=(SELECT id FROM utenti WHERE username=?) " +
                            "AND id_ristorante=?;");
            ps.setString(1, username);
            ps.setInt(2, idRistorante);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore removeFavorite: " + e.getMessage());
        }
    }

    /**
     * Restituisce la lista dei ristoranti preferiti di un utente.
     *
     * @param username username dell'utente
     * @return lista dei ristoranti preferiti
     */
    public List<Ristorante> listFavorites(String username) {
        List<Ristorante> lista = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT r.* FROM preferiti p JOIN ristoranti r ON p.id_ristorante=r.id WHERE p.id_utente=(SELECT id FROM utenti WHERE username=?)");
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            while (rs.next())
                lista.add(buildRestaurantFromResultSet(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Verifica se un ristorante è presente tra i preferiti di un utente.
     *
     * @param username     username dell'utente
     * @param idRistorante identificativo del ristorante
     * @return true se il ristorante è tra i preferiti
     */
    public boolean isFavorite(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT 1 " +
                            "FROM preferiti p " +
                            "WHERE p.id_utente = (SELECT id FROM utenti WHERE username = ?) " +
                            "AND p.id_ristorante = ? " +
                            "LIMIT 1;");
            ps.setString(1, username);
            ps.setInt(2, idRistorante);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore isFavorite: " + e.getMessage());
            return false;
        }
    }

    // UTILITY
    /**
     * Converte un {@link ResultSet} in una lista di mappe chiave/valore.
     * Ogni riga del result set viene trasformata in una {@link HashMap} dove
     * la chiave è il nome della colonna e il valore è l'oggetto corrispondente.
     *
     * @param rs result set da convertire
     * @return lista di righe convertite in mappe
     */
    public List<HashMap<String, Object>> resultSetToList(ResultSet rs) {
        List<HashMap<String, Object>> list = new ArrayList<>();

        try {
            while (rs.next()) {
                HashMap<String, Object> row = new HashMap<>();
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                for (int i = 1; i <= colCount; i++) {
                    row.put(meta.getColumnName(i), rs.getObject(i));
                }

                list.add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore resultSetToList: " + e.getMessage());
        }

        return list;
    }
}
