package com.amikom.bumdesma.ui.anggota;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.amikom.bumdesma.model.RiwayatAktifitasItem;
import com.amikom.bumdesma.utils.BottomNavHelper;
import com.amikom.bumdesma.utils.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RiwayatAktifitasAnggotaActivity extends AppCompatActivity {

    private SessionManager session;
    private ProgressBar progressBar;
    private View layoutEmpty;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private final List<RiwayatAktifitasItem> data = new ArrayList<>();
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_aktifitas_anggota);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Notifikasi");
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavHelper.setupAnggotaNav(this, bottomNav, R.id.nav_anggota_notifikasi);

        progressBar  = findViewById(R.id.progress_bar);
        layoutEmpty  = findViewById(R.id.layout_empty);
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefresh = findViewById(R.id.swipe_refresh);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new Adapter());

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

        ApiClient.getService()
                .getRiwayatAktifitasAnggota(session.getBearerToken(), 50)
                .enqueue(new Callback<ApiResponse<List<RiwayatAktifitasItem>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<RiwayatAktifitasItem>>> call,
                                           Response<ApiResponse<List<RiwayatAktifitasItem>>> response) {
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
                    public void onFailure(Call<ApiResponse<List<RiwayatAktifitasItem>>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        swipeRefresh.setRefreshing(false);
                        layoutEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
                        Toast.makeText(RiwayatAktifitasAnggotaActivity.this,
                                "Gagal memuat: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** Icon per jenis aktivitas - semua reuse icon yang udah ada di project, gak ada file baru */
    private int getIconForTipe(String tipe) {
        if (tipe == null) return R.drawable.ic_description_24;
        switch (tipe) {
            case "proposal_disetujui": return R.drawable.ic_verified_24;
            case "proposal_ditolak":   return R.drawable.ic_cancel_24;
            case "proposal_revisi":    return R.drawable.ic_refresh_24;
            case "kredit_cair":        return R.drawable.ic_payments_24;
            case "pembayaran":         return R.drawable.ic_verified_24;
            case "pengumuman":         return R.drawable.ic_campaign_24;
            default:                   return R.drawable.ic_description_24; // proposal_baru
        }
    }

    private int getColorForTipe(String tipe) {
        if (tipe == null) return R.color.da_accent_text;
        switch (tipe) {
            case "proposal_disetujui":
            case "kredit_cair":
            case "pembayaran":
                return R.color.status_disetujui;
            case "proposal_ditolak":
                return R.color.status_ditolak;
            case "proposal_revisi":
                return R.color.status_revisi;
            default:
                return R.color.da_accent_text; // proposal_baru & pengumuman - netral
        }
    }

    private String getLabelForTipe(String tipe) {
        if (tipe == null) return "Aktivitas";
        switch (tipe) {
            case "proposal_baru":      return "Proposal Diajukan";
            case "proposal_disetujui": return "Proposal Disetujui";
            case "proposal_ditolak":   return "Proposal Ditolak";
            case "proposal_revisi":    return "Revisi Diminta";
            case "kredit_cair":        return "Kredit Dicairkan";
            case "pembayaran":         return "Pembayaran Diterima";
            case "pengumuman":         return "Pengumuman";
            default:                   return "Aktivitas";
        }
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_riwayat_aktifitas_anggota, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            RiwayatAktifitasItem item = data.get(position);
            String tipe = item.getTipe();
            int colorRes = getColorForTipe(tipe);
            int color = getColor(colorRes);

            holder.ivIcon.setImageResource(getIconForTipe(tipe));
            holder.ivIcon.setColorFilter(color);

            holder.tvLabel.setText(getLabelForTipe(tipe));
            holder.tvLabel.setTextColor(color);

            String keterangan = item.getKeterangan();
            if (keterangan != null && !keterangan.isEmpty()) {
                holder.tvKeterangan.setVisibility(View.VISIBLE);
                holder.tvKeterangan.setText(keterangan);
            } else {
                holder.tvKeterangan.setVisibility(View.GONE);
            }

            holder.tvWaktu.setText(item.getWaktu() != null ? item.getWaktu() : "-");

            if (item.getNominal() != null) {
                holder.tvNominal.setVisibility(View.VISIBLE);
                holder.tvNominal.setText(fmt.format(item.getNominal()));
            } else {
                holder.tvNominal.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() { return data.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvLabel, tvKeterangan, tvWaktu, tvNominal;
            ViewHolder(View itemView) {
                super(itemView);
                ivIcon       = itemView.findViewById(R.id.iv_icon_tipe);
                tvLabel      = itemView.findViewById(R.id.tv_label_tipe);
                tvKeterangan = itemView.findViewById(R.id.tv_keterangan);
                tvWaktu      = itemView.findViewById(R.id.tv_waktu);
                tvNominal    = itemView.findViewById(R.id.tv_nominal);
            }
        }
    }
}