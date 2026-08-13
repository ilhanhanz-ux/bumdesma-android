package com.amikom.bumdesma.ui.admin;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.amikom.bumdesma.utils.BottomNavHelper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.LaporanRingkasan;
import com.amikom.bumdesma.model.Pengumuman;
import com.amikom.bumdesma.ui.anggota.InfoTerbaruAdapter;
import com.amikom.bumdesma.utils.ImageUtils;
import com.amikom.bumdesma.utils.SessionManager;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardAdminActivity extends AppCompatActivity {

    private static final int LIMIT_INFO_TERBARU = 5;

    // Harus sama persis dengan konstanta di ProfilAdminActivity
    private static final String PREF_FOTO_ADMIN = "admin_foto_prefs";
    private static final String KEY_FOTO_PATH_ADMIN = "foto_path_admin";
    private static final int DIAMETER_AVATAR_PX = 160;

    private SessionManager session;
    private TextView tvNama, tvPeriode, tvTotalAnggota, tvKreditAktif,
            tvDanaBeredar, tvProposalMenunggu, tvTagihanJatuhTempo,
            tvPenerimaan, tvKreditMacet;
    private View progressDashboard;
    private TextView badgeProposal, badgeAngsuran;
    private ImageView ivFotoAdmin;
    private TextView tvAdminAvatarInitial;
    private final NumberFormat rupiahFmt =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    // Informasi Terbaru (carousel)
    private RecyclerView recyclerInfoTerbaru;
    private LinearLayout layoutDots;
    private TextView tvInfoKosong;
    private List<Pengumuman> listInfoTerbaru = new ArrayList<>();
    private SnapHelper snapHelper;
    private int posisiDotAktif = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_admin);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavHelper.setupAdminNav(this, bottomNav, R.id.nav_admin_beranda);

        session = new SessionManager(this);

        // Bind views
        tvNama              = findViewById(R.id.tv_nama_admin);
        tvPeriode           = findViewById(R.id.tv_periode_bulan);
        tvTotalAnggota      = findViewById(R.id.tv_total_anggota);
        tvKreditAktif       = findViewById(R.id.tv_kredit_aktif);
        tvDanaBeredar       = findViewById(R.id.tv_dana_beredar);
        tvProposalMenunggu  = findViewById(R.id.tv_proposal_menunggu);
        tvTagihanJatuhTempo = findViewById(R.id.tv_tagihan_jatuh_tempo);
        tvPenerimaan        = findViewById(R.id.tv_penerimaan);
        tvKreditMacet       = findViewById(R.id.tv_kredit_macet);
        progressDashboard   = findViewById(R.id.progress_dashboard);
        badgeProposal       = findViewById(R.id.badge_proposal);
        badgeAngsuran       = findViewById(R.id.badge_angsuran);

        // Set nama admin
        tvNama.setText(session.getNama());

        // Foto profil di header — dibaca dari SharedPreferences yang sama
        // dengan yang dipakai ProfilAdminActivity saat upload
        ivFotoAdmin          = findViewById(R.id.iv_foto_admin);
        tvAdminAvatarInitial = findViewById(R.id.tv_admin_avatar_initial);
        String namaAdmin = session.getNama();
        if (namaAdmin != null && !namaAdmin.trim().isEmpty()) {
            tvAdminAvatarInitial.setText(String.valueOf(namaAdmin.trim().charAt(0)).toUpperCase());
        }
        tampilkanFotoAvatarAdmin();

        // Klik foto/avatar profil di header -> buka halaman Profil Admin.
        findViewById(R.id.frame_avatar_admin).setOnClickListener(v ->
                startActivity(new Intent(this, ProfilAdminActivity.class)));

        // Menu cards
        CardView cardProposal   = findViewById(R.id.card_proposal);
        CardView cardAngsuran   = findViewById(R.id.card_angsuran);
        CardView cardLaporan    = findViewById(R.id.card_laporan);
        CardView cardPengumuman = findViewById(R.id.card_pengumuman);
        CardView cardVerifikasiPorsi = findViewById(R.id.card_verifikasi_porsi);
        cardVerifikasiPorsi.setOnClickListener(v ->
                startActivity(new Intent(this, VerifikasiPorsiActivity.class)));
        View bannerTagihan      = findViewById(R.id.banner_tagihan_jatuh_tempo);

        cardProposal.setOnClickListener(v ->
                startActivity(new Intent(this, ProposalListAdminActivity.class)));

        cardAngsuran.setOnClickListener(v ->
                startActivity(new Intent(this, AngsuranListActivity.class)));

        cardLaporan.setOnClickListener(v ->
                startActivity(new Intent(this, LaporanKeuanganActivity.class)));

        cardPengumuman.setOnClickListener(v ->
                startActivity(new Intent(this, PengumumanAdminActivity.class)));

        bannerTagihan.setOnClickListener(v ->
                startActivity(new Intent(this, DaftarTagihanActivity.class)));

        // Ringkasan Bulan Ini — dua card ini juga bisa diklik
        CardView cardPenerimaan  = findViewById(R.id.card_penerimaan);
        CardView cardKreditMacet = findViewById(R.id.card_kredit_macet);

        cardPenerimaan.setOnClickListener(v ->
                startActivity(new Intent(this, LaporanKeuanganActivity.class)));

        cardKreditMacet.setOnClickListener(v -> {
            Intent intent = new Intent(this, AngsuranListActivity.class);
            intent.putExtra("filter_status", "macet");
            startActivity(intent);
        });

        // Informasi Terbaru (carousel) — pola sama seperti DashboardAnggotaActivity
        recyclerInfoTerbaru = findViewById(R.id.recycler_info_terbaru);
        layoutDots           = findViewById(R.id.layout_dots);
        tvInfoKosong          = findViewById(R.id.tv_info_kosong);
        recyclerInfoTerbaru.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerInfoTerbaru);

        // Dot mengikuti card yang benar-benar sudah "ke-snap" (berhenti di posisi),
        // bukan cuma card pertama yang mulai terlihat di layar — supaya urutannya
        // selalu sinkron dengan card yang sedang aktif.
        recyclerInfoTerbaru.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView rv, int newState) {
                super.onScrollStateChanged(rv, newState);
                if (newState != RecyclerView.SCROLL_STATE_IDLE) return;

                LinearLayoutManager lm = (LinearLayoutManager) rv.getLayoutManager();
                if (lm == null) return;

                View snapView = snapHelper.findSnapView(lm);
                if (snapView == null) return;

                int posisi = lm.getPosition(snapView);
                if (posisi >= 0) buatDots(posisi);
            }
        });

        TextView tvLihatSemua = findViewById(R.id.tv_lihat_semua_pengumuman);
        tvLihatSemua.setOnClickListener(v ->
                startActivity(new Intent(this, PengumumanAdminActivity.class)));

        loadDashboard();
        loadInfoTerbaru();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
        loadInfoTerbaru();
        tampilkanFotoAvatarAdmin();
    }

    private void tampilkanFotoAvatarAdmin() {
        SharedPreferences fotoPrefs = getSharedPreferences(PREF_FOTO_ADMIN, MODE_PRIVATE);
        String path = fotoPrefs.getString(KEY_FOTO_PATH_ADMIN, null);

        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                if (bitmap != null) {
                    Bitmap bulat = ImageUtils.buatBitmapBulat(bitmap, DIAMETER_AVATAR_PX);
                    ivFotoAdmin.setImageBitmap(bulat);
                    ivFotoAdmin.setVisibility(View.VISIBLE);
                    tvAdminAvatarInitial.setVisibility(View.GONE);
                    return;
                }
            }
        }
        ivFotoAdmin.setVisibility(View.GONE);
        tvAdminAvatarInitial.setVisibility(View.VISIBLE);
    }

    private void loadDashboard() {
        progressDashboard.setVisibility(View.VISIBLE);

        String bulan = new SimpleDateFormat("yyyy-MM",
                Locale.getDefault()).format(new Date());

        tvPeriode.setText("Periode: " + new SimpleDateFormat(
                "MMMM yyyy", new Locale("id")).format(new Date()));

        ApiClient.getService()
                .getLaporanRingkasan(session.getBearerToken(), "ringkasan", bulan)
                .enqueue(new Callback<LaporanRingkasan>() {
                    @Override
                    public void onResponse(Call<LaporanRingkasan> call,
                                           Response<LaporanRingkasan> response) {
                        progressDashboard.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            updateUI(response.body().getData());
                        } else {
                            setDefaultValues();
                        }
                    }

                    @Override
                    public void onFailure(Call<LaporanRingkasan> call, Throwable t) {
                        progressDashboard.setVisibility(View.GONE);
                        setDefaultValues();
                        Toast.makeText(DashboardAdminActivity.this,
                                "Gagal memuat data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUI(LaporanRingkasan.Data data) {
        if (data == null) {
            setDefaultValues();
            return;
        }

        tvTotalAnggota.setText(String.valueOf(data.getTotalAnggota()));
        tvKreditAktif.setText(String.valueOf(data.getKreditAktif()));
        tvDanaBeredar.setText(rupiahFmt.format(data.getDanaBeredar()));
        tvProposalMenunggu.setText(String.valueOf(data.getProposalMenunggu()));

        tvPenerimaan.setText(rupiahFmt.format(data.getPenerimaanBulanIni()));

        if (data.getTagihanJatuhTempo() > 0) {
            tvTagihanJatuhTempo.setText(data.getTagihanJatuhTempo()
                    + " tagihan belum dibayar (" + rupiahFmt.format(data.getNominalJatuhTempo()) + ")");
        } else {
            tvTagihanJatuhTempo.setText("Semua tagihan bulan ini lunas");
        }

        tvKreditMacet.setText(String.valueOf(data.getKreditMacet()));
        tvKreditMacet.setTextColor(data.getKreditMacet() > 0 ? 0xFFC62828 : 0xFF2E7D32);

        badgeProposal.setVisibility(data.getProposalMenunggu() > 0 ? View.VISIBLE : View.GONE);
        if (data.getProposalMenunggu() > 0) {
            badgeProposal.setText(data.getProposalMenunggu() + " menunggu");
        }

        badgeAngsuran.setVisibility(data.getTagihanJatuhTempo() > 0 ? View.VISIBLE : View.GONE);
        if (data.getTagihanJatuhTempo() > 0) {
            badgeAngsuran.setText(data.getTagihanJatuhTempo() + " tagihan");
        }
    }

    private void setDefaultValues() {
        tvTotalAnggota.setText("—");
        tvKreditAktif.setText("—");
        tvDanaBeredar.setText("—");
        tvProposalMenunggu.setText("—");
        tvTagihanJatuhTempo.setText("Tidak dapat memuat data");
        tvPenerimaan.setText("—");
        tvKreditMacet.setText("—");
        tvKreditMacet.setTextColor(0xFF9E9E9E);
        badgeProposal.setVisibility(View.GONE);
        badgeAngsuran.setVisibility(View.GONE);
    }

    // ═══════════════════════════════════════
    //  INFORMASI TERBARU (CAROUSEL)
    // ═══════════════════════════════════════

    private void loadInfoTerbaru() {
        ApiClient.getService()
                .getPengumumanList(session.getBearerToken(), LIMIT_INFO_TERBARU)
                .enqueue(new Callback<ApiResponse<List<Pengumuman>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Pengumuman>>> call,
                                           Response<ApiResponse<List<Pengumuman>>> response) {
                        List<Pengumuman> list = (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) ? response.body().getData() : null;

                        listInfoTerbaru = (list != null) ? list : new ArrayList<>();
                        tampilkanCarousel();
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Pengumuman>>> call, Throwable t) {
                        listInfoTerbaru = new ArrayList<>();
                        tampilkanCarousel();
                    }
                });
    }

    private void tampilkanCarousel() {
        if (listInfoTerbaru.isEmpty()) {
            recyclerInfoTerbaru.setVisibility(View.GONE);
            layoutDots.setVisibility(View.GONE);
            tvInfoKosong.setVisibility(View.VISIBLE);
            return;
        }

        recyclerInfoTerbaru.setVisibility(View.VISIBLE);
        layoutDots.setVisibility(View.VISIBLE);
        tvInfoKosong.setVisibility(View.GONE);

        InfoTerbaruAdapter adapter = new InfoTerbaruAdapter(listInfoTerbaru, p ->
                startActivity(new Intent(this, PengumumanAdminActivity.class)));
        recyclerInfoTerbaru.setAdapter(adapter);

        // Reset ke posisi awal setiap kali data dimuat ulang (misal saat onResume
        // setelah admin menambah/menghapus pengumuman), supaya dot tidak "nyasar"
        // menunjuk index lama yang sudah tidak relevan dengan data baru.
        posisiDotAktif = 0;
        buatDots(0);
    }

    private void buatDots(int posisiAktif) {
        posisiDotAktif = posisiAktif;

        if (layoutDots.getChildCount() != listInfoTerbaru.size()) {
            layoutDots.removeAllViews();
            int dotSizePx = dpToPx(8);
            int dotMarginPx = dpToPx(4);
            for (int i = 0; i < listInfoTerbaru.size(); i++) {
                View dot = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dotSizePx, dotSizePx);
                lp.setMargins(dotMarginPx, 0, dotMarginPx, 0);
                dot.setLayoutParams(lp);
                layoutDots.addView(dot);
            }
        }
        for (int i = 0; i < layoutDots.getChildCount(); i++) {
            layoutDots.getChildAt(i).setBackgroundResource(
                    i == posisiAktif ? R.drawable.dot_indicator_active : R.drawable.dot_indicator_inactive);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}