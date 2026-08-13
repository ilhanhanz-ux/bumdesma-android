package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class PorsiInput {

    @SerializedName("anggota_id")
    private final int anggotaId;

    @SerializedName("jumlah_porsi")
    private final double jumlahPorsi;

    public PorsiInput(int anggotaId, double jumlahPorsi) {
        this.anggotaId = anggotaId;
        this.jumlahPorsi = jumlahPorsi;
    }
}