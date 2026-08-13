package com.amikom.bumdesma.ui.admin;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TagihanAdapter extends RecyclerView.Adapter<TagihanAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(Angsuran angsuran);
    }

    private List<Angsuran> items = new ArrayList<>();
    private final OnItemClickListener listener;

    public TagihanAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Angsuran> newItems) {
        items = newItems != null ? newItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_tagihan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Angsuran item = items.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textNamaLengkap, textNoKreditAngsuran, textStatusBadge, textTotalBayar, textTanggalJatuhTempo;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            textNamaLengkap      = itemView.findViewById(R.id.textNamaLengkap);
            textNoKreditAngsuran = itemView.findViewById(R.id.textNoKreditAngsuran);
            textStatusBadge      = itemView.findViewById(R.id.textStatusBadge);
            textTotalBayar       = itemView.findViewById(R.id.textTotalBayar);
            textTanggalJatuhTempo = itemView.findViewById(R.id.textTanggalJatuhTempo);
        }

        void bind(Angsuran item, OnItemClickListener listener) {
            textNamaLengkap.setText(item.getNamaLengkap());
            textNoKreditAngsuran.setText(item.getNoKredit() + "  \u2022  Angsuran ke-" + item.getNoAngsuran());

            NumberFormat rupiah = NumberFormat.getCurrencyInstance(new Locale("in", "ID"));
            rupiah.setMaximumFractionDigits(0);
            textTotalBayar.setText(rupiah.format(item.getTotalBayar()));

            textTanggalJatuhTempo.setText("Jatuh tempo " + formatTanggal(item.getTanggalJatuhTempo()));

            applyStatusBadge(item);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }

        private void applyStatusBadge(Angsuran item) {
            String status = item.getStatusBayar();

            if ("sudah_bayar".equals(status)) {
                setBadge("Lunas", "#1D9E75", "#E1F5EE");
            } else if ("terlambat".equals(status) || item.getHariTerlambat() > 0) {
                setBadge("Terlambat " + item.getHariTerlambat() + " hari", "#A32D2D", "#FCEBEB");
            } else if (isDueSoon(item.getTanggalJatuhTempo())) {
                setBadge("Jatuh tempo dekat", "#854F0B", "#FAEEDA");
            } else {
                setBadge("Belum jatuh tempo", "#5F5E5A", "#F1EFE8");
            }
        }

        private void setBadge(String text, String textColor, String bgColor) {
            textStatusBadge.setText(text);
            textStatusBadge.setTextColor(Color.parseColor(textColor));
            textStatusBadge.setBackgroundColor(Color.parseColor(bgColor));
        }

        private boolean isDueSoon(String tanggalJatuhTempo) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                Date jatuhTempo = sdf.parse(tanggalJatuhTempo);
                Date today = sdf.parse(sdf.format(new Date()));
                if (jatuhTempo == null || today == null) return false;
                long diffDays = TimeUnit.MILLISECONDS.toDays(jatuhTempo.getTime() - today.getTime());
                return in3DaysRange(diffDays);
            } catch (ParseException e) {
                return false;
            }
        }

        private boolean in3DaysRange(long diffDays) {
            return diffDays >= 0 && diffDays <= 3;
        }

        private String formatTanggal(String isoDate) {
            try {
                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SimpleDateFormat output = new SimpleDateFormat("d MMM yyyy", new Locale("id", "ID"));
                Date date = input.parse(isoDate);
                return date != null ? output.format(date) : isoDate;
            } catch (ParseException e) {
                return isoDate;
            }
        }
    }
}