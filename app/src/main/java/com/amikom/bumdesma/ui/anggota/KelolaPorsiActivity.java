package com.amikom.bumdesma.ui.anggota;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.RekeningSetoran;
import com.amikom.bumdesma.model.RingkasanAngsuranKetua;
import com.amikom.bumdesma.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Layar khusus ketua: daftar angsuran (jadwal bulanan) kredit kelompoknya,
// tiap baris menunjukkan apakah porsi tiap anggota untuk bulan itu sudah
// lengkap dibagi atau belum. Ketuk 1 baris -> AturPorsiAngsuranActivity.
public class KelolaPorsiActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private View layoutEmpty;

    private View layoutRekeningSection;
    private RecyclerView recyclerRekening;
    private RekeningAdapter rekeningAdapter;
    private final List<RekeningSetoran> rekeningList = new ArrayList<>();

    private RingkasanAngsuranKetuaAdapter adapter;
    private final List<RingkasanAngsuranKetua> list = new ArrayList<>();
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kelola_porsi);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Kelola Porsi Angsuran");
        }

        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar  = findViewById(R.id.progress_bar);
        tvEmpty      = findViewById(R.id.tv_empty);
        layoutEmpty  = findViewById(R.id.layout_empty);

        layoutRekeningSection = findViewById(R.id.layout_rekening_section);
        recyclerRekening       = findViewById(R.id.recycler_rekening);

        rekeningAdapter = new RekeningAdapter(rekeningList);
        recyclerRekening.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerRekening.setAdapter(rekeningAdapter);

        new LinearSnapHelper().attachToRecyclerView(recyclerRekening);

        adapter = new RingkasanAngsuranKetuaAdapter(list, item -> {
            Intent intent = new Intent(this, AturPorsiAngsuranActivity.class);
            intent.putExtra("angsuran_id", item.getAngsuranId());
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadData);

        loadData();
        loadRekening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh tiap kembali dari AturPorsiAngsuranActivity, biar status
        // "SUDAH DIBAGI" / "BELUM LENGKAP" langsung ter-update.
        loadData();
    }

    private void loadRekening() {
        ApiClient.getService()
                .getRekeningSetoran(session.getBearerToken())
                .enqueue(new Callback<ApiResponse<List<RekeningSetoran>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<RekeningSetoran>>> call,
                                           Response<ApiResponse<List<RekeningSetoran>>> resp) {
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()
                                && resp.body().getData() != null && !resp.body().getData().isEmpty()) {
                            rekeningList.clear();
                            rekeningList.addAll(resp.body().getData());
                            rekeningAdapter.notifyDataSetChanged();
                            recyclerRekening.scrollToPosition(0);
                            layoutRekeningSection.setVisibility(View.VISIBLE);
                        } else {
                            layoutRekeningSection.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<RekeningSetoran>>> call, Throwable t) {
                        layoutRekeningSection.setVisibility(View.GONE);
                    }
                });
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        ApiClient.getService()
                .getRingkasanAngsuranKetua(session.getBearerToken())
                .enqueue(new Callback<ApiResponse<List<RingkasanAngsuranKetua>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<RingkasanAngsuranKetua>>> call,
                                           Response<ApiResponse<List<RingkasanAngsuranKetua>>> resp) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);

                        if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                            list.clear();
                            if (resp.body().getData() != null) {
                                list.addAll(resp.body().getData());
                            }
                            adapter.notifyDataSetChanged();

                            if (list.isEmpty()) {
                                tvEmpty.setText("Belum ada jadwal angsuran untuk kelompok Anda.\nJadwal akan muncul setelah proposal pinjaman disetujui admin.");
                                layoutEmpty.setVisibility(View.VISIBLE);
                            }
                        } else {
                            String pesan = resp.body() != null ? resp.body().getMessage() : "Gagal memuat data";
                            Toast.makeText(KelolaPorsiActivity.this, pesan, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<RingkasanAngsuranKetua>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(KelolaPorsiActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}