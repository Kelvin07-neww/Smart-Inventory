package com.cvserbaada.smartinventory.dao;

import com.cvserbaada.smartinventory.database.DatabaseConnection;
import com.cvserbaada.smartinventory.model.DashboardStatistics;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardDAOImpl implements DashboardDAO {
    private static final DateTimeFormatter MONTH_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter MONTH_LABEL_FORMATTER = DateTimeFormatter.ofPattern("MMM yyyy", Locale.of("id", "ID"));

    private static final String TOTAL_BARANG_SQL = "SELECT COUNT(*) FROM barang";
    private static final String TOTAL_SUPPLIER_SQL = "SELECT COUNT(*) FROM supplier";
    private static final String TOTAL_PENJUALAN_HARI_INI_SQL = """
            SELECT COALESCE(SUM(grand_total), 0)
            FROM penjualan
            WHERE DATE(tanggal) = CURDATE()
            """;
    private static final String TOTAL_NILAI_INVENTORY_SQL = """
            SELECT COALESCE(SUM(harga_beli * stok), 0)
            FROM barang
            """;
    private static final String PRODUK_TERLARIS_SQL = """
            SELECT b.kode_barang, b.nama_barang, COALESCE(SUM(dp.qty), 0) AS total_qty,
                   COALESCE(SUM(dp.subtotal), 0) AS total_nilai
            FROM detail_penjualan dp
            INNER JOIN barang b ON b.id = dp.barang_id
            GROUP BY b.id, b.kode_barang, b.nama_barang
            ORDER BY total_qty DESC, total_nilai DESC
            LIMIT 5
            """;
    private static final String STOK_MENIPIS_SQL = """
            SELECT kode_barang, nama_barang, stok
            FROM barang
            WHERE stok <= 10
            ORDER BY stok ASC, nama_barang ASC
            LIMIT 10
            """;
    private static final String TRANSAKSI_TERBARU_SQL = """
            SELECT p.nomor_faktur AS no_faktur, DATE_FORMAT(p.tanggal, '%d/%m/%Y %H:%i') AS tanggal,
                   CONCAT('User #', p.user_id) AS kasir, p.grand_total
            FROM penjualan p
            ORDER BY p.tanggal DESC, p.id DESC
            LIMIT 10
            """;
    private static final String PENJUALAN_BULANAN_SQL = """
            SELECT DATE_FORMAT(tanggal, '%Y-%m') AS bulan, COALESCE(SUM(grand_total), 0) AS total
            FROM penjualan
            WHERE tanggal >= DATE_FORMAT(DATE_SUB(CURDATE(), INTERVAL 11 MONTH), '%Y-%m-01')
            GROUP BY DATE_FORMAT(tanggal, '%Y-%m')
            ORDER BY bulan ASC
            """;
    private static final String STOK_TERBANYAK_SQL = """
            SELECT nama_barang, stok
            FROM barang
            ORDER BY stok DESC, nama_barang ASC
            LIMIT 10
            """;
    private static final String DISTRIBUSI_KATEGORI_SQL = """
            SELECT COALESCE(k.nama_kategori, 'Tanpa Kategori') AS nama_kategori, COUNT(b.id) AS total_barang
            FROM barang b
            LEFT JOIN kategori k ON k.id = b.kategori_id
            GROUP BY COALESCE(k.nama_kategori, 'Tanpa Kategori')
            ORDER BY total_barang DESC
            """;

    @Override
    public DashboardStatistics loadStatistics() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            DashboardStatistics statistics = new DashboardStatistics();
            statistics.setTotalBarang(queryInt(connection, TOTAL_BARANG_SQL));
            statistics.setTotalSupplier(queryInt(connection, TOTAL_SUPPLIER_SQL));
            statistics.setTotalPenjualanHariIni(queryBigDecimal(connection, TOTAL_PENJUALAN_HARI_INI_SQL));
            statistics.setTotalNilaiInventory(queryBigDecimal(connection, TOTAL_NILAI_INVENTORY_SQL));
            statistics.setProdukTerlaris(loadProdukTerlaris(connection));
            statistics.setBarangStokMenipis(loadBarangStokMenipis(connection));
            statistics.setTransaksiTerbaru(loadTransaksiTerbaru(connection));
            statistics.setPenjualanBulanan(loadPenjualanBulanan(connection));
            statistics.setStokTerbanyak(loadStokTerbanyak(connection));
            statistics.setDistribusiKategori(loadDistribusiKategori(connection));
            return statistics;
        }
    }

    private int queryInt(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        }
    }

    private BigDecimal queryBigDecimal(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }

    private List<DashboardStatistics.ProductSales> loadProdukTerlaris(Connection connection) throws SQLException {
        List<DashboardStatistics.ProductSales> data = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(PRODUK_TERLARIS_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                data.add(new DashboardStatistics.ProductSales(
                        resultSet.getString("kode_barang"),
                        resultSet.getString("nama_barang"),
                        resultSet.getInt("total_qty"),
                        resultSet.getBigDecimal("total_nilai")
                ));
            }
        }
        return data;
    }

    private List<DashboardStatistics.LowStockItem> loadBarangStokMenipis(Connection connection) throws SQLException {
        List<DashboardStatistics.LowStockItem> data = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(STOK_MENIPIS_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                data.add(new DashboardStatistics.LowStockItem(
                        resultSet.getString("kode_barang"),
                        resultSet.getString("nama_barang"),
                        resultSet.getInt("stok")
                ));
            }
        }
        return data;
    }

    private List<DashboardStatistics.RecentTransaction> loadTransaksiTerbaru(Connection connection) throws SQLException {
        List<DashboardStatistics.RecentTransaction> data = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(TRANSAKSI_TERBARU_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                data.add(new DashboardStatistics.RecentTransaction(
                        resultSet.getString("no_faktur"),
                        resultSet.getString("tanggal"),
                        resultSet.getString("kasir"),
                        resultSet.getBigDecimal("grand_total")
                ));
            }
        }
        return data;
    }

    private List<DashboardStatistics.MonthlySales> loadPenjualanBulanan(Connection connection) throws SQLException {
        Map<String, BigDecimal> salesByMonth = new LinkedHashMap<>();
        YearMonth startMonth = YearMonth.now().minusMonths(11);
        for (int i = 0; i < 12; i++) {
            salesByMonth.put(startMonth.plusMonths(i).format(MONTH_KEY_FORMATTER), BigDecimal.ZERO);
        }

        try (PreparedStatement statement = connection.prepareStatement(PENJUALAN_BULANAN_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                salesByMonth.put(resultSet.getString("bulan"), resultSet.getBigDecimal("total"));
            }
        }

        List<DashboardStatistics.MonthlySales> data = new ArrayList<>();
        for (Map.Entry<String, BigDecimal> entry : salesByMonth.entrySet()) {
            YearMonth month = YearMonth.parse(entry.getKey(), MONTH_KEY_FORMATTER);
            data.add(new DashboardStatistics.MonthlySales(month.format(MONTH_LABEL_FORMATTER), entry.getValue()));
        }
        return data;
    }

    private List<DashboardStatistics.StockRanking> loadStokTerbanyak(Connection connection) throws SQLException {
        List<DashboardStatistics.StockRanking> data = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(STOK_TERBANYAK_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                data.add(new DashboardStatistics.StockRanking(
                        resultSet.getString("nama_barang"),
                        resultSet.getInt("stok")
                ));
            }
        }
        return data;
    }

    private List<DashboardStatistics.CategoryDistribution> loadDistribusiKategori(Connection connection) throws SQLException {
        List<DashboardStatistics.CategoryDistribution> data = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(DISTRIBUSI_KATEGORI_SQL);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                data.add(new DashboardStatistics.CategoryDistribution(
                        resultSet.getString("nama_kategori"),
                        resultSet.getInt("total_barang")
                ));
            }
        }
        return data;
    }
}
