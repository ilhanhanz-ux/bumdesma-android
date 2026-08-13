package com.amikom.bumdesma.ui.admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.amikom.bumdesma.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.amikom.bumdesma.utils.BottomNavHelper;

public class PengelolaanAdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengelolaan_admin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Pengelolaan");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            toolbar.setNavigationOnClickListener(v -> finish());
        }


        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavHelper.setupAdminNav(this, bottomNav, R.id.nav_admin_pengelolaan);

        CardView cardProposal            = findViewById(R.id.card_proposal);
        CardView cardAnggota             = findViewById(R.id.card_anggota);
        CardView cardVerifikasiAnggota   = findViewById(R.id.card_verifikasi_anggota);
        CardView cardAngsuran            = findViewById(R.id.card_angsuran);
        CardView cardLaporan             = findViewById(R.id.card_laporan);
        CardView cardRiwayat             = findViewById(R.id.card_riwayat);
        CardView cardPengumuman          = findViewById(R.id.card_pengumuman);

        cardProposal.setOnClickListener(v ->
                startActivity(new Intent(this, ProposalListAdminActivity.class)));
        cardAnggota.setOnClickListener(v ->
                startActivity(new Intent(this, DataAnggotaActivity.class)));
        cardVerifikasiAnggota.setOnClickListener(v ->
                startActivity(new Intent(this, VerifikasiAnggotaListActivity.class)));
        cardAngsuran.setOnClickListener(v ->
                startActivity(new Intent(this, AngsuranListActivity.class)));
        cardLaporan.setOnClickListener(v ->
                startActivity(new Intent(this, LaporanKeuanganActivity.class)));
        cardRiwayat.setOnClickListener(v ->
                startActivity(new Intent(this, RiwayatAktifitasActivity.class)));
        cardPengumuman.setOnClickListener(v ->
                startActivity(new Intent(this, PengumumanAdminActivity.class)));
    }
}