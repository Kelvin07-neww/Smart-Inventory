package com.cvserbaada.smartinventory.dao;

import com.cvserbaada.smartinventory.database.DatabaseConnection;
import com.cvserbaada.smartinventory.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {
    private static final String FIND_BY_EMAIL_SQL = """
            SELECT id, nama, email, password, role
            FROM users
            WHERE email = ?
            LIMIT 1
            """;

    private static final String FIND_BY_ID_SQL = """
            SELECT id, nama, email, password, role
            FROM users
            WHERE id = ?
            LIMIT 1
            """;

    private static final String UPDATE_PASSWORD_SQL = """
            UPDATE users
            SET password = ?
            WHERE id = ?
            """;

    private static final String UPDATE_PROFILE_SQL = """
            UPDATE users
            SET nama = ?, email = ?
            WHERE id = ?
            """;

    @Override
    public Optional<User> findByEmail(String email) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_EMAIL_SQL)) {

            statement.setString(1, email);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapToUser(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public Optional<User> findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID_SQL)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(mapToUser(resultSet));
                }
                return Optional.empty();
            }
        }
    }

    @Override
    public void updatePassword(int userId, String hashedPassword) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PASSWORD_SQL)) {

            statement.setString(1, hashedPassword);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }
    }

    @Override
    public void updateProfile(User user) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_PROFILE_SQL)) {

            statement.setString(1, user.getNama());
            statement.setString(2, user.getEmail());
            statement.setInt(3, user.getId());
            statement.executeUpdate();
        }
    }

    private User mapToUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getInt("id"),
                resultSet.getString("nama"),
                resultSet.getString("email"),
                resultSet.getString("password"),
                resultSet.getString("role")
        );
    }
}
