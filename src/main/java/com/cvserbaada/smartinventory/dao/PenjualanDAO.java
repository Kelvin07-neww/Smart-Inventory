package com.cvserbaada.smartinventory.dao;

import com.cvserbaada.smartinventory.model.Barang;
import com.cvserbaada.smartinventory.model.Penjualan;

import java.sql.SQLException;
import java.util.List;

public interface PenjualanDAO {
    String generateNoFaktur() throws SQLException;

    List<Barang> findBarangForSale(String keyword) throws SQLException;

    List<Penjualan> findRecentSales(int limit) throws SQLException;

    List<com.cvserbaada.smartinventory.model.DetailPenjualan> findDetailsByPenjualanId(int penjualanId) throws SQLException;

    void saveTransaction(Penjualan penjualan) throws SQLException;
}
