package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class RekapBulananItem {
    @SerializedName("tanggal_bayar") private String tanggalBayar;
    @SerializedName("jumlah_pokok")  private double jumlahPokok;
    @SerializedName("jumlah_bunga")  private double jumlahBunga;
    @SerializedName("jumlah_denda")  private double jumlahDenda;
    @SerializedName("total_bayar")   private double totalBayar;
    @SerializedName("no_angsuran")   private int noAngsuran;
    @SerializedName("no_kredit")     private String noKredit;
    @SerializedName("nama_lengkap")  private String namaLengkap;
    @SerializedName("nama_desa")     private String namaDesa;

    public String getTanggalBayar() { return tanggalBayar; }
    public double getJumlahPokok()  { return jumlahPokok; }
    public double getJumlahBunga()  { return jumlahBunga; }
    public double getJumlahDenda()  { return jumlahDenda; }
    public double getTotalBayar()   { return totalBayar; }
    public int getNoAngsuran()      { return noAngsuran; }
    public String getNoKredit()     { return noKredit; }
    public String getNamaLengkap()  { return namaLengkap; }
    public String getNamaDesa()     { return namaDesa; }
}