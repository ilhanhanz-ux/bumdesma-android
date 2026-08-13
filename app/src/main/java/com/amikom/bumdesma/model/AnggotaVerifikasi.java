package com.amikom.bumdesma.model;

import com.amikom.bumdesma.utils.Constants;
import com.google.gson.annotations.SerializedName;

public class AnggotaVerifikasi {
    @SerializedName("anggota_id")        private int anggotaId;
    @SerializedName("nama")              private String nama;
    @SerializedName("username")          private String username;
    @SerializedName("no_telepon")        private String noTelepon;
    @SerializedName("nama_kelompok")     private String namaKelompok;
    @SerializedName("nama_desa")         private String namaDesa;
    @SerializedName("tempat_lahir")      private String tempatLahir;
    @SerializedName("tanggal_lahir")     private String tanggalLahir;
    @SerializedName("alamat")            private String alamat;
    @SerializedName("foto_ktp")          private String fotoKtp;
    @SerializedName("status_verifikasi") private String statusVerifikasi;
    @SerializedName("kelompok_sudah_ada_ketua") private boolean kelompokSudahAdaKetua;
    @SerializedName("nama_ketua_sekarang")      private String  namaKetuaSekarang;

    public int    getAnggotaId()        { return anggotaId; }
    public String getNama()             { return nama; }
    public String getUsername()         { return username; }
    public String getNoTelepon()        { return noTelepon; }
    public String getNamaKelompok()     { return namaKelompok; }
    public String getNamaDesa()         { return namaDesa; }
    public String getTempatLahir()      { return tempatLahir; }
    public String getTanggalLahir()     { return tanggalLahir; }
    public String getAlamat()           { return alamat; }
    public String getFotoKtp()          { return fotoKtp; }
    public String getStatusVerifikasi() { return statusVerifikasi; }
    public boolean isKelompokSudahAdaKetua() { return kelompokSudahAdaKetua; }
    public String  getNamaKetuaSekarang()    { return namaKetuaSekarang; }

    // Sama persis pola buildFileUrl() di Proposal.java
    public String getFotoKtpUrl() {
        if (fotoKtp == null || fotoKtp.trim().isEmpty()) {
            return null;
        }
        return Constants.FILE_BASE_URL + fotoKtp;
    }
}