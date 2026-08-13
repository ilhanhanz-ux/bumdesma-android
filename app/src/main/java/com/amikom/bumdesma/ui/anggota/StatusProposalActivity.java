package com.amikom.bumdesma.ui.anggota;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.Proposal;
import com.amikom.bumdesma.ui.admin.ProposalAdapter;
import com.amikom.bumdesma.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.ProgressBar;

public class StatusProposalActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private View progressBar;
    private android.widget.TextView tvEmpty;
    private ProposalAdapter adapter;
    private final List<Proposal> list = new ArrayList<>();
    private SessionManager session;
    private View layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proposal_list);

        session      = new SessionManager(this);
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar  = findViewById(R.id.progress_bar);
        tvEmpty      = findViewById(R.id.tv_empty);
        layoutEmpty = findViewById(R.id.layout_empty);

        // Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Status Proposal Saya");
        }

        adapter = new ProposalAdapter(list, p ->
                Toast.makeText(this, "Status: " + p.getStatus().toUpperCase(),
                        Toast.LENGTH_SHORT).show());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        ApiClient.getService()
                .getProposalList(session.getBearerToken())
                .enqueue(new Callback<ApiResponse<List<Proposal>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Proposal>>> call,
                                           Response<ApiResponse<List<Proposal>>> resp) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        if (resp.isSuccessful() && resp.body() != null
                                && resp.body().isSuccess()) {
                            list.clear();
                            if (resp.body().getData() != null)
                                list.addAll(resp.body().getData());
                            adapter.notifyDataSetChanged();
                            layoutEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                            tvEmpty.setText("Belum ada proposal yang diajukan");
                        }
                    }
                    @Override
                    public void onFailure(Call<ApiResponse<List<Proposal>>> c, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(StatusProposalActivity.this,
                                "Koneksi gagal", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() { onBackPressed(); return true; }
}