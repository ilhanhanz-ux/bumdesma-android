package com.amikom.bumdesma.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.AnggotaAdmin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AnggotaAdapter extends RecyclerView.Adapter<AnggotaAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(AnggotaAdmin anggota);
    }

    private List<AnggotaAdmin> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public AnggotaAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<AnggotaAdmin> newItems) {
        items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_anggota, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textInisial, textNamaLengkap, textKelompokDesa, textStatusBadge;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textInisial      = itemView.findViewById(R.id.textInisial);
            textNamaLengkap  = itemView.findViewById(R.id.textNamaLengkap);
            textKelompokDesa = itemView.findViewById(R.id.textKelompokDesa);
            textStatusBadge  = itemView.findViewById(R.id.textStatusBadge);
        }

        void bind(AnggotaAdmin item, OnItemClickListener listener) {
            String nama = item.getNamaLengkap() != null ? item.getNamaLengkap() : "-";
            textNamaLengkap.setText(nama);
            textInisial.setText(nama.isEmpty() ? "?" : nama.substring(0, 1).toUpperCase(Locale.getDefault()));

            String kelompok = item.getNamaKelompok() != null ? item.getNamaKelompok() : "-";
            String desa = item.getNamaDesa() != null ? item.getNamaDesa() : "-";
            textKelompokDesa.setText(kelompok + "  \u2022  " + desa);

            if (item.isStatusAktif()) {
                textStatusBadge.setText("Aktif");
                textStatusBadge.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.admin_badge_aktif_text));
                textStatusBadge.setBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.admin_badge_aktif_bg));
            } else {
                textStatusBadge.setText("Nonaktif");
                textStatusBadge.setTextColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.admin_badge_nonaktif_text));
                textStatusBadge.setBackgroundColor(
                        ContextCompat.getColor(itemView.getContext(), R.color.admin_badge_nonaktif_bg));
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }
    }
}