package com.amikom.bumdesma.ui.admin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.ui.LoginActivity;
import com.amikom.bumdesma.ui.admin.PanduanAplikasiActivity;
import com.amikom.bumdesma.ui.anggota.UbahPasswordActivity;
import com.amikom.bumdesma.utils.ImageUtils;
import com.amikom.bumdesma.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilAdminActivity extends AppCompatActivity {

    private static final String PREF_FOTO = "admin_foto_prefs";
    private static final String KEY_FOTO_PATH = "foto_path_admin";
    private static final String FOTO_FILE_NAME = "foto_profil_admin_lokal.jpg";
    private static final int DIAMETER_AVATAR_PX = 300;

    private SessionManager session;
    private SharedPreferences fotoPrefs;

    private ImageView ivFoto;
    private ImageView ivAvatarInitial;
    private TextView tvNama;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) simpanFotoLokal(uri);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_admin);

        session   = new SessionManager(this);
        fotoPrefs = getSharedPreferences(PREF_FOTO, MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profil Admin");
        }

        ivFoto          = findViewById(R.id.iv_foto_profil);
        ivAvatarInitial = findViewById(R.id.tv_avatar_initial);
        tvNama          = findViewById(R.id.tv_nama);
        TextView tvUbahFoto = findViewById(R.id.tv_ubah_foto);
        ImageView ivEditNama = findViewById(R.id.iv_edit_nama);

        tvNama.setText(session.getNama());

        ivFoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        tvUbahFoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        ivEditNama.setOnClickListener(v -> showEditNamaDialog());

        // Ubah Password — pakai ulang activity & endpoint milik anggota (logikanya
        // generik untuk semua role, cukup verifikasi password lama via token login)
        LinearLayout itemUbahPassword = findViewById(R.id.item_ubah_password);
        itemUbahPassword.setOnClickListener(v ->
                startActivity(new Intent(this, UbahPasswordActivity.class)));

        // Panduan Aplikasi
        LinearLayout itemPanduanAplikasi = findViewById(R.id.item_panduan_aplikasi);
        itemPanduanAplikasi.setOnClickListener(v ->
                startActivity(new Intent(this, PanduanAplikasiActivity.class)));

        CardView btnKeluar = findViewById(R.id.btn_keluar);
        btnKeluar.setOnClickListener(v -> showLogoutDialog());

        tampilkanFotoTersimpan();
    }

    @Override
    protected void onResume() {
        super.onResume();
        tampilkanFotoTersimpan();
    }

    private void showEditNamaDialog() {
        final EditText input = new EditText(this);
        input.setText(session.getNama());
        input.setSelection(input.getText().length());
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);

        new AlertDialog.Builder(this)
                .setTitle("Ubah Nama")
                .setView(input)
                .setPositiveButton("Simpan", (d, w) -> {
                    String namaBaru = input.getText().toString().trim();
                    if (namaBaru.isEmpty()) {
                        Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    session.setNama(namaBaru);
                    tvNama.setText(namaBaru);
                    Toast.makeText(this, "Nama berhasil diperbarui", Toast.LENGTH_SHORT).show();
                    sinkronkanNamaKeServer(namaBaru);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void sinkronkanNamaKeServer(String namaBaru) {
        Map<String, String> body = new HashMap<>();
        body.put("nama", namaBaru);

        ApiClient.getService()
                .updateNamaAdmin(session.getBearerToken(), body)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        if (!response.isSuccessful() || response.body() == null
                                || !response.body().isSuccess()) {
                            Toast.makeText(ProfilAdminActivity.this,
                                    "Nama tersimpan di HP, tapi gagal sinkron ke server",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        Toast.makeText(ProfilAdminActivity.this,
                                "Nama tersimpan di HP, tapi gagal sinkron ke server: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void simpanFotoLokal(Uri uri) {
        try {
            InputStream in = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(in);
            if (in != null) in.close();
            if (bitmap == null) {
                Toast.makeText(this, "Gagal membaca gambar", Toast.LENGTH_SHORT).show();
                return;
            }

            File file = new File(getFilesDir(), FOTO_FILE_NAME);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.close();

            fotoPrefs.edit().putString(KEY_FOTO_PATH, file.getAbsolutePath()).apply();
            tampilkanBitmapBulat(bitmap);

            Toast.makeText(this, "Foto profil diperbarui", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Gagal menyimpan foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void tampilkanFotoTersimpan() {
        String path = fotoPrefs.getString(KEY_FOTO_PATH, null);
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(path);
                if (bitmap != null) {
                    tampilkanBitmapBulat(bitmap);
                    return;
                }
            }
        }
        ivFoto.setVisibility(View.GONE);
        ivAvatarInitial.setVisibility(View.VISIBLE);
    }

    private void tampilkanBitmapBulat(Bitmap source) {
        Bitmap bulat = ImageUtils.buatBitmapBulat(source, DIAMETER_AVATAR_PX);
        ivFoto.setImageBitmap(bulat);
        ivFoto.setVisibility(View.VISIBLE);
        ivAvatarInitial.setVisibility(View.GONE);
    }

    private void showLogoutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Keluar")
                .setMessage("Yakin ingin keluar dari akun admin?")
                .setPositiveButton("Keluar", (d, w) -> {
                    session.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Batal", null)
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}