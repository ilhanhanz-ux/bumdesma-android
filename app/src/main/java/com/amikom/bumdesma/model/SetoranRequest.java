package com.amikom.bumdesma.model;

public class SetoranRequest {
    private int angsuran_id;
    private String tanggal_bayar;
    private String keterangan;
    private String bukti_bayar; // base64 string

    public SetoranRequest(int angsuranId, String tanggalBayar, String keterangan, String buktiBayarBase64) {
        this.angsuran_id = angsuranId;
        this.tanggal_bayar = tanggalBayar;
        this.keterangan = keterangan;
        this.bukti_bayar = buktiBayarBase64;
    }
}