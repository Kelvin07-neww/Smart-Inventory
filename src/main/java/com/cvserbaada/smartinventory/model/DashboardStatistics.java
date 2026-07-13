package com.cvserbaada.smartinventory.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class DashboardStatistics {
    private int totalBarang;
    private int totalSupplier;
    private BigDecimal totalPenjualanHariIni = BigDecimal.ZERO;
    private BigDecimal totalNilaiInventory = BigDecimal.ZERO;
    private List<ProductSales> produkTerlaris = new ArrayList<>();
    private List<LowStockItem> barangStokMenipis = new ArrayList<>();
    private List<RecentTransaction> transaksiTerbaru = new ArrayList<>();
    private List<MonthlySales> penjualanBulanan = new ArrayList<>();
    private List<StockRanking> stokTerbanyak = new ArrayList<>();
    private List<CategoryDistribution> distribusiKategori = new ArrayList<>();

    public int getTotalBarang() {
        return totalBarang;
    }

    public void setTotalBarang(int totalBarang) {
        this.totalBarang = totalBarang;
    }

    public int getTotalSupplier() {
        return totalSupplier;
    }

    public void setTotalSupplier(int totalSupplier) {
        this.totalSupplier = totalSupplier;
    }

    public BigDecimal getTotalPenjualanHariIni() {
        return totalPenjualanHariIni;
    }

    public void setTotalPenjualanHariIni(BigDecimal totalPenjualanHariIni) {
        this.totalPenjualanHariIni = totalPenjualanHariIni == null ? BigDecimal.ZERO : totalPenjualanHariIni;
    }

    public BigDecimal getTotalNilaiInventory() {
        return totalNilaiInventory;
    }

    public void setTotalNilaiInventory(BigDecimal totalNilaiInventory) {
        this.totalNilaiInventory = totalNilaiInventory == null ? BigDecimal.ZERO : totalNilaiInventory;
    }

    public List<ProductSales> getProdukTerlaris() {
        return produkTerlaris;
    }

    public void setProdukTerlaris(List<ProductSales> produkTerlaris) {
        this.produkTerlaris = produkTerlaris == null ? new ArrayList<>() : produkTerlaris;
    }

    public List<LowStockItem> getBarangStokMenipis() {
        return barangStokMenipis;
    }

    public void setBarangStokMenipis(List<LowStockItem> barangStokMenipis) {
        this.barangStokMenipis = barangStokMenipis == null ? new ArrayList<>() : barangStokMenipis;
    }

    public List<RecentTransaction> getTransaksiTerbaru() {
        return transaksiTerbaru;
    }

    public void setTransaksiTerbaru(List<RecentTransaction> transaksiTerbaru) {
        this.transaksiTerbaru = transaksiTerbaru == null ? new ArrayList<>() : transaksiTerbaru;
    }

    public List<MonthlySales> getPenjualanBulanan() {
        return penjualanBulanan;
    }

    public void setPenjualanBulanan(List<MonthlySales> penjualanBulanan) {
        this.penjualanBulanan = penjualanBulanan == null ? new ArrayList<>() : penjualanBulanan;
    }

    public List<StockRanking> getStokTerbanyak() {
        return stokTerbanyak;
    }

    public void setStokTerbanyak(List<StockRanking> stokTerbanyak) {
        this.stokTerbanyak = stokTerbanyak == null ? new ArrayList<>() : stokTerbanyak;
    }

    public List<CategoryDistribution> getDistribusiKategori() {
        return distribusiKategori;
    }

    public void setDistribusiKategori(List<CategoryDistribution> distribusiKategori) {
        this.distribusiKategori = distribusiKategori == null ? new ArrayList<>() : distribusiKategori;
    }

    public record ProductSales(String kodeBarang, String namaBarang, int totalQty, BigDecimal totalNilai) {
    }

    public record LowStockItem(String kodeBarang, String namaBarang, int stok) {
    }

    public record RecentTransaction(String noFaktur, String tanggal, String kasir, BigDecimal grandTotal) {
    }

    public record MonthlySales(String bulan, BigDecimal total) {
    }

    public record StockRanking(String namaBarang, int stok) {
    }

    public record CategoryDistribution(String namaKategori, int totalBarang) {
    }
}
