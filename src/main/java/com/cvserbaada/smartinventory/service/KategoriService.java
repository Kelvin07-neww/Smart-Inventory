package com.cvserbaada.smartinventory.service;

import com.cvserbaada.smartinventory.dao.KategoriDAO;
import com.cvserbaada.smartinventory.dao.KategoriDAOImpl;
import com.cvserbaada.smartinventory.model.Kategori;

import java.sql.SQLException;
import java.util.List;

public class KategoriService {
    private final KategoriDAO kategoriDAO;

    public KategoriService() {
        this(new KategoriDAOImpl());
    }

    public KategoriService(KategoriDAO kategoriDAO) {
        this.kategoriDAO = kategoriDAO;
    }

    public List<Kategori> findPage(String keyword, int pageIndex, int pageSize) throws SQLException {
        int offset = Math.max(pageIndex, 0) * pageSize;
        return kategoriDAO.findAll(keyword, offset, pageSize);
    }

    public int count(String keyword) throws SQLException {
        return kategoriDAO.count(keyword);
    }

    public String generateNextKode() throws SQLException {
        return kategoriDAO.generateNextKode();
    }

    public void create(Kategori kategori) throws SQLException {
        validate(kategori, 0);
        kategoriDAO.insert(normalize(kategori));
    }

    public void update(Kategori kategori) throws SQLException {
        if (kategori.getId() <= 0) {
            throw new IllegalArgumentException("Pilih data kategori yang akan diubah.");
        }
        validate(kategori, kategori.getId());
        kategoriDAO.update(normalize(kategori));
    }

    public void delete(Kategori kategori) throws SQLException {
        if (kategori == null || kategori.getId() <= 0) {
            throw new IllegalArgumentException("Pilih data kategori yang akan dihapus.");
        }
        kategoriDAO.delete(kategori.getId());
    }

    private void validate(Kategori kategori, int excludeId) throws SQLException {
        String kode = kategori.getKodeKategori() == null ? "" : kategori.getKodeKategori().trim();
        String nama = kategori.getNamaKategori() == null ? "" : kategori.getNamaKategori().trim();

        if (kode.isBlank()) {
            throw new IllegalArgumentException("Kode kategori wajib diisi.");
        }

        if (nama.isBlank()) {
            throw new IllegalArgumentException("Nama kategori wajib diisi.");
        }

        if (nama.length() < 3) {
            throw new IllegalArgumentException("Nama kategori minimal 3 karakter.");
        }

        if (kategoriDAO.existsByKode(kode, excludeId)) {
            throw new IllegalArgumentException("Kode kategori sudah digunakan.");
        }
    }

    private Kategori normalize(Kategori kategori) {
        kategori.setKodeKategori(kategori.getKodeKategori().trim().toUpperCase());
        kategori.setNamaKategori(kategori.getNamaKategori().trim());
        return kategori;
    }
}
