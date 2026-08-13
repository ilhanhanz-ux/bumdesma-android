package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SetPorsiRequest {

    @SerializedName("angsuran_id")
    private final int angsuranId;

    @SerializedName("porsi")
    private final List<PorsiInput> porsi;

    public SetPorsiRequest(int angsuranId, List<PorsiInput> porsi) {
        this.angsuranId = angsuranId;
        this.porsi = porsi;
    }
}