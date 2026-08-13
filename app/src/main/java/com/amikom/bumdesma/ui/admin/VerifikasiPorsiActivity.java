package com.amikom.bumdesma.ui.admin;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.PorsiVerifikasi;
import com.amikom.bumdesma.model.VerifikasiAksiRequest;
import com.amikom.bumdesma.utils.Constants;
import com.amikom.bumdesma.utils.SessionManager;
import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Layar admin: verifikasi bukti setor porsi angsuran per anggota.
// Tab "Menunggu" -> status=menunggu_verifikasi (bisa approve/reject).
// Tab "Riwayat"  -> status=riwayat (gabungan sudah_bayar+ditolak, read-only).
public class VerifikasiPorsiActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private TextView tvEmpty;

    private PorsiVerifikasiAdapter adapter;
    private final List<PorsiVerifikasi> list = new ArrayList<>();
    private SessionManager session;
    private String statusAktif = "menunggu_verifikasi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifikasi_porsi);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Verifikasi Porsi Angsuran");
        }

        tabLayout    = findViewById(R.id.tab_layout);
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar  = findViewById(R.id.progress_bar);
        layoutEmpty  = findViewById(R.id.layout_empty);
        tvEmpty      = findViewById(R.id.tv_empty);

        adapter = new PorsiVerifikasiAdapter(list, new PorsiVerifikasiAdapter.OnAksi() {
            @Override public void onApprove(PorsiVerifikasi item) { konfirmasiApprove(item); }
            @Override public void onReject(PorsiVerifikasi item) { dialogReject(item); }
            @Override public void onLihatBukti(PorsiVerifikasi item) { lihatBuktiFull(item); }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        tabLayout.addTab(tabLayout.newTab().setText("Menunggu"));
        tabLayout.addTab(tabLayout.newTab().setText("Riwayat"));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                statusAktif = tab.getPosition() == 0 ? "menunggu_verifikasi" : "riwayat";
                loadData();
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });

        swipeRefresh.setOnRefreshListener(this::loadData);

        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        ApiClient.getService()
                .getPorsiVerifikasi(session.getBearerToken(), statusAktif)
                .enqueue(new Callback<ApiResponse<List<PorsiVerifikasi>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<PorsiVerifikasi>>> call,
                                           Response<ApiResponse<List<PorsiVerifikasi>>> resp) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);

                        if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                            list.clear();
                            if (resp.body().getData() != null) {
                                list.addAll(resp.body().getData());
                            }
                            adapter.notifyDataSetChanged();

                            if (list.isEmpty()) {
                                tvEmpty.setText(statusAktif.equals("menunggu_verifikasi")
                                        ? "Tidak ada setoran yang menunggu verifikasi."
                                        : "Belum ada riwayat verifikasi.");
                                layoutEmpty.setVisibility(View.VISIBLE);
                            }
                        } else {
                            String pesan = resp.body() != null ? resp.body().getMessage() : "Gagal memuat data";
                            Toast.makeText(VerifikasiPorsiActivity.this, pesan, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<PorsiVerifikasi>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(VerifikasiPorsiActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void konfirmasiApprove(PorsiVerifikasi item) {
        new AlertDialog.Builder(this)
                .setTitle("Setujui Setoran")
                .setMessage("Setujui bukti setor dari " + item.getNamaLengkap() + " sebesar "
                        + formatRupiahSingkat(item.getJumlahPorsi()) + "?")
                .setPositiveButton("Setujui", (d, w) -> kirimAksi(item.getId(), "approve", null))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void dialogReject(PorsiVerifikasi item) {
        View v = LayoutInflater.from(this).inflate(R.layout.dialog_reject_porsi, null);
        EditText etAlasan = v.findViewById(R.id.et_alasan_tolak);

        new AlertDialog.Builder(this)
                .setTitle("Tolak Setoran")
                .setMessage("Tolak bukti setor dari " + item.getNamaLengkap() + "?")
                .setView(v)
                .setPositiveButton("Tolak", (d, w) -> {
                    String alasan = etAlasan.getText().toString().trim();
                    if (alasan.isEmpty()) {
                        Toast.makeText(this, "Alasan penolakan wajib diisi", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    kirimAksi(item.getId(), "reject", alasan);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void kirimAksi(int porsiId, String aksi, String catatanAdmin) {
        progressBar.setVisibility(View.VISIBLE);

        VerifikasiAksiRequest request = new VerifikasiAksiRequest(porsiId, aksi, catatanAdmin);

        ApiClient.getService()
                .verifikasiPorsi(session.getBearerToken(), request)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> resp) {
                        progressBar.setVisibility(View.GONE);
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                            Toast.makeText(VerifikasiPorsiActivity.this,
                                    aksi.equals("approve") ? "Setoran disetujui" : "Setoran ditolak",
                                    Toast.LENGTH_SHORT).show();
                            loadData(); // refresh -> item pindah dari tab Menunggu ke Riwayat
                        } else {
                            String pesan = resp.body() != null ? resp.body().getMessage() : "Gagal memproses";
                            Toast.makeText(VerifikasiPorsiActivity.this, "Gagal: " + pesan, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(VerifikasiPorsiActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void lihatBuktiFull(PorsiVerifikasi item) {
        if (item.getBuktiBayar() == null || item.getBuktiBayar().isEmpty()) {
            Toast.makeText(this, "Belum ada bukti bayar", Toast.LENGTH_SHORT).show();
            return;
        }
        Dialog dialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        ImageView iv = new ImageView(this);
        iv.setAdjustViewBounds(true);
        iv.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Glide.with(this)
                .load(Constants.buildFileUrl(item.getBuktiBayar()))
                .error(R.drawable.ic_placeholder_image)
                .into(iv);
        iv.setOnClickListener(v -> dialog.dismiss());
        dialog.setContentView(iv);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.BLACK));
        dialog.show();
    }

    private String formatRupiahSingkat(double nilai) {
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