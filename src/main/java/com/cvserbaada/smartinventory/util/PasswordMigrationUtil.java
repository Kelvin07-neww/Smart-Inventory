package com.cvserbaada.smartinventory.util;

import com.cvserbaada.smartinventory.dao.UserDAO;
import com.cvserbaada.smartinventory.model.User;

import java.sql.SQLException;

public final class PasswordMigrationUtil {
    private PasswordMigrationUtil() {
    }

    public static void migratePlainTextPassword(UserDAO userDAO, User user, String plainPassword) throws SQLException {
        String hashedPassword = PasswordUtil.hashPassword(plainPassword);
        userDAO.updatePassword(user.getId(), hashedPassword);
        user.setPassword(hashedPassword);
    }
}
