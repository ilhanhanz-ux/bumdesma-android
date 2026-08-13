package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class Pengumuman {

    private int id;
    private String judul;
    private String isi;
    private String tanggal;

    @SerializedName("admin_id")
    private int adminId;

    @SerializedName("nama_admin")
    private String namaAdmin;

    @SerializedName("created_at")
    private String createdAt;

    public int getId() { return id; }
    public String getJudul() { return judul; }
    public String getIsi() { return isi; }
    public String getTanggal() { return tanggal; }
    public int getAdminId() { return adminId; }
    public String getNamaAdmin() { return namaAdmin; }
    public String getCreatedAt() { return createdAt; }
}