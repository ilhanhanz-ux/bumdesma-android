package com.amikom.bumdesma.utils;

import com.amikom.bumdesma.model.Angsuran;

import java.util.List;

/**
 * Utilitas hitung skor kepatuhan pembayaran anggota dari daftar angsuran
 * yang sudah diambil dari backend (endpoint angsuran.php?anggota_id=..).
 *
 * Skor = (jumlah angsuran jatuh tempo yang dibayar tepat waktu)
 *        ÷ (jumlah angsuran yang sudah jatuh tempo) × 100
 *
 * Angsuran yang belum jatuh tempo (status "belum_bayar" menurut definisi
 * backend angsuran.php) tidak ikut dihitung — belum ada dasar menilainya.
 */
public class SkorKepatuhanUtil {

    public static final String LANCAR = "lancar";
    public static final String PERLU_PERHATIAN = "perlu_perhatian";
    public static final String MACET = "macet";
    public static final String BELUM_ADA_RIWAYAT = "belum_ada_riwayat";

    public static class Hasil {
        public final Double skorPersen;   // null kalau belum ada riwayat
        public final String statusKredit;
        public final int totalJatuhTempo;
        public final int totalTepatWaktu;

        public Hasil(Double skorPersen, String statusKredit,
                     int totalJatuhTempo, int totalTepatWaktu) {
            this.skorPersen = skorPersen;
            this.statusKredit = statusKredit;
            this.totalJatuhTempo = totalJatuhTempo;
            this.totalTepatWaktu = totalTepatWaktu;
        }
    }

    public static Hasil hitung(List<Angsuran> daftar) {
        int totalJatuhTempo = 0;
        int totalTepatWaktu = 0;

        if (daftar != null) {
            for (Angsuran a : daftar) {
                // "belum_bayar" dari backend = belum jatuh tempo (lihat logika
                // statusTampil di angsuran.php), jadi dilewati dari perhitungan.
                if (a.isBelumBayar()) continue;

                totalJatuhTempo++;

                if (a.isSudahBayar() && dibayarTepatWaktu(a)) {
                    totalTepatWaktu++;
                }
            }
        }

        if (totalJatuhTempo == 0) {
            return new Hasil(null, BELUM_ADA_RIWAYAT, 0, 0);
        }

        double skor = Math.round((totalTepatWaktu * 1000.0 / totalJatuhTempo)) / 10.0;
        String status;
        if (skor >= 80) status = LANCAR;
        else if (skor >= 60) status = PERLU_PERHATIAN;
        else status = MACET;

        return new Hasil(skor, status, totalJatuhTempo, totalTepatWaktu);
    }

    /** Status per baris, dipakai adapter riwayat untuk kasih badge warna. */
    public static String statusBaris(Angsuran a) {
        if (a.isSudahBayar()) {
            return dibayarTepatWaktu(a) ? "tepat_waktu" : "terlambat_bayar";
        }
        if (a.isBelumBayar()) {
            return "belum_jatuh_tempo";
        }
        // status_bayar == "terlambat" dari backend → belum bayar & sudah lewat tempo
        return "menunggak";
    }

    private static boolean dibayarTepatWaktu(Angsuran a) {
        String tglBayar = a.getTanggalBayar();
        String jatuhTempo = a.getTanggalJatuhTempo();
        if (tglBayar == null || jatuhTempo == null) return false;
        // Format "yyyy-MM-dd" bisa dibandingkan langsung sebagai String
        return tglBayar.compareTo(jatuhTempo) <= 0;
    }
}