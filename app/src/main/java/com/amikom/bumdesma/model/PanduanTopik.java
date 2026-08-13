package com.amikom.bumdesma;
// Kalau project kamu pakai subpackage model, pindahkan ke: com.amikom.bumdesma.model

public class PanduanTopik {

    private final int iconResId;
    private final String judul;
    private final String isi;
    private boolean expanded;

    public PanduanTopik(int iconResId, String judul, String isi) {
        this.iconResId = iconResId;
        this.judul = judul;
        this.isi = isi;
        this.expanded = false;
    }

    public int getIconResId() {
        return iconResId;
    }

    public String getJudul() {
        return judul;
    }

    public String getIsi() {
        return isi;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}