package gmedia.net.id.psp.Utils;

/**
 * Created by Shin on 11/09/2017.
 */

public class ServerURL {

    //private static final String baseURL = "http://192.168.12.203/psp/";
    //private static final String baseURL = "http://api.putmasaripratama.co.id/";
    private static final String baseURL = "http://192.168.12.181/psp/";

    public static final String login = baseURL + "api/auth/login/";
    public static final String getReseller = baseURL + "api/mkios/get_reseller/";
    public static final String getMKIOSNonota = baseURL + "api/mkios/generate_nonota/";
    public static final String getPerdanaNonota = baseURL + "api/perdana/generate_nonota/";
    public static final String saveMKIOS = baseURL + "api/mkios/save/";
    public static final String savePerdana = baseURL + "api/perdana/save/";
    public static final String getMkios = baseURL + "api/mkios/get_mkios/";
    public static final String getCustomerPerdana = baseURL + "api/perdana/get_customer/";
    public static final String getBarangPerdana = baseURL + "api/perdana/get_list_barang/";
    public static final String getListCCIDPerdana = baseURL + "api/perdana/get_list_ccid/";
    public static final String getSelectedCCIDPerdana = baseURL + "api/perdana/get_selected_ccid/";
    public static final String getPenjualanPerdana = baseURL + "api/perdana/get_penjualan_perdana/";
}
