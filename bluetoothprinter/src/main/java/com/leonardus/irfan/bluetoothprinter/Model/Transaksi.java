package com.leonardus.irfan.bluetoothprinter.Model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Transaksi {
    private String outlet;
    private String sales;
    private String no_nota;
    private Date tgl_transaksi;
    private double tunai;
    private List<Item> listItem;

    public Transaksi(String outlet, String sales, String no_nota, Date tgl_transaksi, List<Item> listItem){
        this.outlet = outlet;
        this.sales = sales;
        this.no_nota = no_nota;
        this.tgl_transaksi = tgl_transaksi;
        this.listItem = listItem;
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

    public Date getTgl_transaksi() {
        return tgl_transaksi;
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
}
