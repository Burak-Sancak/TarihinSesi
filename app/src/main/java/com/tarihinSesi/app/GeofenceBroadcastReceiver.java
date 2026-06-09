package com.tarihinSesi.app;


import java.util.ArrayList;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "tarihin_sesi_channel";
    private static final long BILDIRIM_ARALIK = 5 * 60 * 1000;

    @Override
    public void onReceive(Context context, Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent == null || geofencingEvent.hasError()) return;

        if (geofencingEvent.getGeofenceTransition() == Geofence.GEOFENCE_TRANSITION_ENTER) {
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            if (triggeringGeofences == null || triggeringGeofences.isEmpty()) return;

            String mekanAd = triggeringGeofences.get(0).getRequestId();

            if (GonderilmisBildirimler.getInstance().gonderildiMi(mekanAd)) return;
            GonderilmisBildirimler.getInstance().ekle(mekanAd);

            // Firebase'den mekan bilgilerini çek
            FirebaseFirestore.getInstance()
                    .collection("mekanlar") 
                    .whereEqualTo("ad", mekanAd)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            Mekan mekan = queryDocumentSnapshots.getDocuments()
                                    .get(0).toObject(Mekan.class);
                            if (mekan != null) {
                                mekan.setId(queryDocumentSnapshots.getDocuments().get(0).getId());
                                bildirimiGonder(context, mekan);
                            }
                        }
                    });
        }
    }

    private void bildirimiGonder(Context context, Mekan mekan) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "TarihinSesi Bildirimleri",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        Intent detailIntent = new Intent(context, DetailActivity.class);
        detailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        detailIntent.putExtra("mekan_id", mekan.getId());
        detailIntent.putExtra("mekan_ad", mekan.getAd());
        detailIntent.putExtra("mekan_aciklama", mekan.getAciklama());
        detailIntent.putExtra("mekan_tarih", mekan.getTarih());
        detailIntent.putStringArrayListExtra("mekan_resimler", new ArrayList<>(mekan.getResimler()));
        detailIntent.putExtra("mekan_ses_url", mekan.getSesUrl());
        detailIntent.putExtra("mekan_kategori", mekan.getKategori());

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, mekan.getAd().hashCode(), detailIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("🏛️ " + mekan.getAd() + " yakınındasınız!")
                .setContentText("Sesli anlatımı dinlemek için dokunun.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(mekan.getAd().hashCode(), builder.build());
    }
}