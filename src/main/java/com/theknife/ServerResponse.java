package com.theknife;

import java.io.Serializable;

/**
 * Rappresenta la risposta del server verso il client.
 * <p>
 * Contiene uno status (ad es. "OK" o "ERROR") e un payload opzionale
 * con i dati restituiti dall'operazione richiesta.
 * </p>
 * <p>
 * Questa classe viene serializzata e inviata tramite
 * {@link java.io.ObjectOutputStream}.
 * </p>
 */
public class ServerResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private String status; // "OK" o "ERROR"
    private Object payload; // può contenere DTO o liste di DTO

    /**
     * Crea una nuova risposta del server.
     *
     * @param status  esito dell'operazione (es. "OK" o "ERROR")
     * @param payload dati associati alla risposta (può essere null)
     */
    public ServerResponse(String status, Object payload) {
        this.status = status;
        this.payload = payload;
    }

    public String getStatus() {
        return status;
    }

    public Object getPayload() {
        return payload;
    }
}
