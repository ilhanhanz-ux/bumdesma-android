package com.amikom.bumdesma.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("success") private boolean success;
    @SerializedName("message") private String message;
    @SerializedName("data")    private Data data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Data getData()      { return data; }

    public static class Data {
        @SerializedName("token")      private String token;
        @SerializedName("role")       private String role;
        @SerializedName("user_id")    private int userId;
        @SerializedName("anggota_id") private Integer anggotaId;
        @SerializedName("nama")       private String nama;
        // ── BARU ──
        @SerializedName("is_ketua")   private int isKetua;

        public String  getToken()     { return token; }
        public String  getRole()      { return role; }
        public int     getUserId()    { return userId; }
        public Integer getAnggotaId() { return anggotaId; }
        public String  getNama()      { return nama; }
        // ── BARU ──
        public boolean isKetua()      { return isKetua == 1; }
    }
}