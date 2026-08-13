package com.amikom.bumdesma.ui.admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.Proposal;
import com.amikom.bumdesma.utils.Constants;
import com.amikom.bumdesma.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProposalListAdminActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private View layoutEmpty;
    private ProposalAdapter adapter;
    private final List<Proposal> list = new ArrayList<>();
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proposal_list);

        session      = new SessionManager(this);
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar  = findViewById(R.id.progress_bar);
        tvEmpty      = findViewById(R.id.tv_empty);
        layoutEmpty  = findViewById(R.id.layout_empty);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Daftar Proposal");
        }
        if (toolbar.getNavigationIcon() != null) {
            toolbar.getNavigationIcon().setTint(
                    ContextCompat.getColor(this, R.color.da_text_primary));
        }

        adapter = new ProposalAdapter(list, proposal -> {
            Intent intent = new Intent(this, ProposalDetailAdminActivity.class);
            intent.putExtra(Constants.KEY_PROPOSAL_ID, proposal.getId());
            startActivity(intent);
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::loadData);
        loadData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        if (layoutEmpty != null) layoutEmpty.setVisibility(View.GONE);

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

                            if (layoutEmpty != null) {
                                layoutEmpty.setVisibility(
                                        list.isEmpty() ? View.VISIBLE : View.GONE);
                            }
                            if (tvEmpty != null)
                                tvEmpty.setText("Belum ada proposal masuk");
                        } else {
                            Toast.makeText(ProposalListAdminActivity.this,
                                    "Gagal memuat proposal", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Proposal>>> c,
                                          Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(ProposalListAdminActivity.this,
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