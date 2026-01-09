package com.theknife;

import java.io.Serializable;

/**
 * Modello dati che rappresenta una recensione inserita da un utente per un
 * ristorante.
 * <p>
 * Contiene valutazione in stelle, testo della recensione ed eventuale risposta
 * del ristoratore.
 * </p>
 */
public class Recensione implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int idRistorante;
    private int idUtente;
    private int stelle;
    private String testo;
    private String risposta;

    /**
     * Crea una nuova recensione con i dati forniti.
     *
     * @param id       identificativo della recensione
     * @param idUtente identificativo dell'utente autore
     * @param stelle   valutazione (1-5)
     * @param testo    testo della recensione
     * @param risposta risposta del ristoratore (può essere null)
     */
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

    public int getIdRistorante() {
        return idRistorante;
    }

    public void setIdRistorante(int idRistorante) {
        this.idRistorante = idRistorante;
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

    public void setRisposta(String risposta) {
        this.risposta = risposta;
    }

    @Override
    public String toString() {
        return "[" + stelle + "★] " + testo + " (di utente con ID " + idUtente + ")";
    }
}
