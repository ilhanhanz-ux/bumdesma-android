package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class DetailPorsiAngsuran {

    @SerializedName("angsuran_id")
    private int angsuranId;

    @SerializedName("kredit_id")
    private int kreditId;

    @SerializedName("no_kredit")
    private String noKredit;

    @SerializedName("no_angsuran")
    private int noAngsuran;

    @SerializedName("total_bayar")
    private double totalBayar;

    @SerializedName("tanggal_jatuh_tempo")
    private String tanggalJatuhTempo;

    @SerializedName("status_induk")
    private String statusInduk;

    @SerializedName("total_sudah_dibagi")
    private double totalSudahDibagi;

    @SerializedName("sisa_belum_dibagi")
    private double sisaBelumDibagi;

    @SerializedName("porsi")
    private List<AngsuranPorsi> porsi;

    public int getAngsuranId() { return angsuranId; }
    public int getKreditId() { return kreditId; }
    public String getNoKredit() { return noKredit; }
    public int getNoAngsuran() { return noAngsuran; }
    public double getTotalBayar() { return totalBayar; }
    public String getTanggalJatuhTempo() { return tanggalJatuhTempo; }
    public String getStatusInduk() { return statusInduk; }
    public double getTotalSudahDibagi() { return totalSudahDibagi; }
    public double getSisaBelumDibagi() { return sisaBelumDibagi; }
    public List<AngsuranPorsi> getPorsi() { return porsi; }
}