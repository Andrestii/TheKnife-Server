package com.theknife;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import theknifeserver.Recensione;
import theknifeserver.Ristorante;
import theknifeserver.Utente;

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
            ps.setString(4, password);
            ps.setString(5, data_nascita);
            ps.setString(6, domicilio);
            ps.setString(7, ruolo);

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
                    "SELECT 1 FROM utenti WHERE username=? AND password=?"
            );
            ps.setString(1, username);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore validateUser: " + e.getMessage());
            return false;
        }
    }

    public void modifyUserNomeCognome(String username, String nome, String cognome) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE utenti SET nome=?, cognome=? WHERE username=?"
            );
            ps.setString(1, nome);
            ps.setString(2, cognome);
            ps.setString(3, username);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore modifyUserNomeCognome: " + e.getMessage());
        }
    }

    public void modifyUserDate(String username, String data) { // Formato data per SQL: YYYY-MM-DD
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE utenti SET data_nascita=? WHERE username=?"
            );
            ps.setString(1, data);
            ps.setString(2, username);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore modifyUserDate: " + e.getMessage());
        }
    }

    public void modifyUserDomicilio(String username, String domicilio) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE utenti SET domicilio=? WHERE username=?"
            );
            ps.setString(1, domicilio);
            ps.setString(2, username);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore modifyUserDomicilio: " + e.getMessage());
        }
    }

    public void modifyUserUsername(String username, String newUsername) {
        try {
            if(!isUsernameFree(newUsername)){ // Se username esiste già
                System.out.println("[DB] Errore modifyUserUsername: Username già esistente");
                return; 
            }

            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE utenti SET username=? WHERE username=?"
            );
            ps.setString(1, newUsername);
            ps.setString(2, username);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore modifyUserUsername: " + e.getMessage());
        }
    }

    public void modifyUserPassword(String username, String password) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "UPDATE utenti SET password=? WHERE username=?"
            );
            ps.setString(1, password);
            ps.setString(2, username);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore modifyUserPassword: " + e.getMessage());
        }
    }

    public Utente getUserData(String username) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT nome, cognome, username, ruolo, domicilio, data_nascita FROM utenti WHERE username=?"
            );
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            if (!rs.next()) return null;

            return new Utente(
                rs.getString("username"),
                rs.getString("ruolo"),
                rs.getString("domicilio"),
                rs.getString("nome"),
                rs.getString("cognome"),
                rs.getString("data_nascita")
            );

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore getUserData: " + e.getMessage());
            return null;
        }
    }


    // RISTORANTI
    public void addRestaurant(int id_ristoratore, String nome, String nazione, String citta,
                            String indirizzo, double lat, double lon, int prezzo,
                            boolean delivery, boolean prenotazione, String tipoCucina) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO ristoranti(id_ristoratore, nome, nazione, citta, indirizzo, latitudine, longitudine, fascia_prezzo, delivery, prenotazione, tipo_cucina) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );

            ps.setInt(1, id_ristoratore);
            ps.setString(2, nome);
            ps.setString(3, nazione);
            ps.setString(4, citta);
            ps.setString(5, indirizzo);
            ps.setDouble(6, lat);
            ps.setDouble(7, lon);
            ps.setInt(8, prezzo);
            ps.setBoolean(9, delivery);
            ps.setBoolean(10, prenotazione);
            ps.setString(11, tipoCucina);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore addRestaurant: " + e.getMessage());
        }
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
            rs.getInt("fascia_prezzo"),
            rs.getBoolean("delivery"),
            rs.getBoolean("prenotazione"),
            rs.getString("tipo_cucina"),
            rs.getString("id_ristoratore")
        );
    }


    // CONTROLLI
    public boolean isUsernameFree(String username) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM utenti WHERE username=?"
            );
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore isOwnerOfRestaurant: " + e.getMessage());
            return false;
        }
    }

    public boolean isOwnerOfRestaurant(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM ristoranti WHERE id=? AND id_ristoratore=?"
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

    public boolean hasUserAlreadyReviewed(int idRecensore, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM recensioni WHERE id_recensore=? AND id_ristorante=?"
            );
            ps.setInt(1, idRecensore);
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
    public void addReview(int idRistorante, int idRecensore, int stelle, String testo) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO recensioni(id_ristorante, id_utente, stelle, testo) VALUES (?, ?, ?, ?)"
            );

            ps.setInt(1, idRistorante);
            ps.setInt(2, idRecensore);
            ps.setInt(3, stelle);
            ps.setString(4, testo);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore addReview: " + e.getMessage());
        }
    }


    public void editReview(int idRecensione, int stelle, String testo) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "UPDATE recensioni SET stelle=?, testo=? WHERE id=?"
            );
            ps.setInt(1, stelle);
            ps.setString(2, testo);
            ps.setInt(3, idRecensione);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore editReview: " + e.getMessage());
        }
    }


    public void deleteReview(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM recensioni WHERE id=?"
            );
            ps.setInt(1, id);
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


    public List<Recensione> getReviewsForOwner(int ownerId) {
        List<Recensione> lista = new ArrayList<>();

        try {
            PreparedStatement ps = connection.prepareStatement(
                "SELECT rec.id, rec.id_utente, rec.stelle, rec.testo, rec.risposta " +
                "FROM recensioni rec " +
                "JOIN ristoranti r ON r.id = rec.id_ristorante " +
                "WHERE r.id_ristoratore=?"
            );
            ps.setInt(1, ownerId);

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


    public void answerReview(int idRecensione, String risposta) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                "UPDATE recensioni SET risposta=? WHERE id=?"
            );
            ps.setString(1, risposta);
            ps.setInt(2, idRecensione);

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
