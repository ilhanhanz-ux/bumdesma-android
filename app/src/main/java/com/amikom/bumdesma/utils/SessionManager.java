package com.amikom.bumdesma.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private final SharedPreferences pref;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        pref   = context.getSharedPreferences(Constants.PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // Simpan data login setelah berhasil masuk (dipertahankan biar call site lama tetap jalan;
    // isKetua default false — dipakai kalau login sebagai admin misalnya)
    public void saveSession(String token, String role, int userId,
                            Integer anggotaId, String nama) {
        saveSession(token, role, userId, anggotaId, nama, false);
    }

    // ── BARU: overload dengan status ketua. Panggil ini dari LoginActivity
    // pakai response.getData().isKetua() ──
    public void saveSession(String token, String role, int userId,
                            Integer anggotaId, String nama, boolean isKetua) {
        editor.putBoolean(Constants.KEY_IS_LOGIN,   true);
        editor.putString(Constants.KEY_TOKEN,       token);
        editor.putString(Constants.KEY_ROLE,        role);
        editor.putInt(Constants.KEY_USER_ID,        userId);
        editor.putString(Constants.KEY_NAMA,        nama);
        if (anggotaId != null) {
            editor.putInt(Constants.KEY_ANGGOTA_ID, anggotaId);
        }
        // ── BARU ──
        editor.putBoolean(Constants.KEY_IS_KETUA, isKetua);
        editor.apply();
    }

    // Ambil token untuk dikirim ke API (format: "Bearer xxxxx")
    public String getBearerToken() {
        return "Bearer " + pref.getString(Constants.KEY_TOKEN, "");
    }

    public String  getRole()      { return pref.getString(Constants.KEY_ROLE, ""); }
    public int     getUserId()    { return pref.getInt(Constants.KEY_USER_ID, 0); }
    public int     getAnggotaId() { return pref.getInt(Constants.KEY_ANGGOTA_ID, 0); }
    public String  getNama()      { return pref.getString(Constants.KEY_NAMA, ""); }
    public boolean isLoggedIn()   { return pref.getBoolean(Constants.KEY_IS_LOGIN, false); }
    public boolean isAdmin()      { return Constants.ROLE_ADMIN.equals(getRole()); }
    public boolean isAnggota()    { return Constants.ROLE_ANGGOTA.equals(getRole()); }
    // ── BARU ──
    public boolean isKetua()      { return pref.getBoolean(Constants.KEY_IS_KETUA, false); }

    // Perbarui nama tersimpan secara lokal (dipakai fitur "Ubah Nama" di Profil Admin)
    public void setNama(String nama) {
        editor.putString(Constants.KEY_NAMA, nama);
        editor.apply();
    }

    // Hapus semua data sesi (logout)
    public void logout() {
        editor.clear();
        editor.apply();
    }
}