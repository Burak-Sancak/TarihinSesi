package com.tarihinSesi.app;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MekanAdapter extends RecyclerView.Adapter<MekanAdapter.MekanViewHolder> {

    private List<Mekan> mekanList;
    private MekanClickListener listener;
    private boolean adminMod;

    public interface MekanClickListener {
        void onDuzenleClick(Mekan mekan);
        void onSilClick(Mekan mekan);
    }

    public MekanAdapter(List<Mekan> mekanList, MekanClickListener listener, boolean adminMod) {
        this.mekanList = mekanList;
        this.listener = listener;
        this.adminMod = adminMod;
    }

    @NonNull
    @Override
    public MekanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mekan, parent, false);
        return new MekanViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MekanViewHolder holder, int position) {
        Mekan mekan = mekanList.get(position);
        holder.tvAd.setText(mekan.getAd());
        holder.tvKategori.setText(mekan.getKategori());
        holder.tvAciklamaKisa.setText(
                mekan.getAciklama() != null && mekan.getAciklama().length() > 80
                        ? mekan.getAciklama().substring(0, 80) + "..."
                        : mekan.getAciklama());

        if (adminMod) {
            holder.btnDuzenle.setVisibility(View.VISIBLE);
            holder.btnSil.setVisibility(View.VISIBLE);
            holder.btnDuzenle.setOnClickListener(v -> listener.onDuzenleClick(mekan));
            holder.btnSil.setOnClickListener(v -> listener.onSilClick(mekan));
        } else {
            holder.btnDuzenle.setVisibility(View.GONE);
            holder.btnSil.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return mekanList.size(); }

    public static class MekanViewHolder extends RecyclerView.ViewHolder {
        TextView tvAd, tvKategori, tvAciklamaKisa;
        Button btnDuzenle, btnSil;

        public MekanViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAd = itemView.findViewById(R.id.tvAd);
            tvKategori = itemView.findViewById(R.id.tvKategori);
            tvAciklamaKisa = itemView.findViewById(R.id.tvAciklamaKisa);
            btnDuzenle = itemView.findViewById(R.id.btnDuzenle);
            btnSil = itemView.findViewById(R.id.btnSil);
        }
    }
}
