package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RiwayatKelompokItem {
    @SerializedName("id")          private int id;
    @SerializedName("namaLengkap") private String namaLengkap;
    @SerializedName("isPengaju")   private boolean isPengaju;
    @SerializedName("jumlahLunas") private int jumlahLunas;
    @SerializedName("jumlahAktif") private int jumlahAktif;
    @SerializedName("jumlahMacet") private int jumlahMacet;

    // ── BARU: daftar transaksi individual milik anggota ini ──
    @SerializedName("transaksi")   private List<TransaksiKelompokItem> transaksi;

    public int     getId()          { return id; }
    public String  getNamaLengkap() { return namaLengkap; }
    public boolean isPengaju()      { return isPengaju; }
    public int     getJumlahLunas() { return jumlahLunas; }
    public int     getJumlahAktif() { return jumlahAktif; }
    public int     getJumlahMacet() { return jumlahMacet; }

    public List<TransaksiKelompokItem> getTransaksi() { return transaksi; }
}