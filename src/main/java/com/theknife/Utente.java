/**
 * Autori del progetto:
 *
 * - Lorenzo De Paoli
 *   Matricola: 753577
 *   Sede: VA
 *
 * - Andrea Onesti
 *   Matricola: 754771
 *   Sede: VA
 *
 * - Weili Wu
 *   Matricola: 752602
 *   Sede: VA
 */

package com.theknife;

import java.io.Serializable;

/**
 * Modello dati che rappresenta un utente registrato nel sistema.
 * <p>
 * Contiene informazioni anagrafiche e credenziali (in forma hashata),
 * oltre al ruolo (cliente o ristoratore).
 * </p>
 */
public class Utente implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private String cognome;
    private String dataNascita;
    private String domicilio;
    private String username;
    private String password;
    private String ruolo;

    /**
     * Crea un nuovo utente con i dati forniti.
     *
     * @param nome        nome dell'utente
     * @param cognome     cognome dell'utente
     * @param dataNascita data di nascita (formato testuale)
     * @param domicilio   domicilio dell'utente
     * @param username    username dell'utente
     * @param password    hash della password
     * @param ruolo       ruolo dell'utente (cliente/ristoratore)
     */
    public Utente(String nome, String cognome, String dataNascita, String domicilio, String username, String password,
            String ruolo) {
        this.nome = nome;
        this.cognome = cognome;
        this.dataNascita = dataNascita;
        this.domicilio = domicilio;
        this.username = username;
        this.password = password;
        this.ruolo = ruolo;
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

    public String getDomicilio() {
        return domicilio;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRuolo() {
        return ruolo;
    }

    @Override
    public String toString() {
        return nome + ";" + cognome + ";" + dataNascita + ";" + domicilio + ";" + username + ";" + password + ";"
                + ruolo;
    }
}
