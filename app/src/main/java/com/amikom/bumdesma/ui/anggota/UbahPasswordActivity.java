package com.amikom.bumdesma.ui.anggota;

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

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UbahPasswordActivity extends AppCompatActivity {

    private SessionManager session;
    private EditText etLama, etBaru, etKonfirmasi;
    private Button btnSimpan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubah_password);

        session = new SessionManager(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ubah Password");
        }

        etLama       = findViewById(R.id.et_password_lama);
        etBaru       = findViewById(R.id.et_password_baru);
        etKonfirmasi = findViewById(R.id.et_konfirmasi);
        btnSimpan    = findViewById(R.id.btn_simpan);

        btnSimpan.setOnClickListener(v -> simpan());
    }

    private void simpan() {
        String lama       = etLama.getText().toString().trim();
        String baru       = etBaru.getText().toString().trim();
        String konfirmasi = etKonfirmasi.getText().toString().trim();

        if (lama.isEmpty()) {
            Toast.makeText(this, "Password lama wajib diisi", Toast.LENGTH_SHORT).show();
            return;
        }
        if (baru.length() < 6) {
            Toast.makeText(this, "Password baru minimal 6 karakter", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!baru.equals(konfirmasi)) {
            Toast.makeText(this, "Konfirmasi password tidak cocok", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> body = new HashMap<>();
        body.put("password_lama", lama);
        body.put("password_baru", baru);
        body.put("konfirmasi", konfirmasi);

        btnSimpan.setEnabled(false);

        ApiClient.getService()
                .ubahPassword(session.getBearerToken(), body)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        btnSimpan.setEnabled(true);
                        if (response.isSuccessful() && response.body() != null
                                && response.body().isSuccess()) {
                            Toast.makeText(UbahPasswordActivity.this,
                                    "Password berhasil diubah", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            String pesan = (response.body() != null)
                                    ? response.body().getMessage() : "Gagal mengubah password";
                            Toast.makeText(UbahPasswordActivity.this, pesan, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        btnSimpan.setEnabled(true);
                        Toast.makeText(UbahPasswordActivity.this,
                                "Koneksi gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}