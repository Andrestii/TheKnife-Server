package theknifeserver;

import java.io.Serializable;

public class Ristorante implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String nome;
    private String nazione;
    private String citta;
    private String indirizzo;
    private double lat;
    private double lon;
    private int prezzo;
    private boolean delivery;
    private boolean prenotazione;
    private String tipoCucina;

    public Ristorante(int id, String nome, String nazione, String citta, String indirizzo,
                         double lat, double lon, int prezzo,
                         boolean delivery, boolean prenotazione, String tipoCucina) {

        this.id = id;
        this.nome = nome;
        this.nazione = nazione;
        this.citta = citta;
        this.indirizzo = indirizzo;
        this.lat = lat;
        this.lon = lon;
        this.prezzo = prezzo;
        this.delivery = delivery;
        this.prenotazione = prenotazione;
        this.tipoCucina = tipoCucina;
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
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public int getPrezzo() {
        return prezzo;
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

    @Override
    public String toString() {
        return nome + " - " + citta + " [" + tipoCucina + "]";
    }
}
