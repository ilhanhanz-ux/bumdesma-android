package com.amikom.bumdesma.ui.anggota;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.SubmitBuktiPorsiRequest;
import com.amikom.bumdesma.utils.Constants;
import com.amikom.bumdesma.utils.SessionManager;
import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadBuktiPorsiActivity extends AppCompatActivity {

    private TextView tvJudul, tvJatuhTempo, tvNominal, tvStatus, tvCatatanAdmin;
    private ImageView ivPreview;
    private Button btnKamera, btnGaleri, btnKirim;
    private ProgressBar progressBar;

    private SessionManager session;
    private int porsiId;
    private boolean bisaUpload;
    private String buktiBase64 = null;
    private Uri fotoUriSementara; // hasil jepretan kamera

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> permissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_bukti_porsi);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Bukti Setor Porsi");
        }

        tvJudul        = findViewById(R.id.tv_judul_angsuran);
        tvJatuhTempo   = findViewById(R.id.tv_jatuh_tempo);
        tvNominal      = findViewById(R.id.tv_nominal_porsi);
        tvStatus       = findViewById(R.id.tv_status_badge);
        tvCatatanAdmin = findViewById(R.id.tv_catatan_admin);
        ivPreview      = findViewById(R.id.iv_preview);
        btnKamera      = findViewById(R.id.btn_kamera);
        btnGaleri      = findViewById(R.id.btn_galeri);
        btnKirim       = findViewById(R.id.btn_kirim);
        progressBar    = findViewById(R.id.progress_bar);

        setupLaunchers();
        ambilDataDariIntent();

        btnKamera.setOnClickListener(v -> bukaKamera());
        btnGaleri.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnKirim.setOnClickListener(v -> kirimBukti());
    }

    private void ambilDataDariIntent() {
        porsiId = getIntent().getIntExtra("porsi_id", 0);
        int noAngsuran = getIntent().getIntExtra("no_angsuran", 0);
        String jatuhTempo = getIntent().getStringExtra("tanggal_jatuh_tempo");
        double jumlahPorsi = getIntent().getDoubleExtra("jumlah_porsi", 0);
        String status = getIntent().getStringExtra("status_bayar");
        String catatanAdmin = getIntent().getStringExtra("catatan_admin");
        String buktiLama = getIntent().getStringExtra("bukti_bayar");

        if (porsiId <= 0) {
            Toast.makeText(this, "Data porsi tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        tvJudul.setText("Angsuran ke-" + noAngsuran);
        tvJatuhTempo.setText("Jatuh tempo: " + formatTanggal(jatuhTempo));
        tvNominal.setText("Porsi Anda: " + formatRupiah(jumlahPorsi));

        bisaUpload = "belum_bayar".equals(status) || "ditolak".equals(status);

        switch (status == null ? "" : status) {
            case "sudah_bayar":
                tvStatus.setText("LUNAS");
                tvStatus.setBackgroundColor(0xFF2E7D32);
                break;
            case "menunggu_verifikasi":
                tvStatus.setText("MENUNGGU VERIFIKASI ADMIN");
                tvStatus.setBackgroundColor(0xFF00796B);
                break;
            case "ditolak":
                tvStatus.setText("DITOLAK, SILAKAN UPLOAD ULANG");
                tvStatus.setBackgroundColor(0xFFC62828);
                break;
            default:
                tvStatus.setText("BELUM BAYAR");
                tvStatus.setBackgroundColor(0xFFE65100);
                break;
        }

        if ("ditolak".equals(status) && catatanAdmin != null && !catatanAdmin.isEmpty()) {
            tvCatatanAdmin.setText("Alasan ditolak: " + catatanAdmin);
            tvCatatanAdmin.setVisibility(View.VISIBLE);
        } else {
            tvCatatanAdmin.setVisibility(View.GONE);
        }

        // Kalau sudah ada bukti sebelumnya (mis. menunggu_verifikasi, sudah_bayar, atau ditolak), tampilkan sebagai preview.
        if (buktiLama != null && !buktiLama.isEmpty()) {
            Glide.with(this)
                    .load(Constants.buildFileUrl(buktiLama))
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(ivPreview);
        }

        // Kalau tidak boleh upload (sudah_bayar / menunggu_verifikasi), sembunyikan kontrol upload.
        btnKamera.setEnabled(bisaUpload);
        btnGaleri.setEnabled(bisaUpload);
        btnKirim.setEnabled(false); // baru aktif setelah pilih foto baru
        if (!bisaUpload) {
            btnKamera.setVisibility(View.GONE);
            btnGaleri.setVisibility(View.GONE);
            btnKirim.setVisibility(View.GONE);
        }
    }

    private void setupLaunchers() {
        cameraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), sukses -> {
            if (sukses && fotoUriSementara != null) {
                prosesGambarTerpilih(fotoUriSementara);
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) prosesGambarTerpilih(uri);
        });

        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) lanjutkanBukaKamera();
            else Toast.makeText(this, "Izin kamera dibutuhkan untuk mengambil foto", Toast.LENGTH_SHORT).show();
        });
    }

    private void bukaKamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(android.Manifest.permission.CAMERA);
        } else {
            lanjutkanBukaKamera();
        }
    }

    private void lanjutkanBukaKamera() {
        try {
            File fotoFile = File.createTempFile("bukti_porsi_", ".jpg", getCacheDir());
            // Ganti "com.amikom.bumdesma.fileprovider" kalau authority provider di
            // AndroidManifest.xml Anda beda (biasanya sudah dipakai juga di SetoranAngsuranActivity).
            fotoUriSementara = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", fotoFile);
            cameraLauncher.launch(fotoUriSementara);
        } catch (IOException e) {
            Toast.makeText(this, "Gagal menyiapkan kamera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void prosesGambarTerpilih(Uri uri) {
        try {
            InputStream is = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            if (is != null) is.close();
            if (bitmap == null) {
                Toast.makeText(this, "Gagal membaca gambar", Toast.LENGTH_SHORT).show();
                return;
            }

            ivPreview.setImageBitmap(bitmap);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
            buktiBase64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);

            btnKirim.setEnabled(true);
        } catch (IOException e) {
            Toast.makeText(this, "Gagal memproses gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void kirimBukti() {
        if (buktiBase64 == null) {
            Toast.makeText(this, "Pilih atau ambil foto bukti setor dulu", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnKirim.setEnabled(false);

        SubmitBuktiPorsiRequest request = new SubmitBuktiPorsiRequest(porsiId, buktiBase64);

        ApiClient.getService()
                .submitBuktiPorsi(session.getBearerToken(), request)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> resp) {
                        progressBar.setVisibility(View.GONE);
                        if (resp.isSuccessful() && resp.body() != null && resp.body().isSuccess()) {
                            Toast.makeText(UploadBuktiPorsiActivity.this,
                                    "Bukti setor berhasil dikirim, menunggu verifikasi admin", Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            btnKirim.setEnabled(true);
                            String pesan = resp.body() != null ? resp.body().getMessage() : "Gagal mengirim bukti";
                            Toast.makeText(UploadBuktiPorsiActivity.this, "Gagal: " + pesan, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        btnKirim.setEnabled(true);
                        Toast.makeText(UploadBuktiPorsiActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String formatRupiah(double nilai) {
        long rounded = Math.round(nilai);
        String s = String.valueOf(rounded);
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            sb.insert(0, s.charAt(i));
            count++;
            if (count % 3 == 0 && i != 0) sb.insert(0, '.');
        }
        return "Rp " + sb;
    }

    private String formatTanggal(String tanggalSql) {
        if (tanggalSql == null || tanggalSql.isEmpty()) return "-";
        try {
            SimpleDateFormat input  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            return output.format(input.parse(tanggalSql));
        } catch (ParseException e) {
            return tanggalSql;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}