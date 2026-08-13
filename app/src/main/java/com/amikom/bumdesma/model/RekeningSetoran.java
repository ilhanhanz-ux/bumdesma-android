package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class RekeningSetoran {
    @SerializedName("nama_bank")   private String namaBank;
    @SerializedName("no_rekening") private String noRekening;
    @SerializedName("atas_nama")   private String atasNama;
    @SerializedName("keterangan")  private String keterangan;

    public String getNamaBank()   { return namaBank; }
    public String getNoRekening() { return noRekening; }
    public String getAtasNama()   { return atasNama; }
    public String getKeterangan() { return keterangan; }
}