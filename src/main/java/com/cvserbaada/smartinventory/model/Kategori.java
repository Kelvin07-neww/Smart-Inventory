package com.cvserbaada.smartinventory.model;

public class Kategori {
    private int id;
    private String kodeKategori;
    private String namaKategori;

    public Kategori() {
    }

    public Kategori(int id, String kodeKategori, String namaKategori) {
        this.id = id;
        this.kodeKategori = kodeKategori;
        this.namaKategori = namaKategori;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKodeKategori() {
        return kodeKategori;
    }

    public void setKodeKategori(String kodeKategori) {
        this.kodeKategori = kodeKategori;
    }

    public String getNamaKategori() {
        return namaKategori;
    }

    public void setNamaKategori(String namaKategori) {
        this.namaKategori = namaKategori;
    }
}
