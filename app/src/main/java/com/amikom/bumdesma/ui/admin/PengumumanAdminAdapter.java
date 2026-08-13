package com.amikom.bumdesma.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.Pengumuman;

import java.util.List;

public class PengumumanAdminAdapter extends RecyclerView.Adapter<PengumumanAdminAdapter.ViewHolder> {

    public interface OnItemAction {
        void onAction(Pengumuman p);
    }

    private final List<Pengumuman> data;
    private final OnItemAction onEdit;
    private final OnItemAction onDelete;

    public PengumumanAdminAdapter(List<Pengumuman> data, OnItemAction onEdit, OnItemAction onDelete) {
        this.data = data;
        this.onEdit = onEdit;
        this.onDelete = onDelete;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pengumuman_admin, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pengumuman p = data.get(position);
        holder.tvJudul.setText(p.getJudul());
        holder.tvIsi.setText(p.getIsi());
        holder.tvTanggal.setText(p.getTanggal());

        holder.itemView.setOnClickListener(v -> onEdit.onAction(p));
        holder.btnHapus.setOnClickListener(v -> onDelete.onAction(p));
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvJudul, tvIsi, tvTanggal, btnHapus;
        ViewHolder(View itemView) {
            super(itemView);
            tvJudul   = itemView.findViewById(R.id.tv_judul);
            tvIsi     = itemView.findViewById(R.id.tv_isi);
            tvTanggal = itemView.findViewById(R.id.tv_tanggal);
            btnHapus  = itemView.findViewById(R.id.btn_hapus);
        }
    }
}