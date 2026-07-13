package com.cvserbaada.smartinventory.service;

import com.cvserbaada.smartinventory.dao.BarangDAO;
import com.cvserbaada.smartinventory.dao.BarangDAOImpl;
import com.cvserbaada.smartinventory.model.Barang;
import com.cvserbaada.smartinventory.model.Kategori;
import com.cvserbaada.smartinventory.model.Supplier;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class BarangService {
    private final BarangDAO barangDAO;

    public BarangService() {
        this(new BarangDAOImpl());
    }

    public BarangService(BarangDAO barangDAO) {
        this.barangDAO = barangDAO;
    }

    public List<Barang> findPage(String keyword, int pageIndex, int pageSize) throws SQLException {
        int offset = Math.max(pageIndex, 0) * pageSize;
        return barangDAO.findAll(keyword, offset, pageSize);
    }

    public int count(String keyword) throws SQLException {
        return barangDAO.count(keyword);
    }

    public List<Kategori> findKategoriOptions() throws SQLException {
        return barangDAO.findKategoriOptions();
    }

    public List<Supplier> findSupplierOptions() throws SQLException {
        return barangDAO.findSupplierOptions();
    }

    public String generateNextKode() throws SQLException {
        return barangDAO.generateNextKode();
    }

    public void create(Barang barang) throws SQLException {
        validate(barang, 0);
        barangDAO.insert(normalize(barang));
    }

    public void update(Barang barang) throws SQLException {
        if (barang.getId() <= 0) {
            throw new IllegalArgumentException("Pilih data barang yang akan diedit.");
        }
        validate(barang, barang.getId());
        barangDAO.update(normalize(barang));
    }

    public void delete(Barang barang) throws SQLException {
        if (barang == null || barang.getId() <= 0) {
            throw new IllegalArgumentException("Pilih data barang yang akan dihapus.");
        }
        barangDAO.delete(barang.getId());
    }

    private void validate(Barang barang, int excludeId) throws SQLException {
        String kode = valueOf(barang.getKodeBarang()).toUpperCase();
        String nama = valueOf(barang.getNamaBarang());
        BigDecimal hargaBeli = barang.getHargaBeli();
        BigDecimal hargaJual = barang.getHargaJual();

        if (kode.isBlank()) {
            throw new IllegalArgumentException("Kode barang wajib diisi.");
        }

        if (barangDAO.existsByKode(kode, excludeId)) {
            throw new IllegalArgumentException("Kode barang sudah digunakan.");
        }

        if (nama.isBlank()) {
            throw new IllegalArgumentException("Nama barang wajib diisi.");
        }

        if (barang.getKategoriId() <= 0) {
            throw new IllegalArgumentException("Kategori wajib dipilih.");
        }

        if (barang.getSupplierId() <= 0) {
            throw new IllegalArgumentException("Supplier wajib dipilih.");
        }

        if (hargaBeli == null || hargaBeli.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Harga beli harus lebih besar dari 0.");
        }

        if (hargaJual == null) {
            throw new IllegalArgumentException("Harga jual wajib diisi.");
        }

        if (hargaJual.compareTo(hargaBeli) < 0) {
            throw new IllegalArgumentException("Harga jual tidak boleh lebih kecil dari harga beli.");
        }

        if (barang.getStok() < 0) {
            throw new IllegalArgumentException("Stok tidak boleh negatif.");
        }
    }

    private Barang normalize(Barang barang) {
        barang.setKodeBarang(valueOf(barang.getKodeBarang()).toUpperCase());
        barang.setNamaBarang(valueOf(barang.getNamaBarang()));
        return barang;
    }

    private String valueOf(String value) {
        return value == null ? "" : value.trim();
    }
}
