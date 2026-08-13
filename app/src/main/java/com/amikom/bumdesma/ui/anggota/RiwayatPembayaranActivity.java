package com.amikom.bumdesma.ui.anggota;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.api.ApiService;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.PorsiSaya;
import com.amikom.bumdesma.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RiwayatPembayaranActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private PorsiSayaAdapter adapter;
    private List<PorsiSaya> list = new ArrayList<>();
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_pembayaran);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Riwayat Pembayaran");
        }

        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar  = findViewById(R.id.progress_bar);
        layoutEmpty  = findViewById(R.id.layout_empty);

        // Item riwayat sudah lunas, jadi tap hanya menampilkan info singkat (read-only)
        adapter = new PorsiSayaAdapter(list, item ->
                Toast.makeText(this, "Dibayar: " + item.getTanggalSetor(), Toast.LENGTH_SHORT).show());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);

        ApiService api = ApiClient.getService();
        // angsuran_porsi.php otomatis filter ke anggota_id milik user login sendiri.
        // Dipakai (bukan angsuran.php) karena status bayar per-anggota yang sebenarnya
        // ada di angsuran_porsi — kolom angsuran.anggota_id selalu terisi id ketua
        // (pemilik kredit), jadi tidak bisa dipakai untuk memfilter riwayat anggota biasa.
        api.getPorsiSaya(session.getBearerToken())
                .enqueue(new Callback<ApiResponse<List<PorsiSaya>>>() {

                    @Override
                    public void onResponse(Call<ApiResponse<List<PorsiSaya>>> call,
                                           Response<ApiResponse<List<PorsiSaya>>> response) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            list.clear();
                            List<PorsiSaya> data = response.body().getData();
                            if (data != null) {
                                for (PorsiSaya item : data) {
                                    if ("sudah_bayar".equals(item.getStatusBayar())) {
                                        list.add(item);
                                    }
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Toast.makeText(RiwayatPembayaranActivity.this,
                                    "Gagal memuat riwayat pembayaran", Toast.LENGTH_SHORT).show();
                        }
                        layoutEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<PorsiSaya>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(RiwayatPembayaranActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        layoutEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}