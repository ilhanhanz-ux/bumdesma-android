package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

// Satu baris tagihan porsi milik anggota yang login, untuk 1 bulan angsuran.
public class PorsiSaya {

    @SerializedName("id")
    private int id;

    @SerializedName("angsuran_id")
    private int angsuranId;

    @SerializedName("no_angsuran")
    private int noAngsuran;

    @SerializedName("tanggal_jatuh_tempo")
    private String tanggalJatuhTempo;

    @SerializedName("jumlah_porsi")
    private double jumlahPorsi;

    @SerializedName("status_bayar")
    private String statusBayar; // belum_bayar | menunggu_verifikasi | sudah_bayar | ditolak

    @SerializedName("bukti_bayar")
    private String buktiBayar; // nullable, path relatif di server

    @SerializedName("catatan_admin")
    private String catatanAdmin; // alasan penolakan, nullable

    @SerializedName("tanggal_setor")
    private String tanggalSetor; // nullable

    @SerializedName("tanggal_verifikasi")
    private String tanggalVerifikasi; // nullable

    @SerializedName("hari_terlambat")
    private int hariTerlambat; // 0 kalau belum/tidak terlambat

    public int getId() { return id; }
    public int getAngsuranId() { return angsuranId; }
    public int getNoAngsuran() { return noAngsuran; }
    public String getTanggalJatuhTempo() { return tanggalJatuhTempo; }
    public double getJumlahPorsi() { return jumlahPorsi; }
    public String getStatusBayar() { return statusBayar; }
    public String getBuktiBayar() { return buktiBayar; }
    public String getCatatanAdmin() { return catatanAdmin; }
    public String getTanggalSetor() { return tanggalSetor; }
    public String getTanggalVerifikasi() { return tanggalVerifikasi; }
    public int getHariTerlambat() { return hariTerlambat; }

    public boolean isBisaUpload() {
        return "belum_bayar".equals(statusBayar) || "ditolak".equals(statusBayar);
    }
}