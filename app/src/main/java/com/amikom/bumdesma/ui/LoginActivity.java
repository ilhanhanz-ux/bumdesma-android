package com.amikom.bumdesma.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.LoginResponse;
import com.amikom.bumdesma.ui.admin.DashboardAdminActivity;
import com.amikom.bumdesma.ui.anggota.DashboardAnggotaActivity;
import com.amikom.bumdesma.utils.Constants;
import com.amikom.bumdesma.utils.SessionManager;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.util.Log;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private SessionManager session;

    // ── Konfigurasi tap rahasia ke halaman admin ──────────
    // Tap logo sebanyak TAP_THRESHOLD kali dalam TAP_TIMEOUT_MS
    // untuk membuka LoginAdminActivity. Tidak ada indikasi visual
    // apapun (tanpa toast/animasi tambahan) supaya tetap tersembunyi.
    private static final int TAP_THRESHOLD = 5;
    private static final long TAP_TIMEOUT_MS = 3000;
    private int tapCount = 0;
    private long firstTapTime = 0L;
    // ───────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // ── Animasi Logo ──────────────────────────────────
        ImageView imgLogo = findViewById(R.id.img_logo);
        // Tombol Daftar Sekarang
        findViewById(R.id.tv_daftar).setOnClickListener(v ->
                startActivity(new Intent(this, DaftarActivity.class)));
        // Tombol Lupa Password
        findViewById(R.id.tv_lupa_password).setOnClickListener(v ->
                startActivity(new Intent(this, LupaPasswordActivity.class)));

        // ── Tap rahasia di logo → halaman login admin ─────
        imgLogo.setOnClickListener(v -> handleLogoTap());
        // ───────────────────────────────────────────────────

        // 1) Animasi masuk: muncul dari kecil + fade in
        ScaleAnimation scaleIn = new ScaleAnimation(
                0.3f, 1.0f,   // dari 30% → 100% ukuran
                0.3f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleIn.setDuration(700);
        scaleIn.setInterpolator(AnimationUtils.loadInterpolator(
                this, android.R.interpolator.overshoot));

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(700);

        AnimationSet enterAnim = new AnimationSet(false);
        enterAnim.addAnimation(scaleIn);
        enterAnim.addAnimation(fadeIn);

// 2) Setelah animasi masuk selesai, jalankan animasi denyut
        enterAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation a) {}

            @Override
            public void onAnimationRepeat(Animation a) {}

            @Override
            public void onAnimationEnd(Animation a) {
                // Mulai animasi denyut setelah logo muncul
                Animation pulse = AnimationUtils.loadAnimation(
                        LoginActivity.this, R.anim.logo_pulse);
                imgLogo.startAnimation(pulse);
            }
        });

// Jalankan animasi masuk
        imgLogo.startAnimation(enterAnim);
// ─────────────────────────────────────────────────

        session     = new SessionManager(this);
        etUsername  = findViewById(R.id.et_username);
        etPassword  = findViewById(R.id.et_password);
        btnLogin    = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);

        btnLogin.setOnClickListener(v -> doLogin());
    }

    /**
     * Menghitung tap pada logo dalam jendela waktu TAP_TIMEOUT_MS.
     * Kalau mencapai TAP_THRESHOLD, buka AdminLoginActivity secara diam-diam.
     * Tidak ada toast/log/animasi khusus supaya fitur ini tetap tersembunyi
     * dari pengguna biasa.
     */
    private void handleLogoTap() {
        long now = System.currentTimeMillis();

        if (now - firstTapTime > TAP_TIMEOUT_MS) {
            // Jendela waktu sudah lewat, mulai hitung dari awal
            tapCount = 1;
            firstTapTime = now;
        } else {
            tapCount++;
        }

        if (tapCount >= TAP_THRESHOLD) {
            tapCount = 0;
            firstTapTime = 0L;
            startActivity(new Intent(LoginActivity.this, LoginAdminActivity.class));
        }
    }

    private void doLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Username wajib diisi");
            etUsername.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            etPassword.setError("Password wajib diisi");
            etPassword.requestFocus();
            return;
        }

        setLoading(true);

        Map<String, String> body = new HashMap<>();
        body.put("username", username);
        body.put("password", password);

        ApiClient.getService().login(body).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call,
                                   Response<LoginResponse> response) {
                setLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse res = response.body();
                    if (res.isSuccess()) {
                        LoginResponse.Data data = res.getData();
                        // ── BARU: is_ketua ikut disimpan ke sesi lewat overload 6-parameter ──
                        session.saveSession(
                                data.getToken(),
                                data.getRole(),
                                data.getUserId(),
                                data.getAnggotaId(),
                                data.getNama(),
                                data.isKetua()
                        );
                        Toast.makeText(LoginActivity.this,
                                "Selamat datang, " + data.getNama(),
                                Toast.LENGTH_SHORT).show();

                        Intent intent = Constants.ROLE_ADMIN.equals(data.getRole())
                                ? new Intent(LoginActivity.this, DashboardAdminActivity.class)
                                : new Intent(LoginActivity.this, DashboardAnggotaActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginActivity.this,
                                res.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginActivity.this,
                            "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Log.e("LoginActivity", "Login gagal", t);
                Toast.makeText(LoginActivity.this,
                        "Gagal konek: " + t.getClass().getSimpleName() + " - " + t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? "Memproses..." : "Masuk");
    }
}