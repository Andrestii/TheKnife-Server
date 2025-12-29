package com.theknife;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private static final int COST = 12;
    private PasswordUtil() {}

    public static String hashPassword(String plain) {
        if (plain == null || plain.isBlank()) throw new IllegalArgumentException("Password vuota");
        return BCrypt.hashpw(plain, BCrypt.gensalt(COST));
    }

    public static boolean verifyPassword(String plain, String storedHash) {
        if (plain == null || storedHash == null || storedHash.isBlank()) return false;
        try {
            return BCrypt.checkpw(plain, storedHash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
