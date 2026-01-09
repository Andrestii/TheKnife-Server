package com.theknife;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Classe di utilità per la gestione sicura delle password.
 * <p>
 * Fornisce metodi statici per l'hashing e la verifica delle password
 * utilizzando l'algoritmo BCrypt.
 * </p>
 */
public final class PasswordUtil {
    private static final int COST = 12;

    private PasswordUtil() {
    }

    /**
     * Calcola l'hash di una password in chiaro utilizzando BCrypt.
     *
     * @param plain password in chiaro
     * @return hash della password
     * @throws IllegalArgumentException se la password è nulla o vuota
     */
    public static String hashPassword(String plain) {
        if (plain == null || plain.isBlank())
            throw new IllegalArgumentException("Password vuota");
        return BCrypt.hashpw(plain, BCrypt.gensalt(COST));
    }

    /**
     * Verifica una password in chiaro confrontandola con un hash BCrypt
     * memorizzato.
     *
     * @param plain      password in chiaro inserita dall'utente
     * @param storedHash hash BCrypt salvato nel database
     * @return true se la password corrisponde all'hash, false altrimenti
     */
    public static boolean verifyPassword(String plain, String storedHash) {
        if (plain == null || storedHash == null || storedHash.isBlank())
            return false;
        try {
            return BCrypt.checkpw(plain, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
