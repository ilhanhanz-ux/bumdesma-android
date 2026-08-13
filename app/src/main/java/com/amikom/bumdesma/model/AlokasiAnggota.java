package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class AlokasiAnggota {
    @SerializedName("anggota_id")
    private int anggotaId;

    @SerializedName("nama_lengkap")
    private String namaLengkap;

    @SerializedName("jumlah_pokok")
    private double jumlahPokok;

    public int getAnggotaId() { return anggotaId; }
    public String getNamaLengkap() { return namaLengkap; }
    public double getJumlahPokok() { return jumlahPokok; }
}