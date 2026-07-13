package com.cvserbaada.smartinventory.util;

import com.cvserbaada.smartinventory.model.User;

import java.util.Optional;

public final class SessionManager {
    private static User currentUser;

    private SessionManager() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public static void clearSession() {
        currentUser = null;
    }
}
