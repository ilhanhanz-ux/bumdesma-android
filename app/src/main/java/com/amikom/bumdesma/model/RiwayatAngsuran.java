package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class RiwayatAngsuran {

    @SerializedName("id")
    private int id;

    @SerializedName("noAngsuran")
    private int noAngsuran;

    @SerializedName("tanggalJatuhTempo")
    private String tanggalJatuhTempo;

    @SerializedName("tanggalBayar")
    private String tanggalBayar;

    @SerializedName("jumlahPokok")
    private double jumlahPokok;

    @SerializedName("jumlahBunga")
    private double jumlahBunga;

    @SerializedName("jumlahDenda")
    private double jumlahDenda;

    @SerializedName("totalBayar")
    private double totalBayar;

    @SerializedName("statusBayar")
    private String statusBayar;

    @SerializedName("statusBaris")
    private String statusBaris;

    @SerializedName("keterangan")
    private String keterangan;

    public int getId() { return id; }
    public int getNoAngsuran() { return noAngsuran; }
    public String getTanggalJatuhTempo() { return tanggalJatuhTempo; }
    public String getTanggalBayar() { return tanggalBayar; }
    public double getJumlahPokok() { return jumlahPokok; }
    public double getJumlahBunga() { return jumlahBunga; }
    public double getJumlahDenda() { return jumlahDenda; }
    public double getTotalBayar() { return totalBayar; }
    public String getStatusBayar() { return statusBayar; }
    public String getStatusBaris() { return statusBaris; }
    public String getKeterangan() { return keterangan; }
}