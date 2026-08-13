package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SetAlokasiRequest {
    @SerializedName("kredit_id") private final int kreditId;
    @SerializedName("alokasi")   private final List<AlokasiInput> alokasi;

    public SetAlokasiRequest(int kreditId, List<AlokasiInput> alokasi) {
        this.kreditId = kreditId;
        this.alokasi = alokasi;
    }
}