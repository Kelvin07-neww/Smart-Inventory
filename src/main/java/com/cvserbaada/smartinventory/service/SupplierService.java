package com.cvserbaada.smartinventory.service;

import com.cvserbaada.smartinventory.dao.SupplierDAO;
import com.cvserbaada.smartinventory.dao.SupplierDAOImpl;
import com.cvserbaada.smartinventory.model.Supplier;

import java.sql.SQLException;
import java.util.List;

public class SupplierService {
    private final SupplierDAO supplierDAO;

    public SupplierService() {
        this(new SupplierDAOImpl());
    }

    public SupplierService(SupplierDAO supplierDAO) {
        this.supplierDAO = supplierDAO;
    }

    public List<Supplier> findPage(String keyword, int pageIndex, int pageSize) throws SQLException {
        int offset = Math.max(pageIndex, 0) * pageSize;
        return supplierDAO.findAll(keyword, offset, pageSize);
    }

    public int count(String keyword) throws SQLException {
        return supplierDAO.count(keyword);
    }

    public String generateNextKode() throws SQLException {
        return supplierDAO.generateNextKode();
    }

    public void create(Supplier supplier) throws SQLException {
        validate(supplier, 0);
        supplierDAO.insert(normalize(supplier));
    }

    public void update(Supplier supplier) throws SQLException {
        if (supplier.getId() <= 0) {
            throw new IllegalArgumentException("Pilih data supplier yang akan diedit.");
        }
        validate(supplier, supplier.getId());
        supplierDAO.update(normalize(supplier));
    }

    public void delete(Supplier supplier) throws SQLException {
        if (supplier == null || supplier.getId() <= 0) {
            throw new IllegalArgumentException("Pilih data supplier yang akan dihapus.");
        }
        supplierDAO.delete(supplier.getId());
    }

    private void validate(Supplier supplier, int excludeId) throws SQLException {
        String kode = valueOf(supplier.getKodeSupplier()).toUpperCase();
        String nama = valueOf(supplier.getNamaSupplier());
        String alamat = valueOf(supplier.getAlamat());
        String telepon = valueOf(supplier.getTelepon());

        if (kode.isBlank()) {
            throw new IllegalArgumentException("Kode supplier wajib diisi.");
        }

        if (supplierDAO.existsByKode(kode, excludeId)) {
            throw new IllegalArgumentException("Kode supplier sudah digunakan.");
        }

        if (nama.isBlank()) {
            throw new IllegalArgumentException("Nama supplier wajib diisi.");
        }

        if (nama.length() < 3) {
            throw new IllegalArgumentException("Nama supplier minimal 3 karakter.");
        }

        if (alamat.isBlank()) {
            throw new IllegalArgumentException("Alamat wajib diisi.");
        }

        if (telepon.isBlank()) {
            throw new IllegalArgumentException("Telepon wajib diisi.");
        }

        if (!telepon.matches("\\d+")) {
            throw new IllegalArgumentException("Telepon hanya boleh berisi angka.");
        }

        if (telepon.length() < 10 || telepon.length() > 15) {
            throw new IllegalArgumentException("Telepon harus 10 sampai 15 digit.");
        }
    }

    private Supplier normalize(Supplier supplier) {
        supplier.setKodeSupplier(valueOf(supplier.getKodeSupplier()).toUpperCase());
        supplier.setNamaSupplier(valueOf(supplier.getNamaSupplier()));
        supplier.setAlamat(valueOf(supplier.getAlamat()));
        supplier.setTelepon(valueOf(supplier.getTelepon()));
        return supplier;
    }

    private String valueOf(String value) {
        return value == null ? "" : value.trim();
    }
}
