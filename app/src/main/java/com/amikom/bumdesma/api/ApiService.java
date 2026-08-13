package com.amikom.bumdesma.api;

import com.amikom.bumdesma.model.AnggotaAdmin;
import com.amikom.bumdesma.model.AnggotaVerifikasi;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.Angsuran;
import com.amikom.bumdesma.model.KelompokLimit;
import com.amikom.bumdesma.model.LaporanRingkasan;
import com.amikom.bumdesma.model.LoginResponse;
import com.amikom.bumdesma.model.Pengumuman;
import com.amikom.bumdesma.model.Proposal;
import com.amikom.bumdesma.model.RekapBulananData;
import com.amikom.bumdesma.model.RekeningSetoran;
import com.amikom.bumdesma.model.KreditRingkasan;
import com.amikom.bumdesma.model.RiwayatKelompokItem;
import com.amikom.bumdesma.model.SetoranKolektifRequest;
import com.amikom.bumdesma.model.SetoranKolektifResult;
import com.amikom.bumdesma.model.SetoranRequest;
import com.amikom.bumdesma.model.AnggotaProfil;
import com.amikom.bumdesma.model.TunggakanData;
import com.amikom.bumdesma.model.RiwayatAktifitasItem;
import com.amikom.bumdesma.model.AngsuranPorsi;
import com.amikom.bumdesma.model.DetailPorsiAngsuran;
import com.amikom.bumdesma.model.PorsiInput;
import com.amikom.bumdesma.model.RingkasanAngsuranKetua;
import com.amikom.bumdesma.model.SetPorsiRequest;
import com.amikom.bumdesma.model.SubmitBuktiPorsiRequest;
import com.amikom.bumdesma.model.VerifikasiPorsiRequest;
import com.amikom.bumdesma.model.PorsiSaya;
import com.amikom.bumdesma.model.SubmitBuktiPorsiRequest;
import com.amikom.bumdesma.model.PorsiVerifikasi;
import com.amikom.bumdesma.model.VerifikasiAksiRequest;
import com.amikom.bumdesma.model.DetailAlokasiPinjaman;
import com.amikom.bumdesma.model.SetAlokasiRequest;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Query;
import okhttp3.ResponseBody;
import retrofit2.http.PartMap;


public interface ApiService {

    // Login
    @POST("login.php")
    Call<LoginResponse> login(@Body Map<String, String> body);

    // Daftar akun baru
    @Multipart
    @POST("register.php")
    Call<ApiResponse<Void>> daftarAkun(
            @PartMap Map<String, RequestBody> fields,
            @Part MultipartBody.Part fotoKtp
    );

    // Ambil daftar kelompok
    @GET("kelompok.php")
    Call<ApiResponse<List<Map<String, Object>>>> getKelompokList(
            @Header("Authorization") String token);

    // Lupa password (verifikasi + reset)
    @POST("lupa_password.php")
    Call<ApiResponse<Map<String, Object>>> lupaPassword(
            @Body Map<String, String> body);

    // Ubah password (user sudah login, verifikasi password lama)
    @POST("ubah_password.php")
    Call<ApiResponse<Void>> ubahPassword(
            @Header("Authorization") String token,
            @Body Map<String, String> body);

    // ── Data Anggota (Admin) ──────────────────────────
    // List semua anggota, dengan filter opsional search & status ("aktif"/"nonaktif"/null)
    @GET("data_anggota.php")
    Call<ApiResponse<List<AnggotaAdmin>>> getDaftarAnggota(
            @Header("Authorization") String token,
            @Query("search") String search,
            @Query("status") String status);

    // Detail 1 anggota
    @GET("data_anggota.php")
    Call<ApiResponse<AnggotaAdmin>> getDetailAnggota(
            @Header("Authorization") String token,
            @Query("id") int id);
    @GET("data_anggota.php")
    Call<ApiResponse<List<RiwayatKelompokItem>>> getRiwayatTransaksiKelompok(
            @Header("Authorization") String token,
            @Query("id") int anggotaId,
            @Query("riwayat_kelompok") int flag
    );

