package com.amikom.bumdesma.ui.anggota;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.amikom.bumdesma.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.amikom.bumdesma.utils.BottomNavHelper;
import com.amikom.bumdesma.utils.SessionManager;

public class AktivitasActivity extends AppCompatActivity {

    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aktivitas);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Aktivitas");
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        BottomNavHelper.setupAnggotaNav(this, bottomNav, R.id.nav_anggota_aktivitas);

        CardView cardAjukan  = findViewById(R.id.card_ajukan);
        CardView cardStatus  = findViewById(R.id.card_status_proposal);
        CardView cardJadwal  = findViewById(R.id.card_jadwal_angsuran);
        CardView cardRiwayat = findViewById(R.id.card_riwayat);

        cardAjukan.setOnClickListener(v ->
                startActivity(new Intent(this, AjukanProposalActivity.class)));
        cardStatus.setOnClickListener(v ->
                startActivity(new Intent(this, StatusProposalActivity.class)));
        // Sama seperti kartu "Jadwal Angsuran" di Dashboard: satu tujuan yang benar,
        // dipilih berdasarkan peran, biar datanya selalu porsi individu bukan total kelompok.
        cardJadwal.setOnClickListener(v -> {
            if (session.isKetua()) {
                startActivity(new Intent(this, KelolaPorsiActivity.class));
            } else {
                startActivity(new Intent(this, TagihanPorsiSayaActivity.class));
            }
        });
        cardRiwayat.setOnClickListener(v ->
                startActivity(new Intent(this, RiwayatPembayaranActivity.class)));
    }
}