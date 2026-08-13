package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class SubmitBuktiPorsiRequest {

    @SerializedName("porsi_id")
    private final int porsiId;

    @SerializedName("bukti_bayar")
    private final String buktiBayarBase64;

    public SubmitBuktiPorsiRequest(int porsiId, String buktiBayarBase64) {
        this.porsiId = porsiId;
        this.buktiBayarBase64 = buktiBayarBase64;
    }
}