package com.cvserbaada.smartinventory.dao;

import com.cvserbaada.smartinventory.database.DatabaseConnection;
import com.cvserbaada.smartinventory.model.Kategori;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KategoriDAOImpl implements KategoriDAO {
    private static final String FIND_ALL_SQL = """
            SELECT id, kode_kategori, nama_kategori
            FROM kategori
            WHERE kode_kategori LIKE ? OR nama_kategori LIKE ?
            ORDER BY kode_kategori ASC
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_SQL = """
            SELECT COUNT(*)
            FROM kategori
            WHERE kode_kategori LIKE ? OR nama_kategori LIKE ?
            """;

    private static final String EXISTS_BY_KODE_SQL = """
            SELECT COUNT(*)
            FROM kategori
            WHERE kode_kategori = ? AND id <> ?
            """;

    private static final String GENERATE_NEXT_KODE_SQL = """
            SELECT COALESCE(MAX(CAST(SUBSTRING(kode_kategori, 4) AS UNSIGNED)), 0) + 1 AS next_number
            FROM kategori
            WHERE kode_kategori LIKE 'KTG%'
            """;

    private static final String INSERT_SQL = """
            INSERT INTO kategori (kode_kategori, nama_kategori)
            VALUES (?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE kategori
            SET kode_kategori = ?, nama_kategori = ?
            WHERE id = ?
            """;

    private static final String DELETE_SQL = """
            DELETE FROM kategori
            WHERE id = ?
            """;

    @Override
    public List<Kategori> findAll(String keyword, int offset, int limit) throws SQLException {
        List<Kategori> kategoris = new ArrayList<>();
        String searchKeyword = toSearchKeyword(keyword);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL)) {

            statement.setString(1, searchKeyword);
            statement.setString(2, searchKeyword);
            statement.setInt(3, limit);
            statement.setInt(4, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    kategoris.add(mapToKategori(resultSet));
                }
            }
        }

        return kategoris;
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
    public boolean existsByKode(String kodeKategori, int excludeId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_BY_KODE_SQL)) {

            statement.setString(1, kodeKategori);
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
            return String.format("KTG%04d", nextNumber);
        }
    }

    @Override
    public void insert(Kategori kategori) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            statement.setString(1, kategori.getKodeKategori());
            statement.setString(2, kategori.getNamaKategori());
            statement.executeUpdate();
        }
    }

    @Override
    public void update(Kategori kategori) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            statement.setString(1, kategori.getKodeKategori());
            statement.setString(2, kategori.getNamaKategori());
            statement.setInt(3, kategori.getId());
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

    private Kategori mapToKategori(ResultSet resultSet) throws SQLException {
        return new Kategori(
                resultSet.getInt("id"),
                resultSet.getString("kode_kategori"),
                resultSet.getString("nama_kategori")
        );
    }
}
