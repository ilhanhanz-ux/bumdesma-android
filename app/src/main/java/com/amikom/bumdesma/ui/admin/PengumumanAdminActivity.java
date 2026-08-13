package com.amikom.bumdesma.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.Pengumuman;
import com.amikom.bumdesma.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PengumumanAdminActivity extends AppCompatActivity {

    private SessionManager session;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private View progressBar, layoutEmpty;
    private final List<Pengumuman> data = new ArrayList<>();
    private PengumumanAdminAdapter adapter;

    private final ActivityResultLauncher<Intent> formLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadData();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengumuman_admin);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Kelola Pengumuman");
        }

        progressBar  = findViewById(R.id.progress_bar);
        layoutEmpty  = findViewById(R.id.layout_empty);
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        FloatingActionButton fabTambah = findViewById(R.id.fab_tambah);

        adapter = new PengumumanAdminAdapter(data, this::bukaFormEdit, this::konfirmasiHapus);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadData);
        fabTambah.setOnClickListener(v ->
                formLauncher.launch(new Intent(this, FormPengumumanActivity.class)));

        loadData();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getService()
                .getPengumumanList(session.getBearerToken(), null)
                .enqueue(new Callback<ApiResponse<List<Pengumuman>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Pengumuman>>> call,
                                           Response<ApiResponse<List<Pengumuman>>> response) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);

                        data.clear();
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            data.addAll(response.body().getData());
                        }
                        adapter.notifyDataSetChanged();
                        layoutEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Pengumuman>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(PengumumanAdminActivity.this,
                                "Gagal memuat: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void bukaFormEdit(Pengumuman p) {
        Intent intent = new Intent(this, FormPengumumanActivity.class);
        intent.putExtra("id", p.getId());
        intent.putExtra("judul", p.getJudul());
        intent.putExtra("isi", p.getIsi());
        intent.putExtra("tanggal", p.getTanggal());
        formLauncher.launch(intent);
    }

    private void konfirmasiHapus(Pengumuman p) {
        new AlertDialog.Builder(this)
                .setTitle("Hapus Pengumuman")
                .setMessage("Yakin ingin menghapus \"" + p.getJudul() + "\"?")
                .setPositiveButton("Hapus", (d, w) -> hapusPengumuman(p))
                .setNegativeButton("Batal", null)
                .show();
    }

    private void hapusPengumuman(Pengumuman p) {
        ApiClient.getService()
                .hapusPengumuman(session.getBearerToken(), p.getId())
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            Toast.makeText(PengumumanAdminActivity.this,
                                    "Pengumuman dihapus", Toast.LENGTH_SHORT).show();
                            loadData();
                        } else {
                            Toast.makeText(PengumumanAdminActivity.this,
                                    "Gagal menghapus", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(PengumumanAdminActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}