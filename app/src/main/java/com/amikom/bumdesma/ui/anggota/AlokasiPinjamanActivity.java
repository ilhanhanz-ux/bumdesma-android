package com.amikom.bumdesma.ui.anggota;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.AlokasiAnggota;
import com.amikom.bumdesma.model.AlokasiInput;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.DetailAlokasiPinjaman;
import com.amikom.bumdesma.model.SetAlokasiRequest;
import com.amikom.bumdesma.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Layar ketua: alokasikan pokok pinjaman kredit kelompok ke tiap anggota,
// sekali saja setelah dana cair. Jadi dasar hitung proporsi porsi angsuran
// bulanan otomatis di AturPorsiAngsuranActivity.
public class AlokasiPinjamanActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvJudul, tvPokokPinjaman, tvRingkasanTotal;
    private RecyclerView recyclerView;
    private Button btnSimpan;

    private SessionManager session;
    private int kreditId;
    private double pokokPinjaman = 0;
    private boolean sudahFinal = false;
    private AlokasiPinjamanAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alokasi_pinjaman);

        session = new SessionManager(this);
        kreditId = getIntent().getIntExtra("kredit_id", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Alokasi Pinjaman ke Anggota");
        }

        progressBar      = findViewById(R.id.progress_bar);
        tvJudul          = findViewById(R.id.tv_judul_angsuran);
        tvPokokPinjaman  = findViewById(R.id.tv_total_tagihan);
        tvRingkasanTotal = findViewById(R.id.tv_ringkasan_total);
        recyclerView     = findViewById(R.id.recycler_view);
        btnSimpan        = findViewById(R.id.btn_simpan_porsi);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        btnSimpan.setOnClickListener(v -> simpanAlokasi());

        if (kreditId <= 0) {
            Toast.makeText(this, "Data kredit tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDetail();
    }

    private void loadDetail() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getService()
                .getDetailAlokasiPinjaman(session.getBearerToken(), kreditId)
                .enqueue(new Callback<ApiResponse<DetailAlokasiPinjaman>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<DetailAlokasiPinjaman>> call,
                                           Response<ApiResponse<DetailAlokasiPinjaman>> resp) {
                        progressBar.setVisibility(View.GONE);
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()
                                && resp.body().getData() != null) {
                            tampilkanDetail(resp.body().getData());
                        } else {
                            String pesan = resp.body() != null ? resp.body().getMessage() : "Gagal memuat data kredit";
                            Toast.makeText(AlokasiPinjamanActivity.this, pesan, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<DetailAlokasiPinjaman>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AlokasiPinjamanActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void tampilkanDetail(DetailAlokasiPinjaman detail) {
        pokokPinjaman = detail.getPokokPinjaman();
        sudahFinal = detail.isSudahFinal();

        tvJudul.setText(detail.getNoKredit() + " — " + detail.getNamaKelompok());
        tvPokokPinjaman.setText("Total pokok pinjaman: " + formatRupiah(pokokPinjaman));

        List<AlokasiAnggota> daftar = detail.getAnggota() != null ? detail.getAnggota() : new ArrayList<>();

        adapter = new AlokasiPinjamanAdapter(daftar, sudahFinal, this::updateRingkasan);
        recyclerView.setAdapter(adapter);
        adapter.kirimTotalAwal();

        btnSimpan.setEnabled(!sudahFinal);
        if (sudahFinal) {
            btnSimpan.setText("Sudah ada setoran berjalan, alokasi tidak bisa diubah");
        } else {
            btnSimpan.setText("Simpan Alokasi Pinjaman");
        }
    }

    private void updateRingkasan(double totalTerisi) {
        boolean pas = Math.abs(totalTerisi - pokokPinjaman) < 1;
        tvRingkasanTotal.setText("Terisi: " + formatRupiah(totalTerisi) + " / " + formatRupiah(pokokPinjaman));
        tvRingkasanTotal.setTextColor(pas ? 0xFF2E7D32 : 0xFFC62828);
    }

    private void simpanAlokasi() {
        if (adapter == null || sudahFinal) return;

        Map<Integer, Double> nilai = adapter.getNilaiSaatIni();
        double total = 0;
        for (double v : nilai.values()) total += v;

        if (Math.abs(total - pokokPinjaman) >= 1) {
            Toast.makeText(this,
                    "Total alokasi (" + formatRupiah(total) + ") harus pas sama dengan pokok pinjaman ("
                            + formatRupiah(pokokPinjaman) + ")", Toast.LENGTH_LONG).show();
            return;
        }

        List<AlokasiInput> daftarAlokasi = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : nilai.entrySet()) {
            if (entry.getValue() <= 0) {
                Toast.makeText(this, "Nominal tiap anggota harus lebih dari 0", Toast.LENGTH_SHORT).show();
                return;
            }
            daftarAlokasi.add(new AlokasiInput(entry.getKey(), entry.getValue()));
        }

        btnSimpan.setEnabled(false);
        SetAlokasiRequest request = new SetAlokasiRequest(kreditId, daftarAlokasi);

        ApiClient.getService()
                .setAlokasiPinjaman(session.getBearerToken(), request)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> resp) {
                        btnSimpan.setEnabled(true);
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                            Toast.makeText(AlokasiPinjamanActivity.this,
                                    "Alokasi pinjaman berhasil disimpan", Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            String pesan = resp.body() != null ? resp.body().getMessage() : "Gagal menyimpan";
                            Toast.makeText(AlokasiPinjamanActivity.this, "Gagal: " + pesan, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        btnSimpan.setEnabled(true);
                        Toast.makeText(AlokasiPinjamanActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String formatRupiah(double nilai) {
        long rounded = Math.round(nilai);
        String s = String.valueOf(rounded);
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            sb.insert(0, s.charAt(i));
            count++;
            if (count % 3 == 0 && i != 0) sb.insert(0, '.');
        }
        return "Rp " + sb;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}