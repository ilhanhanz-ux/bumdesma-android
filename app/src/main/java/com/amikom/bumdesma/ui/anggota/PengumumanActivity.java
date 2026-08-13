package com.amikom.bumdesma.ui.anggota;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PengumumanActivity extends AppCompatActivity {

    private SessionManager session;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private final List<Pengumuman> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengumuman);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pengumuman");
        }

        progressBar    = findViewById(R.id.progress_bar);
        layoutEmpty    = findViewById(R.id.layout_empty);
        recyclerView   = findViewById(R.id.recycler_view);
        swipeRefresh   = findViewById(R.id.swipe_refresh);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new Adapter());

        swipeRefresh.setOnRefreshListener(this::loadData);

        loadData();
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
                        recyclerView.getAdapter().notifyDataSetChanged();
                        layoutEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Pengumuman>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        layoutEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                        Toast.makeText(PengumumanActivity.this,
                                "Gagal memuat: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_pengumuman, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Pengumuman item = data.get(position);
            holder.tvJudul.setText(item.getJudul());
            holder.tvIsi.setText(item.getIsi());
            holder.tvTanggal.setText(item.getTanggal());
        }

        @Override
        public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvJudul, tvIsi, tvTanggal;
            ViewHolder(View itemView) {
                super(itemView);
                tvJudul   = itemView.findViewById(R.id.tv_judul);
                tvIsi     = itemView.findViewById(R.id.tv_isi);
                tvTanggal = itemView.findViewById(R.id.tv_tanggal);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}