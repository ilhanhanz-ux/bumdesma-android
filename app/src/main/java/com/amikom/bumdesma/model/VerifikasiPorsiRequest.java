package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class VerifikasiPorsiRequest {

    @SerializedName("porsi_id")
    private final int porsiId;

    @SerializedName("aksi")
    private final String aksi; // "approve" atau "reject"

    @SerializedName("catatan_admin")
    private final String catatanAdmin;

    public VerifikasiPorsiRequest(int porsiId, String aksi, String catatanAdmin) {
        this.porsiId = porsiId;
        this.aksi = aksi;
        this.catatanAdmin = catatanAdmin;
    }
}