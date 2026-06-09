package com.tarihinSesi.app;

import java.util.ArrayList;
import java.util.List;

public class Mekan {
    private String id;
    private String ad;
    private String aciklama;
    private String tarih;
    private List<String> resimler;
    private String sesUrl;
    private double enlem;
    private double boylam;
    private String kategori;
    private double yaricap;

    public Mekan() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }
    public String getAciklama() { return aciklama; }
    public void setAciklama(String aciklama) { this.aciklama = aciklama; }
    public String getTarih() { return tarih; }
    public void setTarih(String tarih) { this.tarih = tarih; }
    public List<String> getResimler() { return resimler != null ? resimler : new ArrayList<>(); }
    public void setResimler(List<String> resimler) { this.resimler = resimler; }
    public String getSesUrl() { return sesUrl; }
    public void setSesUrl(String sesUrl) { this.sesUrl = sesUrl; }
    public double getEnlem() { return enlem; }
    public void setEnlem(double enlem) { this.enlem = enlem; }
    public double getBoylam() { return boylam; }
    public void setBoylam(double boylam) { this.boylam = boylam; }
    public String getKategori() { return kategori; }
    public void setKategori(String kategori) { this.kategori = kategori; }
    public double getYaricap() { return yaricap == 0 ? 150 : yaricap; }
    public void setYaricap(double yaricap) { this.yaricap = yaricap; }
}