    // ── Verifikasi Anggota Baru (Admin) ───────────────
    // List anggota, difilter status: "pending" (menunggu), "diterima", "ditolak",
    // "riwayat" (diterima+ditolak, dipakai tab Riwayat), atau "semua"
    @GET("verifikasi_anggota.php")
    Call<ApiResponse<List<AnggotaVerifikasi>>> getDaftarVerifikasiAnggota(
            @Header("Authorization") String token,
            @Query("status") String status);

    // Detail 1 anggota (dipakai VerifikasiAnggotaDetailActivity)
    @GET("verifikasi_anggota.php")
    Call<ApiResponse<AnggotaVerifikasi>> getDetailVerifikasiAnggota(
            @Header("Authorization") String token,
            @Query("id") int id);

    // Aksi verifikasi: body berisi {"anggota_id": "..", "aksi": "diterima"/"ditolak", "keterangan": ".."}
    @POST("verifikasi_anggota.php")
    Call<ApiResponse<Void>> verifikasiAnggota(
            @Header("Authorization") String token,
            @Body Map<String, String> body);

    // Proposal - ambil semua
    @GET("proposal.php")
    Call<ApiResponse<List<Proposal>>> getProposalList(
            @Header("Authorization") String token);

    // Proposal - ambil detail 1
    @GET("proposal.php")
    Call<ApiResponse<Proposal>> getProposalDetail(
            @Header("Authorization") String token,
            @Query("id") int id);

    // Proposal - ajukan baru (dengan upload file)
    @Multipart
    @POST("proposal.php")
    Call<ResponseBody> ajukanProposal(
            @Header("Authorization") String token,
            @Part("jumlah_pinjaman") RequestBody jumlahPinjaman,
            @Part("tenor") RequestBody tenor,
            @Part("keperluan") RequestBody keperluan,
            @Part MultipartBody.Part dokumenProposal);

    // Ringkasan kredit aktif anggota — dipakai kartu "Info Kredit Aktif" di dashboard
    @GET("kredit.php")
    Call<ApiResponse<KreditRingkasan>> getKreditAktif(
            @Header("Authorization") String token);

    // Proposal - verifikasi (admin)
    @PUT("proposal.php")
    Call<ApiResponse<Void>> verifikasiProposal(
            @Header("Authorization") String token,
            @Query("id") int id,
            @Body Map<String, String> body);

    // Angsuran - per kredit (anggota: kredit_id diabaikan backend, otomatis pakai punya sendiri)
    @GET("angsuran.php")
    Call<ApiResponse<List<Angsuran>>> getAngsuranByKredit(
            @Header("Authorization") String token,
            @Query("kredit_id") int kreditId);

    @GET("angsuran/riwayat.php")
    Call<List<Angsuran>> getRiwayatAngsuran(
            @Header("Authorization") String token,
            @Query("anggota_id") int anggotaId
    );

    // Angsuran - belum bayar (admin)
    @GET("angsuran.php")
    Call<ApiResponse<List<Angsuran>>> getTagihanBelumBayar(
            @Header("Authorization") String token,
            @Query("status") String status);

    // Angsuran - catat bayar (admin)
    @Multipart
    @POST("angsuran.php")
    Call<ApiResponse<Void>> catatBayar(
            @Header("Authorization") String token,
            @Query("id") int angsuranId,
            @Part("jumlah_bayar")  RequestBody jumlahBayar,
            @Part("tanggal_bayar") RequestBody tanggalBayar,
            @Part("keterangan")    RequestBody keterangan,
            @Part MultipartBody.Part buktiBayar);

    @GET("angsuran.php")
    Call<ApiResponse<List<Angsuran>>> getDaftarTagihan(
            @Header("Authorization") String token,
            @Query("status") String status,
            @Query("search") String search
    );

    @PUT("setoran_angsuran.php")
    Call<ApiResponse<Object>> setoranAngsuran(
            @Header("Authorization") String token,
            @Body SetoranRequest body
    );

    // Rekening tujuan setoran
    @GET("rekening.php")
    Call<ApiResponse<List<RekeningSetoran>>> getRekeningSetoran(
            @Header("Authorization") String token);

    // Laporan dashboard admin — ringkasan
    @GET("laporan.php")
    Call<LaporanRingkasan> getLaporanRingkasan(
            @Header("Authorization") String token,
            @Query("type") String type,
            @Query("bulan") String bulan);

