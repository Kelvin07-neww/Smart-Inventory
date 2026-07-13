package com.cvserbaada.smartinventory.dao;

import com.cvserbaada.smartinventory.database.DatabaseConnection;
import com.cvserbaada.smartinventory.model.Barang;
import com.cvserbaada.smartinventory.model.DetailPenjualan;
import com.cvserbaada.smartinventory.model.Penjualan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class PenjualanDAOImpl implements PenjualanDAO {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final String FIND_LAST_FAKTUR_SQL = """
            SELECT nomor_faktur AS no_faktur
            FROM penjualan
            WHERE nomor_faktur LIKE ?
            ORDER BY nomor_faktur DESC
            LIMIT 1
            """;

    private static final String FIND_BARANG_FOR_SALE_SQL = """
            SELECT b.id, b.kode_barang, b.nama_barang, b.kategori_id, b.supplier_id,
                   b.harga_beli, b.harga_jual, b.stok,
                   COALESCE(k.nama_kategori, '') AS kategori_nama,
                   COALESCE(s.nama_supplier, '') AS supplier_nama
            FROM barang b
            LEFT JOIN kategori k ON k.id = b.kategori_id
            LEFT JOIN supplier s ON s.id = b.supplier_id
            WHERE b.kode_barang LIKE ? OR b.nama_barang LIKE ?
            ORDER BY b.nama_barang ASC
            LIMIT 30
            """;

    private static final String FIND_RECENT_SALES_SQL = """
            SELECT p.id, p.nomor_faktur, p.tanggal, p.user_id,
                   COALESCE(u.nama, CONCAT('User #', p.user_id)) AS kasir,
                   p.subtotal, p.diskon, p.ppn, p.grand_total
            FROM penjualan p
            LEFT JOIN users u ON u.id = p.user_id
            ORDER BY p.tanggal DESC, p.id DESC
            LIMIT ?
            """;

    private static final String FIND_DETAILS_BY_PENJUALAN_ID_SQL = """
            SELECT dp.id, dp.penjualan_id, dp.barang_id,
                   COALESCE(b.kode_barang, '') AS kode_barang,
                   COALESCE(b.nama_barang, CONCAT('Barang #', dp.barang_id)) AS nama_barang,
                   dp.harga, dp.qty, COALESCE(b.stok, 0) AS stok_tersedia
            FROM detail_penjualan dp
            LEFT JOIN barang b ON b.id = dp.barang_id
            WHERE dp.penjualan_id = ?
            ORDER BY dp.id ASC
            """;

    private static final String INSERT_PENJUALAN_SQL = """
            INSERT INTO penjualan (nomor_faktur, tanggal, user_id, subtotal, diskon, ppn, grand_total)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String LOCK_STOCK_SQL = """
            SELECT stok
            FROM barang
            WHERE id = ?
            FOR UPDATE
            """;

    private static final String UPDATE_STOCK_SQL = """
            UPDATE barang
            SET stok = stok - ?
            WHERE id = ? AND stok >= ?
            """;

    private final DetailPenjualanDAO detailPenjualanDAO;

    public PenjualanDAOImpl() {
        this(new DetailPenjualanDAOImpl());
    }

    public PenjualanDAOImpl(DetailPenjualanDAO detailPenjualanDAO) {
        this.detailPenjualanDAO = detailPenjualanDAO;
    }

    @Override
    public String generateNoFaktur() throws SQLException {
        String prefix = "INV-" + LocalDate.now().format(DATE_FORMATTER) + "-";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_LAST_FAKTUR_SQL)) {

            statement.setString(1, prefix + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    String lastNoFaktur = resultSet.getString("no_faktur");
                    int lastSequence = Integer.parseInt(lastNoFaktur.substring(lastNoFaktur.lastIndexOf('-') + 1));
                    return prefix + String.format("%04d", lastSequence + 1);
                }
            }
        }

        return prefix + "0001";
    }

    @Override
    public List<Barang> findBarangForSale(String keyword) throws SQLException {
        List<Barang> barangList = new ArrayList<>();
        String searchKeyword = "%" + (keyword == null ? "" : keyword.trim()) + "%";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BARANG_FOR_SALE_SQL)) {

            statement.setString(1, searchKeyword);
            statement.setString(2, searchKeyword);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    barangList.add(mapToBarang(resultSet));
                }
            }
        }

        return barangList;
    }

    @Override
    public List<Penjualan> findRecentSales(int limit) throws SQLException {
        List<Penjualan> sales = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_RECENT_SALES_SQL)) {

            statement.setInt(1, Math.max(1, limit));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    sales.add(mapToPenjualan(resultSet));
                }
            }
        }
        return sales;
    }

    @Override
    public List<DetailPenjualan> findDetailsByPenjualanId(int penjualanId) throws SQLException {
        List<DetailPenjualan> details = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_DETAILS_BY_PENJUALAN_ID_SQL)) {

            statement.setInt(1, penjualanId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    details.add(mapToDetailPenjualan(resultSet));
                }
            }
        }
        return details;
    }

    @Override
    public void saveTransaction(Penjualan penjualan) throws SQLException {
        Connection connection = null;
        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            validateAndReduceStock(connection, penjualan.getDetailList());
            int penjualanId = insertPenjualan(connection, penjualan);
            detailPenjualanDAO.insertBatch(connection, penjualanId, penjualan.getDetailList());

            connection.commit();
        } catch (SQLException | RuntimeException exception) {
            if (connection != null) {
                connection.rollback();
            }
            throw exception;
        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    private int insertPenjualan(Connection connection, Penjualan penjualan) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_PENJUALAN_SQL, PreparedStatement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, penjualan.getNoFaktur());
            statement.setTimestamp(2, Timestamp.valueOf(penjualan.getTanggal()));
            statement.setInt(3, penjualan.getUserId());
            statement.setBigDecimal(4, penjualan.getSubtotal());
            statement.setBigDecimal(5, penjualan.getDiskon());
            statement.setBigDecimal(6, penjualan.getPpn());
            statement.setBigDecimal(7, penjualan.getGrandTotal());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
                throw new SQLException("Gagal mendapatkan ID penjualan.");
            }
        }
    }

    private void validateAndReduceStock(Connection connection, List<DetailPenjualan> detailList) throws SQLException {
        for (DetailPenjualan detail : detailList) {
            int currentStock = getLockedStock(connection, detail.getBarangId());
            if (detail.getQty() > currentStock) {
                throw new SQLException("Stok barang " + detail.getNamaBarang() + " tidak cukup. Stok tersedia: " + currentStock + ".");
            }

            try (PreparedStatement statement = connection.prepareStatement(UPDATE_STOCK_SQL)) {
                statement.setInt(1, detail.getQty());
                statement.setInt(2, detail.getBarangId());
                statement.setInt(3, detail.getQty());
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Gagal mengurangi stok barang " + detail.getNamaBarang() + ".");
                }
            }
        }
    }

    private int getLockedStock(Connection connection, int barangId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(LOCK_STOCK_SQL)) {
            statement.setInt(1, barangId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("stok");
                }
                throw new SQLException("Barang tidak ditemukan.");
            }
        }
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

    private Penjualan mapToPenjualan(ResultSet resultSet) throws SQLException {
        return new Penjualan(
                resultSet.getInt("id"),
                resultSet.getString("nomor_faktur"),
                resultSet.getTimestamp("tanggal").toLocalDateTime(),
                resultSet.getInt("user_id"),
                resultSet.getString("kasir"),
                resultSet.getBigDecimal("subtotal"),
                resultSet.getBigDecimal("diskon"),
                resultSet.getBigDecimal("ppn"),
                resultSet.getBigDecimal("grand_total"),
                new ArrayList<>()
        );
    }

    private DetailPenjualan mapToDetailPenjualan(ResultSet resultSet) throws SQLException {
        return new DetailPenjualan(
                resultSet.getInt("id"),
                resultSet.getInt("penjualan_id"),
                resultSet.getInt("barang_id"),
                resultSet.getString("kode_barang"),
                resultSet.getString("nama_barang"),
                resultSet.getBigDecimal("harga"),
                resultSet.getInt("qty"),
                resultSet.getInt("stok_tersedia")
        );
    }
}
