package com.cvserbaada.smartinventory.util;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {
    private PasswordUtil() {
    }

    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(12));
    }

    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (!isBCryptHash(hashedPassword)) {
            return false;
        }
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }

    public static boolean isBCryptHash(String password) {
        return password != null && password.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
    }
}
