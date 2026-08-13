package com.amikom.bumdesma.ui.anggota;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.RekeningSetoran;

import java.util.List;

public class RekeningAdapter extends RecyclerView.Adapter<RekeningAdapter.RekeningViewHolder> {

    private final List<RekeningSetoran> list;

    public RekeningAdapter(List<RekeningSetoran> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public RekeningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rekening, parent, false);
        return new RekeningViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RekeningViewHolder holder, int position) {
        RekeningSetoran rek = list.get(position);
        holder.tvBank.setText(rek.getNamaBank() + " - " + rek.getNoRekening());
        holder.tvNama.setText("a.n. " + rek.getAtasNama());

        if (rek.getKeterangan() != null && !rek.getKeterangan().trim().isEmpty()) {
            holder.tvKet.setText(rek.getKeterangan());
            holder.tvKet.setVisibility(View.VISIBLE);
        } else {
            holder.tvKet.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class RekeningViewHolder extends RecyclerView.ViewHolder {
        TextView tvBank, tvNama, tvKet;

        RekeningViewHolder(@NonNull View itemView) {
            super(itemView);
            tvBank = itemView.findViewById(R.id.tv_rekening_bank);
            tvNama = itemView.findViewById(R.id.tv_rekening_nama);
            tvKet  = itemView.findViewById(R.id.tv_rekening_ket);
        }
    }
}