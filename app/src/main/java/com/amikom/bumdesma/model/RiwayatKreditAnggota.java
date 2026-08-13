package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class RiwayatKreditAnggota {

    @SerializedName("skorKepatuhan")
    private Double skorKepatuhan; // null kalau belum ada riwayat

    @SerializedName("statusKredit")
    private String statusKredit; // lancar | perlu_perhatian | macet | belum_ada_riwayat

    @SerializedName("totalJatuhTempo")
    private int totalJatuhTempo;

    @SerializedName("totalTepatWaktu")
    private int totalTepatWaktu;

    @SerializedName("riwayat")
    private List<RiwayatAngsuran> riwayat;

    public Double getSkorKepatuhan() { return skorKepatuhan; }
    public String getStatusKredit() { return statusKredit; }
    public int getTotalJatuhTempo() { return totalJatuhTempo; }
    public int getTotalTepatWaktu() { return totalTepatWaktu; }
    public List<RiwayatAngsuran> getRiwayat() { return riwayat; }
}