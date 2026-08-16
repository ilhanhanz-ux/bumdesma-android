# BUMDesma Android

Aplikasi **Sistem Informasi Pencatatan Kredit** berbasis Android untuk BUMDesma (Badan Usaha Milik Desa Bersama), dibangun untuk mendigitalisasi pencatatan kredit pada Program Simpan Pinjam Perempuan (SPP). Aplikasi ini dibangun menggunakan **Kotlin/Java (Android native)** dan **PHP + MySQL** sebagai backend, sehingga pengelola (admin) dan anggota kelompok SPP dapat mencatat, memverifikasi, dan memantau proses pinjaman-angsuran secara real-time melalui antarmuka mobile.

---

## Tentang Penelitian

Repository ini merupakan implementasi dari penelitian skripsi berjudul:

> **Sistem Informasi Pencatatan Kredit pada BUMDesma Randudongkal Berbasis Android**

Penelitian ini dilakukan pada **BUMDesma Mandiri Sejahtera LKD Randudongkal**, sebuah badan usaha milik desa bersama yang menjalankan program simpan pinjam perempuan (Program Simpan Pinjam Perempuan/SPP). Penelitian bertujuan untuk merancang dan membangun sistem informasi berbasis Android yang mampu menggantikan pencatatan manual dalam proses pengajuan pinjaman, verifikasi, penyaluran dana, hingga pencatatan angsuran.

Tahapan penelitian mengikuti metode **SDLC Waterfall**, meliputi:

1. Identifikasi Masalah
2. Pengumpulan Data
3. Analisis Kebutuhan Sistem
4. Perancangan Sistem
5. Implementasi Sistem
6. Pengujian Sistem
7. Evaluasi Sistem

---

## Fitur

**Untuk Anggota:**
- Registrasi dan login akun anggota, dengan verifikasi keanggotaan oleh admin
- Dashboard anggota dengan ringkasan kredit aktif dan tagihan bulan berjalan
- Pengajuan proposal pinjaman (khusus ketua kelompok), dengan limit pengajuan dihitung otomatis berdasarkan jumlah anggota aktif kelompok
- Jadwal dan riwayat angsuran
- Setoran angsuran mandiri per porsi anggota, dengan unggah bukti transfer
- Notifikasi dan riwayat aktivitas
- Mode gelap/terang (dark mode) di seluruh halaman

**Untuk Admin:**
- Dashboard admin dengan statistik ringkas
- Verifikasi proposal pinjaman (setujui/tolak/minta revisi)
- Verifikasi pendaftaran anggota baru
- Pengelolaan data anggota dan kelompok SPP
- Pencatatan setoran angsuran (individu maupun kolektif per kelompok)
- Verifikasi bukti setoran porsi anggota
- Laporan keuangan dengan ekspor PDF
- Pengelolaan pengumuman

---

## Teknologi yang Digunakan

**Android (Client)**
- Kotlin & Java
- Retrofit + OkHttp untuk komunikasi REST API
- Glide untuk pemuatan gambar
- Material Components untuk antarmuka

**Backend**
- PHP native (satu skrip per entitas, menangani beberapa metode HTTP)
- MySQL sebagai basis data

---

## Menjalankan Aplikasi

Clone repository:
```
git clone https://github.com/ilhanhanz-ux/bumdesma-android.git
```

Masuk ke folder project:
```
cd bumdesma-android
```

Buka project menggunakan **Android Studio**, lalu tunggu proses *Gradle sync* selesai untuk mengunduh seluruh dependency (dikonfigurasi melalui `build.gradle.kts` dan `settings.gradle.kts`).

Sesuaikan `BASE_URL` dan `FILE_BASE_URL` pada file konfigurasi konstanta aplikasi agar mengarah ke alamat backend PHP yang sudah dijalankan (mis. XAMPP lokal atau hosting).

Build dan jalankan aplikasi melalui Android Studio (tombol Run ▶), atau melalui terminal menggunakan Gradle wrapper:
```
./gradlew assembleDebug
```
*(gunakan `gradlew.bat` sebagai ganti `./gradlew` jika berjalan di Windows Command Prompt)*

Aplikasi dapat dijalankan pada emulator atau perangkat Android (minimum SDK 24 / Android 7.0).

---

## Author

**Muhamad Ilhan Maulana Aziz**
Informatika – Universitas AMIKOM Yogyakarta

GitHub: [@ilhanhanz-ux](https://github.com/ilhanhanz-ux)
