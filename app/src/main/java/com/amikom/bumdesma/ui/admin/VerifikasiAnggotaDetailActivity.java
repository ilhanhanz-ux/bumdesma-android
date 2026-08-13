package com.amikom.bumdesma.ui.admin;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.AnggotaVerifikasi;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.utils.Constants;
import com.amikom.bumdesma.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifikasiAnggotaDetailActivity extends AppCompatActivity {

    private SessionManager session;
    private ProgressBar progressBar;
    private LinearLayout layoutContent;
    private int anggotaId;

    private TextView tvStatus, tvNama, tvUsername, tvTelepon, tvKelompok,
            tvDesa, tvTempatTanggalLahir, tvAlamat;
    private LinearLayout layoutTombol;
    private Button btnVerifikasi, btnTolak;

    private Button btnLihatFotoKtp;
    private TextView tvFotoKtpKosong;

    // ── BARU: views Jadikan Ketua Kelompok ──
    private CardView cardJadikanKetua;
    private TextView tvInfoKetua;
    private CheckBox cbJadikanKetua;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifikasi_anggota_detail);

        session   = new SessionManager(this);
        anggotaId = getIntent().getIntExtra(Constants.KEY_ANGGOTA_VERIFIKASI_ID, 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detail Anggota Baru");
        }

        progressBar          = findViewById(R.id.progress_bar);
        layoutContent        = findViewById(R.id.layout_content);
        tvStatus             = findViewById(R.id.tv_status);
        tvNama               = findViewById(R.id.tv_nama);
        tvUsername           = findViewById(R.id.tv_username);
        tvTelepon            = findViewById(R.id.tv_telepon);
        tvKelompok           = findViewById(R.id.tv_kelompok);
        tvDesa               = findViewById(R.id.tv_desa);
        tvTempatTanggalLahir = findViewById(R.id.tv_tempat_tanggal_lahir);
        tvAlamat             = findViewById(R.id.tv_alamat);
        layoutTombol         = findViewById(R.id.layout_tombol_verifikasi);
        btnVerifikasi        = findViewById(R.id.btn_verifikasi);
        btnTolak             = findViewById(R.id.btn_tolak);

        btnLihatFotoKtp = findViewById(R.id.btn_lihat_foto_ktp);
        tvFotoKtpKosong = findViewById(R.id.tv_foto_ktp_kosong);

        // ── BARU: bind views Jadikan Ketua Kelompok ──
        cardJadikanKetua = findViewById(R.id.card_jadikan_ketua);
        tvInfoKetua      = findViewById(R.id.tv_info_ketua);
        cbJadikanKetua   = findViewById(R.id.cb_jadikan_ketua);

        btnVerifikasi.setOnClickListener(v ->
                showKonfirmasiDialog(Constants.VERIFIKASI_DITERIMA));
        btnTolak.setOnClickListener(v ->
                showKonfirmasiDialog(Constants.VERIFIKASI_DITOLAK));

        loadDetail();
    }

    private void loadDetail() {
        progressBar.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);

        ApiClient.getService()
                .getDetailVerifikasiAnggota(session.getBearerToken(), anggotaId)
                .enqueue(new Callback<ApiResponse<AnggotaVerifikasi>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AnggotaVerifikasi>> call,
                                           Response<ApiResponse<AnggotaVerifikasi>> resp) {
                        progressBar.setVisibility(View.GONE);
                        if (resp.isSuccessful() && resp.body() != null
                                && resp.body().isSuccess()) {
                            fillData(resp.body().getData());
                        } else {
                            Toast.makeText(VerifikasiAnggotaDetailActivity.this,
                                    "Gagal memuat detail", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AnggotaVerifikasi>> c, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(VerifikasiAnggotaDetailActivity.this,
                                "Koneksi gagal", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fillData(AnggotaVerifikasi a) {
        layoutContent.setVisibility(View.VISIBLE);

        tvNama.setText(safe(a.getNama()));
        tvUsername.setText(safe(a.getUsername()));
        tvTelepon.setText(safe(a.getNoTelepon()));
        tvKelompok.setText(safe(a.getNamaKelompok()));
        tvDesa.setText(safe(a.getNamaDesa()));
        tvTempatTanggalLahir.setText(safe(a.getTempatLahir()) + ", " + safe(a.getTanggalLahir()));
        tvAlamat.setText(safe(a.getAlamat()));

        String status = a.getStatusVerifikasi() != null ? a.getStatusVerifikasi() : "";
        switch (status) {
            case Constants.VERIFIKASI_DITERIMA:
                tvStatus.setText("✅ DITERIMA");
                tvStatus.setTextColor(getColor(R.color.status_disetujui));
                tvStatus.setBackgroundResource(R.drawable.bg_badge_disetujui);
                break;
            case Constants.VERIFIKASI_DITOLAK:
                tvStatus.setText("❌ DITOLAK");
                tvStatus.setTextColor(getColor(R.color.status_ditolak));
                tvStatus.setBackgroundResource(R.drawable.bg_badge_ditolak);
                break;
            default:
                tvStatus.setText("⏳ MENUNGGU VERIFIKASI");
                tvStatus.setTextColor(getColor(R.color.status_menunggu));
                tvStatus.setBackgroundResource(R.drawable.bg_badge_menunggu);
        }

        // Tombol cuma tampil kalau masih pending
        boolean masihPending = Constants.VERIFIKASI_PENDING.equals(status) || status.isEmpty();
        layoutTombol.setVisibility(masihPending ? View.VISIBLE : View.GONE);

        fillDokumenKtp(a);

        // ── BARU: card Jadikan Ketua Kelompok, cuma relevan pas masih pending ──
        fillJadikanKetua(a, masihPending);
    }

    private void fillDokumenKtp(AnggotaVerifikasi a) {
        String urlFotoKtp = a.getFotoKtpUrl();
        boolean adaFotoKtp = urlFotoKtp != null;

        btnLihatFotoKtp.setVisibility(adaFotoKtp ? View.VISIBLE : View.GONE);
        tvFotoKtpKosong.setVisibility(adaFotoKtp ? View.GONE : View.VISIBLE);

        if (adaFotoKtp) {
            btnLihatFotoKtp.setOnClickListener(v -> bukaDokumen(urlFotoKtp));
        }
    }

    // ── BARU: tampilkan info kalau kelompok sudah ada ketua, dan kunci checkbox kalau begitu ──
    private void fillJadikanKetua(AnggotaVerifikasi a, boolean masihPending) {
        cardJadikanKetua.setVisibility(masihPending ? View.VISIBLE : View.GONE);
        if (!masihPending) return;

        boolean sudahAdaKetua = a.isKelompokSudahAdaKetua();
        cbJadikanKetua.setChecked(false);

        if (sudahAdaKetua) {
            tvInfoKetua.setVisibility(View.VISIBLE);
            tvInfoKetua.setText("Kelompok ini sudah punya ketua: " + safe(a.getNamaKetuaSekarang()));
            cbJadikanKetua.setEnabled(false);
        } else {
            tvInfoKetua.setVisibility(View.GONE);
            cbJadikanKetua.setEnabled(true);
        }
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

    private void showKonfirmasiDialog(String aksi) {
        String judul = aksi.equals(Constants.VERIFIKASI_DITERIMA)
                ? "✅ Verifikasi Anggota"
                : "❌ Tolak Anggota";

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_verifikasi, null);
        EditText etCatatan = dialogView.findViewById(R.id.et_catatan_verifikasi);

        new AlertDialog.Builder(this)
                .setTitle(judul)
                .setMessage(aksi.equals(Constants.VERIFIKASI_DITERIMA)
                        ? "Pastikan data ini benar warga Kecamatan Randudongkal."
                        : "Tambahkan alasan penolakan (opsional):")
                .setView(dialogView)
                .setPositiveButton("Konfirmasi", (d, w) -> {
                    String catatan = etCatatan.getText().toString().trim();
                    kirimVerifikasi(aksi, catatan);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void kirimVerifikasi(String aksi, String keterangan) {
        progressBar.setVisibility(View.VISIBLE);
        layoutTombol.setVisibility(View.GONE);

        Map<String, String> body = new HashMap<>();
        body.put("anggota_id", String.valueOf(anggotaId));
        body.put("aksi", aksi);
        body.put("keterangan", keterangan);

        // ── BARU: kirim status jadikan_ketua, hanya relevan kalau aksinya diterima ──
        if (Constants.VERIFIKASI_DITERIMA.equals(aksi)) {
            body.put("jadikan_ketua", cbJadikanKetua.isChecked() ? "1" : "0");
        }

        ApiClient.getService()
                .verifikasiAnggota(session.getBearerToken(), body)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> resp) {
                        progressBar.setVisibility(View.GONE);
                        if (resp.isSuccessful() && resp.body() != null
                                && resp.body().isSuccess()) {
                            String pesan = aksi.equals(Constants.VERIFIKASI_DITERIMA)
                                    ? "✅ Anggota berhasil diverifikasi"
                                    : "❌ Anggota ditolak";
                            Toast.makeText(VerifikasiAnggotaDetailActivity.this,
                                    pesan, Toast.LENGTH_LONG).show();
                            loadDetail();
                        } else {
                            String msg = resp.body() != null
                                    ? resp.body().getMessage()
                                    : "Gagal memproses";
                            Toast.makeText(VerifikasiAnggotaDetailActivity.this,
                                    msg, Toast.LENGTH_SHORT).show();
                            layoutTombol.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> c, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        layoutTombol.setVisibility(View.VISIBLE);
                        Toast.makeText(VerifikasiAnggotaDetailActivity.this,
                                "Koneksi gagal", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String safe(String s) {
        return s != null && !s.isEmpty() ? s : "-";
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}