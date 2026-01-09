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
 * Modello dati che rappresenta un ristorante presente nel sistema.
 * <p>
 * Include informazioni anagrafiche/di localizzazione, servizi disponibili
 * (delivery/prenotazione) e riferimento al ristoratore proprietario.
 * </p>
 */
public class Ristorante implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nome;
    private String nazione;
    private String citta;
    private String indirizzo;
    private double latitudine;
    private double longitudine;
    private int fasciaPrezzo;
    private boolean delivery;
    private boolean prenotazione;
    private String tipoCucina;
    private String idRistoratore;

    /**
     * Crea un nuovo ristorante con i dati forniti.
     *
     * @param id            identificativo del ristorante
     * @param nome          nome del ristorante
     * @param nazione       nazione
     * @param citta         città
     * @param indirizzo     indirizzo
     * @param latitudine    latitudine
     * @param longitudine   longitudine
     * @param fasciaPrezzo  prezzo medio
     * @param delivery      true se offre delivery
     * @param prenotazione  true se supporta prenotazione online
     * @param tipoCucina    tipologia di cucina
     * @param idRistoratore identificativo/username del ristoratore proprietario (in
     *                      base al tuo modello)
     */
    public Ristorante(int id, String nome, String nazione, String citta, String indirizzo,
            double latitudine, double longitudine, int fasciaPrezzo,
            boolean delivery, boolean prenotazione, String tipoCucina,
            String idRistoratore) {

        this.id = id;
        this.nome = nome;
        this.nazione = nazione;
        this.citta = citta;
        this.indirizzo = indirizzo;
        this.latitudine = latitudine;
        this.longitudine = longitudine;
        this.fasciaPrezzo = fasciaPrezzo;
        this.delivery = delivery;
        this.prenotazione = prenotazione;
        this.tipoCucina = tipoCucina;
        this.idRistoratore = idRistoratore;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getNazione() {
        return nazione;
    }

    public String getCitta() {
        return citta;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public double getLat() {
        return latitudine;
    }

    public double getLon() {
        return longitudine;
    }

    public int getPrezzo() {
        return fasciaPrezzo;
    }

    public boolean isDelivery() {
        return delivery;
    }

    public boolean isPrenotazione() {
        return prenotazione;
    }

    public String getTipoCucina() {
        return tipoCucina;
    }

    public String getIdRistoratore() {
        return idRistoratore;
    }

    @Override
    public String toString() {
        return nome + " - " + citta + " [" + tipoCucina + "]";
    }
}
