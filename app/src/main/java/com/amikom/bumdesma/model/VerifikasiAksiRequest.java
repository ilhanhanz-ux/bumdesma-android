package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class VerifikasiAksiRequest {

    @SerializedName("porsi_id")
    private final int porsiId;

    @SerializedName("aksi")
    private final String aksi; // "approve" | "reject"

    @SerializedName("catatan_admin")
    private final String catatanAdmin; // wajib diisi kalau aksi = reject

    public VerifikasiAksiRequest(int porsiId, String aksi, String catatanAdmin) {
        this.porsiId = porsiId;
        this.aksi = aksi;
        this.catatanAdmin = catatanAdmin;
    }
}