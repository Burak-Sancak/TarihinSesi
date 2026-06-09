package com.tarihinSesi.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AdminPanelActivity extends AppCompatActivity implements MekanAdapter.MekanClickListener {

    private FirebaseFirestore db;
    private List<Mekan> mekanList;
    private MekanAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvBos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_panel);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Paneli");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        mekanList = new ArrayList<>();
        progressBar = findViewById(R.id.progressBar);
        tvBos = findViewById(R.id.tvBos);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MekanAdapter(mekanList, this, true);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fabEkle = findViewById(R.id.fabEkle);
        fabEkle.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminAddEditActivity.class);
            intent.putExtra("mod", "ekle");
            startActivity(intent);
        });

        mekanlariYukle();
    }

    private void mekanlariYukle() {
        progressBar.setVisibility(View.VISIBLE);
        tvBos.setVisibility(View.GONE);

        db.collection("mekanlar").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    mekanList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Mekan mekan = document.toObject(Mekan.class);
                        mekan.setId(document.getId());
                        mekanList.add(mekan);
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    tvBos.setVisibility(mekanList.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDuzenleClick(Mekan mekan) {
        Intent intent = new Intent(this, AdminAddEditActivity.class);
        intent.putExtra("mod", "duzenle");
        intent.putExtra("mekan_id", mekan.getId());
        intent.putExtra("mekan_ad", mekan.getAd());
        intent.putExtra("mekan_aciklama", mekan.getAciklama());
        intent.putExtra("mekan_tarih", mekan.getTarih());
        intent.putStringArrayListExtra("mekan_resimler", new ArrayList<>(mekan.getResimler()));
        intent.putExtra("mekan_ses_url", mekan.getSesUrl());
        intent.putExtra("mekan_enlem", mekan.getEnlem());
        intent.putExtra("mekan_boylam", mekan.getBoylam());
        intent.putExtra("mekan_kategori", mekan.getKategori());
        intent.putExtra("mekan_yaricap", mekan.getYaricap());
        startActivity(intent);
    }

    @Override
    public void onSilClick(Mekan mekan) {
        new AlertDialog.Builder(this)
                .setTitle("Mekanı Sil")
                .setMessage(mekan.getAd() + " silinecek. Emin misiniz?")
                .setPositiveButton("Evet", (dialog, which) -> {
                    db.collection("mekanlar").document(mekan.getId()).delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Silindi", Toast.LENGTH_SHORT).show();
                                mekanlariYukle();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Hata: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("İptal", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mekanlariYukle();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
