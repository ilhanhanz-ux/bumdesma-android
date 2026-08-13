package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class TransaksiKelompokItem {
    @SerializedName("id")               private int id;
    @SerializedName("noKredit")         private String noKredit;
    @SerializedName("sisaPokok")        private double sisaPokok;
    @SerializedName("jangkaWaktuBulan") private int jangkaWaktuBulan;
    @SerializedName("statusKredit")     private String statusKredit;

    public int    getId()               { return id; }
    public String getNoKredit()         { return noKredit; }
    public double getSisaPokok()        { return sisaPokok; }
    public int    getJangkaWaktuBulan() { return jangkaWaktuBulan; }
    public String getStatusKredit()     { return statusKredit; }
}