package com.amikom.bumdesma.model;

import com.amikom.bumdesma.utils.Constants;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Proposal {
    @SerializedName("id")                 private int id;
    @SerializedName("no_proposal")        private String noProposal;
    @SerializedName("nama_pengaju")       private String namaPengaju;
    @SerializedName("nama_kelompok")      private String namaKelompok;
    @SerializedName("nama_desa")          private String namaDesa;
    @SerializedName("no_telepon")         private String noTelepon;
    @SerializedName("jumlah_pinjaman")    private double jumlahPinjaman;
    @SerializedName("jangka_waktu_bulan") private int jangkaWaktu;
    @SerializedName("bunga_persen")       private double bungaPersen;
    @SerializedName("tujuan_pinjaman")    private String tujuan;
    @SerializedName("deskripsi_usaha")    private String deskripsi;
    @SerializedName("status_pengajuan")   private String status;
    @SerializedName("catatan_admin")      private String catatanAdmin;
    @SerializedName("tanggal_pengajuan")  private String tanggalPengajuan;
    @SerializedName("dok_proposal")       private String dokProposal;
    @SerializedName("dok_ktp")            private String dokKtp;
    @SerializedName("dok_jaminan")        private String dokJaminan;

    // ── fitur limit pinjaman dinamis & dana tersedia ──
    @SerializedName("jumlah_disetujui")       private Double jumlahDisetujui;       // null selama masih 'menunggu'
    @SerializedName("anggota_limit_pinjaman") private Double anggotaLimitPinjaman;
    @SerializedName("dana_tersedia")          private Double danaTersedia;          // cuma dikirim server buat role admin

    // ── BARU: limit berbasis status ketua/anggota + riwayat kredit pengaju ──
    @SerializedName("is_ketua")      private boolean isKetua;
    @SerializedName("limit_berlaku") private Double limitBerlaku;   // limit final (sudah termasuk bonus riwayat kalau ada)
    @SerializedName("riwayat_bagus") private boolean riwayatBagus;

    // ── BARU: riwayat transaksi seluruh anggota kelompok — cuma dikirim server
    // buat role admin (null kalau yang minta anggota biasa) ──
    @SerializedName("riwayat_kelompok") private List<RiwayatAnggotaKelompok> riwayatKelompok;

    public int    getId()             { return id; }
    public String getNoProposal()     { return noProposal; }
    public String getNamaPengaju()    { return namaPengaju; }
    public String getNamaKelompok()   { return namaKelompok; }
    public String getNamaDesa()       { return namaDesa; }
    public String getNoTelepon()      { return noTelepon; }
    public double getJumlahPinjaman() { return jumlahPinjaman; }
    public int    getJangkaWaktu()    { return jangkaWaktu; }
    public double getBungaPersen()    { return bungaPersen; }
    public String getTujuan()         { return tujuan; }
    public String getDeskripsi()      { return deskripsi; }
    public String getStatus()         { return status; }
    public String getCatatanAdmin()   { return catatanAdmin; }
    public String getTanggalPengajuan(){ return tanggalPengajuan; }
    public String getDokProposal()    { return dokProposal; }
    public String getDokKtp()         { return dokKtp; }
    public String getDokJaminan()     { return dokJaminan; }

    public Double getJumlahDisetujui()       { return jumlahDisetujui; }
    public Double getAnggotaLimitPinjaman()  { return anggotaLimitPinjaman; }
    public Double getDanaTersedia()          { return danaTersedia; }

    public boolean isKetua()               { return isKetua; }
    public Double  getLimitBerlaku()       { return limitBerlaku; }
    public boolean isRiwayatBagus()        { return riwayatBagus; }

    public List<RiwayatAnggotaKelompok> getRiwayatKelompok() { return riwayatKelompok; }

    // Helper: gabungkan path relatif dari server dengan FILE_BASE_URL
    // supaya jadi link lengkap yang bisa langsung dibuka via browser/Intent.
    public String getDokProposalUrl() {
        return buildFileUrl(dokProposal);
    }

    public String getDokKtpUrl() {
        return buildFileUrl(dokKtp);
    }

    public String getDokJaminanUrl() {
        return buildFileUrl(dokJaminan);
    }

    private String buildFileUrl(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }
        return Constants.FILE_BASE_URL + relativePath;
    }
}