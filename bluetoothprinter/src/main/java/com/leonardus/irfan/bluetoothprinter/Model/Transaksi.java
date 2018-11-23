package com.leonardus.irfan.bluetoothprinter.Model;

import java.util.Date;
import java.util.List;

public class Transaksi {
    private String outlet;
    private String sales;
    private String no_nota;
    private Date tglNota;
    private double tunai;
    private List<Item> listItem;
    private String tglTransaksi;

    public Transaksi(String outlet, String sales, String no_nota, Date tglNota, List<Item> listItem, String tglTransaksi){
        this.outlet = outlet;
        this.sales = sales;
        this.no_nota = no_nota;
        this.tglNota = tglNota;
        this.listItem = listItem;
        this.tglTransaksi = tglTransaksi;
    }

    public void setTunai(double tunai){
        this.tunai = tunai;
    }

    public double getTunai() {
        return tunai;
    }

    public String getNo_nota() {
        return no_nota;
    }

    public Date getTglNota() {
        return tglNota;
    }

    public List<Item> getListItem() {
        return listItem;
    }

    public String getOutlet() {
        return outlet;
    }

    public String getSales() {
        return sales;
    }

    public String getTglTransaksi() {
        return tglTransaksi;
    }

    public void setTglTransaksi(String tglTransaksi) {
        this.tglTransaksi = tglTransaksi;
    }
}
