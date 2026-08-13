package com.amikom.bumdesma.ui.admin;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amikom.bumdesma.R;
import com.amikom.bumdesma.model.Angsuran;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class TagihanKelompokAdapter extends RecyclerView.Adapter<TagihanKelompokAdapter.ViewHolder> {

    public interface OnSelectionChangedListener {
        void onChanged(int jumlahDipilih, double totalNominal);
    }

    private final List<Angsuran> data;
    private final Set<Integer> selectedIds = new HashSet<>();
    private final OnSelectionChangedListener listener;
    private final NumberFormat fmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    public TagihanKelompokAdapter(List<Angsuran> data, OnSelectionChangedListener listener) {
        this.data = data;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tagihan_kelompok, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Angsuran item = data.get(position);

        holder.tvNama.setText(item.getNamaLengkap() != null ? item.getNamaLengkap() : "-");
        holder.tvInfo.setText("Angsuran ke-" + item.getNoAngsuran()
                + "  •  Jatuh tempo: " + formatTanggal(item.getTanggalJatuhTempo()));
        holder.tvNominal.setText(fmt.format(item.getTotalBayar()));

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selectedIds.contains(item.getId()));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selectedIds.add(item.getId());
            else selectedIds.remove(item.getId());
            notifySelectionChanged();
        });

        holder.itemView.setOnClickListener(v -> holder.checkBox.toggle());
    }

    @Override
    public int getItemCount() {
        return data != null ? data.size() : 0;
    }

    private void notifySelectionChanged() {
        double total = 0;
        for (Angsuran a : data) {
            if (selectedIds.contains(a.getId())) total += a.getTotalBayar();
        }
        if (listener != null) listener.onChanged(selectedIds.size(), total);
    }

    public void pilihSemua() {
        selectedIds.clear();
        for (Angsuran a : data) selectedIds.add(a.getId());
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public void batalkanSemua() {
        selectedIds.clear();
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public List<Integer> getSelectedIds() {
        return new ArrayList<>(selectedIds);
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
        CheckBox checkBox;
        TextView tvNama, tvInfo, tvNominal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox  = itemView.findViewById(R.id.cb_pilih);
            tvNama    = itemView.findViewById(R.id.tv_nama);
            tvInfo    = itemView.findViewById(R.id.tv_info);
            tvNominal = itemView.findViewById(R.id.tv_nominal);
        }
    }
}