package com.amikom.bumdesma.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.Angsuran;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class AngsuranAdapter extends RecyclerView.Adapter<AngsuranAdapter.ViewHolder> {

    public interface OnItemClick {
        void onClick(Angsuran angsuran);
    }

    private List<Angsuran> data;
    private final OnItemClick listener;

    public AngsuranAdapter(List<Angsuran> data, OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    public void setData(List<Angsuran> newData) {
        this.data = newData;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_angsuran, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Angsuran a = data.get(position);
        NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));

        holder.tvNama.setText(a.getNamaLengkap());
        holder.tvNoKredit.setText("No. Kredit: " + a.getNoKredit());
        holder.tvAngsuranKe.setText("Angsuran ke-" + a.getNoAngsuran());
        holder.tvJumlah.setText(rupiah.format(a.getTotalBayar()));
        holder.tvJatuhTempo.setText("Jatuh tempo: " + a.getTanggalJatuhTempo());

        if (a.isSudahBayar()) {
            holder.tvStatus.setText("Lunas");
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.status_lunas));
        } else if (a.isTerlambat()) {
            holder.tvStatus.setText("Terlambat " + a.getHariTerlambat() + " hari");
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.status_terlambat));
        } else {
            holder.tvStatus.setText("Belum Bayar");
            holder.tvStatus.setTextColor(
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.status_belum_bayar));
        }

        holder.itemView.setOnClickListener(v -> {
            if (!a.isSudahBayar()) {
                listener.onClick(a);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNama, tvNoKredit, tvAngsuranKe, tvJumlah, tvJatuhTempo, tvStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNama       = itemView.findViewById(R.id.tvNama);
            tvNoKredit   = itemView.findViewById(R.id.tvNoKredit);
            tvAngsuranKe = itemView.findViewById(R.id.tvAngsuranKe);
            tvJumlah     = itemView.findViewById(R.id.tvJumlah);
            tvJatuhTempo = itemView.findViewById(R.id.tvJatuhTempo);
            tvStatus     = itemView.findViewById(R.id.tvStatus);
        }
    }
}