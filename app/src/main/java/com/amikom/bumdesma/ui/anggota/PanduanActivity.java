package com.amikom.bumdesma.ui.anggota;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.PanduanAdapter;
import com.amikom.bumdesma.PanduanTopik;

import java.util.ArrayList;
import java.util.List;

public class PanduanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_panduan);

        Toolbar toolbar = findViewById(R.id.toolbarPanduan);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Panduan Aplikasi");
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        RecyclerView rvPanduan = findViewById(R.id.rvPanduan);
        rvPanduan.setLayoutManager(new LinearLayoutManager(this));
        rvPanduan.setHasFixedSize(true);

        PanduanAdapter adapter = new PanduanAdapter(buatDaftarPanduan());
        rvPanduan.setAdapter(adapter);
    }

    private List<PanduanTopik> buatDaftarPanduan() {
        List<PanduanTopik> daftar = new ArrayList<>();

        daftar.add(new PanduanTopik(
                R.drawable.ic_dashboard,
                "Mengenal Halaman Dashboard",
                "Halaman Dashboard adalah tampilan utama saat kamu masuk ke aplikasi.\n\n"
                        + "1. Kartu ringkasan di bagian atas menampilkan status pinjaman dan sisa angsuran kamu.\n"
                        + "2. Carousel pengumuman menampilkan info terbaru dari BUMDesma, geser ke kiri/kanan untuk melihat semua.\n"
                        + "3. Menu cepat (quick menu) berisi jalan pintas ke fitur yang sering dipakai, seperti Ajukan Pinjaman dan Jadwal Angsuran.\n"
                        + "4. Gunakan menu navigasi di bagian bawah layar (Beranda, Aktivitas, Notifikasi, Akun) untuk berpindah halaman."
        ));

        daftar.add(new PanduanTopik(
                R.drawable.ic_pinjaman,
                "Cara Mengajukan Pinjaman",
                "Ikuti langkah berikut untuk mengajukan pinjaman baru:\n\n"
                        + "1. Buka menu Ajukan Pinjaman dari Dashboard atau menu cepat.\n"
                        + "2. Isi jumlah pinjaman yang diajukan dan tujuan penggunaan dana.\n"
                        + "3. Pastikan data kelompok SPP dan data diri kamu sudah sesuai.\n"
                        + "4. Kirim pengajuan, lalu tunggu proses verifikasi dari admin BUMDesma.\n"
                        + "5. Status pengajuan (Diajukan, Disetujui, Revisi, atau Ditolak) bisa dipantau di menu Aktivitas.\n"
                        + "6. Setelah disetujui, jadwal angsuran akan otomatis dibuat dan bisa dilihat di menu Jadwal Angsuran."
        ));

        daftar.add(new PanduanTopik(
                R.drawable.ic_angsuran,
                "Cara Cek Jadwal dan Bayar Angsuran",
                "Panduan untuk memantau dan membayar angsuran:\n\n"
                        + "1. Buka menu Jadwal Angsuran untuk melihat daftar tagihan beserta tanggal jatuh tempo.\n"
                        + "2. Gunakan kolom pencarian atau filter untuk menemukan tagihan tertentu.\n"
                        + "3. Pilih tagihan yang ingin dibayar, lalu buka menu Setoran Angsuran.\n"
                        + "4. Unggah bukti pembayaran dengan mengambil foto langsung atau memilih dari galeri.\n"
                        + "5. Kirim setoran, admin akan memverifikasi pembayaran kamu.\n"
                        + "6. Riwayat pembayaran yang sudah terverifikasi bisa dilihat di menu Aktivitas."
        ));

        daftar.add(new PanduanTopik(
                R.drawable.ic_profil,
                "Mengelola Profil dan Akun",
                "Panduan untuk mengatur data akun kamu:\n\n"
                        + "1. Buka menu Akun di navigasi bawah, lalu pilih Profil Saya.\n"
                        + "2. Di halaman ini kamu bisa melihat data diri, kelompok SPP, dan status keanggotaan.\n"
                        + "3. Untuk mengganti password, buka menu Ganti Password dan ikuti langkah verifikasinya.\n"
                        + "4. Kamu juga bisa mengaktifkan Mode Gelap (dark mode) melalui pengaturan tampilan di halaman ini.\n"
                        + "5. Untuk keluar dari akun, tekan tombol Logout di bagian bawah halaman Profil."
        ));

        daftar.add(new PanduanTopik(
                R.drawable.ic_bantuan,
                "Butuh Bantuan Lebih Lanjut?",
                "Kalau masih ada kendala yang belum terjawab di panduan ini:\n\n"
                        + "1. Buka menu Bantuan (FAQ) untuk pertanyaan-pertanyaan umum lainnya.\n"
                        + "2. Hubungi pengurus BUMDesma di kelompok SPP kamu untuk bantuan langsung.\n"
                        + "3. Sampaikan kendala secara detail (nama menu, langkah yang dilakukan) agar lebih cepat dibantu."
        ));

        return daftar;
    }
}