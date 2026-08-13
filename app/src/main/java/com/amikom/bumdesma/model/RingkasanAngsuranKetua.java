package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

// Dipakai layar Ketua: daftar angsuran (jadwal bulanan) kredit kelompoknya,
// beserta status apakah porsi tiap anggota untuk bulan itu sudah lengkap dibagi.
public class RingkasanAngsuranKetua {

    @SerializedName("angsuran_id")
    private int angsuranId;

    @SerializedName("no_angsuran")
    private int noAngsuran;

    @SerializedName("total_bayar")
    private double totalBayar;

    @SerializedName("tanggal_jatuh_tempo")
    private String tanggalJatuhTempo;

    @SerializedName("status_induk")
    private String statusInduk;

    @SerializedName("total_dibagi")
    private double totalDibagi;

    @SerializedName("sudah_lengkap")
    private boolean sudahLengkap;

    public int getAngsuranId() { return angsuranId; }
    public int getNoAngsuran() { return noAngsuran; }
    public double getTotalBayar() { return totalBayar; }
    public String getTanggalJatuhTempo() { return tanggalJatuhTempo; }
    public String getStatusInduk() { return statusInduk; }
    public double getTotalDibagi() { return totalDibagi; }
    public boolean isSudahLengkap() { return sudahLengkap; }
    public boolean isLunas() { return "sudah_bayar".equals(statusInduk); }
}