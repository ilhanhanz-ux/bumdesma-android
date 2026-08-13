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
import com.amikom.bumdesma.model.AlokasiAnggota;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

// Adapter layar Alokasi Pinjaman ke Anggota (ketua). Isi SEKALI setelah
// kredit dicairkan -- dasar hitung proporsi porsi angsuran bulanan nanti.
public class AlokasiPinjamanAdapter extends RecyclerView.Adapter<AlokasiPinjamanAdapter.ViewHolder> {

    public interface OnTotalChangedListener {
        void onChanged(double totalTerisi);
    }

    private final List<AlokasiAnggota> data;
    private final OnTotalChangedListener listener;
    private final boolean terkunciSemua;
    private final Map<Integer, Double> nilaiSaatIni = new LinkedHashMap<>();

    public AlokasiPinjamanAdapter(List<AlokasiAnggota> data, boolean terkunciSemua, OnTotalChangedListener listener) {
        this.data = data;
        this.terkunciSemua = terkunciSemua;
        this.listener = listener;
        for (AlokasiAnggota a : data) {
            nilaiSaatIni.put(a.getAnggotaId(), a.getJumlahPokok());
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_atur_porsi_anggota, parent, false); // recycle layout yang sudah ada
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AlokasiAnggota item = data.get(position);
        holder.tvNama.setText(item.getNamaLengkap() != null ? item.getNamaLengkap() : "-");

        if (holder.watcher != null) holder.etNominal.removeTextChangedListener(holder.watcher);

        holder.etNominal.setEnabled(!terkunciSemua);
        double nilaiAwal = nilaiSaatIni.containsKey(item.getAnggotaId())
                ? nilaiSaatIni.get(item.getAnggotaId()) : item.getJumlahPokok();
        holder.etNominal.setText(nilaiAwal > 0 ? formatRibuan(String.valueOf(Math.round(nilaiAwal))) : "");

        if (terkunciSemua) {
            holder.tvStatusKunci.setVisibility(View.VISIBLE);
            holder.tvStatusKunci.setText("Terkunci, sudah ada setoran berjalan");
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

    private String formatRibuan(String hanyaAngka) {
        try {
            long nilai = Long.parseLong(hanyaAngka);
            return String.format(Locale.US, "%,d", nilai).replace(',', '.');
        } catch (NumberFormatException e) {
            return hanyaAngka;
        }
    }

    @Override
    public int getItemCount() { return data.size(); }

    private void notifyTotal() {
        double total = 0;
        for (double v : nilaiSaatIni.values()) total += v;
        if (listener != null) listener.onChanged(total);
    }

    public void kirimTotalAwal() { notifyTotal(); }

    public Map<Integer, Double> getNilaiSaatIni() { return nilaiSaatIni; }

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