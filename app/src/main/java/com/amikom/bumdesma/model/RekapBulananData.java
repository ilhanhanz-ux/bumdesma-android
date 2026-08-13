package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RekapBulananData {
    @SerializedName("periode")          private String periode;
    @SerializedName("jumlah_transaksi") private int jumlahTransaksi;
    @SerializedName("total_pokok")      private double totalPokok;
    @SerializedName("total_bunga")      private double totalBunga;
    @SerializedName("total_denda")      private double totalDenda;
    @SerializedName("total_bayar")      private double totalBayar;
    @SerializedName("detail")           private List<RekapBulananItem> detail;

    public String getPeriode()             { return periode; }
    public int getJumlahTransaksi()        { return jumlahTransaksi; }
    public double getTotalPokok()          { return totalPokok; }
    public double getTotalBunga()          { return totalBunga; }
    public double getTotalDenda()          { return totalDenda; }
    public double getTotalBayar()          { return totalBayar; }
    public List<RekapBulananItem> getDetail() { return detail; }
}