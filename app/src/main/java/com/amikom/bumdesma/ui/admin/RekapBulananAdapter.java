package com.amikom.bumdesma.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.RekapBulananItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RekapBulananAdapter extends RecyclerView.Adapter<RekapBulananAdapter.ViewHolder> {

    private final List<RekapBulananItem> data;
    private final NumberFormat fmtRupiah =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public RekapBulananAdapter(List<RekapBulananItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rekap_bulanan, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RekapBulananItem item = data.get(position);
        holder.tvNama.setText(item.getNamaLengkap());
        holder.tvDetail.setText(item.getNoKredit() + " • Angsuran ke-" + item.getNoAngsuran()
                + " • " + item.getTanggalBayar());
        holder.tvTotal.setText(fmtRupiah.format(item.getTotalBayar()));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvDetail, tvTotal;
        ViewHolder(View itemView) {
            super(itemView);
            tvNama   = itemView.findViewById(R.id.tv_nama);
            tvDetail = itemView.findViewById(R.id.tv_detail);
            tvTotal  = itemView.findViewById(R.id.tv_total);
        }
    }
}