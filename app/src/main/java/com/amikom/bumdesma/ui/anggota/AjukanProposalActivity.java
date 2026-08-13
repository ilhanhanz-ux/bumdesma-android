package com.amikom.bumdesma.ui.anggota;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.api.ApiService;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.model.KelompokLimit;
import com.amikom.bumdesma.utils.SessionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AjukanProposalActivity extends AppCompatActivity {

    private EditText etJumlahPinjaman, etKeperluan;
    private Spinner spinnerTenor;
    private TextView tvPdfName;
    private LinearLayout layoutUploadPdf;
    private Button btnKirimProposal;

    // BARU: info limit kelompok
    private LinearLayout layoutLimitKelompok;
    private TextView tvLimitKelompok;

    private Uri pdfUri = null;
    private String pdfFileName = "";

    private SessionManager sessionManager;

    private final ActivityResultLauncher<Intent> pdfPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    pdfUri = result.getData().getData();
                    pdfFileName = getFileName(pdfUri);

                    if (!pdfFileName.toLowerCase().endsWith(".pdf")) {
                        Toast.makeText(this, "Hanya file PDF yang diizinkan!", Toast.LENGTH_SHORT).show();
                        pdfUri = null;
                        pdfFileName = "";
                        return;
                    }

                    long fileSize = getFileSize(pdfUri);
                    if (fileSize > 5 * 1024 * 1024) {
                        Toast.makeText(this, "Ukuran file melebihi 5MB!", Toast.LENGTH_SHORT).show();
                        pdfUri = null;
                        pdfFileName = "";
                        return;
                    }

                    tvPdfName.setText("📄 " + pdfFileName);
                    tvPdfName.setTextColor(ContextCompat.getColor(this, R.color.da_accent_text));
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ajukan_proposal);

        sessionManager = new SessionManager(this);

        // ── BARU: gerbang ketua-only (defense in depth). Tombol banner di
        // Dashboard sudah disembunyikan/di-gate untuk non-ketua, tapi ini
        // jaga-jaga kalau activity ini diakses langsung (deep link, notifikasi
        // lama, dsb). Validasi final tetap di backend (proposal.php).
        if (!sessionManager.isKetua()) {
            Toast.makeText(this,
                    "Hanya ketua kelompok yang dapat mengajukan proposal pinjaman. Hubungi ketua kelompok Anda untuk mengajukan pinjaman baru.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ajukan Proposal");
        }

        etJumlahPinjaman = findViewById(R.id.etJumlahPinjaman);
        etKeperluan      = findViewById(R.id.etKeperluan);
        spinnerTenor     = findViewById(R.id.spinnerTenor);
        tvPdfName        = findViewById(R.id.tvPdfName);
        layoutUploadPdf  = findViewById(R.id.layoutUploadPdf);
        btnKirimProposal = findViewById(R.id.btnKirimProposal);

        // BARU
        layoutLimitKelompok = findViewById(R.id.layoutLimitKelompok);
        tvLimitKelompok      = findViewById(R.id.tvLimitKelompok);

        String[] tenorOptions = {"3 bulan", "6 bulan", "12 bulan", "18 bulan", "24 bulan"};
        ArrayAdapter<String> tenorAdapter = new ArrayAdapter<>(
                this, R.layout.item_spinner_text, tenorOptions);
        tenorAdapter.setDropDownViewResource(R.layout.item_spinner_dropdown);
        spinnerTenor.setAdapter(tenorAdapter);

        pasangFormatRibuan(etJumlahPinjaman);

        layoutUploadPdf.setOnClickListener(v -> openPdfPicker());
        btnKirimProposal.setOnClickListener(v -> validateAndSubmit());

        // BARU
        muatLimitKelompok();
    }

    /**
     * BARU: ambil data kelompok milik ketua yang login (nama + limit_pinjaman)
     * lewat kelompok_saya.php, lalu tampilkan di info box atas form.
     * Kalau gagal (koneksi/404/dsb), box disembunyikan lagi -- tidak
     * menghalangi ketua untuk tetap mengisi & mengirim proposal.
     */
    private void muatLimitKelompok() {
        String token = sessionManager.getBearerToken();
        ApiService api = ApiClient.getInstance().create(ApiService.class);

        api.getKelompokSaya(token).enqueue(new Callback<ApiResponse<KelompokLimit>>() {
            @Override
            public void onResponse(Call<ApiResponse<KelompokLimit>> call,
                                   Response<ApiResponse<KelompokLimit>> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getData() != null) {
                    KelompokLimit kelompok = response.body().getData();
                    tvLimitKelompok.setText(String.format(
                            "Limit Kelompok %s: %s",
                            kelompok.getNamaKelompok(),
                            formatRupiah(kelompok.getLimitPinjaman())));
                    layoutLimitKelompok.setVisibility(android.view.View.VISIBLE);
                } else {
                    layoutLimitKelompok.setVisibility(android.view.View.GONE);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<KelompokLimit>> call, Throwable t) {
                android.util.Log.e("AJUKAN_PROPOSAL", "Gagal ambil limit kelompok: " + t.getMessage(), t);
                layoutLimitKelompok.setVisibility(android.view.View.GONE);
            }
        });
    }

    /** BARU: format angka jadi "Rp 50.000.000" pakai pemisah ribuan ala Indonesia */
    private String formatRupiah(double nilai) {
        DecimalFormatSymbols simbol = new DecimalFormatSymbols(new Locale("id", "ID"));
        DecimalFormat formatter = new DecimalFormat("Rp #,###", simbol);
        return formatter.format(nilai);
    }

    /**
     * Pasang TextWatcher yang otomatis nambahin titik ribuan
     * setiap kali user ngetik angka, contoh: 50000000 -> 50.000.000
     */
    private void pasangFormatRibuan(EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            private boolean sedangFormat = false;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (sedangFormat) return;
                sedangFormat = true;

                String angkaSaja = s.toString().replaceAll("[^\\d]", "");

                if (angkaSaja.isEmpty()) {
                    sedangFormat = false;
                    return;
                }

                try {
                    long nilai = Long.parseLong(angkaSaja);

                    DecimalFormatSymbols simbol = new DecimalFormatSymbols(new Locale("id", "ID"));
                    DecimalFormat formatter = new DecimalFormat("#,###", simbol);
                    String hasilFormat = formatter.format(nilai);

                    editText.setText(hasilFormat);
                    editText.setSelection(hasilFormat.length());
                } catch (NumberFormatException e) {
                    editText.setText(angkaSaja);
                    editText.setSelection(angkaSaja.length());
                }

                sedangFormat = false;
            }
        });
    }

    /** Buang semua titik/karakter non-digit, sisain angka polos buat dikirim ke backend */
    private String bersihkanAngka(String teksBerformat) {
        return teksBerformat.replaceAll("[^\\d]", "");
    }

    private void openPdfPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        pdfPickerLauncher.launch(Intent.createChooser(intent, "Pilih File PDF"));
    }

    private void validateAndSubmit() {
        String jumlahBersih = bersihkanAngka(etJumlahPinjaman.getText().toString().trim());
        String keperluan    = etKeperluan.getText().toString().trim();
        String tenor         = spinnerTenor.getSelectedItem().toString();

        if (jumlahBersih.isEmpty()) {
            etJumlahPinjaman.setError("Jumlah pinjaman wajib diisi");
            return;
        }
        if (keperluan.isEmpty()) {
            etKeperluan.setError("Keperluan wajib diisi");
            return;
        }
        if (pdfUri == null) {
            Toast.makeText(this, "Harap upload dokumen proposal (PDF)", Toast.LENGTH_SHORT).show();
            return;
        }

        submitProposal(jumlahBersih, tenor, keperluan);
    }

    private void submitProposal(String jumlah, String tenor, String keperluan) {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Mengirim proposal...");
        progressDialog.show();

        try {
            File pdfFile = uriToFile(pdfUri, pdfFileName);

            RequestBody rbJumlah    = RequestBody.create(MediaType.parse("text/plain"), jumlah);
            RequestBody rbTenor     = RequestBody.create(MediaType.parse("text/plain"), tenor);
            RequestBody rbKeperluan = RequestBody.create(MediaType.parse("text/plain"), keperluan);

            RequestBody rbFile = RequestBody.create(MediaType.parse("application/pdf"), pdfFile);
            MultipartBody.Part filePart = MultipartBody.Part.createFormData(
                    "dokumen_proposal", pdfFile.getName(), rbFile);

            // Token dengan prefix "Bearer "
            String token = sessionManager.getBearerToken();

            ApiService api = ApiClient.getInstance().create(ApiService.class);
            Call<ResponseBody> call = api.ajukanProposal(
                    token, rbJumlah, rbTenor, rbKeperluan, filePart);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call,
                                       Response<ResponseBody> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        Toast.makeText(AjukanProposalActivity.this,
                                "Proposal berhasil dikirim! Menunggu verifikasi Admin.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        try {
                            String errorBody = response.errorBody() != null
                                    ? response.errorBody().string() : "Unknown error";
                            android.util.Log.e("PROPOSAL", "Error " + response.code() + ": " + errorBody);

                            // Backend (proposal.php) selalu membungkus error lewat sendResponse(),
                            // yang isinya field "message" berisi kalimat yang sudah manusiawi
                            // (contoh: "Jumlah pinjaman melebihi limit kelompok kamu saat ini (Rp 8.120.000)").
                            // Jadi kita ambil field itu, bukan menampilkan JSON mentahnya ke user.
                            String pesan = tampilkanPesanRamah(errorBody, response.code());

                            new androidx.appcompat.app.AlertDialog.Builder(AjukanProposalActivity.this)
                                    .setTitle("Proposal Tidak Dapat Dikirim")
                                    .setMessage(pesan)
                                    .setPositiveButton("Mengerti", null)
                                    .show();
                        } catch (Exception ex) {
                            Toast.makeText(AjukanProposalActivity.this,
                                    "Gagal mengirim proposal (" + response.code() + ")",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    progressDialog.dismiss();
                    android.util.Log.e("PROPOSAL", "onFailure: " + t.getMessage(), t);
                    Toast.makeText(AjukanProposalActivity.this,
                            "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            progressDialog.dismiss();
            android.util.Log.e("PROPOSAL", "Exception: " + e.getMessage(), e);
            Toast.makeText(this, "Error membaca file: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * BARU: ambil field "message" dari body error JSON backend supaya yang
     * ditampilkan ke ketua kelompok adalah kalimat peringatan yang jelas
     * (mis. "Jumlah pinjaman melebihi limit kelompok kamu saat ini (Rp X)"),
     * bukan JSON mentah seperti {"success":false,"message":"..."}.
     * Kalau body ternyata bukan JSON valid (mis. error server/HTML), pakai
     * pesan default supaya tidak menampilkan sesuatu yang membingungkan.
     */
    private String tampilkanPesanRamah(String errorBody, int httpCode) {
        try {
            org.json.JSONObject json = new org.json.JSONObject(errorBody);
            if (json.has("message") && !json.isNull("message")) {
                String pesan = json.getString("message");
                if (httpCode == 422) {
                    // 422 dari proposal.php khusus dipakai untuk kasus jumlah
                    // pinjaman melebihi limit -- tegaskan supaya ketua paham
                    // ini bukan error teknis, tapi memang harus mengurangi jumlah.
                    return pesan + "\n\nSilakan kurangi jumlah pinjaman sesuai limit yang tersedia.";
                }
                return pesan;
            }
        } catch (org.json.JSONException je) {
            android.util.Log.e("PROPOSAL", "errorBody bukan JSON valid: " + errorBody);
        }
        return "Proposal tidak dapat diproses (kode " + httpCode + "). Silakan coba lagi atau hubungi admin.";
    }

    private File uriToFile(Uri uri, String fileName) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        File tempFile = new File(getCacheDir(), fileName);
        FileOutputStream fos = new FileOutputStream(tempFile);
        byte[] buf = new byte[4096];
        int len;
        while ((len = inputStream.read(buf)) > 0) fos.write(buf, 0, len);
        fos.close();
        inputStream.close();
        return tempFile;
    }

    private String getFileName(Uri uri) {
        String result = "proposal.pdf";
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (idx >= 0) result = cursor.getString(idx);
                }
            }
        }
        return result;
    }

    private long getFileSize(Uri uri) {
        long size = 0;
        try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (idx >= 0) size = cursor.getLong(idx);
            }
        }
        return size;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}