package com.amikom.bumdesma.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.api.ApiClient;
import com.amikom.bumdesma.model.LoginResponse;
import com.amikom.bumdesma.ui.admin.DashboardAdminActivity;
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
import android.util.Log;

public class LoginAdminActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_admin);

        ImageView imgLogo = findViewById(R.id.img_logo);

        findViewById(R.id.tv_lupa_password).setOnClickListener(v ->
                startActivity(new Intent(this, LupaPasswordActivity.class)));

        ScaleAnimation scaleIn = new ScaleAnimation(
                0.3f, 1.0f, 0.3f, 1.0f,
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

        enterAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation a) {}

            @Override
            public void onAnimationRepeat(Animation a) {}

            @Override
            public void onAnimationEnd(Animation a) {
                Animation pulse = AnimationUtils.loadAnimation(
                        LoginAdminActivity.this, R.anim.logo_pulse);
                imgLogo.startAnimation(pulse);
            }
        });
        imgLogo.startAnimation(enterAnim);

        session     = new SessionManager(this);
        etUsername  = findViewById(R.id.et_username);
        etPassword  = findViewById(R.id.et_password);
        btnLogin    = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);

        btnLogin.setOnClickListener(v -> doLogin());
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

                        // Gerbang tambahan: halaman ini KHUSUS admin.
                        // Kalau ternyata yang login itu akun anggota (walau
                        // username/password valid), tolak di sini juga.
                        if (!Constants.ROLE_ADMIN.equals(data.getRole())) {
                            Toast.makeText(LoginAdminActivity.this,
                                    "Akun ini bukan akun admin. Silakan masuk lewat halaman Login Anggota.",
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        session.saveSession(
                                data.getToken(),
                                data.getRole(),
                                data.getUserId(),
                                data.getAnggotaId(),
                                data.getNama()
                        );
                        Toast.makeText(LoginAdminActivity.this,
                                "Selamat datang, " + data.getNama(),
                                Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(LoginAdminActivity.this,
                                DashboardAdminActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        Toast.makeText(LoginAdminActivity.this,
                                res.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(LoginAdminActivity.this,
                            "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoading(false);
                Log.e("LoginAdminActivity", "Login gagal", t);
                Toast.makeText(LoginAdminActivity.this,
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