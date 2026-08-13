package com.amikom.bumdesma.ui;

import android.app.DatePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.ApiResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DaftarActivity extends AppCompatActivity {

    private EditText etNama, etUsername, etNik,
            etTelepon, etPassword, etKonfirmasi,
            etTempatLahir, etTanggalLahir, etAlamat;
    private Spinner  spinnerKelompok;
    private Button   btnDaftar;
    private ProgressBar progressBar;

    // ── BARU: view untuk upload foto KTP ──
    private ImageView ivPreviewKtp;
    private TextView  tvKtpPlaceholder;
    private Button    btnPilihKtp;
    private Uri       fotoKtpUri;   // URI hasil pilih dari galeri
    private File      fotoKtpFile;  // hasil salin ke file lokal, siap dikirim

    // Format buat dikirim ke API (harus sesuai kolom DATE di MySQL)
    private static final SimpleDateFormat FORMAT_API =
            new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    // Format buat ditampilkan ke user
    private static final SimpleDateFormat FORMAT_TAMPIL =
            new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));

    // Menyimpan tanggal lahir terpilih (null kalau belum dipilih)
    private Calendar tanggalLahirTerpilih = null;

    // Data kelompok sesuai posisi spinner — dipisah per field
    // supaya bisa dikirim langsung sesuai kolom tabel (nama_kelompok, nama_desa)
    private final List<String>  kelompokNama    = new ArrayList<>(); // teks tampilan (gabungan, buat spinner)
    private final List<String>  kelompokNamaSaja= new ArrayList<>(); // "Kelompok Mawar"
    private final List<String>  kelompokDesa    = new ArrayList<>(); // "Randudongkal"
    private final List<Integer> kelompokId      = new ArrayList<>(); // disimpan buat jaga-jaga

    // ── BARU: launcher buat buka galeri & terima hasil pilih gambar ──
    private final ActivityResultLauncher<String> pickKtpLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) return;
                try {
                    fotoKtpFile = salinUriKeFile(uri);
                    fotoKtpUri  = uri;
                    ivPreviewKtp.setImageURI(uri);
                    ivPreviewKtp.setVisibility(View.VISIBLE);
                    tvKtpPlaceholder.setVisibility(View.GONE);
                } catch (IOException e) {
                    Toast.makeText(this, "Gagal membaca foto, coba lagi",
                            Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_daftar);

        // Sambungkan view
        etNama          = findViewById(R.id.et_nama);
        etUsername      = findViewById(R.id.et_username_daftar);
        etNik           = findViewById(R.id.et_nik);
        etTelepon       = findViewById(R.id.et_telepon);
        etTempatLahir   = findViewById(R.id.et_tempat_lahir);
        etTanggalLahir  = findViewById(R.id.et_tanggal_lahir);
        etAlamat        = findViewById(R.id.et_alamat);
        etPassword      = findViewById(R.id.et_password_daftar);
        etKonfirmasi    = findViewById(R.id.et_konfirmasi);
        spinnerKelompok = findViewById(R.id.spinner_kelompok);
        btnDaftar       = findViewById(R.id.btn_daftar);
        progressBar     = findViewById(R.id.progress_bar_daftar);

        // ── BARU: sambungkan view foto KTP ──
        ivPreviewKtp     = findViewById(R.id.iv_preview_ktp);
        tvKtpPlaceholder = findViewById(R.id.tv_ktp_placeholder);
        btnPilihKtp      = findViewById(R.id.btn_pilih_ktp);
        btnPilihKtp.setOnClickListener(v -> pickKtpLauncher.launch("image/*"));

        // Tombol kembali
        findViewById(R.id.tv_kembali_login).setOnClickListener(
                v -> finish());

        // Klik field tanggal lahir -> buka DatePickerDialog
        etTanggalLahir.setOnClickListener(v -> bukaDatePicker());

        loadKelompok();
        btnDaftar.setOnClickListener(v -> daftar());
    }

    /** Tampilkan DatePickerDialog, batasi maksimal hari ini (nggak boleh lahir di masa depan) */
    private void bukaDatePicker() {
        Calendar awal = (tanggalLahirTerpilih != null)
                ? tanggalLahirTerpilih : Calendar.getInstance();

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar dipilih = Calendar.getInstance();
                    dipilih.set(year, month, dayOfMonth, 0, 0, 0);
                    tanggalLahirTerpilih = dipilih;
                    etTanggalLahir.setText(FORMAT_TAMPIL.format(dipilih.getTime()));
                },
                awal.get(Calendar.YEAR),
                awal.get(Calendar.MONTH),
                awal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dialog.show();
    }

    /**
     * BARU: salin isi content:// URI (hasil galeri) ke file lokal di cache dir.
     * Perlu karena MultipartBody.Part butuh File / stream nyata, bukan URI.
     */
    private File salinUriKeFile(Uri uri) throws IOException {
        InputStream input = getContentResolver().openInputStream(uri);
        if (input == null) throw new IOException("Tidak bisa membuka foto");

        File output = new File(getCacheDir(), "ktp_" + System.currentTimeMillis() + ".jpg");
        try (FileOutputStream fos = new FileOutputStream(output)) {
            byte[] buffer = new byte[4096];
            int panjang;
            while ((panjang = input.read(buffer)) != -1) {
                fos.write(buffer, 0, panjang);
            }
        }
        input.close();
        return output;
    }

    /**
     * PERBAIKAN (fix crash ClassCastException): konversi nilai "id" dari
     * response API ke int dengan aman. Backend PHP (mysqli) kadang membalas
     * angka sebagai String berkutip (mis. "id": "1"), bukan angka murni
     * ("id": 1). Gson lalu men-deserialize jadi tipe Java yang berbeda
     * tergantung itu — bisa Double (kalau angka murni) atau String (kalau
     * berkutip). Method ini menangani kedua kasus supaya tidak
     * ClassCastException lagi walau backend belum/tidak diperbaiki.
     */
    private int parseIdAman(Object raw) {
        if (raw == null) return 0;
        if (raw instanceof Number) {
            return ((Number) raw).intValue();
        }
        try {
            return (int) Double.parseDouble(String.valueOf(raw).trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Ambil daftar kelompok dari API untuk isi dropdown */
    private void loadKelompok() {
        // Isi dulu dengan data sementara supaya tidak kosong
        isiKelompokDefault();

        // Coba ambil dari API
        ApiClient.getService()
                .getKelompokList("")
                .enqueue(new Callback<ApiResponse<List<Map<String,Object>>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<List<Map<String,Object>>>> call,
                            Response<ApiResponse<List<Map<String,Object>>>> resp) {

                        if (resp.isSuccessful() && resp.body() != null
                                && resp.body().isSuccess()) {
                            List<Map<String,Object>> data = resp.body().getData();
                            if (data != null && !data.isEmpty()) {
                                kelompokNama.clear();
                                kelompokNamaSaja.clear();
                                kelompokDesa.clear();
                                kelompokId.clear();
                                for (Map<String,Object> k : data) {
                                    String namaSaja = String.valueOf(k.get("nama_kelompok"));
                                    String desa     = String.valueOf(k.get("desa"));

                                    kelompokNamaSaja.add(namaSaja);
                                    kelompokDesa.add(desa);
                                    kelompokNama.add("🏘 " + namaSaja + "  —  " + desa);
                                    kelompokId.add(parseIdAman(k.get("id")));
                                }
                                refreshSpinner();
                            }
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<List<Map<String,Object>>>> call,
                            Throwable t) {
                        // Tetap pakai data default
                    }
                });
    }

    /** Data kelompok sementara (dari skripsi kamu) */
    private void isiKelompokDefault() {
        kelompokNama.clear();
        kelompokNamaSaja.clear();
        kelompokDesa.clear();
        kelompokId.clear();

        tambahKelompokDefault("Kelompok Mawar",   "Randudongkal", 1);
        tambahKelompokDefault("Kelompok Melati",  "Kecepit",      2);
        tambahKelompokDefault("Kelompok Dahlia",  "Gombong",      3);
        tambahKelompokDefault("Kelompok Anggrek", "Tanahbaya",    4);
        tambahKelompokDefault("Kelompok Kenanga", "Semingkir",    5);

        refreshSpinner();
    }

    private void tambahKelompokDefault(String nama, String desa, int id) {
        kelompokNamaSaja.add(nama);
        kelompokDesa.add(desa);
        kelompokNama.add("🏘 " + nama + "  —  " + desa);
        kelompokId.add(id);
    }

    private void refreshSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                kelompokNama);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerKelompok.setAdapter(adapter);
    }

    /** BARU: bungkus String jadi RequestBody teks buat dikirim Multipart */
    private RequestBody teks(String value) {
        return RequestBody.create(MediaType.parse("text/plain"), value);
    }

    /** Kirim data pendaftaran ke API */
    private void daftar() {
        String nama         = etNama.getText().toString().trim();
        String username     = etUsername.getText().toString().trim();
        String nik           = etNik.getText().toString().trim();
        String telepon       = etTelepon.getText().toString().trim();
        String tempatLahir   = etTempatLahir.getText().toString().trim();
        String alamat        = etAlamat.getText().toString().trim();
        String password      = etPassword.getText().toString().trim();
        String konfirmasi    = etKonfirmasi.getText().toString().trim();

        // Validasi sisi client
        if (nama.isEmpty())     { etNama.setError("Wajib diisi"); return; }
        if (username.isEmpty()) { etUsername.setError("Wajib diisi"); return; }
        if (nik.isEmpty())      { etNik.setError("Wajib diisi"); return; }
        if (nik.length() != 16) { etNik.setError("NIK harus 16 digit"); return; }
        if (telepon.isEmpty())  { etTelepon.setError("Wajib diisi"); return; }
        if (tempatLahir.isEmpty()) {
            etTempatLahir.setError("Wajib diisi"); return; }
        if (tanggalLahirTerpilih == null) {
            Toast.makeText(this, "Pilih tanggal lahir dulu",
                    Toast.LENGTH_SHORT).show(); return; }
        if (alamat.isEmpty()) {
            etAlamat.setError("Wajib diisi"); return; }
        // ── BARU: validasi foto KTP wajib ──
        if (fotoKtpFile == null) {
            Toast.makeText(this, "Pilih foto KTP dulu",
                    Toast.LENGTH_SHORT).show(); return; }
        if (password.length() < 6) {
            etPassword.setError("Minimal 6 karakter"); return; }
        if (!password.equals(konfirmasi)) {
            etKonfirmasi.setError("Password tidak cocok"); return; }
        if (kelompokId.isEmpty()) {
            Toast.makeText(this, "Pilih kelompok dulu",
                    Toast.LENGTH_SHORT).show(); return; }

        setLoading(true);

        int posisi = spinnerKelompok.getSelectedItemPosition();
        int idKelompok       = kelompokId.get(posisi);
        String namaKelompok  = kelompokNamaSaja.get(posisi);
        String desaKelompok  = kelompokDesa.get(posisi);
        String tanggalLahirApi = FORMAT_API.format(tanggalLahirTerpilih.getTime());

        // ── PERUBAHAN: dari Map<String,String> JSON -> field Multipart ──
        Map<String, RequestBody> body = new HashMap<>();
        body.put("nama_lengkap",   teks(nama));
        body.put("username",       teks(username));
        body.put("password",       teks(password));
        body.put("konfirmasi",     teks(konfirmasi));
        body.put("nik",            teks(nik));
        body.put("no_telepon",     teks(telepon));
        body.put("tempat_lahir",   teks(tempatLahir));
        body.put("tanggal_lahir",  teks(tanggalLahirApi));
        body.put("alamat",         teks(alamat));
        body.put("kelompok_id",    teks(String.valueOf(idKelompok))); // disimpan buat jaga-jaga, saat ini tidak dipakai backend
        body.put("nama_kelompok",  teks(namaKelompok));
        body.put("nama_desa",      teks(desaKelompok));

        RequestBody fotoKtpBody = RequestBody.create(
                MediaType.parse("image/*"), fotoKtpFile);
        MultipartBody.Part fotoKtpPart = MultipartBody.Part.createFormData(
                "foto_ktp", fotoKtpFile.getName(), fotoKtpBody);

        ApiClient.getService().daftarAkun(body, fotoKtpPart)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> resp) {
                        setLoading(false);
                        if (resp.isSuccessful() && resp.body() != null) {
                            if (resp.body().isSuccess()) {
                                Toast.makeText(DaftarActivity.this,
                                        "Pendaftaran berhasil! Silakan login.",
                                        Toast.LENGTH_LONG).show();
                                finish(); // kembali ke login
                            } else {
                                Toast.makeText(DaftarActivity.this,
                                        resp.body().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call,
                                          Throwable t) {
                        setLoading(false);
                        Toast.makeText(DaftarActivity.this,
                                "Koneksi gagal!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnDaftar.setEnabled(!loading);
        btnDaftar.setText(loading ? "Mendaftarkan..." : "Daftar Sekarang");
    }
}