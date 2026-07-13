package com.cvserbaada.smartinventory.dao;

import com.cvserbaada.smartinventory.model.Barang;
import com.cvserbaada.smartinventory.model.Kategori;
import com.cvserbaada.smartinventory.model.Supplier;

import java.sql.SQLException;
import java.util.List;

public interface BarangDAO {
    List<Barang> findAll(String keyword, int offset, int limit) throws SQLException;

    int count(String keyword) throws SQLException;

    boolean existsByKode(String kodeBarang, int excludeId) throws SQLException;

    String generateNextKode() throws SQLException;

    void insert(Barang barang) throws SQLException;

    void update(Barang barang) throws SQLException;

    void delete(int id) throws SQLException;

    List<Kategori> findKategoriOptions() throws SQLException;

    List<Supplier> findSupplierOptions() throws SQLException;
}
