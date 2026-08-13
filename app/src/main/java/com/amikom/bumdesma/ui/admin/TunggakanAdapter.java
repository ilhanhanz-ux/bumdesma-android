package com.amikom.bumdesma.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.TunggakanItem;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class TunggakanAdapter extends RecyclerView.Adapter<TunggakanAdapter.ViewHolder> {

    private final List<TunggakanItem> data;
    private final NumberFormat fmtRupiah =
            NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public TunggakanAdapter(List<TunggakanItem> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tunggakan, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TunggakanItem item = data.get(position);
        holder.tvNama.setText(item.getNamaLengkap());
        holder.tvKelompokDesa.setText(item.getNamaKelompok() + " • " + item.getNamaDesa());
        holder.tvNoKredit.setText(item.getNoKredit());
        holder.tvNominal.setText(fmtRupiah.format(item.getTotalBayar()));
        holder.tvHariTunggak.setText(item.getHariTunggak() + " hari");
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvKelompokDesa, tvNoKredit, tvNominal, tvHariTunggak;
        ViewHolder(View itemView) {
            super(itemView);
            tvNama         = itemView.findViewById(R.id.tv_nama);
            tvKelompokDesa = itemView.findViewById(R.id.tv_kelompok_desa);
            tvNoKredit     = itemView.findViewById(R.id.tv_no_kredit);
            tvNominal      = itemView.findViewById(R.id.tv_nominal);
            tvHariTunggak  = itemView.findViewById(R.id.tv_hari_tunggak);
        }
    }
}