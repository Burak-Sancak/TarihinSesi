package com.tarihinSesi.app;

import java.util.HashSet;
import java.util.Set;

public class GonderilmisBildirimler {

    private static GonderilmisBildirimler instance;
    private Set<String> gonderilen = new HashSet<>();

    private GonderilmisBildirimler() {}

    public static GonderilmisBildirimler getInstance() {
        if (instance == null) {
            instance = new GonderilmisBildirimler();
        }
        return instance;
    }

    public boolean gonderildiMi(String mekanAd) {
        return gonderilen.contains(mekanAd);
    }

    public void ekle(String mekanAd) {
        gonderilen.add(mekanAd);
    }

    public void sifirla() {
        gonderilen.clear();
    }
}