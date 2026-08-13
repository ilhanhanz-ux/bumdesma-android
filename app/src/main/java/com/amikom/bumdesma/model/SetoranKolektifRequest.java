package com.amikom.bumdesma.model;

import java.util.List;

public class SetoranKolektifRequest {
    private String nama_kelompok;
    private String nama_penyetor;
    private String tanggal_setor;
    private String keterangan;
    private String bukti_setor; // base64
    private List<Integer> angsuran_ids;

    public SetoranKolektifRequest(String namaKelompok, String namaPenyetor, String tanggalSetor,
                                  String keterangan, String buktiSetorBase64, List<Integer> angsuranIds) {
        this.nama_kelompok = namaKelompok;
        this.nama_penyetor = namaPenyetor;
        this.tanggal_setor = tanggalSetor;
        this.keterangan = keterangan;
        this.bukti_setor = buktiSetorBase64;
        this.angsuran_ids = angsuranIds;
    }
}