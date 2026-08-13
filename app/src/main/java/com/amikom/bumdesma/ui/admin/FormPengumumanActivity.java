package com.amikom.bumdesma.ui.admin;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.ApiResponse;
import com.amikom.bumdesma.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FormPengumumanActivity extends AppCompatActivity {

    private SessionManager session;
    private EditText etJudul, etIsi, etTanggal;
    private Button btnSimpan;
    private int pengumumanId = -1; // -1 = mode tambah baru

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_pengumuman);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        etJudul   = findViewById(R.id.et_judul);
        etIsi     = findViewById(R.id.et_isi);
        etTanggal = findViewById(R.id.et_tanggal);
        btnSimpan = findViewById(R.id.btn_simpan);

        pengumumanId = getIntent().getIntExtra("id", -1);
        boolean modeEdit = pengumumanId != -1;

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(modeEdit ? "Edit Pengumuman" : "Tambah Pengumuman");
        }

        if (modeEdit) {
            etJudul.setText(getIntent().getStringExtra("judul"));
            etIsi.setText(getIntent().getStringExtra("isi"));
            etTanggal.setText(getIntent().getStringExtra("tanggal"));
        } else {
            etTanggal.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date()));
        }

        etTanggal.setOnClickListener(v -> tampilkanDatePicker());
        btnSimpan.setText(modeEdit ? "Simpan Perubahan" : "Terbitkan Pengumuman");
        btnSimpan.setOnClickListener(v -> simpan(modeEdit));
    }

    private void tampilkanDatePicker() {
        Calendar c = Calendar.getInstance();
        try {
            String[] parts = etTanggal.getText().toString().split("-");
            c.set(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]) - 1, Integer.parseInt(parts[2]));
        } catch (Exception ignored) { }

        new DatePickerDialog(this, (view, year, month, day) -> {
            String tgl = String.format(Locale.US, "%04d-%02d-%02d", year, month + 1, day);
            etTanggal.setText(tgl);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void simpan(boolean modeEdit) {
        String judul   = etJudul.getText().toString().trim();
        String isi     = etIsi.getText().toString().trim();
        String tanggal = etTanggal.getText().toString().trim();

        if (judul.isEmpty()) {
            Toast.makeText(this, "Judul wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (isi.isEmpty()) {
            Toast.makeText(this, "Isi pengumuman wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("judul", judul);
        body.put("isi", isi);
        body.put("tanggal", tanggal);

        btnSimpan.setEnabled(false);

        if (modeEdit) {
            ApiClient.getService()
                    .updatePengumuman(session.getBearerToken(), pengumumanId, body)
                    .enqueue(new Callback<ApiResponse<Void>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Void>> call,
                                               Response<ApiResponse<Void>> response) {
                            btnSimpan.setEnabled(true);
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().isSuccess()) {
                                Toast.makeText(FormPengumumanActivity.this,
                                        "Pengumuman diperbarui", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(FormPengumumanActivity.this,
                                        "Gagal menyimpan", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                            btnSimpan.setEnabled(true);
                            Toast.makeText(FormPengumumanActivity.this,
                                    "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            ApiClient.getService()
                    .buatPengumuman(session.getBearerToken(), body)
                    .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                        @Override
                        public void onResponse(Call<ApiResponse<Map<String, Object>>> call,
                                               Response<ApiResponse<Map<String, Object>>> response) {
                            btnSimpan.setEnabled(true);
                            if (response.isSuccessful() && response.body() != null
                                    && response.body().isSuccess()) {
                                Toast.makeText(FormPengumumanActivity.this,
                                        "Pengumuman diterbitkan", Toast.LENGTH_SHORT).show();
                                setResult(RESULT_OK);
                                finish();
                            } else {
                                Toast.makeText(FormPengumumanActivity.this,
                                        "Gagal menyimpan", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ApiResponse<Map<String, Object>>> call, Throwable t) {
                            btnSimpan.setEnabled(true);
                            Toast.makeText(FormPengumumanActivity.this,
                                    "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}