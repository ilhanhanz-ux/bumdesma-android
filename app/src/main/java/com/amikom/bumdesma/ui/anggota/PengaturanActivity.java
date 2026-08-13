package com.amikom.bumdesma.ui.anggota;

import android.os.Bundle;
import android.content.Intent;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.amikom.bumdesma.BuildConfig;
import com.amikom.bumdesma.R;

public class PengaturanActivity extends AppCompatActivity {

    private static final String PREF_NOTIF = "pengaturan_prefs";
    private static final String KEY_NOTIF_AKTIF = "notifikasi_aktif";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pengaturan);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pengaturan");
        }

        // ── Ubah Password ────────────────────────────────
        LinearLayout itemUbahPassword = findViewById(R.id.item_ubah_password);
        itemUbahPassword.setOnClickListener(v ->
                startActivity(new Intent(this, UbahPasswordActivity.class)));

        // ── Notifikasi (disimpan lokal, belum terhubung ke push notification) ──
        Switch switchNotifikasi = findViewById(R.id.switch_notifikasi);
        var prefs = getSharedPreferences(PREF_NOTIF, MODE_PRIVATE);
        switchNotifikasi.setChecked(prefs.getBoolean(KEY_NOTIF_AKTIF, true));
        switchNotifikasi.setOnCheckedChangeListener((btn, isChecked) ->
                prefs.edit().putBoolean(KEY_NOTIF_AKTIF, isChecked).apply());

        // ── Tentang Aplikasi ─────────────────────────────
        LinearLayout itemTentang = findViewById(R.id.item_tentang_aplikasi);
        itemTentang.setOnClickListener(v -> tampilkanDialogTentang());
    }

    private void tampilkanDialogTentang() {
        String versi = BuildConfig.VERSION_NAME;
        new AlertDialog.Builder(this)
                .setTitle("Tentang Aplikasi")
                .setMessage("BUMDesma Mandiri Sejahtera\n" +
                        "Sistem Informasi Pencatatan Kredit\n" +
                        "Versi " + versi + "\n\n" +
                        "Dikembangkan untuk BUMDesma Randudongkal\n" +
                        "sebagai bagian dari program SPP.")
                .setPositiveButton("Tutup", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}