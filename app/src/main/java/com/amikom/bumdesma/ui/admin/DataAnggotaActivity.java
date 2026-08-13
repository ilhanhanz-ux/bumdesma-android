package com.amikom.bumdesma.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.amikom.bumdesma.model.AnggotaAdmin;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.utils.SessionManager;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DataAnggotaActivity extends AppCompatActivity {

    private static final long SEARCH_DEBOUNCE_MS = 400;

    private TextInputEditText editTextSearch;
    private ChipGroup chipGroupFilter;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerViewAnggota;
    private TextView textEmptyState;
    private ProgressBar progressBar;

    private AnggotaAdapter adapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_anggota);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        bindViews();
        setupRecyclerView();
        setupSearch();
        setupFilterChips();

        swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(); // refresh kalau baru saja lihat/ubah detail anggota
    }

    private void bindViews() {
        editTextSearch      = findViewById(R.id.editTextSearch);
        chipGroupFilter      = findViewById(R.id.chipGroupFilter);
        swipeRefresh         = findViewById(R.id.swipeRefresh);
        recyclerViewAnggota  = findViewById(R.id.recyclerViewAnggota);
        textEmptyState       = findViewById(R.id.textEmptyState);
        progressBar          = findViewById(R.id.progressBar);
    }

    private void setupRecyclerView() {
        adapter = new AnggotaAdapter(this::openDetailAnggota);
        recyclerViewAnggota.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAnggota.setAdapter(adapter);
    }

    private void setupSearch() {
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                searchRunnable = () -> loadData();
                searchHandler.postDelayed(searchRunnable, SEARCH_DEBOUNCE_MS);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void setupFilterChips() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> loadData());
    }

    private String getSelectedStatus() {
        int checkedId = chipGroupFilter.getCheckedChipId();
        if (checkedId == R.id.chipAktif) return "aktif";
        if (checkedId == R.id.chipNonaktif) return "nonaktif";
        return null; // chip "Semua"
    }

    private void loadData() {
        progressBar.setVisibility(recyclerViewAnggota.getChildCount() == 0 ? android.view.View.VISIBLE : android.view.View.GONE);
        textEmptyState.setVisibility(android.view.View.GONE);

        String token = new SessionManager(this).getBearerToken();
        String status = getSelectedStatus();
        String search = editTextSearch.getText() != null ? editTextSearch.getText().toString().trim() : null;
        if (search != null && search.isEmpty()) search = null;

        ApiClient.getService().getDaftarAnggota(token, search, status).enqueue(new Callback<ApiResponse<List<AnggotaAdmin>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<AnggotaAdmin>>> call, Response<ApiResponse<List<AnggotaAdmin>>> response) {
                progressBar.setVisibility(android.view.View.GONE);
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<AnggotaAdmin> data = response.body().getData();
                    adapter.submitList(data);
                    textEmptyState.setVisibility(data == null || data.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                } else {
                    Toast.makeText(DataAnggotaActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<AnggotaAdmin>>> call, Throwable t) {
                progressBar.setVisibility(android.view.View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(DataAnggotaActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openDetailAnggota(AnggotaAdmin anggota) {
        Intent intent = new Intent(this, DetailAnggotaActivity.class);
        intent.putExtra("anggota_id", anggota.getId());
        startActivity(intent);
    }
}