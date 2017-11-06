package gmedia.net.id.psp.Utils;

/**
 * Created by Shin on 11/09/2017.
 */

public class ServerURL {

    //private static final String baseURL = "http://192.168.12.203/psp/";
    //private static final String baseURL = "http://api.putmasaripratama.co.id/";
    private static final String baseURL = "http://192.168.12.180/psp/";

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
    public static final String getSemuaPenjualan = baseURL + "api/penjualan/get_penjualan/";
    public static final String getPiutang = baseURL + "api/piutang/get_piutang/";
    public static final String getStok = baseURL + "api/stok/get_stok/";
    public static final String getUserInfo = baseURL + "api/profile/get_user_info/";
    public static final String getUserOmset= baseURL + "api/profile/get_user_omset/";
    public static final String getPromosi = baseURL + "api/promosi/get_promosi/";
    public static final String getArea = baseURL + "api/customer/get_area/";
    public static final String saveCustomer = baseURL + "api/customer/save/";
    public static final String getCustomer = baseURL + "api/customer/get_customer/";
    public static final String getCustomerVerifikasi = baseURL + "api/customer/get_customer_verifikasi/";
    public static final String getCustomerKunjungan = baseURL + "api/customer/get_customer_kunjungan/";
    public static final String verifikasiOutlet = baseURL + "api/customer/verifikasi_outlet/";
    public static final String getImages = baseURL + "api/location/get_images/";
    public static final String getComplaint = baseURL + "api/complaint/get_complaint/";
    public static final String saveComplaint = baseURL + "api/complaint/save/";
    public static final String updateCustomer = baseURL + "api/customer/update_customer/";
    public static final String saveCheckin = baseURL + "api/checkin/save/";
    public static final String logLocation = baseURL + "api/location/log_location/";
}
