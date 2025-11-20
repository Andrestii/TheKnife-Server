package theknifeserver;

import java.io.Serializable;

public class Recensione implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private int stelle;
    private String testo;
    private String risposta;

    public Recensione(int id, String username, int stelle, String testo, String risposta) {
        this.id = id;
        this.username = username;
        this.stelle = stelle;
        this.testo = testo;
        this.risposta = risposta;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public int getStelle() {
        return stelle;
    }

    public String getTesto() {
        return testo;
    }

    public String getRisposta() {
        return risposta;
    }

    @Override
    public String toString() {
        return "[" + stelle + "★] " + testo + " (di " + username + ")";
    }
}
