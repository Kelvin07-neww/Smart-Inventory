package com.cvserbaada.smartinventory.model;

import java.math.BigDecimal;

public class Barang {
    private int id;
    private String kodeBarang;
    private String namaBarang;
    private int kategoriId;
    private String kategoriNama;
    private int supplierId;
    private String supplierNama;
    private BigDecimal hargaBeli;
    private BigDecimal hargaJual;
    private int stok;

    public Barang() {
    }

    public Barang(int id, String kodeBarang, String namaBarang, int kategoriId, String kategoriNama,
                  int supplierId, String supplierNama, BigDecimal hargaBeli, BigDecimal hargaJual, int stok) {
        this.id = id;
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.kategoriId = kategoriId;
        this.kategoriNama = kategoriNama;
        this.supplierId = supplierId;
        this.supplierNama = supplierNama;
        this.hargaBeli = hargaBeli;
        this.hargaJual = hargaJual;
        this.stok = stok;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKodeBarang() {
        return kodeBarang;
    }

    public void setKodeBarang(String kodeBarang) {
        this.kodeBarang = kodeBarang;
    }

    public String getNamaBarang() {
        return namaBarang;
    }

    public void setNamaBarang(String namaBarang) {
        this.namaBarang = namaBarang;
    }

    public int getKategoriId() {
        return kategoriId;
    }

    public void setKategoriId(int kategoriId) {
        this.kategoriId = kategoriId;
    }

    public String getKategoriNama() {
        return kategoriNama;
    }

    public void setKategoriNama(String kategoriNama) {
        this.kategoriNama = kategoriNama;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getSupplierNama() {
        return supplierNama;
    }

    public void setSupplierNama(String supplierNama) {
        this.supplierNama = supplierNama;
    }

    public BigDecimal getHargaBeli() {
        return hargaBeli;
    }

    public void setHargaBeli(BigDecimal hargaBeli) {
        this.hargaBeli = hargaBeli;
    }

    public BigDecimal getHargaJual() {
        return hargaJual;
    }

    public void setHargaJual(BigDecimal hargaJual) {
        this.hargaJual = hargaJual;
    }

    public int getStok() {
        return stok;
    }

    public void setStok(int stok) {
        this.stok = stok;
    }
}
