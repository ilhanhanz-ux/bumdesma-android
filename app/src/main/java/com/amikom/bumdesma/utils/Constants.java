package com.amikom.bumdesma.utils;

public class Constants {

    // URL API via ngrok (testing lokal)
    public static final String BASE_URL =
            "https://showplace-crank-blimp.ngrok-free.dev/bumdesma/api/";

    // URL file upload via ngrok
    public static final String FILE_BASE_URL =
            "https://showplace-crank-blimp.ngrok-free.dev/bumdesma/uploads/";

    // SharedPreferences
    public static final String PREF_NAME      = "BUMDesmaSession";
    public static final String KEY_TOKEN      = "token";
    public static final String KEY_ROLE       = "role";
    public static final String KEY_USER_ID    = "user_id";
    public static final String KEY_ANGGOTA_ID = "anggota_id";
    public static final String KEY_NAMA       = "nama";
    public static final String KEY_IS_LOGIN   = "is_login";

    // Role pengguna
    public static final String ROLE_ADMIN   = "admin";
    public static final String ROLE_ANGGOTA = "anggota";

    // Status Proposal
    public static final String MENUNGGU  = "menunggu";
    public static final String DISETUJUI = "disetujui";
    public static final String DITOLAK   = "ditolak";
    public static final String REVISI    = "revisi";

    // Status Angsuran
    public static final String BELUM_BAYAR = "belum_bayar";
    public static final String SUDAH_BAYAR = "sudah_bayar";
    public static final String TERLAMBAT   = "terlambat";

    // Status Verifikasi Anggota Baru (beda konsep dari status proposal di atas,
    // sengaja dikasih prefix VERIFIKASI_ biar gak ketuker meski ada kata "ditolak" juga)
    public static final String VERIFIKASI_PENDING  = "pending";
    public static final String VERIFIKASI_DITERIMA = "diterima";
    public static final String VERIFIKASI_DITOLAK  = "ditolak";

    // Intent Keys
    public static final String KEY_PROPOSAL_ID          = "proposal_id";
    public static final String KEY_KREDIT_ID            = "kredit_id";
    public static final String KEY_ANGSURAN_ID          = "angsuran_id";
    public static final String KEY_ANGGOTA_VERIFIKASI_ID = "anggota_verifikasi_id";
    public static final String KEY_IS_KETUA = "key_is_ketua";

    // Gabungkan path relatif file dari server (mis. kolom bukti_bayar / dok_ktp)
    // dengan FILE_BASE_URL. FILE_BASE_URL sudah diakhiri "/uploads/", sementara
    // beberapa kolom di DB (mis. angsuran_porsi.bukti_bayar) menyimpan path yang
    // SUDAH diawali "uploads/" (mis. "uploads/bukti_bayar/porsi_46_xxx.jpg"),
    // beda dengan kolom lain (mis. dok_proposal = "proposal/xxx.pdf") yang tidak.
    // Kalau digabung mentah-mentah jadi "...uploads/uploads/bukti_bayar/..."
    // (404, foto nggak muncul). Method ini menormalkan dulu biar aman dipakai
    // untuk path yang pakai prefix "uploads/" maupun yang tidak.
    public static String buildFileUrl(String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return null;
        }
        String path = relativePath.trim();
        if (path.startsWith("uploads/")) {
            path = path.substring("uploads/".length());
        }
        return FILE_BASE_URL + path;
    }
}