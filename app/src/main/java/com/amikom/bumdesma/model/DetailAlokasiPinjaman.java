package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class DetailAlokasiPinjaman {
    @SerializedName("kredit_id")      private int kreditId;
    @SerializedName("no_kredit")      private String noKredit;
    @SerializedName("nama_kelompok")  private String namaKelompok;
    @SerializedName("pokok_pinjaman") private double pokokPinjaman;
    @SerializedName("sudah_final")    private boolean sudahFinal;
    @SerializedName("anggota")        private List<AlokasiAnggota> anggota;

    public int getKreditId() { return kreditId; }
    public String getNoKredit() { return noKredit; }
    public String getNamaKelompok() { return namaKelompok; }
    public double getPokokPinjaman() { return pokokPinjaman; }
    public boolean isSudahFinal() { return sudahFinal; }
    public List<AlokasiAnggota> getAnggota() { return anggota; }
}