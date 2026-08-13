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
import com.amikom.bumdesma.model.PorsiSaya;
import com.amikom.bumdesma.model.RekeningSetoran;
import com.amikom.bumdesma.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Layar anggota: daftar semua porsi tagihan angsuran miliknya sendiri
// (lintas bulan), tiap baris menunjukkan status bayar. Ketuk baris yang
// belum_bayar/ditolak -> UploadBuktiPorsiActivity untuk kirim bukti setor.
public class TagihanPorsiSayaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private View layoutEmpty;

    private View layoutRekeningSection;
    private RecyclerView recyclerRekening;
    private RekeningAdapter rekeningAdapter;
    private final List<RekeningSetoran> rekeningList = new ArrayList<>();

    private PorsiSayaAdapter adapter;
    private final List<PorsiSaya> list = new ArrayList<>();
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tagihan_porsi_saya);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tagihan Porsi Saya");
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

        adapter = new PorsiSayaAdapter(list, item -> {
            Intent intent = new Intent(this, UploadBuktiPorsiActivity.class);
            intent.putExtra("porsi_id", item.getId());
            intent.putExtra("no_angsuran", item.getNoAngsuran());
            intent.putExtra("tanggal_jatuh_tempo", item.getTanggalJatuhTempo());
            intent.putExtra("jumlah_porsi", item.getJumlahPorsi());
            intent.putExtra("status_bayar", item.getStatusBayar());
            intent.putExtra("catatan_admin", item.getCatatanAdmin());
            intent.putExtra("bukti_bayar", item.getBuktiBayar());
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
        // Refresh tiap kembali dari UploadBuktiPorsiActivity, biar status
        // langsung ter-update setelah kirim bukti.
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
                .getPorsiSaya(session.getBearerToken())
                .enqueue(new Callback<ApiResponse<List<PorsiSaya>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<PorsiSaya>>> call,
                                           Response<ApiResponse<List<PorsiSaya>>> resp) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);

                        if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                            list.clear();
                            if (resp.body().getData() != null) {
                                list.addAll(resp.body().getData());
                            }
                            adapter.notifyDataSetChanged();

                            if (list.isEmpty()) {
                                tvEmpty.setText("Belum ada tagihan porsi angsuran untuk Anda.");
                                layoutEmpty.setVisibility(View.VISIBLE);
                            }
                        } else {
                            String pesan = resp.body() != null ? resp.body().getMessage() : "Gagal memuat data";
                            Toast.makeText(TagihanPorsiSayaActivity.this, pesan, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<PorsiSaya>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(TagihanPorsiSayaActivity.this,
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