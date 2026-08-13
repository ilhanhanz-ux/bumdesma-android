package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

/**
 * Ringkasan riwayat transaksi (jumlah kredit per status) untuk satu anggota
 * dalam sebuah kelompok. Dipakai di layar Detail Proposal Admin supaya admin
 * bisa menilai rekam jejak SELURUH anggota kelompok, bukan cuma pengaju.
 */
public class RiwayatAnggotaKelompok {
    @SerializedName("anggota_id")   private int anggotaId;
    @SerializedName("nama_lengkap") private String namaLengkap;
    @SerializedName("is_ketua")     private boolean isKetua;
    @SerializedName("is_pengaju")   private boolean isPengaju;
    @SerializedName("jumlah_lunas") private int jumlahLunas;
    @SerializedName("jumlah_aktif") private int jumlahAktif;
    @SerializedName("jumlah_macet") private int jumlahMacet;

    public int getAnggotaId()      { return anggotaId; }
    public String getNamaLengkap() { return namaLengkap; }
    public boolean isKetua()       { return isKetua; }
    public boolean isPengaju()     { return isPengaju; }
    public int getJumlahLunas()    { return jumlahLunas; }
    public int getJumlahAktif()    { return jumlahAktif; }
    public int getJumlahMacet()    { return jumlahMacet; }
}