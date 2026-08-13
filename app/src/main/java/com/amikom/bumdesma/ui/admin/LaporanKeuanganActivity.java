package com.amikom.bumdesma.ui.admin;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.BuildConfig;
import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.LaporanRingkasan;
import com.amikom.bumdesma.model.RekapBulananData;
import com.amikom.bumdesma.model.TunggakanData;
import com.amikom.bumdesma.utils.RekapPdfExporter;
import com.amikom.bumdesma.utils.SessionManager;

import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LaporanKeuanganActivity extends AppCompatActivity {

    private enum Tab { RINGKASAN, TUNGGAKAN, BULANAN }
    private Tab tabAktif = Tab.RINGKASAN;

    private SessionManager session;
    private Calendar bulanAktif = Calendar.getInstance();
    private final NumberFormat fmtRupiah =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    // Nav & Tab views
    private LinearLayout layoutBulanNav;
    private TextView tvPeriodeBulan;
    private TextView tvTabRingkasan, tvTabTunggakan, tvTabBulanan;
    private View underlineRingkasan, underlineTunggakan, underlineBulanan;
    private View progressBar;

    // Konten tab
    private View layoutTabRingkasan, layoutTabTunggakan, layoutTabBulanan;

    // Ringkasan
    private TextView tvDanaBeredar, tvPenerimaan, tvProposalMenunggu;
    private TextView tvKreditLancar, tvKreditMacet, tvTunggakanAlert;
    private View bannerTunggakan;
    private View barPortofolioLancar, barPortofolioMacet;

    // Tunggakan
    private TextView tvTotalTunggakan, tvEmptyTunggakan;
    private RecyclerView recyclerTunggakan;

    // Bulanan
    private TextView tvTotalPokok, tvTotalBunga, tvTotalBayar, tvEmptyBulanan;
    private RecyclerView recyclerBulanan;

    // Cache — supaya tidak fetch ulang tiap ganti tab kalau bulan tidak berubah
    private boolean tunggakanSudahDimuat = false;

    // Data rekap bulanan aktif (dipakai untuk export PDF)
    private RekapBulananData rekapDataAktif;
    private MenuItem menuExportPdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laporan_keuangan);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Laporan Keuangan");
        }

        bindViews();
        setupTabClicks();
        setupBulanNav();

        loadRingkasan();
    }

    private void bindViews() {
        layoutBulanNav   = findViewById(R.id.layout_bulan_nav);
        tvPeriodeBulan   = findViewById(R.id.tv_periode_bulan);
        progressBar      = findViewById(R.id.progress_bar);

        tvTabRingkasan   = findViewById(R.id.tv_tab_ringkasan);
        tvTabTunggakan   = findViewById(R.id.tv_tab_tunggakan);
        tvTabBulanan     = findViewById(R.id.tv_tab_bulanan);
        underlineRingkasan = findViewById(R.id.underline_ringkasan);
        underlineTunggakan = findViewById(R.id.underline_tunggakan);
        underlineBulanan   = findViewById(R.id.underline_bulanan);

        layoutTabRingkasan = findViewById(R.id.layout_tab_ringkasan);
        layoutTabTunggakan = findViewById(R.id.layout_tab_tunggakan);
        layoutTabBulanan   = findViewById(R.id.layout_tab_bulanan);

        tvDanaBeredar      = findViewById(R.id.tv_dana_beredar);
        tvPenerimaan       = findViewById(R.id.tv_penerimaan);
        tvProposalMenunggu = findViewById(R.id.tv_proposal_menunggu);
        tvKreditLancar     = findViewById(R.id.tv_kredit_lancar);
        tvKreditMacet      = findViewById(R.id.tv_kredit_macet);
        tvTunggakanAlert   = findViewById(R.id.tv_tunggakan_alert);
        bannerTunggakan    = findViewById(R.id.banner_tunggakan);
        barPortofolioLancar = findViewById(R.id.bar_portofolio_lancar);
        barPortofolioMacet  = findViewById(R.id.bar_portofolio_macet);

        tvTotalTunggakan = findViewById(R.id.tv_total_tunggakan);
        tvEmptyTunggakan = findViewById(R.id.tv_empty_tunggakan);
        recyclerTunggakan = findViewById(R.id.recycler_tunggakan);
        recyclerTunggakan.setLayoutManager(new LinearLayoutManager(this));

        tvTotalPokok  = findViewById(R.id.tv_total_pokok);
        tvTotalBunga  = findViewById(R.id.tv_total_bunga);
        tvTotalBayar  = findViewById(R.id.tv_total_bayar);
        tvEmptyBulanan = findViewById(R.id.tv_empty_bulanan);
        recyclerBulanan = findViewById(R.id.recycler_bulanan);
        recyclerBulanan.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupTabClicks() {
        findViewById(R.id.tab_ringkasan).setOnClickListener(v -> pilihTab(Tab.RINGKASAN));
        findViewById(R.id.tab_tunggakan).setOnClickListener(v -> pilihTab(Tab.TUNGGAKAN));
        findViewById(R.id.tab_bulanan).setOnClickListener(v -> pilihTab(Tab.BULANAN));
    }

    private void setupBulanNav() {
        updateLabelBulan();
        findViewById(R.id.btn_bulan_prev).setOnClickListener(v -> gantiBulan(-1));
        findViewById(R.id.btn_bulan_next).setOnClickListener(v -> gantiBulan(1));
    }

    private void gantiBulan(int delta) {
        bulanAktif.add(Calendar.MONTH, delta);
        updateLabelBulan();
        loadRingkasan();
        if (tabAktif == Tab.BULANAN) loadRekapBulanan();
    }

    private void updateLabelBulan() {
        tvPeriodeBulan.setText(new java.text.SimpleDateFormat("MMMM yyyy", new Locale("id"))
                .format(bulanAktif.getTime()));
    }

    private String getBulanParam() {
        return new java.text.SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(bulanAktif.getTime());
    }

    private void pilihTab(Tab tab) {
        tabAktif = tab;

        layoutTabRingkasan.setVisibility(tab == Tab.RINGKASAN ? View.VISIBLE : View.GONE);
        layoutTabTunggakan.setVisibility(tab == Tab.TUNGGAKAN ? View.VISIBLE : View.GONE);
        layoutTabBulanan.setVisibility(tab == Tab.BULANAN ? View.VISIBLE : View.GONE);

        // Filter bulan tidak relevan untuk Tunggakan (data real-time global) — sembunyikan
        layoutBulanNav.setVisibility(tab == Tab.TUNGGAKAN ? View.GONE : View.VISIBLE);

        int colorAktif = getColor(R.color.admin_accent_text);
        int colorNonaktif = getColor(R.color.admin_text_tertiary);

        tvTabRingkasan.setTextColor(tab == Tab.RINGKASAN ? colorAktif : colorNonaktif);
        tvTabTunggakan.setTextColor(tab == Tab.TUNGGAKAN ? colorAktif : colorNonaktif);
        tvTabBulanan.setTextColor(tab == Tab.BULANAN ? colorAktif : colorNonaktif);
        underlineRingkasan.setBackgroundColor(tab == Tab.RINGKASAN ? colorAktif : 0x00000000);
        underlineTunggakan.setBackgroundColor(tab == Tab.TUNGGAKAN ? colorAktif : 0x00000000);
        underlineBulanan.setBackgroundColor(tab == Tab.BULANAN ? colorAktif : 0x00000000);

        if (tab == Tab.TUNGGAKAN && !tunggakanSudahDimuat) loadTunggakan();
        if (tab == Tab.BULANAN) loadRekapBulanan();

        invalidateOptionsMenu(); // supaya tombol export PDF cuma muncul di tab Bulanan
    }

    private void loadRingkasan() {
        progressBar.setVisibility(View.VISIBLE);
        ApiClient.getService()
                .getLaporanRingkasan(session.getBearerToken(), "ringkasan", getBulanParam())
                .enqueue(new Callback<LaporanRingkasan>() {
                    @Override
                    public void onResponse(Call<LaporanRingkasan> call, Response<LaporanRingkasan> response) {
                        progressBar.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            tampilkanRingkasan(response.body().getData());
                        } else {
                            Toast.makeText(LaporanKeuanganActivity.this,
                                    "Gagal memuat ringkasan", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LaporanRingkasan> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(LaporanKeuanganActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void tampilkanRingkasan(LaporanRingkasan.Data d) {
        tvDanaBeredar.setText(fmtRupiah.format(d.getDanaBeredar()));
        tvPenerimaan.setText(fmtRupiah.format(d.getPenerimaanBulanIni()));
        tvProposalMenunggu.setText(String.valueOf(d.getProposalMenunggu()));

        int kreditLancar = Math.max(d.getKreditAktif() - d.getKreditMacet(), 0);
        int totalKredit  = Math.max(d.getKreditAktif(), 1); // hindari div/0

        LinearLayout.LayoutParams lpLancar = (LinearLayout.LayoutParams) barPortofolioLancar.getLayoutParams();
        LinearLayout.LayoutParams lpMacet  = (LinearLayout.LayoutParams) barPortofolioMacet.getLayoutParams();
        lpLancar.weight = kreditLancar;
        lpMacet.weight  = Math.max(d.getKreditMacet(), 0);
        // Kalau semua nol (belum ada kredit sama sekali), tampilkan bar penuh abu-abu netral
        if (kreditLancar == 0 && d.getKreditMacet() == 0) { lpLancar.weight = 1; lpMacet.weight = 0; }
        barPortofolioLancar.setLayoutParams(lpLancar);
        barPortofolioMacet.setLayoutParams(lpMacet);

        tvKreditLancar.setText(kreditLancar + " Lancar");
        tvKreditMacet.setText(d.getKreditMacet() + " Macet");

        if (d.getTagihanJatuhTempo() > 0) {
            bannerTunggakan.setVisibility(View.VISIBLE);
            tvTunggakanAlert.setText(d.getTagihanJatuhTempo() + " tagihan jatuh tempo bulan ini ("
                    + fmtRupiah.format(d.getNominalJatuhTempo()) + ")");
        } else {
            bannerTunggakan.setVisibility(View.GONE);
        }
    }

    private void loadTunggakan() {
        recyclerTunggakan.setVisibility(View.VISIBLE);
        tvEmptyTunggakan.setVisibility(View.GONE);

        ApiClient.getService()
                .getLaporanTunggakan(session.getBearerToken(), "tunggakan")
                .enqueue(new Callback<ApiResponse<TunggakanData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<TunggakanData>> call,
                                           Response<ApiResponse<TunggakanData>> response) {
                        tunggakanSudahDimuat = true;

                        TunggakanData data = (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) ? response.body().getData() : null;

                        List<com.amikom.bumdesma.model.TunggakanItem> list =
                                (data != null && data.getList() != null) ? data.getList() : new ArrayList<>();

                        if (list.isEmpty()) {
                            recyclerTunggakan.setVisibility(View.GONE);
                            tvEmptyTunggakan.setVisibility(View.VISIBLE);
                            tvTotalTunggakan.setText("Total tunggakan: " + fmtRupiah.format(0));
                        } else {
                            tvTotalTunggakan.setText(list.size() + " tunggakan • Total: "
                                    + fmtRupiah.format(data.getTotalNominal()));
                            recyclerTunggakan.setAdapter(new TunggakanAdapter(list));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<TunggakanData>> call, Throwable t) {
                        recyclerTunggakan.setVisibility(View.GONE);
                        tvEmptyTunggakan.setVisibility(View.VISIBLE);
                        Toast.makeText(LaporanKeuanganActivity.this,
                                "Gagal memuat tunggakan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadRekapBulanan() {
        recyclerBulanan.setVisibility(View.VISIBLE);
        tvEmptyBulanan.setVisibility(View.GONE);

        ApiClient.getService()
                .getLaporanBulanan(session.getBearerToken(), "bulanan", getBulanParam())
                .enqueue(new Callback<ApiResponse<RekapBulananData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<RekapBulananData>> call,
                                           Response<ApiResponse<RekapBulananData>> response) {
                        RekapBulananData data = (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) ? response.body().getData() : null;

                        rekapDataAktif = data;

                        if (data == null) {
                            tvTotalPokok.setText("—"); tvTotalBunga.setText("—"); tvTotalBayar.setText("—");
                            recyclerBulanan.setVisibility(View.GONE);
                            tvEmptyBulanan.setVisibility(View.VISIBLE);
                            return;
                        }

                        tvTotalPokok.setText(fmtRupiah.format(data.getTotalPokok()));
                        tvTotalBunga.setText(fmtRupiah.format(data.getTotalBunga()));
                        tvTotalBayar.setText(fmtRupiah.format(data.getTotalBayar()));

                        List<com.amikom.bumdesma.model.RekapBulananItem> list =
                                data.getDetail() != null ? data.getDetail() : new ArrayList<>();

                        if (list.isEmpty()) {
                            recyclerBulanan.setVisibility(View.GONE);
                            tvEmptyBulanan.setVisibility(View.VISIBLE);
                        } else {
                            recyclerBulanan.setAdapter(new RekapBulananAdapter(list));
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<RekapBulananData>> call, Throwable t) {
                        recyclerBulanan.setVisibility(View.GONE);
                        tvEmptyBulanan.setVisibility(View.VISIBLE);
                        Toast.makeText(LaporanKeuanganActivity.this,
                                "Gagal memuat rekap: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_laporan_keuangan, menu);
        menuExportPdf = menu.findItem(R.id.action_export_pdf);
        menuExportPdf.setVisible(tabAktif == Tab.BULANAN);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        if (item.getItemId() == R.id.action_export_pdf) { exportRekapPdf(); return true; }
        return super.onOptionsItemSelected(item);
    }

    private void exportRekapPdf() {
        if (rekapDataAktif == null || rekapDataAktif.getDetail() == null
                || rekapDataAktif.getDetail().isEmpty()) {
            Toast.makeText(this, "Tidak ada data bulan ini untuk diekspor", Toast.LENGTH_SHORT).show();
            return;
        }

        String periodeLabel = tvPeriodeBulan.getText().toString();

        try {
            File file = RekapPdfExporter.export(
                    this, periodeLabel,
                    rekapDataAktif.getTotalPokok(),
                    rekapDataAktif.getTotalBunga(),
                    rekapDataAktif.getTotalBayar(),
                    rekapDataAktif.getDetail());

            Uri uri = FileProvider.getUriForFile(
                    this, BuildConfig.APPLICATION_ID + ".fileprovider", file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                // Kalau tidak ada aplikasi pembuka PDF, tawarkan share saja
                Intent share = new Intent(Intent.ACTION_SEND);
                share.setType("application/pdf");
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(Intent.createChooser(share, "Bagikan PDF"));
            }
        } catch (IOException e) {
            Toast.makeText(this, "Gagal membuat PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}