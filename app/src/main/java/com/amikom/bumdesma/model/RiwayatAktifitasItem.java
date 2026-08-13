package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class RiwayatAktifitasItem {
    @SerializedName("tipe")       private String tipe;
    @SerializedName("waktu")      private String waktu;
    @SerializedName("nama")       private String nama;
    @SerializedName("nominal")    private Double nominal;
    @SerializedName("keterangan") private String keterangan;

    public String getTipe()       { return tipe; }
    public String getWaktu()      { return waktu; }
    public String getNama()       { return nama; }
    public Double getNominal()    { return nominal; }
    public String getKeterangan() { return keterangan; }
}