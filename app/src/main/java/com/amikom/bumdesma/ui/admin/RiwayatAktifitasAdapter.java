package com.amikom.bumdesma.ui.admin;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.RiwayatAktifitasItem;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RiwayatAktifitasAdapter extends RecyclerView.Adapter<RiwayatAktifitasAdapter.ViewHolder> {

    private final List<RiwayatAktifitasItem> data;
    private final NumberFormat fmtRupiah =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public RiwayatAktifitasAdapter(List<RiwayatAktifitasItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_riwayat_aktifitas, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RiwayatAktifitasItem item = data.get(position);
        String tipe = item.getTipe() != null ? item.getTipe() : "";
        Context ctx = holder.itemView.getContext();

        String ikon; int bgRes; String judul; int warnaNominal;

        switch (tipe) {
            case "proposal_baru":
                ikon = "📝"; bgRes = R.drawable.bg_circle_blue;
                judul = (item.getNama() != null ? item.getNama() : "Anggota") + " mengajukan pinjaman";
                warnaNominal = ContextCompat.getColor(ctx, R.color.da_riwayat_badge_navy);
                break;
            case "proposal_disetujui":
                ikon = "✅"; bgRes = R.drawable.bg_circle_green;
                judul = "Proposal " + (item.getNama() != null ? item.getNama() : "-") + " disetujui";
                warnaNominal = ContextCompat.getColor(ctx, R.color.da_riwayat_badge_green);
                break;
            case "proposal_ditolak":
                ikon = "❌"; bgRes = R.drawable.bg_circle_red;
                judul = "Proposal " + (item.getNama() != null ? item.getNama() : "-") + " ditolak";
                warnaNominal = ContextCompat.getColor(ctx, R.color.da_riwayat_badge_red);
                break;
            case "proposal_revisi":
                ikon = "🔄"; bgRes = R.drawable.bg_circle_orange;
                judul = "Proposal " + (item.getNama() != null ? item.getNama() : "-") + " diminta revisi";
                warnaNominal = ContextCompat.getColor(ctx, R.color.da_riwayat_badge_orange);
                break;
            case "kredit_cair":
                ikon = "💰"; bgRes = R.drawable.bg_circle_navy;
                judul = "Kredit dicairkan untuk " + (item.getNama() != null ? item.getNama() : "-");
                warnaNominal = ContextCompat.getColor(ctx, R.color.da_riwayat_badge_navy);
                break;
            case "pembayaran":
                ikon = "🧾"; bgRes = R.drawable.bg_circle_green;
                judul = (item.getNama() != null ? item.getNama() : "Anggota") + " membayar angsuran";
                warnaNominal = ContextCompat.getColor(ctx, R.color.da_riwayat_badge_green);
                break;
            case "pengumuman":
                ikon = "📢"; bgRes = R.drawable.bg_circle_purple;
                judul = "Pengumuman baru diterbitkan";
                warnaNominal = ContextCompat.getColor(ctx, R.color.da_riwayat_badge_purple);
                break;
            // ── BARU: riwayat verifikasi akun anggota ──
            case "anggota_diterima":
                ikon = "🙋"; bgRes = R.drawable.bg_circle_green;
                judul = (item.getNama() != null ? item.getNama() : "Anggota") + " terverifikasi sebagai anggota";
                warnaNominal = ContextCompat.getColor(ctx, R.color.da_riwayat_badge_green);
                break;
            case "anggota_ditolak":
                ikon = "🚫"; bgRes = R.drawable.bg_circle_red;
                judul = "Pendaftaran " + (item.getNama() != null ? item.getNama() : "-") + " ditolak";
                warnaNominal = ContextCompat.getColor(ctx, R.color.da_riwayat_badge_red);
                break;
            default:
                ikon = "•"; bgRes = R.drawable.bg_circle_blue;
                judul = "Aktivitas";
                warnaNominal = ContextCompat.getColor(ctx, R.color.da_riwayat_badge_default);
        }

        holder.tvIkon.setText(ikon);
        holder.bgIkon.setBackgroundResource(bgRes);
        holder.tvJudul.setText(judul);
        holder.tvSubjudul.setText(item.getKeterangan() != null ? item.getKeterangan() : "");
        holder.tvSubjudul.setVisibility(
                (item.getKeterangan() == null || item.getKeterangan().trim().isEmpty())
                        ? View.GONE : View.VISIBLE);

        if (item.getNominal() != null) {
            holder.tvNominal.setText(fmtRupiah.format(item.getNominal()));
            holder.tvNominal.setTextColor(warnaNominal);
            holder.tvNominal.setVisibility(View.VISIBLE);
        } else {
            holder.tvNominal.setVisibility(View.GONE);
        }

        holder.tvWaktu.setText(formatWaktuRelatif(item.getWaktu()));
    }

    private String formatWaktuRelatif(String waktuSql) {
        if (waktuSql == null || waktuSql.trim().isEmpty()) return "-";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            Date tanggal = in.parse(waktuSql);
            if (tanggal == null) return waktuSql;

            long diffMs  = System.currentTimeMillis() - tanggal.getTime();
            long diffMin = diffMs / 60000;

            if (diffMin < 1)   return "Baru saja";
            if (diffMin < 60)  return diffMin + " menit lalu";
            long diffJam = diffMin / 60;
            if (diffJam < 24)  return diffJam + " jam lalu";
            long diffHari = diffJam / 24;
            if (diffHari < 7)  return diffHari + " hari lalu";

            SimpleDateFormat out = new SimpleDateFormat("d MMM yyyy", new Locale("id", "ID"));
            return out.format(tanggal);
        } catch (ParseException e) {
            return waktuSql;
        }
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View bgIkon;
        TextView tvIkon, tvJudul, tvSubjudul, tvNominal, tvWaktu;
        ViewHolder(View itemView) {
            super(itemView);
            bgIkon     = itemView.findViewById(R.id.bg_ikon);
            tvIkon     = itemView.findViewById(R.id.tv_ikon);
            tvJudul    = itemView.findViewById(R.id.tv_judul);
            tvSubjudul = itemView.findViewById(R.id.tv_subjudul);
            tvNominal  = itemView.findViewById(R.id.tv_nominal);
            tvWaktu    = itemView.findViewById(R.id.tv_waktu);
        }
    }
}