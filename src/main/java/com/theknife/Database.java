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

import theknifeserver.Recensione;

public class Database {

    private Connection connection;

    public Database(String dbName, String usernameDB, String passwordDB) {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(
                    "jdbc:postgresql://localhost:5432/" + dbName,
                    usernameDB,
                    passwordDB
            );
            System.out.println("[DB] Connessione avvenuta con successo!");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("[DB] Errore connessione: " + e.getMessage());
        }
    }

    // UTENTI
    public boolean registerUser(String nome, String cognome, String username,
                                String password, String data_nascita, String domicilio, String ruolo) {
        try {
            if(!isUsernameFree(username)){ // Se username esiste già
                System.out.println("[DB] Errore registerUser: Username già esistente");
                return false; 
            }

            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO utenti(nome, cognome, username, password, data_nascita, domicilio, ruolo) VALUES (?, ?, ?, ?, ?, ?, ?)"
            );
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

    public boolean validateUser(String username, String password) { // Per login
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT password FROM utenti WHERE username=?"
            );
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return false; // username non esiste

            String storedHash = rs.getString("password");
            return PasswordUtil.verifyPassword(password, storedHash);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore validateUser: " + e.getMessage());
            return false;
        }
    }

    public boolean updateUserInfo(String currentUsername, String nome, String cognome, String dataNascita, String domicilio, String newUsername, String newPassword) {
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

    public Utente getUserData(String username) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT nome, cognome, data_nascita, domicilio, username, password, ruolo FROM utenti WHERE username=?"
            );
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            return new Utente(
                rs.getString("nome"),
                rs.getString("cognome"),
                rs.getString("data_nascita"),
                rs.getString("domicilio"),
                rs.getString("username"),
                rs.getString("password"),
                rs.getString("ruolo")
            );

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore getUserData: " + e.getMessage());
            return null;
        }
    }


    // RISTORANTI
    public boolean addRestaurant(String nome, String nazione, String citta, String indirizzo,
                             double lat, double lon, boolean delivery, boolean prenotazione,
                             String tipoCucina, int prezzo, String username_ristoratore) {

        try (PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO ristoranti(nome, nazione, citta, indirizzo, latitudine, longitudine, delivery, prenotazione, tipo_cucina, id_ristoratore, prezzo)" 
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, (SELECT id FROM utenti WHERE username = ?), ?)"
        )) {

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

    public List<Ristorante> getMyRestaurants(String usernameRistoratore) {
        List<Ristorante> lista = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM ristoranti WHERE id_ristoratore=(SELECT id FROM utenti WHERE username=?)"
            );
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

    public List<Ristorante> searchRestaurants(String filtro) {
        List<Ristorante> lista = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM ristoranti WHERE LOWER(nome) LIKE LOWER(?) OR LOWER(citta) LIKE LOWER(?)"
            );
            ps.setString(1, "%" + filtro + "%");
            ps.setString(2, "%" + filtro + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(buildRestaurantFromResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }


    public List<Ristorante> searchRestaurantsAdvanced(
            String citta,
            String tipoCucina,
            Integer prezzoMin,
            Integer prezzoMax,
            Boolean delivery,
            Boolean prenotazione
        ) {

        List<Ristorante> lista = new ArrayList<>();
        String query = "SELECT * FROM ristoranti WHERE 1=1 ";

        if (citta != null) query += " AND LOWER(citta) = LOWER(?) ";
        if (tipoCucina != null) query += " AND LOWER(tipo_cucina) = LOWER(?) ";
        if (prezzoMin != null) query += " AND fascia_prezzo >= ? ";
        if (prezzoMax != null) query += " AND fascia_prezzo <= ? ";
        if (delivery != null) query += " AND delivery = ? ";
        if (prenotazione != null) query += " AND prenotazione = ? ";

        try {
            PreparedStatement ps = connection.prepareStatement(query);
            int idx = 1;

            if (citta != null) ps.setString(idx++, citta);
            if (tipoCucina != null) ps.setString(idx++, tipoCucina);
            if (prezzoMin != null) ps.setInt(idx++, prezzoMin);
            if (prezzoMax != null) ps.setInt(idx++, prezzoMax);
            if (delivery != null) ps.setBoolean(idx++, delivery);
            if (prenotazione != null) ps.setBoolean(idx++, prenotazione);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) lista.add(buildRestaurantFromResultSet(rs));

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore searchRestaurantsAdvanced: " + e.getMessage());
        }

        return lista;
    }


    public Ristorante getRestaurantDetails(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM ristoranti WHERE id = ?"
            );
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

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
            rs.getString("id_ristoratore")
        );
    }


    // CONTROLLI E SUPPORTO
    public boolean isUsernameFree(String username) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM utenti WHERE username=?"
            );
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            return !rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore isUsernameFree: " + e.getMessage());
            return false;
        }
    }

    public boolean isOwnerOfRestaurant(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM ristoranti WHERE id=? AND id_ristoratore=(SELECT id FROM utenti WHERE username=?)"
            );
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

    public boolean hasUserAlreadyReviewed(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM recensioni WHERE id_utente=(SELECT id FROM utenti WHERE username=?) AND id_ristorante=?"
            );
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
    public void addReview(int idRistorante, String username, int stelle, String testo) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO recensioni(id_ristorante, id_utente, stelle, testo) VALUES (?, (SELECT id FROM utenti WHERE username=?), ?, ?)"
            );

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


    public void editReview(int idRistorante, String username, int stelle, String testo) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "UPDATE recensioni SET stelle=?, testo=? WHERE id_utente=(SELECT id FROM utenti WHERE username=?) AND id_ristorante=?"
            );
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


    public void deleteReview(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM recensioni WHERE id_utente=(SELECT id FROM utenti WHERE username=?) AND id_ristorante=?"
            );
            ps.setString(1, username);
            ps.setInt(2, idRistorante);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore deleteReview: " + e.getMessage());
        }
    }


    public List<Recensione> getReviews(int idRistorante) {
        List<Recensione> lista = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM recensioni WHERE id_ristorante=?"
            );
            ps.setInt(1, idRistorante);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Recensione(
                    rs.getInt("id"),
                    rs.getInt("id_utente"),
                    rs.getInt("stelle"),
                    rs.getString("testo"),
                    rs.getString("risposta")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }


    public List<Recensione> getReviewsForOwner(String usernameRistoratore) {
        List<Recensione> lista = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT rec.id, rec.id_utente, rec.stelle, rec.testo, rec.risposta " +
                "FROM recensioni rec " +
                "JOIN ristoranti r ON r.id = rec.id_ristorante " +
                "WHERE r.id_ristoratore=(SELECT id FROM utenti WHERE username=?)"
            );
            ps.setString(1, usernameRistoratore);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Recensione(
                    rs.getInt("id"),
                    rs.getInt("id_utente"),
                    rs.getInt("stelle"),
                    rs.getString("testo"),
                    rs.getString("risposta")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore getReviewsForOwner: " + e.getMessage());
        }

        return lista;
    }


    public void answerReview(String username, int idRistorante, String risposta) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "UPDATE recensioni SET risposta=? WHERE id_utente=(SELECT id FROM utenti WHERE username=?) AND id_ristorante=?"
            );
            ps.setString(1, risposta);
            ps.setString(2, username);
            ps.setInt(3, idRistorante);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore answerReview: " + e.getMessage());
        }
    }


    // RIEPILOGO RISTORATORE
    public List<HashMap<String, Object>> getRestaurantSummary(int ownerId) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT r.id, r.nome, " +
                "       COALESCE(AVG(rec.stelle),0) AS media_stelle, " +
                "       COUNT(rec.id) AS num_recensioni " +
                "FROM ristoranti r " +
                "LEFT JOIN recensioni rec ON rec.id_ristorante = r.id " +
                "WHERE r.id_ristoratore=? " +
                "GROUP BY r.id"
            );

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
    public void addFavorite(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO preferiti (id_utente, id_ristorante) " + 
                "SELECT u.id, ? " +
                "FROM utenti u " +
                "WHERE u.username = ? " +
                "ON CONFLICT (id_utente, id_ristorante) DO NOTHING;"
            );
            ps.setInt(1, idRistorante);
            ps.setString(2, username);

            ps.executeUpdate();

            //int rows = ps.executeUpdate();
            //if (rows == 0) { // o username non esiste, oppure era già preferito (con DO NOTHING)
            //    System.out.println("[DB] Nessun inserimento: utente inesistente o preferito già presente.");
            //}

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore addFavorite: " + e.getMessage());
        }
    }


    public void removeFavorite(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM preferiti " + 
                "WHERE id_utente=(SELECT id FROM utenti WHERE username=?) " + 
                "AND id_ristorante=?;"
            );
            ps.setString(1, username);
            ps.setInt(2, idRistorante);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore removeFavorite: " + e.getMessage());
        }
    }


    public List<Ristorante> listFavorites(String username) {
        List<Ristorante> lista = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT r.* FROM preferiti p JOIN ristoranti r ON p.id_ristorante=r.id WHERE p.id_utente=(SELECT id FROM utenti WHERE username=?)"
            );
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) lista.add(buildRestaurantFromResultSet(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    // UTILITY
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
