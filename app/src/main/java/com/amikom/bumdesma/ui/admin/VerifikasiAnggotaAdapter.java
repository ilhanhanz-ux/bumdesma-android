package com.amikom.bumdesma.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.AnggotaVerifikasi;
import com.amikom.bumdesma.utils.Constants;

import java.util.List;

public class VerifikasiAnggotaAdapter
        extends RecyclerView.Adapter<VerifikasiAnggotaAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onClick(AnggotaVerifikasi anggota);
    }

    private final List<AnggotaVerifikasi> list;
    private final OnItemClickListener listener;

    public VerifikasiAnggotaAdapter(List<AnggotaVerifikasi> list, OnItemClickListener listener) {
        this.list     = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_verifikasi_anggota, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnggotaVerifikasi a = list.get(position);

        holder.tvNama.setText(safe(a.getNama()));

        String kelompokDesa = safe(a.getNamaKelompok()) + " • " + safe(a.getNamaDesa());
        holder.tvKelompokDesa.setText(kelompokDesa);

        holder.tvTempatTanggalLahir.setText(
                "Lahir " + safe(a.getTempatLahir()) + ", " + safe(a.getTanggalLahir()));

        // ── BARU: badge status — cuma relevan di tab Riwayat (diterima/ditolak).
        // Di tab Menunggu semua item statusnya sama (pending), jadi badge disembunyikan
        // biar nggak berulang-ulang nampilin info yang sama di tiap kartu.
        String status = a.getStatusVerifikasi() != null ? a.getStatusVerifikasi() : "";
        if (holder.tvStatus != null) {
            switch (status) {
                case Constants.VERIFIKASI_DITERIMA:
                    holder.tvStatus.setVisibility(View.VISIBLE);
                    holder.tvStatus.setText("✅ Diterima");
                    holder.tvStatus.setTextColor(
                            holder.itemView.getContext().getColor(R.color.status_disetujui));
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_disetujui);
                    break;
                case Constants.VERIFIKASI_DITOLAK:
                    holder.tvStatus.setVisibility(View.VISIBLE);
                    holder.tvStatus.setText("❌ Ditolak");
                    holder.tvStatus.setTextColor(
                            holder.itemView.getContext().getColor(R.color.status_ditolak));
                    holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_ditolak);
                    break;
                default:
                    holder.tvStatus.setVisibility(View.GONE);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(a);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    private String safe(String s) {
        return s != null && !s.isEmpty() ? s : "-";
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvKelompokDesa, tvTempatTanggalLahir, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama               = itemView.findViewById(R.id.tv_nama);
            tvKelompokDesa       = itemView.findViewById(R.id.tv_kelompok_desa);
            tvTempatTanggalLahir = itemView.findViewById(R.id.tv_tempat_tanggal_lahir);
            // ── BARU: boleh null sampai item_verifikasi_anggota.xml ditambah view ini ──
            tvStatus             = itemView.findViewById(R.id.tv_status_verif);
        }
    }
}