package com.cvserbaada.smartinventory.dao;

import com.cvserbaada.smartinventory.model.DetailPenjualan;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface DetailPenjualanDAO {
    void insertBatch(Connection connection, int penjualanId, List<DetailPenjualan> detailList) throws SQLException;
}
