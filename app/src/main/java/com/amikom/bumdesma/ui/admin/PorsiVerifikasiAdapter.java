package com.amikom.bumdesma.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.PorsiVerifikasi;
import com.amikom.bumdesma.utils.Constants;
import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PorsiVerifikasiAdapter extends RecyclerView.Adapter<PorsiVerifikasiAdapter.VH> {

    public interface OnAksi {
        void onApprove(PorsiVerifikasi item);
        void onReject(PorsiVerifikasi item);
        void onLihatBukti(PorsiVerifikasi item);
    }

    private final List<PorsiVerifikasi> data;
    private final OnAksi listener;

    public PorsiVerifikasiAdapter(List<PorsiVerifikasi> data, OnAksi listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_porsi_verifikasi, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        PorsiVerifikasi item = data.get(position);

        h.tvNama.setText(item.getNamaLengkap());
        h.tvKelompok.setText(item.getNamaKelompok());
        h.tvAngsuran.setText("Angsuran ke-" + item.getNoAngsuran() + " • " + item.getNoKredit());
        h.tvNominal.setText(formatRupiah(item.getJumlahPorsi()));
        h.tvTanggalSetor.setText("Disetor: " + formatTanggal(item.getTanggalSetor()));

        if (item.getBuktiBayar() != null && !item.getBuktiBayar().isEmpty()) {
            Glide.with(h.itemView.getContext())
                    .load(Constants.buildFileUrl(item.getBuktiBayar()))
                    .placeholder(R.drawable.ic_placeholder_image)
                    .error(R.drawable.ic_placeholder_image)
                    .into(h.ivBukti);
        } else {
            h.ivBukti.setImageResource(R.drawable.ic_placeholder_image);
        }
        h.ivBukti.setOnClickListener(v -> listener.onLihatBukti(item));

        if (item.isMenunggu()) {
            h.layoutAksi.setVisibility(View.VISIBLE);
            h.tvStatusBadge.setVisibility(View.GONE);
            h.tvCatatanAdmin.setVisibility(View.GONE);

            h.btnApprove.setOnClickListener(v -> listener.onApprove(item));
            h.btnReject.setOnClickListener(v -> listener.onReject(item));
        } else {
            h.layoutAksi.setVisibility(View.GONE);
            h.tvStatusBadge.setVisibility(View.VISIBLE);

            if ("sudah_bayar".equals(item.getStatusBayar())) {
                h.tvStatusBadge.setText("DIVERIFIKASI, LUNAS");
                h.tvStatusBadge.setBackgroundColor(0xFF2E7D32);
                h.tvCatatanAdmin.setVisibility(View.GONE);
            } else { // ditolak
                h.tvStatusBadge.setText("DITOLAK");
                h.tvStatusBadge.setBackgroundColor(0xFFC62828);
                if (item.getCatatanAdmin() != null && !item.getCatatanAdmin().isEmpty()) {
                    h.tvCatatanAdmin.setText("Alasan: " + item.getCatatanAdmin());
                    h.tvCatatanAdmin.setVisibility(View.VISIBLE);
                } else {
                    h.tvCatatanAdmin.setVisibility(View.GONE);
                }
            }
        }
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
        TextView tvNama, tvKelompok, tvAngsuran, tvNominal, tvTanggalSetor,
                tvStatusBadge, tvCatatanAdmin;
        ImageView ivBukti;
        LinearLayout layoutAksi;
        android.widget.Button btnApprove, btnReject;

        VH(@NonNull View itemView) {
            super(itemView);
            tvNama          = itemView.findViewById(R.id.tv_nama_anggota);
            tvKelompok      = itemView.findViewById(R.id.tv_nama_kelompok);
            tvAngsuran      = itemView.findViewById(R.id.tv_angsuran_info);
            tvNominal       = itemView.findViewById(R.id.tv_nominal_porsi);
            tvTanggalSetor  = itemView.findViewById(R.id.tv_tanggal_setor);
            ivBukti         = itemView.findViewById(R.id.iv_bukti_thumb);
            layoutAksi      = itemView.findViewById(R.id.layout_aksi);
            btnApprove      = itemView.findViewById(R.id.btn_approve);
            btnReject       = itemView.findViewById(R.id.btn_reject);
            tvStatusBadge   = itemView.findViewById(R.id.tv_status_badge);
            tvCatatanAdmin  = itemView.findViewById(R.id.tv_catatan_admin);
        }
    }
}