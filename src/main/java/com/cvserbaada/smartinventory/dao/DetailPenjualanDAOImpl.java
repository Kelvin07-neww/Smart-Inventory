package com.cvserbaada.smartinventory.dao;

import com.cvserbaada.smartinventory.model.DetailPenjualan;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class DetailPenjualanDAOImpl implements DetailPenjualanDAO {
    private static final String INSERT_SQL = """
            INSERT INTO detail_penjualan (penjualan_id, barang_id, harga, qty, subtotal)
            VALUES (?, ?, ?, ?, ?)
            """;

    @Override
    public void insertBatch(Connection connection, int penjualanId, List<DetailPenjualan> detailList) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(INSERT_SQL)) {
            for (DetailPenjualan detail : detailList) {
                statement.setInt(1, penjualanId);
                statement.setInt(2, detail.getBarangId());
                statement.setBigDecimal(3, detail.getHarga());
                statement.setInt(4, detail.getQty());
                statement.setBigDecimal(5, detail.getSubtotal());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }
}
