package com.cvserbaada.smartinventory.service;

import com.cvserbaada.smartinventory.dao.UserDAO;
import com.cvserbaada.smartinventory.dao.UserDAOImpl;
import com.cvserbaada.smartinventory.database.DatabaseConnection;
import com.cvserbaada.smartinventory.model.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SettingsService {
    private static final Logger LOGGER = Logger.getLogger(SettingsService.class.getName());
    private final UserDAO userDAO;

    public SettingsService() {
        this(new UserDAOImpl());
    }

    public SettingsService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User refreshCurrentUser(User currentUser) throws SQLException {
        if (currentUser == null) {
            throw new IllegalArgumentException("User session tidak ditemukan.");
        }

        Optional<User> userOptional = userDAO.findById(currentUser.getId());
        return userOptional.orElse(currentUser);
    }

    public User updateProfile(User currentUser, String nama, String email) throws SQLException {
        if (currentUser == null) {
            throw new IllegalArgumentException("User session tidak ditemukan.");
        }

        validateProfileInput(nama, email);

        User updatedUser = new User(
                currentUser.getId(),
                nama.trim(),
                email.trim(),
                currentUser.getPassword(),
                currentUser.getRole()
        );
        userDAO.updateProfile(updatedUser);
        return refreshCurrentUser(updatedUser);
    }

    public boolean isDatabaseConnected() {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return connection.isValid(2);
        } catch (SQLException exception) {
            LOGGER.log(Level.WARNING, "Database status check failed.", exception);
            return false;
        }
    }

    private void validateProfileInput(String nama, String email) {
        if (nama == null || nama.trim().isBlank()) {
            throw new IllegalArgumentException("Nama tidak boleh kosong.");
        }

        if (email == null || email.trim().isBlank()) {
            throw new IllegalArgumentException("Email tidak boleh kosong.");
        }

        if (!email.trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            throw new IllegalArgumentException("Format email tidak valid.");
        }
    }
}
