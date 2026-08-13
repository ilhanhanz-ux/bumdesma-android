package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

// Satu baris porsi tagihan milik 1 anggota untuk 1 angsuran (bulan) tertentu.
// Dipakai untuk 3 konteks (field yang tidak relevan otomatis null/0 dari Gson):
//   - Anggota biasa: daftar tagihan porsi miliknya sendiri
//   - Admin: daftar porsi yang perlu diverifikasi / riwayat
//   - Ketua: nested di dalam DetailPorsiAngsuran saat atur porsi per anggota
public class AngsuranPorsi {

    @SerializedName("id")
    private int id;

    @SerializedName("angsuran_id")
    private int angsuranId;

    @SerializedName("anggota_id")
    private int anggotaId;

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

    // BARU: nominal proporsional berdasarkan alokasi pokok pinjaman anggota
    // ini (dari fitur Alokasi Pinjaman ke Anggota). Dipakai AturPorsiAdapter
    // buat pre-fill kotak input kalau ketua belum pernah mengisi porsi
    // untuk baris ini (jumlahPorsi masih 0). Kalau alokasi belum pernah
    // diisi sama sekali, backend kirim 0 di sini juga -- aman, tidak error.
    @SerializedName("nominal_default")
    private double nominalDefault;

    @SerializedName("tanggal_jatuh_tempo")
    private String tanggalJatuhTempo;

    @SerializedName("status_bayar")
    private String statusBayar;

    @SerializedName("bukti_bayar")
    private String buktiBayar;

    @SerializedName("tanggal_setor")
    private String tanggalSetor;

    @SerializedName("tanggal_verifikasi")
    private String tanggalVerifikasi;

    @SerializedName("catatan_admin")
    private String catatanAdmin;

    @SerializedName("hari_terlambat")
    private int hariTerlambat;

    public int getId() { return id; }
    public int getAngsuranId() { return angsuranId; }
    public int getAnggotaId() { return anggotaId; }
    public String getNoKredit() { return noKredit; }
    public int getNoAngsuran() { return noAngsuran; }
    public String getNamaLengkap() { return namaLengkap; }
    public String getNamaKelompok() { return namaKelompok; }
    public double getJumlahPorsi() { return jumlahPorsi; }
    public double getNominalDefault() { return nominalDefault; }
    public String getTanggalJatuhTempo() { return tanggalJatuhTempo; }
    public String getStatusBayar() { return statusBayar; }
    public String getBuktiBayar() { return buktiBayar; }
    public String getTanggalSetor() { return tanggalSetor; }
    public String getTanggalVerifikasi() { return tanggalVerifikasi; }
    public String getCatatanAdmin() { return catatanAdmin; }
    public int getHariTerlambat() { return hariTerlambat; }

    public boolean isBelumBayar() { return "belum_bayar".equals(statusBayar); }
    public boolean isMenungguVerifikasi() { return "menunggu_verifikasi".equals(statusBayar); }
    public boolean isSudahBayar() { return "sudah_bayar".equals(statusBayar); }
    public boolean isDitolak() { return "ditolak".equals(statusBayar); }
}