package com.cvserbaada.smartinventory.model;

public class Supplier {
    private int id;
    private String kodeSupplier;
    private String namaSupplier;
    private String alamat;
    private String telepon;

    public Supplier() {
    }

    public Supplier(int id, String kodeSupplier, String namaSupplier, String alamat, String telepon) {
        this.id = id;
        this.kodeSupplier = kodeSupplier;
        this.namaSupplier = namaSupplier;
        this.alamat = alamat;
        this.telepon = telepon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKodeSupplier() {
        return kodeSupplier;
    }

    public void setKodeSupplier(String kodeSupplier) {
        this.kodeSupplier = kodeSupplier;
    }

    public String getNamaSupplier() {
        return namaSupplier;
    }

    public void setNamaSupplier(String namaSupplier) {
        this.namaSupplier = namaSupplier;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getTelepon() {
        return telepon;
    }

    public void setTelepon(String telepon) {
        this.telepon = telepon;
    }
}
