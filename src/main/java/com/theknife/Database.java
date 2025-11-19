package com.theknife;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
                                String password, String ruolo, String domicilio) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO utenti(nome, cognome, username, password, ruolo, domicilio) VALUES (?, ?, ?, ?, ?, ?)"
            );
            ps.setString(1, nome);
            ps.setString(2, cognome);
            ps.setString(3, username);
            ps.setString(4, password);
            ps.setString(5, ruolo);
            ps.setString(6, domicilio);

            ps.executeUpdate();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore registerUser: " + e.getMessage());
            return false;
        }
    }

    public boolean validateUser(String username, String password) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM utenti WHERE username=? AND password=?"
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

    // RISTORANTI

    public void addRestaurant(String owner, String nome, String nazione, String citta,
                              String indirizzo, double lat, double lon, int prezzo,
                              boolean delivery, boolean prenotazione, String tipoCucina) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO ristoranti(owner_username, nome, nazione, citta, indirizzo, lat, lon, prezzo, delivery, prenotazione, tipo_cucina) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
            );

            ps.setString(1, owner);
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

    public ResultSet searchRestaurants(String filtro) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM ristoranti WHERE LOWER(nome) LIKE LOWER(?) OR LOWER(citta) LIKE LOWER(?)"
            );
            ps.setString(1, "%" + filtro + "%");
            ps.setString(2, "%" + filtro + "%");

            return ps.executeQuery();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore searchRestaurants: " + e.getMessage());
            return null;
        }
    }

    public HashMap<String, Object> getRestaurantDetails(int id) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM ristoranti WHERE id = ?"
            );
            ps.setInt(1, id);

            ResultSet rs = ps.executeQuery();

            if (!rs.next()) return null;

            HashMap<String, Object> m = new HashMap<>();
            m.put("id", rs.getInt("id"));
            m.put("nome", rs.getString("nome"));
            m.put("citta", rs.getString("citta"));
            m.put("prezzo", rs.getInt("prezzo"));
            m.put("tipo_cucina", rs.getString("tipo_cucina"));
            m.put("delivery", rs.getBoolean("delivery"));
            m.put("prenotazione", rs.getBoolean("prenotazione"));
            m.put("lat", rs.getDouble("lat"));
            m.put("lon", rs.getDouble("lon"));

            return m;

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore getRestaurantDetails: " + e.getMessage());
            return null;
        }
    }

    // RECENSIONI

    public void addReview(int idRistorante, String username, int stelle, String testo) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO recensioni(id_ristorante, username, stelle, testo) VALUES (?, ?, ?, ?)"
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

    public List<HashMap<String, Object>> getReviews(int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * FROM recensioni WHERE id_ristorante=?"
            );
            ps.setInt(1, idRistorante);

            ResultSet rs = ps.executeQuery();
            return resultSetToList(rs);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore getReviews: " + e.getMessage());
            return null;
        }
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


    // PREFERITI

    public void addFavorite(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO preferiti(username, id_ristorante) VALUES (?, ?)"
            );
            ps.setString(1, username);
            ps.setInt(2, idRistorante);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore addFavorite: " + e.getMessage());
        }
    }

    public void removeFavorite(String username, int idRistorante) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "DELETE FROM preferiti WHERE username=? AND id_ristorante=?"
            );
            ps.setString(1, username);
            ps.setInt(2, idRistorante);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore removeFavorite: " + e.getMessage());
        }
    }

    public List<HashMap<String, Object>> listFavorites(String username) {
        try {
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT r.* FROM preferiti p JOIN ristoranti r ON p.id_ristorante=r.id WHERE p.username=?"
            );
            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();
            return resultSetToList(rs);

        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DB] Errore listFavorites: " + e.getMessage());
            return null;
        }
    }

    // UTILITY

    /** Converte un ResultSet in una lista di HashMap per inviarlo facilmente al client */
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
