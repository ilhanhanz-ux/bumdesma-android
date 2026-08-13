package com.amikom.bumdesma.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.ApiResponse;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LupaPasswordActivity extends AppCompatActivity {

    // Step 1
    private LinearLayout layoutStep1;
    private EditText etUsername, etNik;
    private Button btnVerifikasi;

    // Step 2
    private LinearLayout layoutStep2;
    private EditText etPasswordBaru, etKonfirmasi;
    private Button btnReset;
    private TextView tvNamaUser;

    private ProgressBar progressBar;
    private String resetToken = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lupa_password);

        // Bind views step 1
        layoutStep1   = findViewById(R.id.layout_step1);
        etUsername    = findViewById(R.id.et_username_lupa);
        etNik         = findViewById(R.id.et_nik_lupa);
        btnVerifikasi = findViewById(R.id.btn_verifikasi);

        // Bind views step 2
        layoutStep2    = findViewById(R.id.layout_step2);
        tvNamaUser     = findViewById(R.id.tv_nama_user);
        etPasswordBaru = findViewById(R.id.et_password_baru);
        etKonfirmasi   = findViewById(R.id.et_konfirmasi_baru);
        btnReset       = findViewById(R.id.btn_reset_password);

        progressBar = findViewById(R.id.progress_bar_lupa);

        // Tampilkan step 1
        showStep(1);

        btnVerifikasi.setOnClickListener(v -> verifikasi());
        btnReset.setOnClickListener(v -> resetPassword());

        // Tombol kembali
        TextView tvKembali = findViewById(R.id.tv_kembali);
        if (tvKembali != null) {
            tvKembali.setOnClickListener(v -> {
                if (layoutStep2.getVisibility() == View.VISIBLE) {
                    // Kalau di step 2, kembali ke step 1
                    resetToken = "";
                    showStep(1);
                } else {
                    finish();
                }
            });
        }
    }

    private void showStep(int step) {
        if (step == 1) {
            layoutStep1.setVisibility(View.VISIBLE);
            layoutStep2.setVisibility(View.GONE);
        } else {
            layoutStep1.setVisibility(View.GONE);
            layoutStep2.setVisibility(View.VISIBLE);
        }
    }

    /** Step 1: Verifikasi username + NIK */
    private void verifikasi() {
        String username = etUsername.getText().toString().trim();
        String nik      = etNik.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Username wajib diisi");
            etUsername.requestFocus();
            return;
        }
        if (nik.isEmpty()) {
            etNik.setError("NIK wajib diisi");
            etNik.requestFocus();
            return;
        }
        if (nik.length() != 16) {
            etNik.setError("NIK harus 16 digit");
            etNik.requestFocus();
            return;
        }

        setLoading(true);

        Map<String, String> body = new HashMap<>();
        body.put("step",     "verifikasi");
        body.put("username", username);
        body.put("nik",      nik);

        ApiClient.getService()
                .lupaPassword(body)
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<Map<String, Object>>> call,
                            Response<ApiResponse<Map<String, Object>>> resp) {
                        setLoading(false);

                        if (resp.isSuccessful() && resp.body() != null) {
                            ApiResponse<Map<String, Object>> result = resp.body();

                            if (result.isSuccess() && result.getData() != null) {
                                // Ambil token dari response
                                Object tokenObj = result.getData().get("reset_token");
                                Object namaObj  = result.getData().get("nama");

                                if (tokenObj != null) {
                                    resetToken = tokenObj.toString();
                                    String nama = namaObj != null
                                            ? namaObj.toString() : "Anggota";

                                    tvNamaUser.setText(
                                            "✅ Halo, " + nama + "!\nSilakan buat password baru.");
                                    showStep(2);

                                } else {
                                    Toast.makeText(LupaPasswordActivity.this,
                                            "Token tidak diterima dari server",
                                            Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(LupaPasswordActivity.this,
                                        result.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(LupaPasswordActivity.this,
                                    "Error: " + resp.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<Map<String, Object>>> call,
                            Throwable t) {
                        setLoading(false);
                        Toast.makeText(LupaPasswordActivity.this,
                                "Koneksi gagal! Cek IP di Constants.java",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    /** Step 2: Kirim password baru */
    private void resetPassword() {
        String pwBaru  = etPasswordBaru.getText().toString().trim();
        String konfirm = etKonfirmasi.getText().toString().trim();

        if (pwBaru.length() < 6) {
            etPasswordBaru.setError("Minimal 6 karakter");
            etPasswordBaru.requestFocus();
            return;
        }
        if (!pwBaru.equals(konfirm)) {
            etKonfirmasi.setError("Password tidak cocok");
            etKonfirmasi.requestFocus();
            return;
        }
        if (resetToken.isEmpty()) {
            Toast.makeText(this,
                    "Sesi tidak valid, ulangi dari awal",
                    Toast.LENGTH_SHORT).show();
            showStep(1);
            return;
        }

        setLoading(true);

        Map<String, String> body = new HashMap<>();
        body.put("step",          "reset");
        body.put("reset_token",   resetToken);
        body.put("password_baru", pwBaru);
        body.put("konfirmasi",    konfirm);

        ApiClient.getService()
                .lupaPassword(body)
                .enqueue(new Callback<ApiResponse<Map<String, Object>>>() {
                    @Override
                    public void onResponse(
                            Call<ApiResponse<Map<String, Object>>> call,
                            Response<ApiResponse<Map<String, Object>>> resp) {
                        setLoading(false);

                        if (resp.isSuccessful() && resp.body() != null) {
                            ApiResponse<Map<String, Object>> result = resp.body();

                            if (result.isSuccess()) {
                                Toast.makeText(LupaPasswordActivity.this,
                                        "✅ " + result.getMessage(),
                                        Toast.LENGTH_LONG).show();
                                finish(); // Kembali ke login
                            } else {
                                Toast.makeText(LupaPasswordActivity.this,
                                        result.getMessage(),
                                        Toast.LENGTH_LONG).show();

                                // Kalau sesi expired, kembali ke step 1
                                if (result.getMessage() != null &&
                                        result.getMessage().contains("kadaluarsa")) {
                                    resetToken = "";
                                    showStep(1);
                                }
                            }
                        } else {
                            Toast.makeText(LupaPasswordActivity.this,
                                    "Error: " + resp.code(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ApiResponse<Map<String, Object>>> call,
                            Throwable t) {
                        setLoading(false);
                        Toast.makeText(LupaPasswordActivity.this,
                                "Koneksi gagal!",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setLoading(boolean loading) {
        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (btnVerifikasi != null) btnVerifikasi.setEnabled(!loading);
        if (btnReset != null) btnReset.setEnabled(!loading);
    }
}