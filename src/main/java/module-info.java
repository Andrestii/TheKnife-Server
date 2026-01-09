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

/**
 * Modulo server dell'applicazione TheKnife.
 * Contiene le classi per la gestione del server,
 * del database e della comunicazione client-server.
 */
module com.theknife {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;


    opens com.theknife to javafx.fxml;
    exports com.theknife;
}
