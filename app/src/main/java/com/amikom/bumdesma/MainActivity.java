package com.amikom.bumdesma;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.amikom.bumdesma.ui.LoginAnggotaActivity;
import com.amikom.bumdesma.ui.admin.DashboardAdminActivity;
import com.amikom.bumdesma.ui.anggota.DashboardAnggotaActivity;
import com.amikom.bumdesma.utils.Constants;
import com.amikom.bumdesma.utils.SessionManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SessionManager session = new SessionManager(this);
        Intent intent;

        if (session.isLoggedIn()) {
            // Sudah login sebelumnya → langsung ke dashboard
            if (Constants.ROLE_ADMIN.equals(session.getRole())) {
                intent = new Intent(this, DashboardAdminActivity.class);
            } else {
                intent = new Intent(this, DashboardAnggotaActivity.class);
            }
        } else {
            // Belum login → langsung ke halaman login anggota.
            // Admin masuk lewat tap rahasia di logo pada halaman ini
            // (lihat LoginAnggotaActivity.handleLogoTap()).
            intent = new Intent(this, LoginAnggotaActivity.class);
        }

        // Pindah halaman dan tutup MainActivity
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}