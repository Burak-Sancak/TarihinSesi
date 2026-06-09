package com.tarihinSesi.app;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminAddEditActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private EditText etAd, etAciklama, etTarih, etSesUrl, etEnlem, etBoylam, etYaricap;
    private EditText etResim1, etResim2, etResim3, etResim4, etResim5;
    private Spinner spinnerKategori;
    private ProgressBar progressBar;
    private String mod;
    private String mekanId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_edit);

        db = FirebaseFirestore.getInstance();
        mod = getIntent().getStringExtra("mod");
        mekanId = getIntent().getStringExtra("mekan_id");

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("ekle".equals(mod) ? "Yeni Mekan Ekle" : "Mekanı Düzenle");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        etAd = findViewById(R.id.etAd);
        etAciklama = findViewById(R.id.etAciklama);
        etTarih = findViewById(R.id.etTarih);
        etSesUrl = findViewById(R.id.etSesUrl);
        etEnlem = findViewById(R.id.etEnlem);
        etBoylam = findViewById(R.id.etBoylam);
        etYaricap = findViewById(R.id.etYaricap);
        etResim1 = findViewById(R.id.etResim1);
        etResim2 = findViewById(R.id.etResim2);
        etResim3 = findViewById(R.id.etResim3);
        etResim4 = findViewById(R.id.etResim4);
        etResim5 = findViewById(R.id.etResim5);
        spinnerKategori = findViewById(R.id.spinnerKategori);
        progressBar = findViewById(R.id.progressBar);
        Button btnKaydet = findViewById(R.id.btnKaydet);

        String[] kategoriler = {"Tarihi", "Dini", "Doğal", "Kültürel", "Diğer"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, kategoriler);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerKategori.setAdapter(spinnerAdapter);

        if ("duzenle".equals(mod)) {
            etAd.setText(getIntent().getStringExtra("mekan_ad"));
            etAciklama.setText(getIntent().getStringExtra("mekan_aciklama"));
            etTarih.setText(getIntent().getStringExtra("mekan_tarih"));
            etSesUrl.setText(getIntent().getStringExtra("mekan_ses_url"));
            etEnlem.setText(String.valueOf(getIntent().getDoubleExtra("mekan_enlem", 0)));
            etBoylam.setText(String.valueOf(getIntent().getDoubleExtra("mekan_boylam", 0)));
            etYaricap.setText(String.valueOf(getIntent().getDoubleExtra("mekan_yaricap", 150)));

            ArrayList<String> resimler = getIntent().getStringArrayListExtra("mekan_resimler");
            if (resimler != null) {
                if (resimler.size() > 0) etResim1.setText(resimler.get(0));
                if (resimler.size() > 1) etResim2.setText(resimler.get(1));
                if (resimler.size() > 2) etResim3.setText(resimler.get(2));
                if (resimler.size() > 3) etResim4.setText(resimler.get(3));
                if (resimler.size() > 4) etResim5.setText(resimler.get(4));
            }

            String kategori = getIntent().getStringExtra("mekan_kategori");
            for (int i = 0; i < kategoriler.length; i++) {
                if (kategoriler[i].equals(kategori)) {
                    spinnerKategori.setSelection(i);
                    break;
                }
            }
        }

        btnKaydet.setOnClickListener(v -> kaydet());
    }

    private void kaydet() {
        String ad = etAd.getText().toString().trim();
        String aciklama = etAciklama.getText().toString().trim();
        String tarih = etTarih.getText().toString().trim();
        String sesUrl = etSesUrl.getText().toString().trim();
        String enlemStr = etEnlem.getText().toString().trim();
        String boylamStr = etBoylam.getText().toString().trim();
        String yaricapStr = etYaricap.getText().toString().trim();
        String kategori = spinnerKategori.getSelectedItem().toString();

        // Resimleri topla (boş olmayanlar)
        List<String> resimler = new ArrayList<>();
        String r1 = etResim1.getText().toString().trim();
        String r2 = etResim2.getText().toString().trim();
        String r3 = etResim3.getText().toString().trim();
        String r4 = etResim4.getText().toString().trim();
        String r5 = etResim5.getText().toString().trim();
        if (!r1.isEmpty()) resimler.add(r1);
        if (!r2.isEmpty()) resimler.add(r2);
        if (!r3.isEmpty()) resimler.add(r3);
        if (!r4.isEmpty()) resimler.add(r4);
        if (!r5.isEmpty()) resimler.add(r5);

        if (ad.isEmpty() || aciklama.isEmpty() || enlemStr.isEmpty() || boylamStr.isEmpty()) {
            Toast.makeText(this, "Ad, Açıklama, Enlem ve Boylam zorunludur", Toast.LENGTH_SHORT).show();
            return;
        }

        double enlem, boylam, yaricap;
        try {
            enlem = Double.parseDouble(enlemStr);
            boylam = Double.parseDouble(boylamStr);
            yaricap = yaricapStr.isEmpty() ? 150 : Double.parseDouble(yaricapStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Sayısal alanları doğru girin", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        Map<String, Object> mekanData = new HashMap<>();
        mekanData.put("ad", ad);
        mekanData.put("aciklama", aciklama);
        mekanData.put("tarih", tarih);
        mekanData.put("sesUrl", sesUrl);
        mekanData.put("enlem", enlem);
        mekanData.put("boylam", boylam);
        mekanData.put("yaricap", yaricap);
        mekanData.put("kategori", kategori);
        mekanData.put("resimler", resimler);

        if ("ekle".equals(mod)) {
            db.collection("mekanlar").add(mekanData)
                    .addOnSuccessListener(documentReference -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Mekan eklendi!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("mekanlar").document(mekanId).update(mekanData)
                    .addOnSuccessListener(aVoid -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Güncellendi!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Hata: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
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
