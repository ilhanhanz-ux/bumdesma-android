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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.Angsuran;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DaftarTagihanActivity extends AppCompatActivity {

    private static final long SEARCH_DEBOUNCE_MS = 400;

    private Toolbar toolbar;
    private TextInputEditText editTextSearch;
    private ChipGroup chipGroupFilter;
    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recyclerViewTagihan;
    private TextView textEmptyState;
    private ProgressBar progressBar;

    private TagihanAdapter adapter;
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    private ActivityResultLauncher<Intent> setoranLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar_tagihan);

        bindViews();
        setupToolbar();
        setupRecyclerView();
        setupSearch();
        setupFilterChips();
        setupResultLauncher();

        swipeRefresh.setOnRefreshListener(this::loadData);

        loadData();
    }

    private void bindViews() {
        toolbar              = findViewById(R.id.toolbar);
        editTextSearch      = findViewById(R.id.editTextSearch);
        chipGroupFilter      = findViewById(R.id.chipGroupFilter);
        swipeRefresh         = findViewById(R.id.swipeRefresh);
        recyclerViewTagihan  = findViewById(R.id.recyclerViewTagihan);
        textEmptyState       = findViewById(R.id.textEmptyState);
        progressBar          = findViewById(R.id.progressBar);
    }

    private void setupToolbar() {
        toolbar.setNavigationOnClickListener(v -> finish());

        // BARU: menu untuk buka Setoran Kolektif
        toolbar.inflateMenu(R.menu.menu_daftar_tagihan);
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.menu_setoran_kolektif) {
                Intent intent = new Intent(this, SetoranKolektifActivity.class);
                setoranLauncher.launch(intent); // reuse launcher yang sudah ada, auto-refresh kalau sukses
                return true;
            }
            return false;
        });
    }

    private void setupRecyclerView() {
        adapter = new TagihanAdapter(this::openSetoranAngsuran);
        recyclerViewTagihan.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTagihan.setAdapter(adapter);
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

    private void setupResultLauncher() {
        // Refresh list otomatis setelah admin berhasil mencatat setoran
        setoranLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        loadData();
                    }
                }
        );
    }

    private String getSelectedStatus() {
        int checkedId = chipGroupFilter.getCheckedChipId();
        if (checkedId == R.id.chipJatuhTempo) return "belum_bayar";
        if (checkedId == R.id.chipTerlambat) return "terlambat";
        return null; // chip "Semua" -> tanpa filter status
    }

    private void loadData() {
        progressBar.setVisibility(recyclerViewTagihan.getChildCount() == 0 ? android.view.View.VISIBLE : android.view.View.GONE);
        textEmptyState.setVisibility(android.view.View.GONE);

        String token = new SessionManager(this).getBearerToken();
        String status = getSelectedStatus();
        String search = editTextSearch.getText() != null ? editTextSearch.getText().toString().trim() : null;
        if (search != null && search.isEmpty()) search = null;

        ApiClient.getService().getDaftarTagihan(token, status, search).enqueue(new Callback<ApiResponse<List<Angsuran>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Angsuran>>> call, Response<ApiResponse<List<Angsuran>>> response) {
                progressBar.setVisibility(android.view.View.GONE);
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Angsuran> data = response.body().getData();
                    adapter.submitList(data);
                    textEmptyState.setVisibility(data == null || data.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
                } else {
                    Toast.makeText(DaftarTagihanActivity.this, "Gagal memuat data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Angsuran>>> call, Throwable t) {
                progressBar.setVisibility(android.view.View.GONE);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(DaftarTagihanActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openSetoranAngsuran(Angsuran angsuran) {
        Intent intent = new Intent(this, SetoranAngsuranActivity.class);
        intent.putExtra("angsuran_id", angsuran.getId());
        intent.putExtra("nama_lengkap", angsuran.getNamaLengkap());
        intent.putExtra("no_angsuran", angsuran.getNoAngsuran());
        intent.putExtra("total_bayar", angsuran.getTotalBayar());
        intent.putExtra("tanggal_jatuh_tempo", angsuran.getTanggalJatuhTempo());
        setoranLauncher.launch(intent);
    }
}