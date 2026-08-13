package com.amikom.bumdesma.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.Angsuran;
import com.amikom.bumdesma.model.AnggotaAdmin;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.RiwayatKelompokItem;
import com.amikom.bumdesma.model.TransaksiKelompokItem;
import com.amikom.bumdesma.utils.SessionManager;
import com.amikom.bumdesma.utils.SkorKepatuhanUtil;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DetailAnggotaActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView textInisial, tvNama, tvStatusAktif, tvNik, tvTtl,
            tvJenisKelamin, tvNoTelepon, tvAlamat, tvKelompok, tvDesa;

    // ── BARU: badge ketua kelompok ──
    private TextView tvBadgeKetua;

    private TextView tvSkorPersen, tvStatusKredit, tvRingkasanSkor, tvRiwayatKosong;
    private ProgressBar progressSkor;
    private RecyclerView rvRiwayatAngsuran;

    private TextView tvNamaKelompokRiwayat, tvRiwayatKelompokKosong;
    private ProgressBar progressRiwayatKelompok;
    private LinearLayout containerRiwayatKelompok;

    private int anggotaId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_anggota);

        anggotaId = getIntent().getIntExtra("anggota_id", 0);
        if (anggotaId <= 0) {
            Toast.makeText(this, "Data anggota tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Detail Anggota");
        }

        progressBar    = findViewById(R.id.progress_detail);
        textInisial    = findViewById(R.id.textInisial);
        tvNama         = findViewById(R.id.tv_nama);
        tvStatusAktif  = findViewById(R.id.tv_status_aktif);
        tvNik          = findViewById(R.id.tv_nik);
        tvTtl          = findViewById(R.id.tv_ttl);
        tvJenisKelamin = findViewById(R.id.tv_jenis_kelamin);
        tvNoTelepon    = findViewById(R.id.tv_no_telepon);
        tvAlamat       = findViewById(R.id.tv_alamat);
        tvKelompok     = findViewById(R.id.tv_kelompok);
        tvDesa         = findViewById(R.id.tv_desa);

        // ── BARU ──
        tvBadgeKetua = findViewById(R.id.tv_badge_ketua);

        tvSkorPersen      = findViewById(R.id.tv_skor_persen);
        tvStatusKredit    = findViewById(R.id.tv_status_kredit);
        tvRingkasanSkor   = findViewById(R.id.tv_ringkasan_skor);
        tvRiwayatKosong   = findViewById(R.id.tv_riwayat_kosong);
        progressSkor      = findViewById(R.id.progress_skor);
        rvRiwayatAngsuran = findViewById(R.id.rv_riwayat_angsuran);
        rvRiwayatAngsuran.setLayoutManager(new LinearLayoutManager(this));
        rvRiwayatAngsuran.setNestedScrollingEnabled(false);

        tvNamaKelompokRiwayat    = findViewById(R.id.tv_nama_kelompok_riwayat);
        tvRiwayatKelompokKosong  = findViewById(R.id.tv_riwayat_kelompok_kosong);
        progressRiwayatKelompok  = findViewById(R.id.progress_riwayat_kelompok);
        containerRiwayatKelompok = findViewById(R.id.container_riwayat_kelompok);

        loadDetail();
        loadRiwayatSkor();
        loadRiwayatKelompok();
    }

    private void loadDetail() {
        progressBar.setVisibility(View.VISIBLE);
        String token = new SessionManager(this).getBearerToken();

        ApiClient.getService().getDetailAnggota(token, anggotaId)
                .enqueue(new Callback<ApiResponse<AnggotaAdmin>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AnggotaAdmin>> call,
                                           Response<ApiResponse<AnggotaAdmin>> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            tampilkanData(response.body().getData());
                        } else {
                            Toast.makeText(DetailAnggotaActivity.this,
                                    "Gagal memuat detail anggota", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AnggotaAdmin>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(DetailAnggotaActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadRiwayatSkor() {
        String token = new SessionManager(this).getBearerToken();

        ApiClient.getService().getRiwayatAngsuranAnggota(token, anggotaId)
                .enqueue(new Callback<ApiResponse<List<Angsuran>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Angsuran>>> call,
                                           Response<ApiResponse<List<Angsuran>>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            tampilkanRiwayatSkor(response.body().getData());
                        } else {
                            tvRingkasanSkor.setText("Gagal memuat riwayat angsuran");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Angsuran>>> call, Throwable t) {
                        tvRingkasanSkor.setText("Koneksi gagal saat memuat riwayat");
                    }
                });
    }

    private void loadRiwayatKelompok() {
        progressRiwayatKelompok.setVisibility(View.VISIBLE);
        String token = new SessionManager(this).getBearerToken();

        ApiClient.getService().getRiwayatTransaksiKelompok(token, anggotaId, 1)
                .enqueue(new Callback<ApiResponse<List<RiwayatKelompokItem>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<RiwayatKelompokItem>>> call,
                                           Response<ApiResponse<List<RiwayatKelompokItem>>> response) {
                        progressRiwayatKelompok.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            tampilkanRiwayatKelompok(response.body().getData());
                        } else {
                            tvRiwayatKelompokKosong.setText("Gagal memuat riwayat kelompok");
                            tvRiwayatKelompokKosong.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<RiwayatKelompokItem>>> call, Throwable t) {
                        progressRiwayatKelompok.setVisibility(View.GONE);
                        tvRiwayatKelompokKosong.setText("Koneksi gagal saat memuat riwayat kelompok");
                        tvRiwayatKelompokKosong.setVisibility(View.VISIBLE);
                    }
                });
    }

    private void tampilkanData(AnggotaAdmin a) {
        String nama = isi(a.getNamaLengkap(), "-");
        tvNama.setText(nama);
        textInisial.setText(nama.isEmpty() || nama.equals("-") ? "?" : nama.substring(0, 1).toUpperCase(Locale.getDefault()));

        if (a.isStatusAktif()) {
            tvStatusAktif.setText("Aktif");
            tvStatusAktif.setBackgroundResource(R.drawable.bg_chip_pill_green);
            tvStatusAktif.setTextColor(0xFF2E7D32);
        } else {
            tvStatusAktif.setText("Nonaktif");
            tvStatusAktif.setBackgroundResource(R.drawable.bg_chip_pill_red);
            tvStatusAktif.setTextColor(0xFFC62828);
        }

        // ── BARU: badge ketua kelompok, cuma tampil kalau is_ketua = 1 ──
        tvBadgeKetua.setVisibility(a.isKetua() ? View.VISIBLE : View.GONE);
        if (a.isKetua()) {
            tvBadgeKetua.setTextColor(0xFF6A1B9A);
        }

        tvNik.setText(isi(a.getNik(), "-"));
        tvTtl.setText(gabungTempatTanggal(a.getTempatLahir(), a.getTanggalLahir()));
        tvJenisKelamin.setText(isi(a.getJenisKelamin(), "-"));
        tvNoTelepon.setText(isi(a.getNoTelepon(), "-"));
        tvAlamat.setText(isi(a.getAlamat(), "-"));
        tvKelompok.setText(isi(a.getNamaKelompok(), "-"));
        tvDesa.setText(isi(a.getNamaDesa(), "-"));

        tvNamaKelompokRiwayat.setText("Kelompok: " + isi(a.getNamaKelompok(), "-"));
    }

    private void tampilkanRiwayatSkor(List<Angsuran> daftar) {
        SkorKepatuhanUtil.Hasil hasil = SkorKepatuhanUtil.hitung(daftar);

        if (hasil.skorPersen == null) {
            tvSkorPersen.setText("-");
            tvStatusKredit.setText("Belum Ada Riwayat");
            tvStatusKredit.setBackgroundResource(R.drawable.bg_info_box);
            tvStatusKredit.setTextColor(0xFF757575);
            progressSkor.setProgress(0);
            tvRingkasanSkor.setText("Anggota ini belum memiliki angsuran yang jatuh tempo.");
        } else {
            tvSkorPersen.setText(String.format(Locale.getDefault(), "%.0f%%", hasil.skorPersen));
            progressSkor.setProgress((int) Math.round(hasil.skorPersen));
            tvRingkasanSkor.setText(hasil.totalTepatWaktu + " dari " + hasil.totalJatuhTempo
                    + " angsuran dibayar tepat waktu");

            switch (hasil.statusKredit) {
                case SkorKepatuhanUtil.LANCAR:
                    tvStatusKredit.setText("Lancar");
                    tvStatusKredit.setBackgroundResource(R.drawable.bg_chip_pill_green);
                    tvStatusKredit.setTextColor(0xFF2E7D32);
                    break;
                case SkorKepatuhanUtil.PERLU_PERHATIAN:
                    tvStatusKredit.setText("Perlu Perhatian");
                    tvStatusKredit.setBackgroundResource(R.drawable.bg_chip_pill_orange);
                    tvStatusKredit.setTextColor(0xFFEF6C00);
                    break;
                default:
                    tvStatusKredit.setText("Macet");
                    tvStatusKredit.setBackgroundResource(R.drawable.bg_chip_pill_red);
                    tvStatusKredit.setTextColor(0xFFC62828);
            }
        }

        boolean kosong = daftar == null || daftar.isEmpty();
        tvRiwayatKosong.setVisibility(kosong ? View.VISIBLE : View.GONE);
        rvRiwayatAngsuran.setVisibility(kosong ? View.GONE : View.VISIBLE);

        if (!kosong) {
            Collections.reverse(daftar);
            rvRiwayatAngsuran.setAdapter(new RiwayatAngsuranAdapter(daftar));
        }
    }

    private void tampilkanRiwayatKelompok(List<RiwayatKelompokItem> daftar) {
        containerRiwayatKelompok.removeAllViews();

        boolean kosong = daftar == null || daftar.isEmpty();
        tvRiwayatKelompokKosong.setText("Belum ada data kelompok");
        tvRiwayatKelompokKosong.setVisibility(kosong ? View.VISIBLE : View.GONE);
        containerRiwayatKelompok.setVisibility(kosong ? View.GONE : View.VISIBLE);

        if (kosong) return;

        LayoutInflater inflater = LayoutInflater.from(this);

        for (int i = 0; i < daftar.size(); i++) {
            RiwayatKelompokItem item = daftar.get(i);

            if (i > 0) {
                containerRiwayatKelompok.addView(buatDivider());
            }

            View row = inflater.inflate(R.layout.item_riwayat_kelompok, containerRiwayatKelompok, false);

            TextView tvNamaAnggota      = row.findViewById(R.id.tv_nama_anggota_kelompok);
            TextView tvBadgePengaju     = row.findViewById(R.id.tv_badge_pengaju);
            TextView tvLunas            = row.findViewById(R.id.tv_jumlah_lunas);
            TextView tvAktif            = row.findViewById(R.id.tv_jumlah_aktif);
            TextView tvMacet            = row.findViewById(R.id.tv_jumlah_macet);
            TextView tvTransaksiKosong  = row.findViewById(R.id.tv_transaksi_kosong);
            LinearLayout containerTransaksi = row.findViewById(R.id.container_transaksi_anggota);

            tvNamaAnggota.setText(isi(item.getNamaLengkap(), "-"));
            tvBadgePengaju.setVisibility(item.isPengaju() ? View.VISIBLE : View.GONE);
            tvLunas.setText("Lunas: " + item.getJumlahLunas());
            tvAktif.setText("Aktif: " + item.getJumlahAktif());
            tvMacet.setText("Macet: " + item.getJumlahMacet());

            List<TransaksiKelompokItem> transaksiList = item.getTransaksi();
            boolean transaksiKosong = transaksiList == null || transaksiList.isEmpty();
            tvTransaksiKosong.setVisibility(transaksiKosong ? View.VISIBLE : View.GONE);
            containerTransaksi.setVisibility(transaksiKosong ? View.GONE : View.VISIBLE);

            if (!transaksiKosong) {
                for (TransaksiKelompokItem t : transaksiList) {
                    View rowTransaksi = inflater.inflate(R.layout.item_transaksi_kelompok, containerTransaksi, false);

                    TextView tvNoKredit  = rowTransaksi.findViewById(R.id.tv_no_kredit_transaksi);
                    TextView tvSisaPokok = rowTransaksi.findViewById(R.id.tv_sisa_pokok_transaksi);
                    TextView tvStatus    = rowTransaksi.findViewById(R.id.tv_status_transaksi);

                    tvNoKredit.setText(isi(t.getNoKredit(), "-"));
                    tvSisaPokok.setText("Sisa pokok: " + formatRupiah(t.getSisaPokok())
                            + " • " + t.getJangkaWaktuBulan() + " bulan");

                    String statusMentah = t.getStatusKredit() == null ? "" : t.getStatusKredit();
                    switch (statusMentah) {
                        case "lunas":
                            tvStatus.setText("Lunas");
                            tvStatus.setBackgroundResource(R.drawable.bg_chip_pill_green);
                            tvStatus.setTextColor(0xFF2E7D32);
                            break;
                        case "aktif":
                            tvStatus.setText("Aktif");
                            tvStatus.setBackgroundResource(R.drawable.bg_info_box);
                            tvStatus.setTextColor(0xFF1565C0);
                            break;
                        case "dalam_perhatian":
                            tvStatus.setText("Perlu Perhatian");
                            tvStatus.setBackgroundResource(R.drawable.bg_chip_pill_orange);
                            tvStatus.setTextColor(0xFFEF6C00);
                            break;
                        case "macet":
                            tvStatus.setText("Macet");
                            tvStatus.setBackgroundResource(R.drawable.bg_chip_pill_red);
                            tvStatus.setTextColor(0xFFC62828);
                            break;
                        default:
                            tvStatus.setText("-");
                            tvStatus.setBackgroundResource(R.drawable.bg_info_box);
                            tvStatus.setTextColor(0xFF757575);
                    }

                    containerTransaksi.addView(rowTransaksi);
                }
            }

            containerRiwayatKelompok.addView(row);
        }
    }

    private String formatRupiah(double value) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("id", "ID"));
        nf.setMaximumFractionDigits(0);
        return "Rp " + nf.format(value);
    }

    private View buatDivider() {
        View divider = new View(this);
        int tinggi = (int) (0.5f * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, tinggi);
        divider.setLayoutParams(lp);
        divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider));
        return divider;
    }

    private String isi(String value, String fallback) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : fallback;
    }

    private String gabungTempatTanggal(String tempatLahir, String tanggalLahirSql) {
        String tgl = formatTanggal(tanggalLahirSql);
        boolean adaTempat = tempatLahir != null && !tempatLahir.trim().isEmpty();
        return adaTempat ? (tempatLahir.trim() + ", " + tgl) : tgl;
    }

    private String formatTanggal(String tanggalSql) {
        if (tanggalSql == null || tanggalSql.trim().isEmpty()) return "-";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));
            Date date = in.parse(tanggalSql);
            return date != null ? out.format(date) : tanggalSql;
        } catch (ParseException e) {
            return tanggalSql;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}