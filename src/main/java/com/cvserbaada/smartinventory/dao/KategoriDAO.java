package com.cvserbaada.smartinventory.dao;

import com.cvserbaada.smartinventory.model.Kategori;

import java.sql.SQLException;
import java.util.List;

public interface KategoriDAO {
    List<Kategori> findAll(String keyword, int offset, int limit) throws SQLException;

    int count(String keyword) throws SQLException;

    boolean existsByKode(String kodeKategori, int excludeId) throws SQLException;

    String generateNextKode() throws SQLException;

    void insert(Kategori kategori) throws SQLException;

    void update(Kategori kategori) throws SQLException;

    void delete(int id) throws SQLException;
}
