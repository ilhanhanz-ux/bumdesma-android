package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class SetoranKolektifResult {
    @SerializedName("setoran_kolektif_id") private int setoranKolektifId;
    @SerializedName("jumlah_angsuran")     private int jumlahAngsuran;
    @SerializedName("total_nominal")       private double totalNominal;
    @SerializedName("bukti_setor")         private String buktiSetor;

    public int getSetoranKolektifId() { return setoranKolektifId; }
    public int getJumlahAngsuran()    { return jumlahAngsuran; }
    public double getTotalNominal()   { return totalNominal; }
    public String getBuktiSetor()     { return buktiSetor; }
}