    // Laporan - tunggakan (real-time, tidak difilter bulan oleh backend)
    @GET("laporan.php")
    Call<ApiResponse<TunggakanData>> getLaporanTunggakan(
            @Header("Authorization") String token,
            @Query("type") String type);

    // Laporan - rekap bulanan (riwayat transaksi pembayaran per bulan)
    @GET("laporan.php")
    Call<ApiResponse<RekapBulananData>> getLaporanBulanan(
            @Header("Authorization") String token,
            @Query("type") String type,
            @Query("bulan") String bulan);

    // Profil lengkap anggota (untuk layar Profil Saya)
    @GET("profil.php")
    Call<ApiResponse<AnggotaProfil>> getProfilSaya(
            @Header("Authorization") String token);

    // ── Pengumuman ─────────────────────────────────────
    @GET("pengumuman.php")
    Call<ApiResponse<List<Pengumuman>>> getPengumumanList(
            @Header("Authorization") String token,
            @Query("limit") Integer limit);

    @GET("pengumuman.php")
    Call<ApiResponse<Pengumuman>> getPengumumanDetail(
            @Header("Authorization") String token,
            @Query("id") int id);

    @POST("pengumuman.php")
    Call<ApiResponse<Map<String, Object>>> buatPengumuman(
            @Header("Authorization") String token,
            @Body Map<String, String> body);

    @PUT("pengumuman.php")
    Call<ApiResponse<Void>> updatePengumuman(
            @Header("Authorization") String token,
            @Query("id") int id,
            @Body Map<String, String> body);

    @DELETE("pengumuman.php")
    Call<ApiResponse<Void>> hapusPengumuman(
            @Header("Authorization") String token,
            @Query("id") int id);

    // Riwayat aktivitas gabungan (admin) — feed proposal, kredit, pembayaran, pengumuman
    @GET("riwayat_aktifitas.php")
    Call<ApiResponse<List<RiwayatAktifitasItem>>> getRiwayatAktifitas(
            @Header("Authorization") String token,
            @Query("limit") int limit);

    // Riwayat aktivitas anggota (dipakai tab Notifikasi) — proposal/kredit/pembayaran
    // milik anggota yang login + pengumuman buat semua orang, digabung 1 feed
    @GET("riwayat_aktifitas_anggota.php")
    Call<ApiResponse<List<RiwayatAktifitasItem>>> getRiwayatAktifitasAnggota(
            @Header("Authorization") String token,
            @Query("limit") int limit);

    // Update nama profil admin (fitur "Ubah Nama" di halaman Profil Admin)
    @PUT("profil_admin.php")
    Call<ApiResponse<Void>> updateNamaAdmin(
            @Header("Authorization") String token,
            @Body Map<String, String> body);
    // Riwayat seluruh angsuran milik 1 anggota, lintas kredit (Admin) —
// dipakai DetailAnggotaActivity untuk hitung skor kepatuhan & tampilkan riwayat.
    @GET("angsuran.php")
    Call<ApiResponse<List<Angsuran>>> getRiwayatAngsuranAnggota(
            @Header("Authorization") String token,
            @Query("anggota_id") int anggotaId
    );
    // Tagihan belum lunas milik anggota-anggota di 1 kelompok (untuk Setoran Kolektif)
    @GET("angsuran.php")
    Call<ApiResponse<List<Angsuran>>> getTagihanByKelompok(
            @Header("Authorization") String token,
            @Query("nama_kelompok") String namaKelompok,
            @Query("status") String status // kirim "belum_lunas"
    );
    // Catat 1 setoran kolektif mencakup banyak angsuran sekaligus (ketua → BUMDesma)
    @POST("setoran_kolektif.php")
    Call<ApiResponse<SetoranKolektifResult>> catatSetoranKolektif(
            @Header("Authorization") String token,
            @Body SetoranKolektifRequest body
    );
    @GET("kelompok_saya.php")
    Call<ApiResponse<KelompokLimit>> getKelompokSaya(@Header("Authorization") String token);
    // ── Porsi Angsuran (tanggung renteng per anggota) ──────────────────────

