package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

// Satu baris porsi yang perlu/sudah diverifikasi admin.
public class PorsiVerifikasi {

    @SerializedName("id")
    private int id;

    @SerializedName("angsuran_id")
    private int angsuranId;

    @SerializedName("no_kredit")
    private String noKredit;

    @SerializedName("no_angsuran")
    private int noAngsuran;

    @SerializedName("nama_lengkap")
    private String namaLengkap;

    @SerializedName("nama_kelompok")
    private String namaKelompok;

    @SerializedName("jumlah_porsi")
    private double jumlahPorsi;

    @SerializedName("status_bayar")
    private String statusBayar; // menunggu_verifikasi | sudah_bayar | ditolak

    @SerializedName("bukti_bayar")
    private String buktiBayar; // nullable, path relatif di server

    @SerializedName("tanggal_setor")
    private String tanggalSetor;

    @SerializedName("tanggal_verifikasi")
    private String tanggalVerifikasi; // nullable

    @SerializedName("catatan_admin")
    private String catatanAdmin; // nullable

    public int getId() { return id; }
    public int getAngsuranId() { return angsuranId; }
    public String getNoKredit() { return noKredit; }
    public int getNoAngsuran() { return noAngsuran; }
    public String getNamaLengkap() { return namaLengkap; }
    public String getNamaKelompok() { return namaKelompok; }
    public double getJumlahPorsi() { return jumlahPorsi; }
    public String getStatusBayar() { return statusBayar; }
    public String getBuktiBayar() { return buktiBayar; }
    public String getTanggalSetor() { return tanggalSetor; }
    public String getTanggalVerifikasi() { return tanggalVerifikasi; }
    public String getCatatanAdmin() { return catatanAdmin; }

    public boolean isMenunggu() {
        return "menunggu_verifikasi".equals(statusBayar);
    }
}