package com.theknife;

import java.io.Serializable;

public class Utente implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nome;
    private String cognome;
    private String dataNascita;
    private String domicilio;
    private String username;
    private String password;
    private String ruolo;

    public Utente(String nome, String cognome, String dataNascita, String domicilio, String username, String password, String ruolo) {
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
        return nome + ";" + cognome + ";" + dataNascita + ";" + domicilio + ";" + username + ";" + password + ";" + ruolo;
    }
}
