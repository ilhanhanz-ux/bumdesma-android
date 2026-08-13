package com.amikom.bumdesma;
// Kalau project kamu pakai subpackage adapter, pindahkan ke: com.amikom.bumdesma.adapter

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import java.util.List;

public class PanduanAdapter extends RecyclerView.Adapter<PanduanAdapter.PanduanViewHolder> {

    private final List<PanduanTopik> daftarTopik;

    public PanduanAdapter(List<PanduanTopik> daftarTopik) {
        this.daftarTopik = daftarTopik;
    }

    @NonNull
    @Override
    public PanduanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_panduan, parent, false);
        return new PanduanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PanduanViewHolder holder, int position) {
        PanduanTopik topik = daftarTopik.get(position);

        holder.tvJudul.setText(topik.getJudul());
        holder.tvIsi.setText(topik.getIsi());
        holder.ivIcon.setImageResource(topik.getIconResId());

        // Tampilkan/sembunyikan isi sesuai status expanded, tanpa animasi ulang saat rebind
        holder.tvIsi.setVisibility(topik.isExpanded() ? View.VISIBLE : View.GONE);
        holder.ivChevron.setImageResource(topik.isExpanded()
                ? R.drawable.ic_expand_less
                : R.drawable.ic_expand_more);

        holder.header.setOnClickListener(v -> {
            boolean newState = !topik.isExpanded();

            // Animasi halus buka/tutup, hanya berlaku di dalam container item ini
            TransitionManager.beginDelayedTransition(holder.container, new AutoTransition());

            topik.setExpanded(newState);
            holder.tvIsi.setVisibility(newState ? View.VISIBLE : View.GONE);
            holder.ivChevron.setImageResource(newState
                    ? R.drawable.ic_expand_less
                    : R.drawable.ic_expand_more);
        });
    }

    @Override
    public int getItemCount() {
        return daftarTopik.size();
    }

    static class PanduanViewHolder extends RecyclerView.ViewHolder {
        LinearLayout container;
        LinearLayout header;
        ImageView ivIcon;
        ImageView ivChevron;
        TextView tvJudul;
        TextView tvIsi;

        PanduanViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.containerPanduan);
            header = itemView.findViewById(R.id.headerPanduan);
            ivIcon = itemView.findViewById(R.id.ivIconPanduan);
            ivChevron = itemView.findViewById(R.id.ivChevronPanduan);
            tvJudul = itemView.findViewById(R.id.tvJudulPanduan);
            tvIsi = itemView.findViewById(R.id.tvIsiPanduan);
        }
    }
}