package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class AlokasiInput {
    @SerializedName("anggota_id")   private final int anggotaId;
    @SerializedName("jumlah_pokok") private final double jumlahPokok;

    public AlokasiInput(int anggotaId, double jumlahPokok) {
        this.anggotaId = anggotaId;
        this.jumlahPokok = jumlahPokok;
    }
}