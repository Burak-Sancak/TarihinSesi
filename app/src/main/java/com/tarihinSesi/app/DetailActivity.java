package com.tarihinSesi.app;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer;
    private ImageButton btnSes;
    private ProgressBar progressSes;
    private boolean sesCalıyor = false;
    private String sesUrl;
    private ViewPager2 viewPager;
    private LinearLayout dotsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String ad = getIntent().getStringExtra("mekan_ad");
        String aciklama = getIntent().getStringExtra("mekan_aciklama");
        String tarih = getIntent().getStringExtra("mekan_tarih");
        ArrayList<String> resimler = getIntent().getStringArrayListExtra("mekan_resimler");
        sesUrl = getIntent().getStringExtra("mekan_ses_url");
        String kategori = getIntent().getStringExtra("mekan_kategori");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(ad != null ? ad : "Detay");
        }

        TextView tvAd = findViewById(R.id.tvAd);
        TextView tvKategori = findViewById(R.id.tvKategori);
        TextView tvTarih = findViewById(R.id.tvTarih);
        TextView tvAciklama = findViewById(R.id.tvAciklama);
        viewPager = findViewById(R.id.viewPager);
        dotsLayout = findViewById(R.id.dotsLayout);
        btnSes = findViewById(R.id.btnSes);
        progressSes = findViewById(R.id.progressSes);

        tvAd.setText(ad);
        tvKategori.setText(kategori);
        tvTarih.setText(tarih);
        tvAciklama.setText(aciklama);

        // Resimleri yükle
        if (resimler != null && !resimler.isEmpty()) {
            ResimAdapter resimAdapter = new ResimAdapter(resimler);
            viewPager.setAdapter(resimAdapter);
            noktalariOlustur(resimler.size());

            viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                @Override
                public void onPageSelected(int position) {
                    noktalariGuncelle(position, resimler.size());
                }
            });
        }

        if (sesUrl != null && !sesUrl.isEmpty()) {
            btnSes.setVisibility(View.VISIBLE);
            btnSes.setOnClickListener(v -> sesToggle());
        } else {
            btnSes.setVisibility(View.GONE);
        }
    }

    private void noktalariOlustur(int count) {
        dotsLayout.removeAllViews();
        if (count <= 1) return;

        for (int i = 0; i < count; i++) {
            TextView dot = new TextView(this);
            dot.setText("●");
            dot.setTextSize(12);
            dot.setPadding(4, 0, 4, 0);
            dot.setTextColor(i == 0 ? 0xFF1a5c38 : 0xFFCCCCCC);
            dotsLayout.addView(dot);
        }
    }

    private void noktalariGuncelle(int position, int count) {
        for (int i = 0; i < dotsLayout.getChildCount(); i++) {
            TextView dot = (TextView) dotsLayout.getChildAt(i);
            dot.setTextColor(i == position ? 0xFF1a5c38 : 0xFFCCCCCC);
        }
    }

    private void sesToggle() {
        if (sesCalıyor) sesDurdur();
        else sesCal();
    }

    private void sesCal() {
        progressSes.setVisibility(View.VISIBLE);
        btnSes.setEnabled(false);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_MEDIA).build());
        try {
            mediaPlayer.setDataSource(sesUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> {
                progressSes.setVisibility(View.GONE);
                btnSes.setEnabled(true);
                btnSes.setImageResource(R.drawable.ic_stop);
                mp.start();
                sesCalıyor = true;
            });
            mediaPlayer.setOnCompletionListener(mp -> {
                sesCalıyor = false;
                btnSes.setImageResource(R.drawable.ic_play);
                mediaPlayer.release();
                mediaPlayer = null;
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                progressSes.setVisibility(View.GONE);
                btnSes.setEnabled(true);
                Toast.makeText(this, "Ses oynatılamadı", Toast.LENGTH_SHORT).show();
                return true;
            });
        } catch (IOException e) {
            progressSes.setVisibility(View.GONE);
            btnSes.setEnabled(true);
            Toast.makeText(this, "Ses yüklenemedi", Toast.LENGTH_SHORT).show();
        }
    }

    private void sesDurdur() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        sesCalıyor = false;
        btnSes.setImageResource(R.drawable.ic_play);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    // ViewPager2 için adapter
    class ResimAdapter extends RecyclerView.Adapter<ResimAdapter.ResimViewHolder> {
        private List<String> resimler;

        ResimAdapter(List<String> resimler) {
            this.resimler = resimler;
        }

        @NonNull
        @Override
        public ResimViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            return new ResimViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ResimViewHolder holder, int position) {
            Glide.with(holder.imageView.getContext())
                    .load(resimler.get(position))
                    .centerCrop()
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() { return resimler.size(); }

        class ResimViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ResimViewHolder(ImageView imageView) {
                super(imageView);
                this.imageView = imageView;
            }
        }
    }
}
