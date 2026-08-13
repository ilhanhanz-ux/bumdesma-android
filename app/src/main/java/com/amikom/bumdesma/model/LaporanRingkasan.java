package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class LaporanRingkasan {
    @SerializedName("success") private boolean success;
    @SerializedName("message") private String message;
    @SerializedName("data")    private Data data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Data getData()      { return data; }

    public static class Data {
        @SerializedName("total_anggota_aktif")  private int totalAnggota;
        @SerializedName("kredit_aktif")         private int kreditAktif;
        @SerializedName("dana_beredar")         private double danaBeredar;
        @SerializedName("proposal_menunggu")    private int proposalMenunggu;
        @SerializedName("tagihan_jatuh_tempo")  private int tagihanJatuhTempo;
        @SerializedName("periode_bulan")        private String periodeBulan;

        // ── Field tambahan (sudah dikirim backend, sebelumnya belum ditangkap) ──
        @SerializedName("penerimaan_bulan_ini") private double penerimaanBulanIni;
        @SerializedName("nominal_jatuh_tempo")  private double nominalJatuhTempo;
        @SerializedName("kredit_macet")         private int kreditMacet;

        public int    getTotalAnggota()       { return totalAnggota; }
        public int    getKreditAktif()        { return kreditAktif; }
        public double getDanaBeredar()        { return danaBeredar; }
        public int    getProposalMenunggu()   { return proposalMenunggu; }
        public int    getTagihanJatuhTempo()  { return tagihanJatuhTempo; }
        public String getPeriodeBulan()       { return periodeBulan; }
        public double getPenerimaanBulanIni() { return penerimaanBulanIni; }
        public double getNominalJatuhTempo()  { return nominalJatuhTempo; }
        public int    getKreditMacet()        { return kreditMacet; }
    }
}