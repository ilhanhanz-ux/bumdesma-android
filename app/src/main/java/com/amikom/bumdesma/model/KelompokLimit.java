package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class KelompokLimit {

    @SerializedName("id")
    private int id;

    @SerializedName("nama_kelompok")
    private String namaKelompok;

    @SerializedName("desa")
    private String desa;

    @SerializedName("limit_pinjaman")
    private double limitPinjaman;

    public int getId() {
        return id;
    }

    public String getNamaKelompok() {
        return namaKelompok;
    }

    public String getDesa() {
        return desa;
    }

    public double getLimitPinjaman() {
        return limitPinjaman;
    }
}