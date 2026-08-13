package com.amikom.bumdesma.ui.anggota;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.AnggotaProfil;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.ui.LoginActivity;
import com.amikom.bumdesma.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfilSayaActivity extends AppCompatActivity {

    private static final String PREF_FOTO = "profil_foto_prefs";
    private static final String KEY_FOTO_PATH = "foto_path";
    private static final String FOTO_FILE_NAME = "foto_profil_lokal.jpg";

    private SessionManager session;
    private SharedPreferences fotoPrefs;

    private ImageView ivFoto;
    private ProgressBar progressBar;
    private TextView tvNama, tvNik, tvTtl, tvJenisKelamin, tvNoTelepon,
            tvAlamat, tvKelompok, tvDesa, tvIdAnggota, tvStatusAktif;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    simpanFotoLokal(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profil_saya);

        session   = new SessionManager(this);
        fotoPrefs = getSharedPreferences(PREF_FOTO, MODE_PRIVATE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profil Saya");
        }

        ivFoto         = findViewById(R.id.iv_foto_profil);
        progressBar    = findViewById(R.id.progress_profil);
        tvNama         = findViewById(R.id.tv_nama);
        tvNik          = findViewById(R.id.tv_nik);
        tvTtl          = findViewById(R.id.tv_ttl);
        tvJenisKelamin = findViewById(R.id.tv_jenis_kelamin);
        tvNoTelepon    = findViewById(R.id.tv_no_telepon);
        tvAlamat       = findViewById(R.id.tv_alamat);
        tvKelompok     = findViewById(R.id.tv_kelompok);
        tvDesa         = findViewById(R.id.tv_desa);
        tvIdAnggota    = findViewById(R.id.tv_id_anggota);
        tvStatusAktif  = findViewById(R.id.tv_status_aktif);

        TextView tvUbahFoto = findViewById(R.id.tv_ubah_foto);

        tvNama.setText(session.getNama());
        tvIdAnggota.setText(String.valueOf(session.getAnggotaId()));

        ivFoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        tvUbahFoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        // ── Logout ───────────────────────────────────────
        CardView btnKeluar = findViewById(R.id.btn_keluar);
        btnKeluar.setOnClickListener(v -> showLogoutDialog());

        tampilkanFotoTersimpan();
        loadProfilLengkap();
        findViewById(R.id.menuPanduanAplikasi).setOnClickListener(v ->
                startActivity(new Intent(this, PanduanActivity.class))
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        tampilkanFotoTersimpan();
    }

    private void loadProfilLengkap() {
        progressBar.setVisibility(View.VISIBLE);

        ApiClient.getService().getProfilSaya(session.getBearerToken())
                .enqueue(new Callback<ApiResponse<AnggotaProfil>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AnggotaProfil>> call,
                                           Response<ApiResponse<AnggotaProfil>> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            tampilkanProfil(response.body().getData());
                        } else {
                            Toast.makeText(ProfilSayaActivity.this,
                                    "Gagal memuat detail profil", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AnggotaProfil>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(ProfilSayaActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void tampilkanProfil(AnggotaProfil p) {
        tvNama.setText(isi(p.getNamaLengkap(), session.getNama()));
        tvNik.setText(isi(p.getNik(), "-"));
        tvTtl.setText(gabungTempatTanggal(p.getTempatLahir(), p.getTanggalLahir()));
        tvJenisKelamin.setText(isi(p.getJenisKelamin(), "-"));
        tvNoTelepon.setText(isi(p.getNoTelepon(), "-"));
        tvAlamat.setText(isi(p.getAlamat(), "-"));
        tvKelompok.setText(isi(p.getNamaKelompok(), "-"));
        tvDesa.setText(isi(p.getNamaDesa(), "-"));

        if (p.isStatusAktif()) {
            tvStatusAktif.setText("Aktif");
            tvStatusAktif.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.badge_disetujui_bg));
            tvStatusAktif.setTextColor(
                    ContextCompat.getColor(this, R.color.status_disetujui));
        } else {
            tvStatusAktif.setText("Nonaktif");
            tvStatusAktif.setBackgroundColor(
                    ContextCompat.getColor(this, R.color.badge_ditolak_bg));
            tvStatusAktif.setTextColor(
                    ContextCompat.getColor(this, R.color.status_ditolak));
        }
    }

    /** Tampilkan "-" kalau data kosong/null, biar tidak terlihat blank/rusak */
    private String isi(String value, String fallback) {
        return (value != null && !value.trim().isEmpty()) ? value.trim() : fallback;
    }

    /** Gabungkan tempat & tanggal lahir tanpa koma nyantol kalau tempat kosong */
    private String gabungTempatTanggal(String tempatLahir, String tanggalLahirSql) {
        String tgl = formatTanggal(tanggalLahirSql);
        boolean adaTempat = tempatLahir != null && !tempatLahir.trim().isEmpty();
        return adaTempat ? (tempatLahir.trim() + ", " + tgl) : tgl;
    }

    private String formatTanggal(String tanggalSql) {
        if (tanggalSql == null || tanggalSql.trim().isEmpty()) return "-";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("d MMMM yyyy", new Locale("id", "ID"));
            Date date = in.parse(tanggalSql);
            return date != null ? out.format(date) : tanggalSql;
        } catch (ParseException e) {
            return tanggalSql;
        }
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
                }
            }
        }
    }

    /** Set foto asli dengan crop bundar sempurna (bukan cuma tumpuk background bulat) */
    private void tampilkanBitmapBulat(Bitmap source) {
        int ukuran = 300; // px, cukup tajam untuk ukuran tampilan 92dp
        Bitmap bulat = buatBitmapBulat(source, ukuran);
        ivFoto.setPadding(0, 0, 0, 0);
        ivFoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ivFoto.setImageBitmap(bulat);
    }

    private Bitmap buatBitmapBulat(Bitmap source, int diameterPx) {
        int ukuranSisi = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - ukuranSisi) / 2;
        int y = (source.getHeight() - ukuranSisi) / 2;
        Bitmap persegi = Bitmap.createBitmap(source, x, y, ukuranSisi, ukuranSisi);
        Bitmap discale = Bitmap.createScaledBitmap(persegi, diameterPx, diameterPx, true);

        Bitmap output = Bitmap.createBitmap(diameterPx, diameterPx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new BitmapShader(discale, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        float radius = diameterPx / 2f;
        canvas.drawCircle(radius, radius, radius, paint);
        return output;
    }

    private void showLogoutDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Keluar")
                .setMessage("Yakin ingin keluar dari akun?")
                .setPositiveButton("Keluar", (d, w) -> {
                    session.logout();
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                            Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .setNegativeButton("Batal", null)
                .show();

        // Tombol default AlertDialog ikut warna colorPrimary (biru); dipaksa putih
        // di sini biar tetap kebaca jelas di atas background dialog yang gelap.
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}