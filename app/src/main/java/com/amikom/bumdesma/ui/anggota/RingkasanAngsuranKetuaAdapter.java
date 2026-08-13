package com.amikom.bumdesma.ui.anggota;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.RingkasanAngsuranKetua;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RingkasanAngsuranKetuaAdapter
        extends RecyclerView.Adapter<RingkasanAngsuranKetuaAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(RingkasanAngsuranKetua item);
    }

    private final List<RingkasanAngsuranKetua> list;
    private final OnItemClickListener listener;

    public RingkasanAngsuranKetuaAdapter(List<RingkasanAngsuranKetua> list, OnItemClickListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ringkasan_angsuran_ketua, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RingkasanAngsuranKetua a = list.get(position);

        holder.tvBadge.setText(String.valueOf(a.getNoAngsuran()));
        holder.tvJudul.setText("Angsuran ke-" + a.getNoAngsuran());
        holder.tvJatuhTempo.setText("Jatuh tempo: " + formatTanggal(a.getTanggalJatuhTempo()));
        holder.tvTotal.setText(formatRupiah(a.getTotalBayar()));
        holder.tvProgres.setText("Porsi terbagi: " + formatRupiah(a.getTotalDibagi())
                + " / " + formatRupiah(a.getTotalBayar()));

        int warna;
        String label;
        if (a.isLunas()) {
            warna = Color.parseColor("#2E7D32");
            label = "LUNAS";
        } else if (a.isSudahLengkap()) {
            // Resource khusus anggota dengan varian mode gelap yang jelas lebih terang
            // (bukan admin_accent_text yang dipakai di banyak layar admin lain).
            warna = holder.itemView.getContext().getColor(R.color.da_status_sudah_dibagi);
            label = "SUDAH DIBAGI";
        } else {
            warna = Color.parseColor("#F57F17");
            label = "BELUM LENGKAP";
        }
        holder.tvProgres.setTextColor(warna);
        holder.tvStatus.setText(label);
        holder.tvStatus.getBackground().mutate().setTint(warna);

        holder.itemView.setOnClickListener(v -> listener.onClick(a));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private String formatRupiah(double nilai) {
        long rounded = Math.round(nilai);
        String s = String.valueOf(rounded);
        StringBuilder sb = new StringBuilder();
        int count = 0;
        for (int i = s.length() - 1; i >= 0; i--) {
            sb.insert(0, s.charAt(i));
            count++;
            if (count % 3 == 0 && i != 0) sb.insert(0, '.');
        }
        return "Rp " + sb;
    }

    private String formatTanggal(String tanggalSql) {
        if (tanggalSql == null || tanggalSql.isEmpty()) return "-";
        try {
            SimpleDateFormat input  = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat output = new SimpleDateFormat("dd MMMM yyyy", new Locale("id", "ID"));
            return output.format(input.parse(tanggalSql));
        } catch (ParseException e) {
            return tanggalSql;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBadge, tvJudul, tvJatuhTempo, tvTotal, tvStatus, tvProgres;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBadge      = itemView.findViewById(R.id.tv_no_angsuran_badge);
            tvJudul      = itemView.findViewById(R.id.tv_judul_angsuran);
            tvJatuhTempo = itemView.findViewById(R.id.tv_jatuh_tempo);
            tvTotal      = itemView.findViewById(R.id.tv_total_bayar);
            tvStatus     = itemView.findViewById(R.id.tv_status_badge);
            tvProgres    = itemView.findViewById(R.id.tv_progres_porsi);
        }
    }
}