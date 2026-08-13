package com.amikom.bumdesma.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.amikom.bumdesma.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BottomNavHelper {

    public static void setupAdminNav(Context context, BottomNavigationView navView, int selectedItemId) {
        navView.setSelectedItemId(selectedItemId);
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) return true;

            if (id == R.id.nav_admin_beranda) {
                navigateTo(context, com.amikom.bumdesma.ui.admin.DashboardAdminActivity.class);
            } else if (id == R.id.nav_admin_pengelolaan) {
                navigateTo(context, com.amikom.bumdesma.ui.admin.PengelolaanAdminActivity.class);
            } else if (id == R.id.nav_admin_notifikasi) {
                navigateTo(context, com.amikom.bumdesma.ui.admin.RiwayatAktifitasActivity.class);
            } else if (id == R.id.nav_admin_akun) {
                navigateTo(context, com.amikom.bumdesma.ui.admin.ProfilAdminActivity.class);
            }
            return true;
        });
    }

    public static void setupAnggotaNav(Context context, BottomNavigationView navView, int selectedItemId) {
        navView.setSelectedItemId(selectedItemId);
        navView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) return true;

            if (id == R.id.nav_anggota_beranda) {
                navigateTo(context, com.amikom.bumdesma.ui.anggota.DashboardAnggotaActivity.class);
            } else if (id == R.id.nav_anggota_aktivitas) {
                navigateTo(context, com.amikom.bumdesma.ui.anggota.AktivitasActivity.class);
            } else if (id == R.id.nav_anggota_notifikasi) {
                navigateTo(context, com.amikom.bumdesma.ui.anggota.RiwayatAktifitasAnggotaActivity.class);
            } else if (id == R.id.nav_anggota_akun) {
                navigateTo(context, com.amikom.bumdesma.ui.anggota.ProfilSayaActivity.class);
            }
            return true;
        });
    }

    private static void navigateTo(Context context, Class<? extends Activity> target) {
        Intent intent = new Intent(context, target);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
    }
}