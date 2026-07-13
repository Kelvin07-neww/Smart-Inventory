package com.cvserbaada.smartinventory.dao;

import com.cvserbaada.smartinventory.model.Supplier;

import java.sql.SQLException;
import java.util.List;

public interface SupplierDAO {
    List<Supplier> findAll(String keyword, int offset, int limit) throws SQLException;

    int count(String keyword) throws SQLException;

    boolean existsByKode(String kodeSupplier, int excludeId) throws SQLException;

    String generateNextKode() throws SQLException;

    void insert(Supplier supplier) throws SQLException;

    void update(Supplier supplier) throws SQLException;

    void delete(int id) throws SQLException;
}
