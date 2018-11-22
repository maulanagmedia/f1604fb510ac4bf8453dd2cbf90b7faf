package com.leonardus.irfan.bluetoothprinter.Model;

public class Item {
    private String nama;
    private int jumlah;
    private double harga;

    public Item(String nama, int jumlah, double harga){
        this.nama = nama;
        this.jumlah = jumlah;
        this.harga = harga;
    }

    public int getJumlah() {
        return jumlah;
    }

    public String getNama() {
        return nama;
    }

    public double getHarga() {
        return harga;
    }
}
