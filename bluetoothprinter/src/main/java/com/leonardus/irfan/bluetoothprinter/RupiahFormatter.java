package com.leonardus.irfan.bluetoothprinter;

import java.text.NumberFormat;
import java.util.Locale;

public class RupiahFormatter {

    public static String getRupiah(double value){
        NumberFormat rupiahFormat = NumberFormat.getInstance(Locale.GERMANY);
        return rupiahFormat.format(Double.parseDouble(String.valueOf(value)));
    }
}
