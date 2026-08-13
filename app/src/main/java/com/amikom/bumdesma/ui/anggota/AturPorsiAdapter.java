package com.amikom.bumdesma.ui.anggota;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.AngsuranPorsi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

// Adapter buat layar Atur Porsi Angsuran (ketua). Tiap baris = 1 anggota
// dengan kotak nominal yang bisa diedit, kecuali porsi anggota itu sudah
// disetor/diverifikasi (dikunci, sama seperti aturan di backend).
public class AturPorsiAdapter extends RecyclerView.Adapter<AturPorsiAdapter.ViewHolder> {

    public interface OnTotalChangedListener {
        void onChanged(double totalTerisi);
    }

    private final List<AngsuranPorsi> data;
    private final OnTotalChangedListener listener;
    // anggotaId -> nominal yang sedang diketik (live), dipakai untuk hitung total & submit
    private final Map<Integer, Double> nilaiSaatIni = new LinkedHashMap<>();

    public AturPorsiAdapter(List<AngsuranPorsi> data, OnTotalChangedListener listener) {
        this.data = data;
        this.listener = listener;
        // BARU: kalau baris ini belum pernah disimpan (jumlahPorsi masih 0),
        // pakai nominalDefault (hasil hitung proporsional dari alokasi pokok
        // pinjaman) sebagai nilai awal, bukan 0/kosong.
        for (AngsuranPorsi p : data) {
            double awal = p.getJumlahPorsi() > 0 ? p.getJumlahPorsi() : p.getNominalDefault();
            nilaiSaatIni.put(p.getAnggotaId(), awal);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_atur_porsi_anggota, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AngsuranPorsi item = data.get(position);
        holder.tvNama.setText(item.getNamaLengkap() != null ? item.getNamaLengkap() : "-");

        if (holder.watcher != null) {
            holder.etNominal.removeTextChangedListener(holder.watcher);
        }

        boolean terkunci = !item.isBelumBayar();
        holder.etNominal.setEnabled(!terkunci);

        double nilaiAwal = nilaiSaatIni.containsKey(item.getAnggotaId())
                ? nilaiSaatIni.get(item.getAnggotaId()) : item.getJumlahPorsi();
        holder.etNominal.setText(nilaiAwal > 0 ? formatRibuan(String.valueOf(Math.round(nilaiAwal))) : "");

        if (terkunci) {
            holder.tvStatusKunci.setVisibility(View.VISIBLE);
            String label = item.isSudahBayar() ? "Sudah lunas, tidak bisa diubah"
                    : item.isMenungguVerifikasi() ? "Menunggu verifikasi, tidak bisa diubah"
                    : "Tidak bisa diubah";
            holder.tvStatusKunci.setText(label);
        } else {
            holder.tvStatusKunci.setVisibility(View.GONE);
        }

        holder.watcher = new TextWatcher() {
            private boolean sedangFormat = false;

            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (sedangFormat) return;
                sedangFormat = true;

                String hanyaAngka = s.toString().replaceAll("[^\\d]", "");
                double nilai = hanyaAngka.isEmpty() ? 0 : Double.parseDouble(hanyaAngka);
                nilaiSaatIni.put(item.getAnggotaId(), nilai);
                notifyTotal();

                String terformat = hanyaAngka.isEmpty() ? "" : formatRibuan(hanyaAngka);
                if (!terformat.equals(s.toString())) {
                    holder.etNominal.setText(terformat);
                    holder.etNominal.setSelection(terformat.length());
                }

                sedangFormat = false;
            }
        };
        holder.etNominal.addTextChangedListener(holder.watcher);
    }

    /** "500000" -> "500.000". Dipakai buat tampilan saja, nilai mentah tetap disimpan terpisah di nilaiSaatIni. */
    private String formatRibuan(String hanyaAngka) {
        try {
            long nilai = Long.parseLong(hanyaAngka);
            return String.format(Locale.US, "%,d", nilai).replace(',', '.');
        } catch (NumberFormatException e) {
            return hanyaAngka;
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void notifyTotal() {
        double total = 0;
        for (double v : nilaiSaatIni.values()) total += v;
        if (listener != null) listener.onChanged(total);
    }

    /** Dipanggil sekali setelah adapter di-set ke RecyclerView, biar total awal langsung tampil. */
    public void kirimTotalAwal() {
        notifyTotal();
    }

    /** Map anggota_id -> nominal porsi (hasil edit terbaru), dipakai saat submit ke server. */
    public Map<Integer, Double> getNilaiSaatIni() {
        return nilaiSaatIni;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvStatusKunci;
        EditText etNominal;
        TextWatcher watcher;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama        = itemView.findViewById(R.id.tv_nama);
            tvStatusKunci = itemView.findViewById(R.id.tv_status_kunci);
            etNominal     = itemView.findViewById(R.id.et_nominal);
        }
    }
}