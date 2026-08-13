package com.amikom.bumdesma.ui.admin;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import android.graphics.drawable.Drawable;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.Proposal;
import com.amikom.bumdesma.model.RiwayatAnggotaKelompok;
import com.amikom.bumdesma.utils.Constants;
import com.amikom.bumdesma.utils.SessionManager;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProposalDetailAdminActivity extends AppCompatActivity {

    private SessionManager session;
    private ProgressBar progressBar;
    private LinearLayout layoutContent;
    private int proposalId;
    private final NumberFormat fmt =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    // Views
    private TextView tvNoProposal, tvStatus, tvNama, tvKelompok,
            tvDesa, tvTelepon, tvJumlah, tvJangka, tvBunga,
            tvCicilan, tvTujuan, tvDeskripsi, tvTanggal, tvCatatanAdmin;
    private LinearLayout layoutTombol;
    private Button btnSetujui, btnTolak, btnRevisi;

    // Views dokumen
    private CardView cardDokumen;
    private Button btnLihatDokProposal, btnLihatDokKtp, btnLihatDokJaminan;
    private TextView tvDokumenKosong;

    // Views persetujuan jumlah (limit & dana tersedia)
    private TextView tvDanaTersedia, tvLimitAnggota;
    private EditText etJumlahDisetujui;

    // ── BARU: views riwayat transaksi kelompok ──
    private CardView cardRiwayatKelompok;
    private LinearLayout layoutRiwayatKelompok;
    private TextView tvRiwayatKosong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proposal_detail_admin);

        session    = new SessionManager(this);
        proposalId = getIntent().getIntExtra(Constants.KEY_PROPOSAL_ID, 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detail Proposal");
        }

        // Bind views
        progressBar    = findViewById(R.id.progress_bar);
        layoutContent  = findViewById(R.id.layout_content);
        tvNoProposal   = findViewById(R.id.tv_no_proposal);
        tvStatus       = findViewById(R.id.tv_status);
        tvNama         = findViewById(R.id.tv_nama_pengaju);
        tvKelompok     = findViewById(R.id.tv_kelompok);
        tvDesa         = findViewById(R.id.tv_desa);
        tvTelepon      = findViewById(R.id.tv_telepon);
        tvJumlah       = findViewById(R.id.tv_jumlah);
        tvJangka       = findViewById(R.id.tv_jangka);
        tvBunga        = findViewById(R.id.tv_bunga);
        tvCicilan      = findViewById(R.id.tv_cicilan_estimasi);
        tvTujuan       = findViewById(R.id.tv_tujuan);
        tvDeskripsi    = findViewById(R.id.tv_deskripsi);
        tvTanggal      = findViewById(R.id.tv_tanggal);
        tvCatatanAdmin = findViewById(R.id.tv_catatan_admin);
        layoutTombol   = findViewById(R.id.layout_tombol_verifikasi);
        btnSetujui     = findViewById(R.id.btn_setujui);
        btnTolak       = findViewById(R.id.btn_tolak);
        btnRevisi      = findViewById(R.id.btn_revisi);

        // Bind views dokumen
        cardDokumen          = findViewById(R.id.card_dokumen);
        btnLihatDokProposal  = findViewById(R.id.btn_lihat_dok_proposal);
        btnLihatDokKtp       = findViewById(R.id.btn_lihat_dok_ktp);
        btnLihatDokJaminan   = findViewById(R.id.btn_lihat_dok_jaminan);
        tvDokumenKosong      = findViewById(R.id.tv_dokumen_kosong);

        // Bind views persetujuan jumlah
        tvDanaTersedia    = findViewById(R.id.tv_dana_tersedia);
        tvLimitAnggota    = findViewById(R.id.tv_limit_anggota);
        etJumlahDisetujui = findViewById(R.id.et_jumlah_disetujui);
        pasangFormatRibuan(etJumlahDisetujui);

        // ── BARU: bind views riwayat kelompok ──
        cardRiwayatKelompok   = findViewById(R.id.card_riwayat_kelompok);
        layoutRiwayatKelompok = findViewById(R.id.layout_riwayat_kelompok);
        tvRiwayatKosong       = findViewById(R.id.tv_riwayat_kosong);

        btnSetujui.setOnClickListener(v ->
                showKonfirmasiDialog(Constants.DISETUJUI));
        btnTolak.setOnClickListener(v ->
                showKonfirmasiDialog(Constants.DITOLAK));
        btnRevisi.setOnClickListener(v ->
                showKonfirmasiDialog(Constants.REVISI));

        loadDetail();
    }

    /**
     * Pasang TextWatcher yang otomatis nambahin titik ribuan setiap kali user
     * ngetik angka, contoh: 20000000 -> 20.000.000. Juga otomatis merapikan
     * nilai default yang di-set programatis lewat setText() (misal di
     * fillPersetujuanJumlah), karena setText() ikut memicu afterTextChanged.
     */
    private void pasangFormatRibuan(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean sedangFormat = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (sedangFormat) return;
                sedangFormat = true;

                String angkaSaja = s.toString().replaceAll("[^\\d]", "");

                if (angkaSaja.isEmpty()) {
                    sedangFormat = false;
                    return;
                }

                try {
                    long nilai = Long.parseLong(angkaSaja);

                    DecimalFormatSymbols simbol = new DecimalFormatSymbols(new Locale("id", "ID"));
                    DecimalFormat formatter = new DecimalFormat("#,###", simbol);
                    String hasilFormat = formatter.format(nilai);

                    editText.setText(hasilFormat);
                    editText.setSelection(hasilFormat.length());
                } catch (NumberFormatException e) {
                    editText.setText(angkaSaja);
                    editText.setSelection(angkaSaja.length());
                }

                sedangFormat = false;
            }
        });
    }

    /** Buang semua titik/karakter non-digit, sisain angka polos buat dikirim ke backend */
    private String bersihkanAngka(String teksBerformat) {
        return teksBerformat.replaceAll("[^\\d]", "");
    }

    private void loadDetail() {
        progressBar.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);

        ApiClient.getService()
                .getProposalDetail(session.getBearerToken(), proposalId)
                .enqueue(new Callback<ApiResponse<Proposal>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Proposal>> call,
                                           Response<ApiResponse<Proposal>> resp) {
                        progressBar.setVisibility(View.GONE);
                        if (resp.isSuccessful() && resp.body() != null
                                && resp.body().isSuccess()) {
                            fillData(resp.body().getData());
                        } else {
                            Toast.makeText(ProposalDetailAdminActivity.this,
                                    "Gagal memuat detail", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Proposal>> c, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProposalDetailAdminActivity.this,
                                "Koneksi gagal", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fillData(Proposal p) {
        layoutContent.setVisibility(View.VISIBLE);

        tvNoProposal.setText(safe(p.getNoProposal()));
        tvNama.setText(safe(p.getNamaPengaju()));
        tvKelompok.setText(safe(p.getNamaKelompok()));
        tvDesa.setText(safe(p.getNamaDesa()));
        tvTelepon.setText(safe(p.getNoTelepon()));
        tvJumlah.setText(fmt.format(p.getJumlahPinjaman()));
        tvJangka.setText(p.getJangkaWaktu() + " Bulan");
        tvBunga.setText(p.getBungaPersen() + "% / bulan");
        tvTujuan.setText(safe(p.getTujuan()));
        tvDeskripsi.setText(p.getDeskripsi() != null
                && !p.getDeskripsi().isEmpty() ? p.getDeskripsi() : "-");
        tvTanggal.setText(safe(p.getTanggalPengajuan()));

        // Estimasi cicilan per bulan
        double pokok  = p.getJumlahPinjaman();
        double bunga  = pokok * (p.getBungaPersen() / 100);
        double cicilan = (pokok / p.getJangkaWaktu()) + bunga;
        tvCicilan.setText(fmt.format(cicilan) + " / bulan");

        // Badge status - pakai icon vector, bukan emoji lagi
        String status = p.getStatus() != null ? p.getStatus() : "";
        switch (status) {
            case Constants.DISETUJUI:
                tvStatus.setText("DISETUJUI");
                tvStatus.setTextColor(getColor(R.color.status_disetujui));
                tvStatus.setBackgroundResource(R.drawable.bg_badge_disetujui);
                setBadgeIcon(tvStatus, R.drawable.ic_verified_24, R.color.status_disetujui);
                break;
            case Constants.DITOLAK:
                tvStatus.setText("DITOLAK");
                tvStatus.setTextColor(getColor(R.color.status_ditolak));
                tvStatus.setBackgroundResource(R.drawable.bg_badge_ditolak);
                setBadgeIcon(tvStatus, R.drawable.ic_cancel_24, R.color.status_ditolak);
                break;
            case Constants.REVISI:
                tvStatus.setText("REVISI");
                tvStatus.setTextColor(getColor(R.color.status_revisi));
                tvStatus.setBackgroundResource(R.drawable.bg_badge_menunggu);
                setBadgeIcon(tvStatus, R.drawable.ic_refresh_24, R.color.status_revisi);
                break;
            default:
                tvStatus.setText("MENUNGGU VERIFIKASI");
                tvStatus.setTextColor(getColor(R.color.status_menunggu));
                tvStatus.setBackgroundResource(R.drawable.bg_badge_menunggu);
                setBadgeIcon(tvStatus, R.drawable.ic_schedule_24, R.color.status_menunggu);
        }

        // Catatan admin
        if (p.getCatatanAdmin() != null && !p.getCatatanAdmin().isEmpty()) {
            tvCatatanAdmin.setVisibility(View.VISIBLE);
            tvCatatanAdmin.setText("Catatan: " + p.getCatatanAdmin());
        } else {
            tvCatatanAdmin.setVisibility(View.GONE);
        }

        // Tampilkan tombol hanya kalau masih menunggu
        boolean masihMenunggu = Constants.MENUNGGU.equals(status)
                || status.isEmpty();
        layoutTombol.setVisibility(
                masihMenunggu ? View.VISIBLE : View.GONE);

        // Dana tersedia, limit anggota, & default jumlah disetujui
        fillPersetujuanJumlah(p, masihMenunggu);

        // ── BARU: riwayat transaksi seluruh anggota kelompok ──
        fillRiwayatKelompok(p);

        // Dokumen pendukung
        fillDokumen(p);
    }

    private void fillPersetujuanJumlah(Proposal p, boolean masihMenunggu) {
        tvDanaTersedia.setText(p.getDanaTersedia() != null
                ? fmt.format(p.getDanaTersedia()) : "-");

        // Limit yang ditampilkan sekarang pakai limit_berlaku (sudah termasuk
        // formula ketua/kelompok + bonus riwayat bagus), fallback ke
        // anggota_limit_pinjaman mentah kalau server lama belum kirim field ini.
        Double limitTampil = p.getLimitBerlaku() != null
                ? p.getLimitBerlaku() : p.getAnggotaLimitPinjaman();
        tvLimitAnggota.setText(limitTampil != null ? fmt.format(limitTampil) : "-");

        // Kartu ini cuma relevan selama proposal masih bisa diproses
        findViewById(R.id.card_persetujuan_jumlah).setVisibility(
                masihMenunggu ? View.VISIBLE : View.GONE);

        // Default: kalau proposal ini sudah pernah punya jumlah_disetujui (misal
        // sedang di-refresh), pakai itu. Kalau belum, default ke jumlah_pinjaman
        // yang diajukan (kasus paling umum: dana cukup, disetujui penuh).
        // setText() di sini otomatis kepasang titik ribuan lewat TextWatcher
        // yang udah dipasang di onCreate.
        double defaultJumlah = p.getJumlahDisetujui() != null
                ? p.getJumlahDisetujui() : p.getJumlahPinjaman();
        etJumlahDisetujui.setText(String.valueOf((long) defaultJumlah));
    }

    /**
     * BARU: isi card "Riwayat Transaksi Kelompok" dengan ringkasan Lunas/
     * Aktif/Macet per anggota. Baris diisi manual lewat inflate (bukan
     * RecyclerView) supaya konsisten sama pola layar ini yang sudah pakai
     * ScrollView + LinearLayout biasa.
     */
    private void fillRiwayatKelompok(Proposal p) {
        List<RiwayatAnggotaKelompok> daftar = p.getRiwayatKelompok();

        // riwayat_kelompok cuma dikirim server buat role admin. Kalau null,
        // sembunyikan card-nya (jaga-jaga kalau dipanggil dari konteks lain).
        if (daftar == null) {
            cardRiwayatKelompok.setVisibility(View.GONE);
            return;
        }

        cardRiwayatKelompok.setVisibility(View.VISIBLE);
        layoutRiwayatKelompok.removeAllViews();

        if (daftar.isEmpty()) {
            tvRiwayatKosong.setVisibility(View.VISIBLE);
            return;
        }
        tvRiwayatKosong.setVisibility(View.GONE);

        LayoutInflater inflater = LayoutInflater.from(this);
        for (RiwayatAnggotaKelompok item : daftar) {
            View baris = inflater.inflate(
                    R.layout.item_ringkasan_anggota_kelompok, layoutRiwayatKelompok, false);

            TextView tvNamaAnggota = baris.findViewById(R.id.tv_nama_anggota);
            TextView tvBadgeLunas  = baris.findViewById(R.id.tv_badge_lunas);
            TextView tvBadgeAktif  = baris.findViewById(R.id.tv_badge_aktif);
            TextView tvBadgeMacet  = baris.findViewById(R.id.tv_badge_macet);

            String namaTampil = item.getNamaLengkap();
            if (item.isPengaju()) {
                namaTampil += "  •  Pengaju";
            } else if (item.isKetua()) {
                namaTampil += "  •  Ketua";
            }
            tvNamaAnggota.setText(namaTampil);

            tvBadgeLunas.setText("Lunas: " + item.getJumlahLunas());
            tvBadgeAktif.setText("Aktif: " + item.getJumlahAktif());
            tvBadgeMacet.setText("Macet: " + item.getJumlahMacet());

            layoutRiwayatKelompok.addView(baris);
        }
    }

    /** Pasang icon vector kecil di sisi kiri badge status, warnanya ikut warna status */
    private void setBadgeIcon(TextView tv, int drawableRes, int colorRes) {
        Drawable icon = ContextCompat.getDrawable(this, drawableRes);
        if (icon != null) {
            icon.setTint(getColor(colorRes));
            int size = (int) (14 * getResources().getDisplayMetrics().density);
            icon.setBounds(0, 0, size, size);
            tv.setCompoundDrawables(icon, null, null, null);
            tv.setCompoundDrawablePadding((int) (6 * getResources().getDisplayMetrics().density));
        }
    }

    private void fillDokumen(Proposal p) {
        String urlProposal = p.getDokProposalUrl();
        String urlKtp      = p.getDokKtpUrl();
        String urlJaminan  = p.getDokJaminanUrl();

        boolean adaDokProposal = urlProposal != null;
        boolean adaDokKtp      = urlKtp != null;
        boolean adaDokJaminan  = urlJaminan != null;

        // Tombol dokumen proposal — selalu tampil, tapi non-aktif kalau tidak ada file
        btnLihatDokProposal.setVisibility(View.VISIBLE);
        btnLihatDokProposal.setEnabled(adaDokProposal);
        btnLihatDokProposal.setAlpha(adaDokProposal ? 1f : 0.5f);
        btnLihatDokProposal.setOnClickListener(v -> bukaDokumen(urlProposal));

        // Tombol KTP — tampil hanya kalau ada
        btnLihatDokKtp.setVisibility(adaDokKtp ? View.VISIBLE : View.GONE);
        if (adaDokKtp) {
            btnLihatDokKtp.setOnClickListener(v -> bukaDokumen(urlKtp));
        }

        // Tombol Jaminan — tampil hanya kalau ada
        btnLihatDokJaminan.setVisibility(adaDokJaminan ? View.VISIBLE : View.GONE);
        if (adaDokJaminan) {
            btnLihatDokJaminan.setOnClickListener(v -> bukaDokumen(urlJaminan));
        }

        // Pesan kalau tidak ada dokumen sama sekali
        boolean tidakAdaDokumenSamaSekali =
                !adaDokProposal && !adaDokKtp && !adaDokJaminan;
        tvDokumenKosong.setVisibility(
                tidakAdaDokumenSamaSekali ? View.VISIBLE : View.GONE);
    }

    private void bukaDokumen(String url) {
        if (url == null) {
            Toast.makeText(this, "Dokumen tidak tersedia", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this,
                    "Tidak ada aplikasi yang bisa membuka dokumen ini",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showKonfirmasiDialog(String statusBaru) {
        // Kalau mau MENYETUJUI, ambil & validasi jumlah disetujui dulu.
        // Nilai di EditText sudah berformat titik ribuan (mis. "20.000.000"),
        // jadi dibersihin dulu jadi angka polos sebelum dikirim ke backend.
        String jumlahDisetujuiStr = null;
        if (Constants.DISETUJUI.equals(statusBaru)) {
            String formatted = etJumlahDisetujui.getText().toString().trim();
            jumlahDisetujuiStr = bersihkanAngka(formatted);
            if (jumlahDisetujuiStr.isEmpty()) {
                Toast.makeText(this, "Isi jumlah yang disetujui dulu",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }
        final String jumlahDisetujuiFinal = jumlahDisetujuiStr;

        String judul;
        int iconDialog;
        switch (statusBaru) {
            case Constants.DISETUJUI:
                judul = "Setujui Proposal";
                iconDialog = R.drawable.ic_verified_24;
                break;
            case Constants.DITOLAK:
                judul = "Tolak Proposal";
                iconDialog = R.drawable.ic_cancel_24;
                break;
            default:
                judul = "Minta Revisi";
                iconDialog = R.drawable.ic_refresh_24;
        }

        // Dialog dengan input catatan
        View dialogView = getLayoutInflater()
                .inflate(R.layout.dialog_verifikasi, null);
        EditText etCatatan = dialogView.findViewById(
                R.id.et_catatan_verifikasi);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(judul)
                .setIcon(iconDialog)
                .setMessage("Tambahkan catatan untuk anggota (opsional):")
                .setView(dialogView)
                .setPositiveButton("Konfirmasi", (d, w) -> {
                    String catatan = etCatatan.getText().toString().trim();
                    kirimVerifikasi(statusBaru, catatan, jumlahDisetujuiFinal);
                })
                .setNegativeButton("Batal", null)
                .create();

        // Tombol AlertDialog cuma kebentuk setelah show(), jadi warnanya
        // di-set di sini (bukan sebelum show()) supaya gak null.
        // Fix dark mode: warna default tema ternyata gelap-di-atas-gelap juga.
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getColor(R.color.admin_accent_text));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(getColor(R.color.admin_accent_text));
        });

        dialog.show();
    }

    private void kirimVerifikasi(String status, String catatan, String jumlahDisetujui) {
        progressBar.setVisibility(View.VISIBLE);
        layoutTombol.setVisibility(View.GONE);

        Map<String, String> body = new HashMap<>();
        body.put("status_pengajuan", status);
        body.put("catatan_admin",    catatan);
        // Cuma dikirim kalau ini aksi menyetujui
        if (jumlahDisetujui != null) {
            body.put("jumlah_disetujui", jumlahDisetujui);
        }

        ApiClient.getService()
                .verifikasiProposal(session.getBearerToken(), proposalId, body)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> resp) {
                        progressBar.setVisibility(View.GONE);
                        if (resp.isSuccessful() && resp.body() != null
                                && resp.body().isSuccess()) {

                            String pesan;
                            switch (status) {
                                case Constants.DISETUJUI:
                                    pesan = "✅ Proposal berhasil disetujui!";
                                    break;
                                case Constants.DITOLAK:
                                    pesan = "❌ Proposal telah ditolak";
                                    break;
                                default:
                                    pesan = "🔄 Proposal diminta untuk direvisi";
                            }
                            Toast.makeText(ProposalDetailAdminActivity.this,
                                    pesan, Toast.LENGTH_LONG).show();
                            loadDetail(); // Refresh tampilan
                        } else {
                            String msg = resp.body() != null
                                    ? resp.body().getMessage()
                                    : "Gagal memproses";
                            Toast.makeText(ProposalDetailAdminActivity.this,
                                    msg, Toast.LENGTH_SHORT).show();
                            layoutTombol.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> c, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        layoutTombol.setVisibility(View.VISIBLE);
                        Toast.makeText(ProposalDetailAdminActivity.this,
                                "Koneksi gagal", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String safe(String s) {
        return s != null ? s : "-";
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}