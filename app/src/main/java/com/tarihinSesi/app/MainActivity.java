package com.tarihinSesi.app;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int KONUM_IZIN_KODU = 1001;
    private static final int BILDIRIM_IZIN_KODU = 1002;
    private static final LatLng SAKARYA_MERKEZ = new LatLng(40.7731, 30.3951);

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private GeofencingClient geofencingClient;
    private PendingIntent geofencePendingIntent;
    private Map<Marker, Mekan> markerMekanMap = new HashMap<>();
    private List<Mekan> mekanListesi = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("TarihinSesi");
        }

        db = FirebaseFirestore.getInstance();
        geofencingClient = LocationServices.getGeofencingClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        izinleriKontrolEt();
    }

    private void izinleriKontrolEt() {
        // Bildirim izni (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        BILDIRIM_IZIN_KODU);
            }
        }

        // Konum izni
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    KONUM_IZIN_KODU);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SAKARYA_MERKEZ, 12f));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mekanlarıYukle();

        mMap.setOnMarkerClickListener(marker -> {
            Mekan mekan = markerMekanMap.get(marker);
            if (mekan != null) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("mekan_id", mekan.getId());
                intent.putExtra("mekan_ad", mekan.getAd());
                intent.putExtra("mekan_aciklama", mekan.getAciklama());
                intent.putExtra("mekan_tarih", mekan.getTarih());
                intent.putStringArrayListExtra("mekan_resimler", new ArrayList<>(mekan.getResimler()));
                intent.putExtra("mekan_ses_url", mekan.getSesUrl());
                intent.putExtra("mekan_kategori", mekan.getKategori());
                startActivity(intent);
            }
            return true;
        });
    }

    private void mekanlarıYukle() {
        db.collection("mekanlar").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    markerMekanMap.clear();
                    mekanListesi.clear();
                    if (mMap != null) mMap.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Mekan mekan = document.toObject(Mekan.class);
                        mekan.setId(document.getId());
                        mekanListesi.add(mekan);
                        pinEkle(mekan);
                    }

                    geofenceKur();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Yükleme hatası: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private void pinEkle(Mekan mekan) {
        LatLng konum = new LatLng(mekan.getEnlem(), mekan.getBoylam());
        float renk;
        switch (mekan.getKategori() != null ? mekan.getKategori() : "") {
            case "Tarihi": renk = BitmapDescriptorFactory.HUE_RED; break;
            case "Dini": renk = BitmapDescriptorFactory.HUE_GREEN; break;
            case "Doğal": renk = BitmapDescriptorFactory.HUE_BLUE; break;
            default: renk = BitmapDescriptorFactory.HUE_ORANGE; break;
        }
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(konum)
                .title(mekan.getAd())
                .snippet(mekan.getKategori())
                .icon(BitmapDescriptorFactory.defaultMarker(renk)));
        if (marker != null) markerMekanMap.put(marker, mekan);
    }

    private void geofenceKur() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (mekanListesi.isEmpty()) return;

        // Mevcut geofenceleri temizle
        geofencingClient.removeGeofences(getGeofencePendingIntent());

        List<Geofence> geofenceList = new ArrayList<>();
        for (Mekan mekan : mekanListesi) {
            if (mekan.getEnlem() == 0 && mekan.getBoylam() == 0) continue;

            geofenceList.add(new Geofence.Builder()
                    .setRequestId(mekan.getAd())
                    .setCircularRegion(
                            mekan.getEnlem(),
                            mekan.getBoylam(),
                            (float) mekan.getYaricap()
                    )
                    .setExpirationDuration(Geofence.NEVER_EXPIRE)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build());
        }

        if (geofenceList.isEmpty()) return;

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(geofenceList)
                .build();

        geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Konum takibi aktif ✓", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Geofence hatası: " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent != null) return geofencePendingIntent;

        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        geofencePendingIntent = PendingIntent.getBroadcast(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );
        return geofencePendingIntent;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == KONUM_IZIN_KODU) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    }
                }
                mekanlarıYukle();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_admin) {
            startActivity(new Intent(this, AdminLoginActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null) mekanlarıYukle();
    }
}
