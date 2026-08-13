package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class AnggotaAdmin {
    @SerializedName("id")           private int id;
    @SerializedName("nik")          private String nik;
    @SerializedName("namaLengkap")  private String namaLengkap;
    @SerializedName("tempatLahir")  private String tempatLahir;
    @SerializedName("tanggalLahir") private String tanggalLahir;
    @SerializedName("jenisKelamin") private String jenisKelamin;
    @SerializedName("noTelepon")    private String noTelepon;
    @SerializedName("alamat")       private String alamat;
    @SerializedName("namaKelompok") private String namaKelompok;
    @SerializedName("namaDesa")     private String namaDesa;
    @SerializedName("statusAktif")  private boolean statusAktif;
    // ── BARU ──
    @SerializedName("isKetua")      private boolean isKetua;

    public int     getId()            { return id; }
    public String  getNik()           { return nik; }
    public String  getNamaLengkap()   { return namaLengkap; }
    public String  getTempatLahir()   { return tempatLahir; }
    public String  getTanggalLahir()  { return tanggalLahir; }
    public String  getJenisKelamin()  { return jenisKelamin; }
    public String  getNoTelepon()     { return noTelepon; }
    public String  getAlamat()        { return alamat; }
    public String  getNamaKelompok()  { return namaKelompok; }
    public String  getNamaDesa()      { return namaDesa; }
    public boolean isStatusAktif()    { return statusAktif; }
    // ── BARU ──
    public boolean isKetua()          { return isKetua; }
}