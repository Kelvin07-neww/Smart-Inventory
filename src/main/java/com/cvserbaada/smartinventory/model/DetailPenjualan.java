package com.cvserbaada.smartinventory.model;

import java.math.BigDecimal;

public class DetailPenjualan {
    private int id;
    private int penjualanId;
    private int barangId;
    private String kodeBarang;
    private String namaBarang;
    private BigDecimal harga;
    private int qty;
    private int stokTersedia;

    public DetailPenjualan() {
    }

    public DetailPenjualan(int id, int penjualanId, int barangId, String kodeBarang, String namaBarang,
                           BigDecimal harga, int qty, int stokTersedia) {
        this.id = id;
        this.penjualanId = penjualanId;
        this.barangId = barangId;
        this.kodeBarang = kodeBarang;
        this.namaBarang = namaBarang;
        this.harga = harga;
        this.qty = qty;
        this.stokTersedia = stokTersedia;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPenjualanId() {
        return penjualanId;
    }

    public void setPenjualanId(int penjualanId) {
        this.penjualanId = penjualanId;
    }

    public int getBarangId() {
        return barangId;
    }

    public void setBarangId(int barangId) {
        this.barangId = barangId;
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

    public BigDecimal getHarga() {
        return harga;
    }

    public void setHarga(BigDecimal harga) {
        this.harga = harga;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getStokTersedia() {
        return stokTersedia;
    }

    public void setStokTersedia(int stokTersedia) {
        this.stokTersedia = stokTersedia;
    }

    public BigDecimal getSubtotal() {
        if (harga == null) {
            return BigDecimal.ZERO;
        }
        return harga.multiply(BigDecimal.valueOf(qty));
    }
}
