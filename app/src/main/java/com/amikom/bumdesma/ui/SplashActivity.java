package com.amikom.bumdesma.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AlphaAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.amikom.bumdesma.MainActivity;
import com.amikom.bumdesma.R;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 3000; // 3 detik

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView imgLogo   = findViewById(R.id.img_logo_splash);
        TextView  tvNama    = findViewById(R.id.tv_nama_splash);
        TextView  tvSubjudul = findViewById(R.id.tv_subjudul_splash);
        TextView  tvVersi   = findViewById(R.id.tv_versi_splash);

        // ── Animasi Logo: muncul dari kecil + fade in ──────
        ScaleAnimation scaleIn = new ScaleAnimation(
                0.2f, 1.0f, 0.2f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleIn.setDuration(800);

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(800);

        AnimationSet logoAnim = new AnimationSet(true);
        logoAnim.addAnimation(scaleIn);
        logoAnim.addAnimation(fadeIn);
        imgLogo.startAnimation(logoAnim);

        // ── Animasi Nama: muncul dari bawah ke atas ────────
        TranslateAnimation slideUp = new TranslateAnimation(
                0, 0, 80f, 0f);
        slideUp.setDuration(800);
        slideUp.setStartOffset(500);

        AlphaAnimation fadeInText = new AlphaAnimation(0f, 1f);
        fadeInText.setDuration(800);
        fadeInText.setStartOffset(500);

        AnimationSet textAnim = new AnimationSet(true);
        textAnim.addAnimation(slideUp);
        textAnim.addAnimation(fadeInText);
        tvNama.startAnimation(textAnim);
        tvSubjudul.startAnimation(textAnim);

        // ── Animasi versi: fade in lambat ──────────────────
        AlphaAnimation fadeInVersi = new AlphaAnimation(0f, 1f);
        fadeInVersi.setDuration(1000);
        fadeInVersi.setStartOffset(1200);
        fadeInVersi.setFillAfter(true);
        tvVersi.startAnimation(fadeInVersi);

        // ── Pindah ke MainActivity setelah 3 detik ─────────
        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            // Animasi transisi: fade
            overridePendingTransition(android.R.anim.fade_in,
                    android.R.anim.fade_out);
        }, SPLASH_DURATION);
    }
}