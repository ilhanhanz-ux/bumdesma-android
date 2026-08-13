package com.amikom.bumdesma.ui.anggota;

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
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.AngsuranPorsi;
import com.amikom.bumdesma.model.DetailPorsiAngsuran;
import com.amikom.bumdesma.model.PorsiInput;
import com.amikom.bumdesma.model.SetPorsiRequest;
import com.amikom.bumdesma.utils.SessionManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AturPorsiAngsuranActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView tvJudul, tvJatuhTempo, tvTotalTagihan, tvRingkasanTotal;
    private RecyclerView recyclerView;
    private Button btnSimpan;

    private SessionManager session;
    private int angsuranId;
    private double totalTagihan = 0;
    private boolean bolehEdit = true;
    private AturPorsiAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_atur_porsi_angsuran);

        session = new SessionManager(this);
        angsuranId = getIntent().getIntExtra("angsuran_id", 0);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Atur Porsi Angsuran");
        }

        progressBar      = findViewById(R.id.progress_bar);
        tvJudul          = findViewById(R.id.tv_judul_angsuran);
        tvJatuhTempo     = findViewById(R.id.tv_jatuh_tempo);
        tvTotalTagihan   = findViewById(R.id.tv_total_tagihan);
        tvRingkasanTotal = findViewById(R.id.tv_ringkasan_total);
        recyclerView     = findViewById(R.id.recycler_view);
        btnSimpan        = findViewById(R.id.btn_simpan_porsi);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        btnSimpan.setOnClickListener(v -> simpanPorsi());

        if (angsuranId <= 0) {
            Toast.makeText(this, "Data angsuran tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDetail();
    }

    private void loadDetail() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getService()
                .getDetailPorsiAngsuran(session.getBearerToken(), angsuranId)
                .enqueue(new Callback<ApiResponse<DetailPorsiAngsuran>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<DetailPorsiAngsuran>> call,
                                           Response<ApiResponse<DetailPorsiAngsuran>> resp) {
                        progressBar.setVisibility(View.GONE);
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()
                                && resp.body().getData() != null) {
                            tampilkanDetail(resp.body().getData());
                        } else {
                            String pesan = resp.body() != null ? resp.body().getMessage() : "Gagal memuat detail angsuran";
                            Toast.makeText(AturPorsiAngsuranActivity.this, pesan, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<DetailPorsiAngsuran>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AturPorsiAngsuranActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void tampilkanDetail(DetailPorsiAngsuran detail) {
        totalTagihan = detail.getTotalBayar();

        tvJudul.setText("Angsuran ke-" + detail.getNoAngsuran());
        tvJatuhTempo.setText("Jatuh tempo: " + formatTanggal(detail.getTanggalJatuhTempo()));
        tvTotalTagihan.setText("Total tagihan bulan ini: " + formatRupiah(totalTagihan));

        List<AngsuranPorsi> porsiList = detail.getPorsi() != null ? detail.getPorsi() : new ArrayList<>();

        // Kalau ada 1 saja anggota yang porsinya sudah mulai disetor/diverifikasi,
        // seluruh pembagian bulan ini terkunci di backend -- jadi kunci juga di sini.
        bolehEdit = true;
        for (AngsuranPorsi p : porsiList) {
            if (!p.isBelumBayar()) {
                bolehEdit = false;
                break;
            }
        }

        adapter = new AturPorsiAdapter(porsiList, this::updateRingkasan);
        recyclerView.setAdapter(adapter);
        adapter.kirimTotalAwal();

        btnSimpan.setEnabled(bolehEdit);
        if (!bolehEdit) {
            btnSimpan.setText("Sebagian anggota sudah menyetor, tidak bisa diubah");
        }
    }

    private void updateRingkasan(double totalTerisi) {
        boolean pas = Math.abs(totalTerisi - totalTagihan) < 0.5;
        tvRingkasanTotal.setText("Terisi: " + formatRupiah(totalTerisi) + " / " + formatRupiah(totalTagihan));
        tvRingkasanTotal.setTextColor(pas ? 0xFF2E7D32 : 0xFFC62828);
    }

    private void simpanPorsi() {
        if (adapter == null || !bolehEdit) return;

        Map<Integer, Double> nilai = adapter.getNilaiSaatIni();
        double total = 0;
        for (double v : nilai.values()) total += v;

        if (Math.abs(total - totalTagihan) >= 0.5) {
            Toast.makeText(this,
                    "Total porsi (" + formatRupiah(total) + ") harus pas sama dengan total tagihan ("
                            + formatRupiah(totalTagihan) + ")", Toast.LENGTH_LONG).show();
            return;
        }

        List<PorsiInput> daftarPorsi = new ArrayList<>();
        for (Map.Entry<Integer, Double> entry : nilai.entrySet()) {
            if (entry.getValue() <= 0) {
                Toast.makeText(this, "Nominal tiap anggota harus lebih dari 0", Toast.LENGTH_SHORT).show();
                return;
            }
            daftarPorsi.add(new PorsiInput(entry.getKey(), entry.getValue()));
        }

        btnSimpan.setEnabled(false);
        SetPorsiRequest request = new SetPorsiRequest(angsuranId, daftarPorsi);

        ApiClient.getService()
                .setPorsiAngsuran(session.getBearerToken(), request)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> resp) {
                        btnSimpan.setEnabled(true);
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                            Toast.makeText(AturPorsiAngsuranActivity.this,
                                    "Pembagian porsi berhasil disimpan", Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            String pesan = resp.body() != null ? resp.body().getMessage() : "Gagal menyimpan";
                            Toast.makeText(AturPorsiAngsuranActivity.this, "Gagal: " + pesan, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        btnSimpan.setEnabled(true);
                        Toast.makeText(AturPorsiAngsuranActivity.this,
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

    private String formatTanggal(String tanggalSql) {
        if (tanggalSql == null || tanggalSql.isEmpty()) return "-";
        try {
            SimpleDateFormat input  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            return output.format(input.parse(tanggalSql));
        } catch (ParseException e) {
            return tanggalSql;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}