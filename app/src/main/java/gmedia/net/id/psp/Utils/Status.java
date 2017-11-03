package gmedia.net.id.psp.Utils;

import android.widget.Switch;

/**
 * Created by Shinmaul on 10/27/2017.
 */

public class Status {

    public static String customer(int i){

        switch(i){

            case 0:
                return "<font color='#000000'>Tidak Aktif</font>";
            case 1:
                return "<font color='#e2332d'>Aktif</font>";
            case 2:
                return "<font color='#FF001FB9'>Sedang di proses</font>";
            case 3:
                return "<font color='#FFF48815'>Disetujui SPV</font>";
            default:
                return "<font color='#000000'>Tidak Aktif</font>";
        }
    }

    public static String mkios(String hasil){

        if(hasil.toUpperCase().equals("PENDING")){

            return "<font color='#FF001FB9'>"+ hasil +"</font>";
        }else if(hasil.toUpperCase().equals("BERHASIL")){

            return "<font color='#e2332d'>"+ hasil +"</font>";
        }else{

            return "<font color='#000000'>"+ hasil +"</font>";
        }

    }
}
