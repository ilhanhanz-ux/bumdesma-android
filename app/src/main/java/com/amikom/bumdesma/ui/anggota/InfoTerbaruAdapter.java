package com.amikom.bumdesma.ui.anggota;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.Pengumuman;

import java.util.List;

public class InfoTerbaruAdapter extends RecyclerView.Adapter<InfoTerbaruAdapter.VH> {

    public interface OnItemClick { void onClick(Pengumuman item); }

    private final List<Pengumuman> data;
    private final OnItemClick listener;

    public InfoTerbaruAdapter(List<Pengumuman> data, OnItemClick listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_info_terbaru_carousel, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Pengumuman p = data.get(position);
        holder.tvJudul.setText("📢 " + p.getJudul());
        holder.tvIsi.setText(p.getIsi());
        holder.itemView.setOnClickListener(v -> listener.onClick(p));
        holder.btnBaca.setOnClickListener(v -> listener.onClick(p));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvJudul, tvIsi, btnBaca;
        VH(View itemView) {
            super(itemView);
            tvJudul  = itemView.findViewById(R.id.tv_judul_info);
            tvIsi    = itemView.findViewById(R.id.tv_isi_info);
            btnBaca  = itemView.findViewById(R.id.btn_baca_selengkapnya);
        }
    }
}