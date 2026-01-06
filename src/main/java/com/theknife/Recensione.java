package com.theknife;

import java.io.Serializable;

public class Recensione implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int idUtente;
    private int stelle;
    private String testo;
    private String risposta;

    public Recensione(int id, int idUtente, int stelle, String testo, String risposta) {
        this.id = id;
        this.idUtente = idUtente;
        this.stelle = stelle;
        this.testo = testo;
        this.risposta = risposta;
    }

    public int getId() {
        return id;
    }

    public int getIdUtente() {
        return idUtente;
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
        return "[" + stelle + "★] " + testo + " (di utente con ID " + idUtente + ")";
    }
}
