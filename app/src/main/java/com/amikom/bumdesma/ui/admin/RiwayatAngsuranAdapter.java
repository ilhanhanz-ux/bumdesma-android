package com.amikom.bumdesma.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.Angsuran;
import com.amikom.bumdesma.utils.SkorKepatuhanUtil;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RiwayatAngsuranAdapter extends RecyclerView.Adapter<RiwayatAngsuranAdapter.ViewHolder> {

    private final List<Angsuran> data;
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public RiwayatAngsuranAdapter(List<Angsuran> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_riwayat_angsuran, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Angsuran item = data.get(position);

        holder.tvAngsuranKe.setText("Angsuran ke-" + item.getNoAngsuran());
        holder.tvTotalBayar.setText(fmt.format(item.getTotalBayar()));

        String jatuhTempo = formatTanggal(item.getTanggalJatuhTempo());
        String dibayar = item.getTanggalBayar() != null
                ? formatTanggal(item.getTanggalBayar())
                : null;

        holder.tvTanggalInfo.setText(dibayar != null
                ? "Jatuh tempo: " + jatuhTempo + "  •  Dibayar: " + dibayar
                : "Jatuh tempo: " + jatuhTempo);

        String statusBaris = SkorKepatuhanUtil.statusBaris(item);
        String label;
        int bgRes;
        int textColor;

        switch (statusBaris) {
            case "tepat_waktu":
                label = "Tepat Waktu";
                bgRes = R.drawable.bg_chip_pill_green;
                textColor = 0xFF2E7D32;
                break;
            case "terlambat_bayar":
                label = "Terlambat";
                bgRes = R.drawable.bg_chip_pill_orange;
                textColor = 0xFFEF6C00;
                break;
            case "menunggak":
                label = "Menunggak";
                bgRes = R.drawable.bg_chip_pill_red;
                textColor = 0xFFC62828;
                break;
            default:
                label = "Belum Jatuh Tempo";
                bgRes = R.drawable.bg_info_box;
                textColor = 0xFF757575;
        }

        holder.tvStatusBaris.setText(label);
        holder.tvStatusBaris.setBackgroundResource(bgRes);
        holder.tvStatusBaris.setTextColor(textColor);
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    private String formatTanggal(String tanggalSql) {
        if (tanggalSql == null || tanggalSql.trim().isEmpty()) return "-";
        try {
            SimpleDateFormat in  = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            SimpleDateFormat out = new SimpleDateFormat("d MMM yyyy", new Locale("id", "ID"));
            Date date = in.parse(tanggalSql);
            return date != null ? out.format(date) : tanggalSql;
        } catch (ParseException e) {
            return tanggalSql;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAngsuranKe, tvStatusBaris, tvTanggalInfo, tvTotalBayar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAngsuranKe  = itemView.findViewById(R.id.tv_angsuran_ke);
            tvStatusBaris = itemView.findViewById(R.id.tv_status_baris);
            tvTanggalInfo = itemView.findViewById(R.id.tv_tanggal_info);
            tvTotalBayar  = itemView.findViewById(R.id.tv_total_bayar);
        }
    }
}