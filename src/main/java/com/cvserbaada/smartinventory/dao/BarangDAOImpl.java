package com.cvserbaada.smartinventory.dao;

import com.cvserbaada.smartinventory.database.DatabaseConnection;
import com.cvserbaada.smartinventory.model.Barang;
import com.cvserbaada.smartinventory.model.Kategori;
import com.cvserbaada.smartinventory.model.Supplier;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BarangDAOImpl implements BarangDAO {
    private static final String FIND_ALL_SQL = """
            SELECT b.id, b.kode_barang, b.nama_barang, b.kategori_id, b.supplier_id,
                   b.harga_beli, b.harga_jual, b.stok,
                   COALESCE(k.nama_kategori, '') AS kategori_nama,
                   COALESCE(s.nama_supplier, '') AS supplier_nama
            FROM barang b
            LEFT JOIN kategori k ON k.id = b.kategori_id
            LEFT JOIN supplier s ON s.id = b.supplier_id
            WHERE b.kode_barang LIKE ? OR b.nama_barang LIKE ?
            ORDER BY b.kode_barang ASC
            LIMIT ? OFFSET ?
            """;

    private static final String COUNT_SQL = """
            SELECT COUNT(*)
            FROM barang
            WHERE kode_barang LIKE ? OR nama_barang LIKE ?
            """;

    private static final String EXISTS_BY_KODE_SQL = """
            SELECT COUNT(*)
            FROM barang
            WHERE kode_barang = ? AND id <> ?
            """;

    private static final String GENERATE_NEXT_KODE_SQL = """
            SELECT COALESCE(MAX(CAST(SUBSTRING(kode_barang, 4) AS UNSIGNED)), 0) + 1 AS next_number
            FROM barang
            WHERE kode_barang LIKE 'BRG%'
            """;

    private static final String INSERT_SQL = """
            INSERT INTO barang (kode_barang, nama_barang, kategori_id, supplier_id, harga_beli, harga_jual, stok)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_SQL = """
            UPDATE barang
            SET kode_barang = ?, nama_barang = ?, kategori_id = ?, supplier_id = ?, harga_beli = ?, harga_jual = ?, stok = ?
            WHERE id = ?
            """;

    private static final String DELETE_SQL = """
            DELETE FROM barang
            WHERE id = ?
            """;

    private static final String FIND_KATEGORI_OPTIONS_SQL = """
            SELECT id, kode_kategori, nama_kategori
            FROM kategori
            ORDER BY nama_kategori ASC
            """;

    private static final String FIND_SUPPLIER_OPTIONS_SQL = """
            SELECT id, kode_supplier, nama_supplier, alamat, telepon
            FROM supplier
            ORDER BY nama_supplier ASC
            """;

    @Override
    public List<Barang> findAll(String keyword, int offset, int limit) throws SQLException {
        List<Barang> barangList = new ArrayList<>();
        String searchKeyword = toSearchKeyword(keyword);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_SQL)) {

            statement.setString(1, searchKeyword);
            statement.setString(2, searchKeyword);
            statement.setInt(3, limit);
            statement.setInt(4, offset);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    barangList.add(mapToBarang(resultSet));
                }
            }
        }

        return barangList;
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
    public boolean existsByKode(String kodeBarang, int excludeId) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(EXISTS_BY_KODE_SQL)) {

            statement.setString(1, kodeBarang);
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
            return String.format("BRG%04d", nextNumber);
        }
    }

    @Override
    public void insert(Barang barang) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {

            bindFormStatement(statement, barang);
            statement.executeUpdate();
        }
    }

    @Override
    public void update(Barang barang) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE_SQL)) {

            bindFormStatement(statement, barang);
            statement.setInt(8, barang.getId());
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

    @Override
    public List<Kategori> findKategoriOptions() throws SQLException {
        List<Kategori> kategoriList = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_KATEGORI_OPTIONS_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                kategoriList.add(new Kategori(
                        resultSet.getInt("id"),
                        resultSet.getString("kode_kategori"),
                        resultSet.getString("nama_kategori")
                ));
            }
        }

        return kategoriList;
    }

    @Override
    public List<Supplier> findSupplierOptions() throws SQLException {
        List<Supplier> supplierList = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_SUPPLIER_OPTIONS_SQL);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                supplierList.add(new Supplier(
                        resultSet.getInt("id"),
                        resultSet.getString("kode_supplier"),
                        resultSet.getString("nama_supplier"),
                        resultSet.getString("alamat"),
                        resultSet.getString("telepon")
                ));
            }
        }

        return supplierList;
    }

    private void bindFormStatement(PreparedStatement statement, Barang barang) throws SQLException {
        statement.setString(1, barang.getKodeBarang());
        statement.setString(2, barang.getNamaBarang());
        statement.setInt(3, barang.getKategoriId());
        statement.setInt(4, barang.getSupplierId());
        statement.setBigDecimal(5, barang.getHargaBeli());
        statement.setBigDecimal(6, barang.getHargaJual());
        statement.setInt(7, barang.getStok());
    }

    private String toSearchKeyword(String keyword) {
        return "%" + (keyword == null ? "" : keyword.trim()) + "%";
    }

    private Barang mapToBarang(ResultSet resultSet) throws SQLException {
        return new Barang(
                resultSet.getInt("id"),
                resultSet.getString("kode_barang"),
                resultSet.getString("nama_barang"),
                resultSet.getInt("kategori_id"),
                resultSet.getString("kategori_nama"),
                resultSet.getInt("supplier_id"),
                resultSet.getString("supplier_nama"),
                resultSet.getBigDecimal("harga_beli"),
                resultSet.getBigDecimal("harga_jual"),
                resultSet.getInt("stok")
        );
    }
}
