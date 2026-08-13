package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class TunggakanItem {
    @SerializedName("no_angsuran")         private int noAngsuran;
    @SerializedName("tanggal_jatuh_tempo")  private String tanggalJatuhTempo;
    @SerializedName("total_bayar")          private double totalBayar;
    @SerializedName("status_bayar")         private String statusBayar;
    @SerializedName("no_kredit")            private String noKredit;
    @SerializedName("pokok_pinjaman")       private double pokokPinjaman;
    @SerializedName("nama_lengkap")         private String namaLengkap;
    @SerializedName("nama_kelompok")        private String namaKelompok;
    @SerializedName("nama_desa")            private String namaDesa;
    @SerializedName("no_telepon")           private String noTelepon;
    @SerializedName("hari_tunggak")         private int hariTunggak;

    public int getNoAngsuran()             { return noAngsuran; }
    public String getTanggalJatuhTempo()   { return tanggalJatuhTempo; }
    public double getTotalBayar()          { return totalBayar; }
    public String getStatusBayar()         { return statusBayar; }
    public String getNoKredit()            { return noKredit; }
    public double getPokokPinjaman()       { return pokokPinjaman; }
    public String getNamaLengkap()         { return namaLengkap; }
    public String getNamaKelompok()        { return namaKelompok; }
    public String getNamaDesa()            { return namaDesa; }
    public String getNoTelepon()           { return noTelepon; }
    public int getHariTunggak()            { return hariTunggak; }
}