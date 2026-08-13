package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class Angsuran {
    @SerializedName("id")                  private int id;
    @SerializedName("kredit_id")           private int kreditId;
    @SerializedName("no_kredit")           private String noKredit;
    @SerializedName("nama_lengkap")        private String namaLengkap;
    @SerializedName("no_angsuran")         private int noAngsuran;
    @SerializedName("total_bayar")         private double totalBayar;
    @SerializedName("tanggal_jatuh_tempo") private String tanggalJatuhTempo;
    @SerializedName("tanggal_bayar")       private String tanggalBayar;
    @SerializedName("status_bayar")        private String statusBayar;
    @SerializedName("hari_terlambat")      private int hariTerlambat;

    public int    getId()                { return id; }
    public int    getKreditId()          { return kreditId; }
    public String getNoKredit()          { return noKredit; }
    public String getNamaLengkap()       { return namaLengkap; }
    public int    getNoAngsuran()        { return noAngsuran; }
    public double getTotalBayar()        { return totalBayar; }
    public String getTanggalJatuhTempo() { return tanggalJatuhTempo; }
    public String getTanggalBayar()      { return tanggalBayar; }
    public String getStatusBayar()       { return statusBayar; }
    public int    getHariTerlambat()     { return hariTerlambat; }
    public boolean isBelumBayar() { return "belum_bayar".equals(statusBayar); }
    public boolean isSudahBayar() { return "sudah_bayar".equals(statusBayar); }
    public boolean isTerlambat()  { return hariTerlambat > 0; }
}