package com.cvserbaada.smartinventory.dao;

import com.cvserbaada.smartinventory.database.DatabaseConnection;
import com.cvserbaada.smartinventory.model.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAOImpl implements SupplierDAO {
    private static final String FIND_ALL_SQL = """
            SELECT id, kode_supplier, nama_supplier, alamat, telepon
            FROM supplier
            WHERE kode_supplier LIKE ? OR nama_supplier LIKE ?
            ORDER BY kode_supplier ASC
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_SQL = """
            SELECT COUNT(*)
            FROM supplier
            WHERE kode_supplier LIKE ? OR nama_supplier LIKE ?
            """;

    private static final String EXISTS_BY_KODE_SQL = """
            SELECT COUNT(*)
            FROM supplier
            WHERE kode_supplier = ? AND id <> ?
            """;

    private static final String GENERATE_NEXT_KODE_SQL = """
            SELECT COALESCE(MAX(CAST(SUBSTRING(kode_supplier, 4) AS UNSIGNED)), 0) + 1 AS next_number
            FROM supplier
            WHERE kode_supplier LIKE 'SUP%'
            """;

    private static final String INSERT_SQL = """
            INSERT INTO supplier (kode_supplier, nama_supplier, alamat, telepon)
            VALUES (?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE supplier
            SET kode_supplier = ?, nama_supplier = ?, alamat = ?, telepon = ?
            WHERE id = ?
            """;

    private static final String DELETE_SQL = """
            DELETE FROM supplier
            WHERE id = ?
            """;

    @Override
    public List<Supplier> findAll(String keyword, int offset, int limit) throws SQLException {
        List<Supplier> suppliers = new ArrayList<>();
        String searchKeyword = toSearchKeyword(keyword);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL)) {

            statement.setString(1, searchKeyword);
            statement.setString(2, searchKeyword);
            statement.setInt(3, limit);
            statement.setInt(4, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    suppliers.add(mapToSupplier(resultSet));
                }
            }
        }

        return suppliers;
    }

    @Override
    public int count(String keyword) throws SQLException {
        String searchKeyword = toSearchKeyword(keyword);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(COUNT_SQL)) {

            statement.setString(1, searchKeyword);
            statement.setString(2, searchKeyword);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
                return 0;
            }
        }
    }

    @Override
    public boolean existsByKode(String kodeSupplier, int excludeId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_BY_KODE_SQL)) {

            statement.setString(1, kodeSupplier);
            statement.setInt(2, excludeId);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    @Override
    public String generateNextKode() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(GENERATE_NEXT_KODE_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            int nextNumber = resultSet.next() ? resultSet.getInt("next_number") : 1;
            return String.format("SUP%04d", nextNumber);
        }
    }

    @Override
    public void insert(Supplier supplier) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setString(1, supplier.getKodeSupplier());
            statement.setString(2, supplier.getNamaSupplier());
            statement.setString(3, supplier.getAlamat());
            statement.setString(4, supplier.getTelepon());
            statement.executeUpdate();
        }
    }

    @Override
    public void update(Supplier supplier) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setString(1, supplier.getKodeSupplier());
            statement.setString(2, supplier.getNamaSupplier());
            statement.setString(3, supplier.getAlamat());
            statement.setString(4, supplier.getTelepon());
            statement.setInt(5, supplier.getId());
            statement.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE_SQL)) {

            statement.setInt(1, id);
            statement.executeUpdate();
        }
    }

    private String toSearchKeyword(String keyword) {
        return "%" + (keyword == null ? "" : keyword.trim()) + "%";
    }

    private Supplier mapToSupplier(ResultSet resultSet) throws SQLException {
        return new Supplier(
                resultSet.getInt("id"),
                resultSet.getString("kode_supplier"),
                resultSet.getString("nama_supplier"),
                resultSet.getString("alamat"),
                resultSet.getString("telepon")
        );
    }
}
