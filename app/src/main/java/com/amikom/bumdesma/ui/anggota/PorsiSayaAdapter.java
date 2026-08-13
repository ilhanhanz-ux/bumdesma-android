package com.amikom.bumdesma.ui.anggota;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.PorsiSaya;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PorsiSayaAdapter extends RecyclerView.Adapter<PorsiSayaAdapter.VH> {

    public interface OnItemClick { void onClick(PorsiSaya item); }

    private final List<PorsiSaya> data;
    private final OnItemClick listener;

    public PorsiSayaAdapter(List<PorsiSaya> data, OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_porsi_saya, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PorsiSaya item = data.get(position);

        h.tvJudul.setText("Angsuran ke-" + item.getNoAngsuran());
        h.tvJatuhTempo.setText("Jatuh tempo: " + formatTanggal(item.getTanggalJatuhTempo()));
        h.tvNominal.setText(formatRupiah(item.getJumlahPorsi()));

        switch (item.getStatusBayar()) {
            case "sudah_bayar":
                h.tvStatus.setText("LUNAS");
                h.tvStatus.setBackgroundColor(0xFF2E7D32);
                break;
            case "menunggu_verifikasi":
                h.tvStatus.setText("MENUNGGU VERIFIKASI");
                h.tvStatus.setBackgroundColor(0xFF00796B);
                break;
            case "ditolak":
                h.tvStatus.setText("DITOLAK, UPLOAD ULANG");
                h.tvStatus.setBackgroundColor(0xFFC62828);
                break;
            default: // belum_bayar
                boolean telat = item.getHariTerlambat() > 0;
                h.tvStatus.setText(telat ? "TERLAMBAT " + item.getHariTerlambat() + " HARI" : "BELUM BAYAR");
                h.tvStatus.setBackgroundColor(telat ? 0xFFC62828 : 0xFFE65100);
                break;
        }

        h.itemView.setOnClickListener(v -> listener.onClick(item));
    }

    @Override
    public int getItemCount() { return data.size(); }

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

    static class VH extends RecyclerView.ViewHolder {
        TextView tvJudul, tvJatuhTempo, tvNominal, tvStatus;
        VH(@NonNull View itemView) {
            super(itemView);
            tvJudul       = itemView.findViewById(R.id.tv_judul_angsuran);
            tvJatuhTempo  = itemView.findViewById(R.id.tv_jatuh_tempo);
            tvNominal     = itemView.findViewById(R.id.tv_nominal_porsi);
            tvStatus      = itemView.findViewById(R.id.tv_status_badge);
        }
    }
}