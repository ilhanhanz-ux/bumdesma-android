package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class AnggotaProfil {
    @SerializedName("nik")           private String nik;
    @SerializedName("nama_lengkap")  private String namaLengkap;
    @SerializedName("tempat_lahir")  private String tempatLahir;
    @SerializedName("tanggal_lahir") private String tanggalLahir;
    @SerializedName("jenis_kelamin") private String jenisKelamin;
    @SerializedName("alamat")        private String alamat;
    @SerializedName("no_telepon")    private String noTelepon;
    @SerializedName("nama_kelompok") private String namaKelompok;
    @SerializedName("nama_desa")     private String namaDesa;
    @SerializedName("status_aktif")  private boolean statusAktif;

    public String  getNik()          { return nik; }
    public String  getNamaLengkap()  { return namaLengkap; }
    public String  getTempatLahir()  { return tempatLahir; }
    public String  getTanggalLahir() { return tanggalLahir; }
    public String  getJenisKelamin() { return jenisKelamin; }
    public String  getAlamat()       { return alamat; }
    public String  getNoTelepon()    { return noTelepon; }
    public String  getNamaKelompok() { return namaKelompok; }
    public String  getNamaDesa()     { return namaDesa; }
    public boolean isStatusAktif()   { return statusAktif; }
}