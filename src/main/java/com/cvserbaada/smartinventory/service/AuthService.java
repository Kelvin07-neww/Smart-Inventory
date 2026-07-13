package com.cvserbaada.smartinventory.service;

import com.cvserbaada.smartinventory.dao.UserDAO;
import com.cvserbaada.smartinventory.dao.UserDAOImpl;
import com.cvserbaada.smartinventory.model.User;
import com.cvserbaada.smartinventory.util.PasswordMigrationUtil;
import com.cvserbaada.smartinventory.util.PasswordUtil;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {
    private final UserDAO userDAO;

    public AuthService() {
        this(new UserDAOImpl());
    }

    public AuthService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User login(String email, String password) throws AuthException {
        try {
            Optional<User> userOptional = userDAO.findByEmail(email);
            if (userOptional.isEmpty()) {
                throw new AuthException(AuthErrorType.EMAIL_NOT_FOUND, "Email tidak ditemukan.");
            }

            User user = userOptional.get();
            if (PasswordUtil.isBCryptHash(user.getPassword())) {
                validateBCryptPassword(password, user);
                return user;
            }

            validatePlainTextPasswordAndMigrate(password, user);
            return user;
        } catch (SQLException exception) {
            throw new AuthException(AuthErrorType.DATABASE_ERROR, "Gagal mengakses database: " + exception.getMessage());
        }
    }

    private void validateBCryptPassword(String password, User user) throws AuthException {
        if (!PasswordUtil.verifyPassword(password, user.getPassword())) {
            throw new AuthException(AuthErrorType.INVALID_PASSWORD, "Password salah.");
        }
    }

    private void validatePlainTextPasswordAndMigrate(String password, User user) throws AuthException, SQLException {
        if (!password.equals(user.getPassword())) {
            throw new AuthException(AuthErrorType.INVALID_PASSWORD, "Password salah.");
        }

        PasswordMigrationUtil.migratePlainTextPassword(userDAO, user, password);
    }
}
