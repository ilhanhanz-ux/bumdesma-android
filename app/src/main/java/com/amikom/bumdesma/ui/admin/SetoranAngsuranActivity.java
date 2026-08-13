package com.amikom.bumdesma.ui.admin;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.amikom.bumdesma.model.SetoranRequest;
import com.amikom.bumdesma.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetoranAngsuranActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TextView textNamaLengkap, textNoAngsuran, textTotalBayar, textJatuhTempo;
    private ImageView imageViewPreview;
    private EditText editTextKeterangan;
    private Button buttonAmbilFoto, buttonPilihGaleri, buttonSimpan;

    private Bitmap selectedBitmap;
    private Uri cameraImageUri; // Uri sementara buat hasil jepretan kamera
    private int angsuranId;

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setoran_angsuran);

        angsuranId = getIntent().getIntExtra("angsuran_id", 0);
        if (angsuranId <= 0) {
            Toast.makeText(this, "angsuran_id tidak valid", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        toolbar             = findViewById(R.id.toolbar);
        textNamaLengkap    = findViewById(R.id.textNamaLengkap);
        textNoAngsuran     = findViewById(R.id.textNoAngsuran);
        textTotalBayar     = findViewById(R.id.textTotalBayar);
        textJatuhTempo     = findViewById(R.id.textJatuhTempo);
        imageViewPreview   = findViewById(R.id.imageViewPreview);
        editTextKeterangan = findViewById(R.id.editTextKeterangan);
        buttonAmbilFoto    = findViewById(R.id.buttonAmbilFoto);
        buttonPilihGaleri  = findViewById(R.id.buttonPilihGaleri);
        buttonSimpan       = findViewById(R.id.buttonSimpan);

        toolbar.setNavigationOnClickListener(v -> finish());

        isiKartuDetail();
        setupLaunchers();

        buttonAmbilFoto.setOnClickListener(v -> checkCameraPermissionThenOpen());
        buttonPilihGaleri.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        buttonSimpan.setOnClickListener(v -> kirimSetoranAngsuran());
    }

    private void isiKartuDetail() {
        String nama = getIntent().getStringExtra("nama_lengkap");
        int noAngsuran = getIntent().getIntExtra("no_angsuran", 0);
        double totalBayar = getIntent().getDoubleExtra("total_bayar", 0);
        String jatuhTempo = getIntent().getStringExtra("tanggal_jatuh_tempo");

        textNamaLengkap.setText(nama != null ? nama : "-");
        textNoAngsuran.setText("Angsuran ke-" + noAngsuran);

        NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
        rupiah.setMaximumFractionDigits(0);
        textTotalBayar.setText(rupiah.format(totalBayar));

        textJatuhTempo.setText("Jatuh tempo: " + (jatuhTempo != null ? jatuhTempo : "-"));
    }

    private void setupLaunchers() {
        // Hasil ambil foto dari kamera (true kalau sukses disimpan ke cameraImageUri)
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && cameraImageUri != null) {
                        selectedBitmap = uriToBitmap(cameraImageUri);
                        imageViewPreview.setImageBitmap(selectedBitmap);
                    } else {
                        Toast.makeText(this, "Gagal mengambil foto", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Hasil pilih foto dari galeri
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedBitmap = uriToBitmap(uri);
                        imageViewPreview.setImageBitmap(selectedBitmap);
                    }
                }
        );

        // Hasil minta izin kamera
        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) {
                        openCamera();
                    } else {
                        Toast.makeText(this, "Izin kamera diperlukan untuk ambil foto bukti transfer", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void checkCameraPermissionThenOpen() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void openCamera() {
        try {
            File photoFile = File.createTempFile(
                    "bukti_" + System.currentTimeMillis(), ".jpg", getCacheDir());

            // Authority ini HARUS sama persis dengan yang didaftarkan di AndroidManifest.xml
            cameraImageUri = FileProvider.getUriForFile(
                    this, "com.amikom.bumdesma.fileprovider", photoFile);

            cameraLauncher.launch(cameraImageUri);
        } catch (IOException e) {
            Toast.makeText(this, "Gagal menyiapkan file foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap uriToBitmap(Uri uri) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(), uri);
                return ImageDecoder.decodeBitmap(source);
            } else {
                return MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            }
        } catch (IOException e) {
            Toast.makeText(this, "Gagal memuat foto: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private String encodeBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Kompres wajib — foto asli kamera bisa 3-5 MB, terlalu besar buat dikirim langsung
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        byte[] imageBytes = baos.toByteArray();
        return Base64.encodeToString(imageBytes, Base64.NO_WRAP);
    }

    private void kirimSetoranAngsuran() {
        if (selectedBitmap == null) {
            Toast.makeText(this, "Pilih atau ambil foto bukti transfer dulu", Toast.LENGTH_SHORT).show();
            return;
        }

        buttonSimpan.setEnabled(false); // cegah double-submit

        String base64Image = encodeBitmapToBase64(selectedBitmap);
        String tanggalBayar = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String keterangan = editTextKeterangan.getText().toString().trim();

        SetoranRequest request = new SetoranRequest(angsuranId, tanggalBayar, keterangan, base64Image);
        String token = new SessionManager(this).getBearerToken();

        ApiClient.getService().setoranAngsuran(token, request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                buttonSimpan.setEnabled(true);
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(SetoranAngsuranActivity.this, "Setoran berhasil dicatat", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String msg = response.body() != null ? response.body().getMessage() : "Gagal menyimpan";
                    Toast.makeText(SetoranAngsuranActivity.this, "Gagal: " + msg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                buttonSimpan.setEnabled(true);
                Toast.makeText(SetoranAngsuranActivity.this, "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}