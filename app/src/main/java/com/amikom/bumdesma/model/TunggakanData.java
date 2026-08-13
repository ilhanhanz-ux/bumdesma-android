package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TunggakanData {
    @SerializedName("list")          private List<TunggakanItem> list;
    @SerializedName("total_nominal") private double totalNominal;

    public List<TunggakanItem> getList() { return list; }
    public double getTotalNominal()      { return totalNominal; }
}