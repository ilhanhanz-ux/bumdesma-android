package com.amikom.bumdesma.ui.admin;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.RiwayatAktifitasItem;
import com.amikom.bumdesma.utils.SessionManager;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RiwayatAktifitasActivity extends AppCompatActivity {

    private static final int LIMIT = 50;

    private SessionManager session;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ChipGroup chipGroupFilter;

    private final List<RiwayatAktifitasItem> dataAsli = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_aktifitas);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Riwayat Aktifitas");
        }

        progressBar     = findViewById(R.id.progress_bar);
        tvEmpty         = findViewById(R.id.tv_empty);
        recyclerView    = findViewById(R.id.recycler_view);
        swipeRefresh    = findViewById(R.id.swipe_refresh);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefresh.setOnRefreshListener(this::loadData);
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> tampilkanTerfilter());

        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(dataAsli.isEmpty() ? View.VISIBLE : View.GONE);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.getService()
                .getRiwayatAktifitas(session.getBearerToken(), LIMIT)
                .enqueue(new Callback<ApiResponse<List<RiwayatAktifitasItem>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<RiwayatAktifitasItem>>> call,
                                           Response<ApiResponse<List<RiwayatAktifitasItem>>> response) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);

                        dataAsli.clear();
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            dataAsli.addAll(response.body().getData());
                        }
                        tampilkanTerfilter();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<RiwayatAktifitasItem>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(RiwayatAktifitasActivity.this,
                                "Gagal memuat: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        tampilkanTerfilter();
                    }
                });
    }

    private void tampilkanTerfilter() {
        int checkedId = chipGroupFilter.getCheckedChipId();
        String prefix = null;
        if (checkedId == R.id.chipProposal)    prefix = "proposal";
        else if (checkedId == R.id.chipKredit)     prefix = "kredit";
        else if (checkedId == R.id.chipPembayaran) prefix = "pembayaran";
        else if (checkedId == R.id.chipPengumuman) prefix = "pengumuman";

        List<RiwayatAktifitasItem> hasil = new ArrayList<>();
        for (RiwayatAktifitasItem item : dataAsli) {
            if (prefix == null || (item.getTipe() != null && item.getTipe().startsWith(prefix))) {
                hasil.add(item);
            }
        }

        recyclerView.setAdapter(new RiwayatAktifitasAdapter(hasil));
        tvEmpty.setVisibility(hasil.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(hasil.isEmpty() ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}