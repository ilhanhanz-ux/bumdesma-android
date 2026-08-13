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
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.Angsuran;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.SetoranKolektifRequest;
import com.amikom.bumdesma.model.SetoranKolektifResult;
import com.amikom.bumdesma.utils.SessionManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SetoranKolektifActivity extends AppCompatActivity {

    private AutoCompleteTextView spinnerKelompok; // BARU: dropdown Material, bukan Spinner
    private Button btnMuatTagihan;
    private TextView tvRingkasanPilihan, tvPilihSemua, tvKosong;
    private RecyclerView rvDaftarTagihan;
    private ProgressBar progressTagihan;

    private EditText etNamaPenyetor, etKeterangan;
    private ImageView imageViewPreview;
    private Button btnAmbilFoto, btnPilihGaleri, btnSimpanSetoran;

    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private final List<String> daftarNamaKelompok = new ArrayList<>();
    private TagihanKelompokAdapter adapter;
    private boolean sedangPilihSemua = false;

    private Bitmap selectedBitmap;
    private Uri cameraImageUri;

    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> galleryLauncher;
    private ActivityResultLauncher<String> cameraPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setoran_kolektif);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Setoran Kolektif");
        }

        spinnerKelompok    = findViewById(R.id.spinner_kelompok);
        btnMuatTagihan     = findViewById(R.id.btn_muat_tagihan);
        tvRingkasanPilihan = findViewById(R.id.tv_ringkasan_pilihan);
        tvPilihSemua       = findViewById(R.id.tv_pilih_semua);
        tvKosong           = findViewById(R.id.tv_kosong);
        rvDaftarTagihan    = findViewById(R.id.rv_daftar_tagihan);
        progressTagihan    = findViewById(R.id.progress_tagihan);

        etNamaPenyetor   = findViewById(R.id.et_nama_penyetor);
        etKeterangan     = findViewById(R.id.et_keterangan);
        imageViewPreview = findViewById(R.id.image_view_preview);
        btnAmbilFoto     = findViewById(R.id.btn_ambil_foto);
        btnPilihGaleri   = findViewById(R.id.btn_pilih_galeri);
        btnSimpanSetoran = findViewById(R.id.btn_simpan_setoran);

        rvDaftarTagihan.setLayoutManager(new LinearLayoutManager(this));
        // BARU: matikan nested-scroll RecyclerView, biar scroll-nya nyatu
        // sama NestedScrollView pembungkus (sama pola dengan DetailAnggotaActivity)
        rvDaftarTagihan.setNestedScrollingEnabled(false);

        setupLaunchers();
        muatDaftarKelompok();

        btnMuatTagihan.setOnClickListener(v -> muatTagihanKelompokTerpilih());
        tvPilihSemua.setOnClickListener(v -> togglePilihSemua());
        btnAmbilFoto.setOnClickListener(v -> checkCameraPermissionThenOpen());
        btnPilihGaleri.setOnClickListener(v -> galleryLauncher.launch("image/*"));
        btnSimpanSetoran.setOnClickListener(v -> kirimSetoranKolektif());

        updateRingkasan(0, 0);
    }

    private void muatDaftarKelompok() {
        String token = new SessionManager(this).getBearerToken();
        ApiClient.getService().getKelompokList(token)
                .enqueue(new Callback<ApiResponse<List<Map<String, Object>>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Map<String, Object>>>> call,
                                           Response<ApiResponse<List<Map<String, Object>>>> response) {
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            daftarNamaKelompok.clear();
                            for (Map<String, Object> row : response.body().getData()) {
                                Object nama = row.get("nama_kelompok");
                                if (nama != null) daftarNamaKelompok.add(String.valueOf(nama));
                            }
                            // FIX DARK/LIGHT MODE: sebelumnya pakai android.R.layout.simple_list_item_1
                            // (layout bawaan Android, warna teks tidak adaptif) sehingga teks item
                            // dropdown nyaris tak terlihat di atas admin_card_bg yang gelap.
                            // Sekarang pakai item_dropdown_kelompok.xml yang textColor-nya
                            // @color/admin_text_primary (adaptif values/ vs values-night/).
                            ArrayAdapter<String> dropdownAdapter = new ArrayAdapter<>(
                                    SetoranKolektifActivity.this,
                                    R.layout.item_dropdown_kelompok, daftarNamaKelompok);
                            spinnerKelompok.setAdapter(dropdownAdapter);
                        } else {
                            Toast.makeText(SetoranKolektifActivity.this,
                                    "Gagal memuat daftar kelompok", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Map<String, Object>>>> call, Throwable t) {
                        Toast.makeText(SetoranKolektifActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /** BARU: helper baca nilai kelompok yang sedang terpilih di dropdown (ganti getSelectedItem() punya Spinner) */
    private String getKelompokTerpilih() {
        return spinnerKelompok.getText() != null ? spinnerKelompok.getText().toString().trim() : "";
    }

    private void muatTagihanKelompokTerpilih() {
        String namaKelompok = getKelompokTerpilih();
        if (namaKelompok.isEmpty()) {
            Toast.makeText(this, "Pilih kelompok dulu", Toast.LENGTH_SHORT).show();
            return;
        }

        progressTagihan.setVisibility(View.VISIBLE);
        rvDaftarTagihan.setVisibility(View.GONE);
        tvKosong.setVisibility(View.GONE);

        String token = new SessionManager(this).getBearerToken();
        ApiClient.getService().getTagihanByKelompok(token, namaKelompok, "belum_lunas")
                .enqueue(new Callback<ApiResponse<List<Angsuran>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<List<Angsuran>>> call,
                                           Response<ApiResponse<List<Angsuran>>> response) {
                        progressTagihan.setVisibility(View.GONE);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess() && response.body().getData() != null) {
                            tampilkanDaftarTagihan(response.body().getData());
                        } else {
                            Toast.makeText(SetoranKolektifActivity.this,
                                    "Gagal memuat tagihan", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<List<Angsuran>>> call, Throwable t) {
                        progressTagihan.setVisibility(View.GONE);
                        Toast.makeText(SetoranKolektifActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void tampilkanDaftarTagihan(List<Angsuran> daftar) {
        boolean kosong = daftar == null || daftar.isEmpty();
        tvKosong.setVisibility(kosong ? View.VISIBLE : View.GONE);
        rvDaftarTagihan.setVisibility(kosong ? View.GONE : View.VISIBLE);

        if (kosong) {
            tvKosong.setText("Tidak ada tagihan yang belum lunas untuk kelompok ini");
            updateRingkasan(0, 0);
            return;
        }

        adapter = new TagihanKelompokAdapter(daftar, this::updateRingkasan);
        rvDaftarTagihan.setAdapter(adapter);
        updateRingkasan(0, 0);
    }

    private void togglePilihSemua() {
        if (adapter == null) return;
        sedangPilihSemua = !sedangPilihSemua;
        if (sedangPilihSemua) {
            adapter.pilihSemua();
            tvPilihSemua.setText("Batalkan Semua");
        } else {
            adapter.batalkanSemua();
            tvPilihSemua.setText("Pilih Semua");
        }
    }

    private void updateRingkasan(int jumlah, double total) {
        tvRingkasanPilihan.setText(jumlah + " dipilih  •  Total " + fmt.format(total));
    }

    private void setupLaunchers() {
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

        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedBitmap = uriToBitmap(uri);
                        imageViewPreview.setImageBitmap(selectedBitmap);
                    }
                }
        );

        cameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if (granted) openCamera();
                    else Toast.makeText(this, "Izin kamera diperlukan untuk ambil foto bukti setor", Toast.LENGTH_SHORT).show();
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
                    "setoran_" + System.currentTimeMillis(), ".jpg", getCacheDir());
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
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP);
    }

    private void kirimSetoranKolektif() {
        if (adapter == null || adapter.getSelectedIds().isEmpty()) {
            Toast.makeText(this, "Pilih minimal 1 tagihan angsuran dulu", Toast.LENGTH_SHORT).show();
            return;
        }
        String namaPenyetor = etNamaPenyetor.getText().toString().trim();
        if (namaPenyetor.isEmpty()) {
            Toast.makeText(this, "Isi nama ketua/penyetor dulu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedBitmap == null) {
            Toast.makeText(this, "Pilih atau ambil foto bukti setor dulu", Toast.LENGTH_SHORT).show();
            return;
        }
        String namaKelompok = getKelompokTerpilih();
        if (namaKelompok.isEmpty()) {
            Toast.makeText(this, "Pilih kelompok dulu", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSimpanSetoran.setEnabled(false);

        String base64Image   = encodeBitmapToBase64(selectedBitmap);
        String tanggalSetor  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String keterangan    = etKeterangan.getText().toString().trim();
        List<Integer> idTerpilih = adapter.getSelectedIds();

        SetoranKolektifRequest request = new SetoranKolektifRequest(
                namaKelompok, namaPenyetor, tanggalSetor, keterangan, base64Image, idTerpilih);

        String token = new SessionManager(this).getBearerToken();
        ApiClient.getService().catatSetoranKolektif(token, request)
                .enqueue(new Callback<ApiResponse<SetoranKolektifResult>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<SetoranKolektifResult>> call,
                                           Response<ApiResponse<SetoranKolektifResult>> response) {
                        btnSimpanSetoran.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            Toast.makeText(SetoranKolektifActivity.this,
                                    "Setoran kolektif berhasil dicatat", Toast.LENGTH_LONG).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            String msg = response.body() != null
                                    ? response.body().getMessage() : "Gagal menyimpan";
                            Toast.makeText(SetoranKolektifActivity.this,
                                    "Gagal: " + msg, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<SetoranKolektifResult>> call, Throwable t) {
                        btnSimpanSetoran.setEnabled(true);
                        Toast.makeText(SetoranKolektifActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}