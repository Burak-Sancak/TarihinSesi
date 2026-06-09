package com.tarihinSesi.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AdminLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Girişi");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        EditText etKullanici = findViewById(R.id.etKullanici);
        EditText etSifre = findViewById(R.id.etSifre);
        Button btnGiris = findViewById(R.id.btnGiris);

        btnGiris.setOnClickListener(v -> {
            String kullanici = etKullanici.getText().toString().trim();
            String sifre = etSifre.getText().toString().trim();

            if (kullanici.equals("admin") && sifre.equals("admin")) {
                startActivity(new Intent(this, AdminPanelActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Hatalı kullanıcı adı veya şifre", Toast.LENGTH_SHORT).show();
            }
        });
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