    // Ketua: daftar angsuran (jadwal bulanan) kredit kelompoknya + status pembagian porsi
    @GET("angsuran_porsi.php")
    Call<ApiResponse<List<RingkasanAngsuranKetua>>> getRingkasanAngsuranKetua(
            @Header("Authorization") String token);
    // Ketua: detail alokasi pokok pinjaman per anggota untuk 1 kredit kelompok
    @GET("alokasi_pinjaman.php")
    Call<ApiResponse<DetailAlokasiPinjaman>> getDetailAlokasiPinjaman(
            @Header("Authorization") String token,
            @Query("kredit_id") int kreditId);

    // Ketua: simpan/ubah alokasi pokok pinjaman per anggota
    @POST("alokasi_pinjaman.php")
    Call<ApiResponse<Object>> setAlokasiPinjaman(
            @Header("Authorization") String token,
            @Body SetAlokasiRequest body);

    // Ketua & Admin: detail 1 angsuran + breakdown porsi tiap anggota
    @GET("angsuran_porsi.php")
    Call<ApiResponse<DetailPorsiAngsuran>> getDetailPorsiAngsuran(
            @Header("Authorization") String token,
            @Query("angsuran_id") int angsuranId);

    // Anggota biasa: daftar tagihan porsi miliknya sendiri (status null = semua)
    @GET("angsuran_porsi.php")
    Call<ApiResponse<List<AngsuranPorsi>>> getTagihanPorsiSaya(
            @Header("Authorization") String token,
            @Query("status") String status);

    // Admin: daftar porsi (default menunggu_verifikasi, atau "riwayat"/"semua")
    @GET("angsuran_porsi.php")
    Call<ApiResponse<List<AngsuranPorsi>>> getDaftarPorsiAdmin(
            @Header("Authorization") String token,
            @Query("status") String status);

    // Ketua: tetapkan/ubah pembagian porsi anggota untuk 1 angsuran
    @POST("angsuran_porsi.php")
    Call<ApiResponse<Object>> setPorsiAngsuran(
            @Header("Authorization") String token,
            @Body SetPorsiRequest body);

    // Anggota: submit bukti transfer untuk porsi miliknya sendiri
    @PUT("angsuran_porsi.php")
    Call<ApiResponse<Object>> submitBuktiPorsi(
            @Header("Authorization") String token,
            @Body SubmitBuktiPorsiRequest body);

    // Admin: verifikasi (approve/reject) 1 porsi
    @PUT("angsuran_porsi.php")
    Call<ApiResponse<Object>> verifikasiPorsi(
            @Header("Authorization") String token,
            @Body VerifikasiPorsiRequest body);
    // Daftar semua porsi angsuran milik anggota yang login (role=anggota,
// backend otomatis filter berdasarkan token, tidak perlu parameter).
// PENTING: pakai ?tampilan=saya. Tanpa ini, kalau yang login KETUA,
// backend malah balikin ringkasan kelompok (format beda, field status_bayar
// kosong) -- bukan porsi pribadinya sendiri. Endpoint ini dipakai
// TagihanPorsiSayaActivity & RiwayatPembayaranActivity, jadi ketua pun
// harus tetap dapat porsi pribadinya di layar itu, bukan ringkasan kelompok
// (ringkasan kelompok itu urusan getRingkasanAngsuranKetua/KelolaPorsiActivity).
    @GET("angsuran_porsi.php?tampilan=saya")
    Call<ApiResponse<List<PorsiSaya>>> getPorsiSaya(@Header("Authorization") String token);
    // Daftar porsi untuk admin. status: "menunggu_verifikasi" (default), "riwayat"
// (gabungan sudah_bayar+ditolak), atau "semua".
    @GET("angsuran_porsi.php")
    Call<ApiResponse<List<PorsiVerifikasi>>> getPorsiVerifikasi(@Header("Authorization") String token,
                                                                @Query("status") String status);

    // Admin approve/reject 1 porsi. catatan_admin wajib diisi kalau aksi=reject.
    @PUT("angsuran_porsi.php")
    Call<ApiResponse<Object>> verifikasiPorsi(@Header("Authorization") String token,
                                              @Body VerifikasiAksiRequest request);
}