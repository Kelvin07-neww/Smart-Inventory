package com.cvserbaada.smartinventory.dao;

import com.cvserbaada.smartinventory.model.User;

import java.sql.SQLException;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findByEmail(String email) throws SQLException;

    Optional<User> findById(int id) throws SQLException;

    void updatePassword(int userId, String hashedPassword) throws SQLException;

    void updateProfile(User user) throws SQLException;
}
