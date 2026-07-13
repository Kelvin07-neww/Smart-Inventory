package com.cvserbaada.smartinventory.service;

import com.cvserbaada.smartinventory.dao.PenjualanDAO;
import com.cvserbaada.smartinventory.dao.PenjualanDAOImpl;
import com.cvserbaada.smartinventory.model.Barang;
import com.cvserbaada.smartinventory.model.DetailPenjualan;
import com.cvserbaada.smartinventory.model.Penjualan;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PenjualanService {
    private final PenjualanDAO penjualanDAO;

    public PenjualanService() {
        this(new PenjualanDAOImpl());
    }

    public PenjualanService(PenjualanDAO penjualanDAO) {
        this.penjualanDAO = penjualanDAO;
    }

    public String generateNoFaktur() throws SQLException {
        return penjualanDAO.generateNoFaktur();
    }

    public List<Barang> findBarangForSale(String keyword) throws SQLException {
        return penjualanDAO.findBarangForSale(keyword);
    }

    public List<Penjualan> findRecentSales(int limit) throws SQLException {
        return penjualanDAO.findRecentSales(limit);
    }

    public List<DetailPenjualan> findDetailsByPenjualanId(int penjualanId) throws SQLException {
        if (penjualanId <= 0) {
            throw new IllegalArgumentException("Transaksi laporan tidak valid.");
        }
        return penjualanDAO.findDetailsByPenjualanId(penjualanId);
    }

    public void saveTransaction(Penjualan penjualan) throws SQLException {
        validate(penjualan);
        penjualan.setTanggal(penjualan.getTanggal() == null ? LocalDateTime.now() : penjualan.getTanggal());
        penjualanDAO.saveTransaction(penjualan);
    }

    private void validate(Penjualan penjualan) {
        if (penjualan == null) {
            throw new IllegalArgumentException("Data transaksi tidak valid.");
        }

        if (penjualan.getUserId() <= 0) {
            throw new IllegalArgumentException("Sesi kasir tidak valid. Silakan login ulang.");
        }

        if (penjualan.getNoFaktur() == null || penjualan.getNoFaktur().isBlank()) {
            throw new IllegalArgumentException("Nomor faktur wajib tersedia.");
        }

        List<DetailPenjualan> detailList = penjualan.getDetailList();
        if (detailList == null || detailList.isEmpty()) {
            throw new IllegalArgumentException("Transaksi tidak boleh kosong.");
        }

        Set<Integer> barangIds = new HashSet<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        for (DetailPenjualan detail : detailList) {
            if (detail.getBarangId() <= 0) {
                throw new IllegalArgumentException("Barang tidak valid.");
            }
            if (!barangIds.add(detail.getBarangId())) {
                throw new IllegalArgumentException("Barang tidak boleh duplikat dalam keranjang.");
            }
            if (detail.getQty() < 1) {
                throw new IllegalArgumentException("Qty minimal 1.");
            }
            if (detail.getQty() > detail.getStokTersedia()) {
                throw new IllegalArgumentException("Qty " + detail.getNamaBarang() + " melebihi stok tersedia.");
            }
            if (detail.getHarga() == null || detail.getHarga().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Harga barang tidak valid.");
            }
            subtotal = subtotal.add(detail.getSubtotal());
        }

        BigDecimal diskon = valueOrZero(penjualan.getDiskon());
        BigDecimal ppn = valueOrZero(penjualan.getPpn());
        BigDecimal grandTotal = subtotal.subtract(diskon).add(ppn);

        if (diskon.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Diskon tidak boleh negatif.");
        }
        if (ppn.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("PPN tidak boleh negatif.");
        }
        if (grandTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Grand total tidak boleh nol.");
        }

        penjualan.setSubtotal(subtotal);
        penjualan.setDiskon(diskon);
        penjualan.setPpn(ppn);
        penjualan.setGrandTotal(grandTotal);
    }

    private BigDecimal valueOrZero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
