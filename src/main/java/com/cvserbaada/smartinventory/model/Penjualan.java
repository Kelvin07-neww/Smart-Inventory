package com.cvserbaada.smartinventory.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Penjualan {
    private int id;
    private String noFaktur;
    private LocalDateTime tanggal;
    private int userId;
    private String kasir;
    private BigDecimal subtotal;
    private BigDecimal diskon;
    private BigDecimal ppn;
    private BigDecimal grandTotal;
    private List<DetailPenjualan> detailList = new ArrayList<>();

    public Penjualan() {
    }

    public Penjualan(int id, String noFaktur, LocalDateTime tanggal, int userId, String kasir,
                     BigDecimal subtotal, BigDecimal diskon, BigDecimal ppn, BigDecimal grandTotal,
                     List<DetailPenjualan> detailList) {
        this.id = id;
        this.noFaktur = noFaktur;
        this.tanggal = tanggal;
        this.userId = userId;
        this.kasir = kasir;
        this.subtotal = subtotal;
        this.diskon = diskon;
        this.ppn = ppn;
        this.grandTotal = grandTotal;
        this.detailList = detailList == null ? new ArrayList<>() : detailList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNoFaktur() {
        return noFaktur;
    }

    public void setNoFaktur(String noFaktur) {
        this.noFaktur = noFaktur;
    }

    public LocalDateTime getTanggal() {
        return tanggal;
    }

    public void setTanggal(LocalDateTime tanggal) {
        this.tanggal = tanggal;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getKasir() {
        return kasir;
    }

    public void setKasir(String kasir) {
        this.kasir = kasir;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getDiskon() {
        return diskon;
    }

    public void setDiskon(BigDecimal diskon) {
        this.diskon = diskon;
    }

    public BigDecimal getPpn() {
        return ppn;
    }

    public void setPpn(BigDecimal ppn) {
        this.ppn = ppn;
    }

    public BigDecimal getGrandTotal() {
        return grandTotal;
    }

    public void setGrandTotal(BigDecimal grandTotal) {
        this.grandTotal = grandTotal;
    }

    public List<DetailPenjualan> getDetailList() {
        return detailList;
    }

    public void setDetailList(List<DetailPenjualan> detailList) {
        this.detailList = detailList == null ? new ArrayList<>() : detailList;
    }
}
