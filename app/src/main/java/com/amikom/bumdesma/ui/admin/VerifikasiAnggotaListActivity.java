package com.amikom.bumdesma.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
import com.amikom.bumdesma.model.AnggotaVerifikasi;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.utils.Constants;
import com.amikom.bumdesma.utils.SessionManager;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifikasiAnggotaListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private View layoutEmpty;
    private VerifikasiAnggotaAdapter adapter;
    private final List<AnggotaVerifikasi> list = new ArrayList<>();
    private SessionManager session;

    // ── BARU: tab Menunggu / Riwayat ──
    private ChipGroup chipGroupStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verifikasi_anggota_list);

        session      = new SessionManager(this);
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar  = findViewById(R.id.progress_bar);
        tvEmpty      = findViewById(R.id.tv_empty);
        layoutEmpty  = findViewById(R.id.layout_empty);

        // ── BARU: bind ChipGroup ──
        chipGroupStatus = findViewById(R.id.chip_group_status);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Verifikasi Anggota Baru");
        }

        adapter = new VerifikasiAnggotaAdapter(list, anggota -> {
            Intent intent = new Intent(this, VerifikasiAnggotaDetailActivity.class);
            intent.putExtra(Constants.KEY_ANGGOTA_VERIFIKASI_ID, anggota.getAnggotaId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadData);

        // ── BARU: reload data begitu tab dipindah ──
        chipGroupStatus.setOnCheckedStateChangeListener((group, checkedIds) -> loadData());

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    /** BARU: "pending" buat tab Menunggu, "riwayat" buat tab Riwayat (diterima+ditolak) */
    private String statusTerpilih() {
        return chipGroupStatus.getCheckedChipId() == R.id.chip_riwayat
                ? "riwayat" : "pending";
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);

        String status = statusTerpilih();

        ApiClient.getService()
                .getDaftarVerifikasiAnggota(session.getBearerToken(), status)
                .enqueue(new Callback<ApiResponse<List<AnggotaVerifikasi>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<AnggotaVerifikasi>>> call,
                                           Response<ApiResponse<List<AnggotaVerifikasi>>> resp) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);

                        if (resp.isSuccessful() && resp.body() != null
                                && resp.body().isSuccess()) {
                            list.clear();
                            if (resp.body().getData() != null)
                                list.addAll(resp.body().getData());
                            adapter.notifyDataSetChanged();

                            if (layoutEmpty != null) {
                                layoutEmpty.setVisibility(
                                        list.isEmpty() ? View.VISIBLE : View.GONE);
                            }
                            if (tvEmpty != null) {
                                tvEmpty.setText("riwayat".equals(status)
                                        ? "Belum ada riwayat verifikasi"
                                        : "Tidak ada anggota baru yang perlu diverifikasi");
                            }
                        } else {
                            Toast.makeText(VerifikasiAnggotaListActivity.this,
                                    "Gagal memuat data anggota", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<AnggotaVerifikasi>>> c,
                                          Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(VerifikasiAnggotaListActivity.this,
                                "Koneksi gagal", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}