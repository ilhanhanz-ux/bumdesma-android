package com.amikom.bumdesma.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.Proposal;
import com.amikom.bumdesma.utils.Constants;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProposalAdapter extends
        RecyclerView.Adapter<ProposalAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Proposal proposal);
    }

    private final List<Proposal> list;
    private final OnItemClickListener listener;
    private final NumberFormat fmt =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public ProposalAdapter(List<Proposal> list, OnItemClickListener listener) {
        this.list     = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_proposal, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Proposal p   = list.get(pos);
        Context  ctx = h.itemView.getContext();

        h.tvNama.setText(p.getNamaPengaju() != null ? p.getNamaPengaju() : "-");
        h.tvNoProposal.setText(p.getNoProposal() != null ? p.getNoProposal() : "-");
        h.tvJumlah.setText(fmt.format(p.getJumlahPinjaman()));
        h.tvTanggal.setText(p.getTanggalPengajuan() != null
                ? p.getTanggalPengajuan() : "-");

        // ── BARU: kalau disetujui sebagian (jumlah_disetujui < jumlah_pinjaman),
        // tampilkan baris tambahan biar anggota/admin tau persis jumlah yang cair ──
        Double disetujui = p.getJumlahDisetujui();
        if (disetujui != null && disetujui < p.getJumlahPinjaman()) {
            h.tvJumlahDisetujui.setVisibility(View.VISIBLE);
            h.tvJumlahDisetujui.setText(
                    "Disetujui: " + fmt.format(disetujui) + " (dana kas terbatas)");
        } else {
            h.tvJumlahDisetujui.setVisibility(View.GONE);
        }

        String kelompok = (p.getNamaKelompok() != null ? p.getNamaKelompok() : "")
                + (p.getNamaDesa() != null ? " — " + p.getNamaDesa() : "");
        h.tvKelompok.setText(kelompok.trim());

        // Badge status
        String status = p.getStatus() != null ? p.getStatus() : "";
        h.tvStatus.setText(labelStatus(status));
        switch (status) {
            case Constants.DISETUJUI:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_disetujui);
                h.tvStatus.setTextColor(ctx.getColor(R.color.status_disetujui));
                break;
            case Constants.DITOLAK:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_ditolak);
                h.tvStatus.setTextColor(ctx.getColor(R.color.status_ditolak));
                break;
            default:
                h.tvStatus.setBackgroundResource(R.drawable.bg_badge_menunggu);
                h.tvStatus.setTextColor(ctx.getColor(R.color.status_menunggu));
        }

        // Catatan admin — tampil hanya kalau ada isinya
        String catatan = p.getCatatanAdmin();
        if (catatan != null && !catatan.trim().isEmpty()) {
            h.tvCatatanAdmin.setVisibility(View.VISIBLE);
            h.tvCatatanAdmin.setText("📝 Catatan: " + catatan.trim());
        } else {
            h.tvCatatanAdmin.setVisibility(View.GONE);
        }

        h.card.setOnClickListener(v -> listener.onItemClick(p));
    }

    @Override
    public int getItemCount() { return list.size(); }

    private String labelStatus(String s) {
        switch (s) {
            case Constants.DISETUJUI: return "Disetujui ✓";
            case Constants.DITOLAK:   return "Ditolak ✗";
            case Constants.REVISI:    return "Revisi";
            default:                  return "Menunggu ⏳";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView card;
        TextView tvNama, tvKelompok, tvJumlah, tvJumlahDisetujui,
                tvNoProposal, tvTanggal, tvStatus, tvCatatanAdmin;

        ViewHolder(View v) {
            super(v);
            card              = v.findViewById(R.id.card_proposal);
            tvNama            = v.findViewById(R.id.tv_nama_pengaju);
            tvKelompok        = v.findViewById(R.id.tv_kelompok);
            tvJumlah          = v.findViewById(R.id.tv_jumlah_pinjaman);
            tvJumlahDisetujui = v.findViewById(R.id.tv_jumlah_disetujui);
            tvNoProposal      = v.findViewById(R.id.tv_no_proposal);
            tvTanggal         = v.findViewById(R.id.tv_tanggal);
            tvStatus          = v.findViewById(R.id.tv_status);
            tvCatatanAdmin    = v.findViewById(R.id.tv_catatan_admin);
        }
    }
}