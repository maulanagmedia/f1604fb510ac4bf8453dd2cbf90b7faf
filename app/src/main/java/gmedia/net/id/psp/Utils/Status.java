package gmedia.net.id.psp.Utils;

import android.widget.Switch;

/**
 * Created by Shinmaul on 10/27/2017.
 */

public class Status {

    public static String customer(int i){

        switch(i){

            case 0:
                return "<font color='#000000'>Tidak Aktif";
            case 1:
                return "<font color='#e2332d'>Aktif";
            case 2:
                return "<font color='#FF001FB9'>Sedang di proses";
            case 3:
                return "<font color='#FFF48815'>Disetujui SPV";
            default:
                return "<font color='#000000'>Tidak Aktive";
        }
    }
}
