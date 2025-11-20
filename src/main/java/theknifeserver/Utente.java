package theknifeserver;

import java.io.Serializable;

public class Utente implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String ruolo;
    private String domicilio;

    public Utente(String username, String ruolo, String domicilio) {
        this.username = username;
        this.ruolo = ruolo;
        this.domicilio = domicilio;
    }

    public String getUsername() {
        return username;
    }

    public String getRuolo() {
        return ruolo;
    }

    public String getDomicilio() {
        return domicilio;
    }

    @Override
    public String toString() {
        return username + " (" + ruolo + ")";
    }
}
