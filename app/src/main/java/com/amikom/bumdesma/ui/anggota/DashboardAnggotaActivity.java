package com.amikom.bumdesma.ui.anggota;
import com.amikom.bumdesma.ui.anggota.AlokasiPinjamanActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.amikom.bumdesma.utils.BottomNavHelper;
import com.amikom.bumdesma.utils.ImageUtils;

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
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;

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
import com.amikom.bumdesma.model.KreditRingkasan;
import com.amikom.bumdesma.model.Pengumuman;
import com.amikom.bumdesma.utils.SessionManager;

import java.io.File;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardAnggotaActivity extends AppCompatActivity {

    private static final int LIMIT_INFO_TERBARU = 5;

    private static final String PREF_FOTO = "profil_foto_prefs";
    private static final String KEY_FOTO_PATH = "foto_path";
    private static final int DIAMETER_AVATAR_PX = 160;

    private SessionManager session;
    private final NumberFormat fmtRupiah =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    private TextView tvSisa, tvAngsuran, tvJatuhTempo, tvTagihan;
    private Button btnAlokasiPinjaman;
    private ImageView ivAvatarFoto;
    private TextView tvAvatarInitial;

    private RecyclerView recyclerInfoTerbaru;
    private LinearLayout layoutDots;
    private TextView tvInfoKosong;
    private List<Pengumuman> listInfoTerbaru = new ArrayList<>();
    private SnapHelper snapHelper;
    private View blobHeaderBlue, blobHeaderPurple, blobHeaderTeal;
    private AnimatorSet animatorBlobs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard_anggota);
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavHelper.setupAnggotaNav(this, bottomNav, R.id.nav_anggota_beranda);

        session = new SessionManager(this);

        TextView tvNama = findViewById(R.id.tv_nama_anggota);
        TextView tvKelompok = findViewById(R.id.tv_kelompok);
        tvNama.setText(session.getNama());
        tvKelompok.setText("Anggota SPP BUMDesma");

        ivAvatarFoto    = findViewById(R.id.iv_avatar_foto);
        tvAvatarInitial = findViewById(R.id.tv_avatar_initial);
        blobHeaderBlue   = findViewById(R.id.blobHeaderBlue);
        blobHeaderPurple = findViewById(R.id.blobHeaderPurple);
        blobHeaderTeal   = findViewById(R.id.blobHeaderTeal);
        mulaiAnimasiBlob();
        String nama = session.getNama();
        if (nama != null && !nama.trim().isEmpty()) {
            tvAvatarInitial.setText(String.valueOf(nama.trim().charAt(0)).toUpperCase());
        }

        // Klik foto/avatar profil di header -> buka halaman Profil Saya.
        findViewById(R.id.frame_avatar_anggota).setOnClickListener(v ->
                startActivity(new Intent(this, ProfilSayaActivity.class)));

        tvSisa        = findViewById(R.id.tv_sisa_pinjaman);
        tvAngsuran    = findViewById(R.id.tv_angsuran_ke);
        tvJatuhTempo  = findViewById(R.id.tv_jatuh_tempo);
        tvTagihan     = findViewById(R.id.tv_tagihan_bulan);
        btnAlokasiPinjaman = findViewById(R.id.btn_alokasi_pinjaman); // HARUS sebelum tampilkanDefaultKredit()
        tampilkanDefaultKredit();

        recyclerInfoTerbaru = findViewById(R.id.recycler_info_terbaru);
        layoutDots           = findViewById(R.id.layout_dots);
        tvInfoKosong          = findViewById(R.id.tv_info_kosong);
        recyclerInfoTerbaru.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerInfoTerbaru);

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
                startActivity(new Intent(this, PengumumanActivity.class)));

        CardView cardStatus     = findViewById(R.id.card_status_proposal);
        CardView cardJadwal     = findViewById(R.id.card_jadwal_angsuran);
        CardView cardRiwayat    = findViewById(R.id.card_riwayat);
        CardView cardBantuan    = findViewById(R.id.card_bantuan);
        CardView cardBannerAjukan = findViewById(R.id.card_banner_ajukan);

        cardStatus.setOnClickListener(v ->
                startActivity(new Intent(this, StatusProposalActivity.class)));
        cardJadwal.setOnClickListener(v -> {
            if (session.isKetua()) {
                startActivity(new Intent(this, KelolaPorsiActivity.class));
            } else {
                startActivity(new Intent(this, TagihanPorsiSayaActivity.class));
            }
        });
        cardRiwayat.setOnClickListener(v ->
                startActivity(new Intent(this, RiwayatPembayaranActivity.class)));
        cardBantuan.setOnClickListener(v ->
                startActivity(new Intent(this, BantuanActivity.class)));
        cardBannerAjukan.setOnClickListener(v -> bukaAjukanProposal());

        loadKreditAktif();
        loadInfoTerbaru();
        tampilkanFotoAvatar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadKreditAktif();
        loadInfoTerbaru();
        tampilkanFotoAvatar();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (animatorBlobs != null) animatorBlobs.cancel();
    }

    // ── BARU: hanya ketua kelompok yang boleh masuk ke Ajukan Proposal.
    // Anggota biasa dikasih tahu lewat Toast, bukan langsung dibiarkan masuk
    // lalu ditolak backend — supaya lebih jelas alasannya.
    private void bukaAjukanProposal() {
        if (!session.isKetua()) {
            Toast.makeText(this,
                    "Hanya ketua kelompok yang dapat mengajukan proposal pinjaman. Hubungi ketua kelompok Anda untuk mengajukan pinjaman baru.",
                    Toast.LENGTH_LONG).show();
            return;
        }
        startActivity(new Intent(this, AjukanProposalActivity.class));
    }

    private void tampilkanFotoAvatar() {
        SharedPreferences fotoPrefs = getSharedPreferences(PREF_FOTO, MODE_PRIVATE);
        String path = fotoPrefs.getString(KEY_FOTO_PATH, null);

        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                if (bitmap != null) {
                    Bitmap bulat = ImageUtils.buatBitmapBulat(bitmap, DIAMETER_AVATAR_PX);
                    ivAvatarFoto.setImageBitmap(bulat);
                    ivAvatarFoto.setVisibility(View.VISIBLE);
                    tvAvatarInitial.setVisibility(View.GONE);
                    return;
                }
            }
        }
        ivAvatarFoto.setVisibility(View.GONE);
        tvAvatarInitial.setVisibility(View.VISIBLE);
    }

    private void tampilkanDefaultKredit() {
        tvSisa.setText("Tidak ada");
        tvAngsuran.setText("-");
        tvJatuhTempo.setText("-");
        tvTagihan.setText("Tidak ada tagihan");
        btnAlokasiPinjaman.setVisibility(View.GONE);
    }

    private void loadKreditAktif() {
        ApiClient.getService()
                .getKreditAktif(session.getBearerToken())
                .enqueue(new Callback<ApiResponse<KreditRingkasan>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<KreditRingkasan>> call,
                                           Response<ApiResponse<KreditRingkasan>> resp) {
                        if (!resp.isSuccessful() || resp.body() == null
                                || !resp.body().isSuccess()) {
                            tampilkanDefaultKredit();
                            return;
                        }

                        KreditRingkasan k = resp.body().getData();
                        if (k == null || !k.isAdaKreditAktif()) {
                            tampilkanDefaultKredit();
                            return;
                        }

                        tvSisa.setText(fmtRupiah.format(k.getSisaPokok()));

                        if (k.getAngsuranKe() != null) {
                            tvAngsuran.setText(k.getAngsuranKe() + " / " + k.getJangkaWaktuBulan());
                        } else {
                            tvAngsuran.setText("Lunas");
                        }

                        tvJatuhTempo.setText(formatTanggal(k.getTanggalJatuhTempo()));

                        if (k.getTagihanBulanIni() != null) {
                            tvTagihan.setText(fmtRupiah.format(k.getTagihanBulanIni()));
                        } else {
                            tvTagihan.setText("Tidak ada tagihan");
                        }

                        // BARU: tombol tetap muncul selama belum terkunci -- teksnya
                        // yang berubah tergantung sudah pernah diisi apa belum. Beda
                        // dengan versi sebelumnya yang hilang permanen begitu sekali
                        // tersimpan; sekarang cuma hilang kalau memang sudah ada
                        // setoran berjalan (alokasiTerkunci).
                        boolean tampilkanTombol = session.isKetua() && !k.isAlokasiTerkunci();
                        btnAlokasiPinjaman.setVisibility(tampilkanTombol ? View.VISIBLE : View.GONE);
                        if (tampilkanTombol) {
                            btnAlokasiPinjaman.setText(k.isAlokasiSudahDiisi()
                                    ? "✏️ Edit Alokasi Pinjaman ke Anggota"
                                    : "⚠️ Alokasikan Pinjaman ke Anggota");
                            final int kreditId = k.getKreditId();
                            btnAlokasiPinjaman.setOnClickListener(v -> {
                                Intent intent = new Intent(DashboardAnggotaActivity.this, AlokasiPinjamanActivity.class);
                                intent.putExtra("kredit_id", kreditId);
                                startActivity(intent);
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<KreditRingkasan>> call, Throwable t) {
                        tampilkanDefaultKredit();
                    }
                });
    }

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
                startActivity(new Intent(this, PengumumanActivity.class)));
        recyclerInfoTerbaru.setAdapter(adapter);

        // Reset ke posisi awal setiap kali data dimuat ulang (misal saat onResume),
        // supaya dot tidak "nyasar" menunjuk index lama yang sudah tidak relevan.
        buatDots(0);
    }

    private void buatDots(int posisiAktif) {
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

    private void mulaiAnimasiBlob() {
        animatorBlobs = new AnimatorSet();
        animatorBlobs.playTogether(
                buatAnimatorBlob(blobHeaderBlue, 16f, 7000),
                buatAnimatorBlob(blobHeaderPurple, -14f, 9000),
                buatAnimatorBlob(blobHeaderTeal, 12f, 5500)
        );
        animatorBlobs.start();
    }

    private ObjectAnimator buatAnimatorBlob(View target, float jarakTranslasiDp, long durasiMs) {
        float jarakPx = jarakTranslasiDp * getResources().getDisplayMetrics().density;
        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationY", 0f, jarakPx);
        animator.setDuration(durasiMs);
        animator.setRepeatMode(ObjectAnimator.REVERSE);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        return animator;
    }

    private String formatTanggal(String tanggalSql) {
        if (tanggalSql == null || tanggalSql.isEmpty()) return "-";
        try {
            SimpleDateFormat input  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
            return output.format(input.parse(tanggalSql));
        } catch (ParseException e) {
            return tanggalSql;
        }
    }
}