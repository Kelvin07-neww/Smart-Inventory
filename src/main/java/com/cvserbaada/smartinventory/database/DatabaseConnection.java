package com.cvserbaada.smartinventory.database;

import com.cvserbaada.smartinventory.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String MYSQL_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName(MYSQL_DRIVER_CLASS);
            return DriverManager.getConnection(
                    DatabaseConfig.getUrl(),
                    DatabaseConfig.getUsername(),
                    DatabaseConfig.getPassword()
            );
        } catch (ClassNotFoundException exception) {
            SQLException sqlException = new SQLException("MySQL JDBC driver tidak ditemukan di runtime aplikasi.", exception);
            LOGGER.log(Level.SEVERE, sqlException.getMessage(), sqlException);
            throw sqlException;
        } catch (SQLException exception) {
            LOGGER.log(Level.SEVERE, "Unable to connect to MySQL database.", exception);
            throw exception;
        }
    }
}
