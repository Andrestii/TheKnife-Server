package theknifeserver;

import java.io.Serializable;

public class Utente implements Serializable {
    private static final long serialVersionUID = 1L;

    private String username;
    private String ruolo;
    private String domicilio;
    private String nome;
    private String cognome;
    private String dataNascita;

    public Utente(String username, String ruolo, String domicilio, String nome, String cognome, String dataNascita) {
        this.username = username;
        this.ruolo = ruolo;
        this.domicilio = domicilio;
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
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

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getDataNascita() {
        return dataNascita;
    }

    @Override
    public String toString() {
        return nome + " " + cognome + " (" + ruolo + ")";
    }
}
