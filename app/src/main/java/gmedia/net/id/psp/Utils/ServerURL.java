package gmedia.net.id.psp.Utils;

/**
 * Created by Shin on 11/09/2017.
 */

public class ServerURL {

    //private static final String baseURL = "http://192.168.43.200/psp/";
    //private static final String baseURL = "http://api.putmasaripratama.co.id/";
    private static final String baseURL = "http://119.2.53.122/mobilesalesforce/";
    //private static final String baseURL = "http://192.168.12.138/psp/";

    public static final String login = baseURL + "api/auth/login/";
    public static final String getLatestVersion = baseURL + "api/auth/get_latest_version/";
    public static final String changePassword = baseURL + "api/auth/change_password/";

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
    public static final String getPenjualanHariIni = baseURL + "api/perdana/get_all_penjualan/";
    public static final String getDetailPenjualanHariIni = baseURL + "api/perdana/get_detail_all_penjualan/";
    public static final String getRiwayatPenjualan = baseURL + "api/perdana/get_riwayat_penjualan/";

    public static final String getSemuaPenjualan = baseURL + "api/penjualan/get_penjualan/";

    public static final String getPiutang = baseURL + "api/piutang/get_piutang/";
    public static final String getPiutangOutlet = baseURL + "api/piutang/get_piutang_outlet/";

    public static final String getStok = baseURL + "api/stok/get_stok/";
    public static final String getStokDetail = baseURL + "api/stok/get_stok_detail/";

    public static final String getUserInfo = baseURL + "api/profile/get_user_info/";
    public static final String getUserOmset= baseURL + "api/profile/get_user_omset/";

    public static final String getPromosi = baseURL + "api/promosi/get_promosi/";

    public static final String getArea = baseURL + "api/customer/get_area/";
    public static final String saveCustomer = baseURL + "api/customer/save/";
    public static final String getCustomer = baseURL + "api/customer/get_customer/";
    public static final String checkSupervisor = baseURL + "api/customer/check_supervisor/";
    public static final String getCustomerKunjungan = baseURL + "api/customer/get_customer_kunjungan/";
    public static final String updateCustomer = baseURL + "api/customer/update_customer/";
    public static final String getCustomerDeposit = baseURL + "api/customer/get_customer_deposit/";
    public static final String getPengajuanDeposit = baseURL + "api/deposit/get_pengajuan_deposit/";

    public static final String getImages = baseURL + "api/location/get_images/";
    public static final String logLocation = baseURL + "api/location/log_location/";
    public static final String getJarak = baseURL + "api/location/get_jarak/";

    public static final String getComplaint = baseURL + "api/complaint/get_complaint/";
    public static final String saveComplaint = baseURL + "api/complaint/save/";

    public static final String saveCheckin = baseURL + "api/checkin/save/";
    public static final String getKunjungan = baseURL + "api/checkin/get_kunjungan/";
    public static final String getDetailKunjungan = baseURL + "api/checkin/get_detail_kunjungan/";
    public static final String getDetailKunjunganImg = baseURL + "api/checkin/get_detail_kunjungan_img/";
    public static final String getSalesKunjungan = baseURL + "api/checkin/get_sales_kunjungan/";

    public static final String getDeposite = baseURL + "api/deposit/get_deposit/";
    public static final String saveDeposite = baseURL + "api/deposit/save/";
    public static final String savePengajuanDeposite = baseURL + "api/deposit/apv_pengajuan_deposit/";
    public static final String getHistoryDeposit = baseURL + "api/deposit/get_pengajuan_deposit/";
    public static final String getPengajuanHeader = baseURL + "api/deposit/header_pengajuan_approval/";
    public static final String getPengajuanDetail = baseURL + "api/deposit/detail_pengajuan_approval/";

    public static final String getTcashReseller= baseURL + "api/tcash/get_reseller/";
    public static final String getPenjualanTcash= baseURL + "api/tcash/get_penjualan_tcash/";
    public static final String getDetailPenjualanTcash= baseURL + "api/tcash/get_detail_penjualan_tcash/";
    public static final String getTcashHarga= baseURL + "api/tcash/get_harga/";
    public static final String saveTcash= baseURL + "api/tcash/save/";

    public static final String verifikasiOutlet = baseURL + "api/Verifikasi/verifikasi_outlet/";
    public static final String aktivasiOutlet = baseURL + "api/Verifikasi/aktivasi_outlet/";
    public static final String getCustomerVerifikasi = baseURL + "api/Verifikasi/get_customer_verifikasi/";
    public static final String getCustomerAktivasi = baseURL + "api/Verifikasi/get_customer_aktivasi/";

    public static final String getPreorder = baseURL + "api/Preorder/get_preorder/";
    public static final String getBarangPreorder = baseURL + "api/Preorder/get_barang_preorder/";
    public static final String savePreorder = baseURL + "api/Preorder/save/";

    public static final String getLocationDS = baseURL + "api/location/get_location_ds/";
    public static final String saveLocationDS = baseURL + "api/location/save_location_ds/";
    public static final String saveImagesDS = baseURL + "api/location/insert_foto_ds/";
    public static final String getImagesDS = baseURL + "api/location/get_images_ds/";

    public static final String getEvent = baseURL + "api/event/get_event/";
    public static final String getBarangUE = baseURL + "api/event/get_barang/";
    public static final String getBarangByCCID = baseURL + "api/event/get_barang_by_ccid/";
    public static final String saveBalasanDS = baseURL + "api/event/save_balasan_ds/";
    public static final String saveDirectSelling = baseURL + "api/event/save_order_direct/";
    public static final String getMarketSurvey = baseURL + "api/event/get_market_survey/";
    public static final String saveMarketSurvey = baseURL + "api/event/save_market_survey/";

    public static final String getProvider = baseURL + "api/event/get_provide/";
    public static final String saveLogMock = baseURL + "api/auth/insert_log_mock/";
}