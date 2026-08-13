package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class KreditRingkasan {
    @SerializedName("ada_kredit_aktif")    private boolean adaKreditAktif;
    @SerializedName("kredit_id")           private int kreditId;
    @SerializedName("no_kredit")           private String noKredit;
    @SerializedName("sisa_pokok")          private double sisaPokok;
    @SerializedName("angsuran_ke")         private Integer angsuranKe;
    @SerializedName("jangka_waktu_bulan")  private int jangkaWaktuBulan;
    @SerializedName("tanggal_jatuh_tempo") private String tanggalJatuhTempo;
    @SerializedName("tagihan_bulan_ini")   private Double tagihanBulanIni;
    @SerializedName("alokasi_sudah_diisi") private boolean alokasiSudahDiisi;

    // BARU: beda dengan alokasiSudahDiisi -- ini nentuin tombol MASIH
    // BOLEH tampil (walau cuma buat edit) atau HARUS disembunyikan total.
    @SerializedName("alokasi_terkunci")    private boolean alokasiTerkunci;

    public boolean isAdaKreditAktif()     { return adaKreditAktif; }
    public int     getKreditId()          { return kreditId; }
    public String  getNoKredit()          { return noKredit; }
    public double  getSisaPokok()         { return sisaPokok; }
    public Integer getAngsuranKe()        { return angsuranKe; }
    public int     getJangkaWaktuBulan()  { return jangkaWaktuBulan; }
    public String  getTanggalJatuhTempo() { return tanggalJatuhTempo; }
    public Double  getTagihanBulanIni()   { return tagihanBulanIni; }
    public boolean isAlokasiSudahDiisi()  { return alokasiSudahDiisi; }
    public boolean isAlokasiTerkunci()    { return alokasiTerkunci; }
}