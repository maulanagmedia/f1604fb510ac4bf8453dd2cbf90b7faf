package com.maulana.custommodul;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * Created by Shin on 2/24/2017.
 */

public class ApiVolley {

    public static RequestQueue requestQueue;

    public ApiVolley(final Context context, JSONObject jsonBody, String requestMethod,
					 String REST_URL, final String successDialog, final String failDialog, final int showDialogFlag, final String t1, final String t2, final VolleyCallback callback){

        /*
        NOTE: you have to customize this class before you use it (haeder, etc)
        context : Application context
        jsonBody : jsonBody (usually be used for POST and PUT)
        requestMethod : GET, POST, PUT, DELETE
        REST_URL : Rest API URL
        successDialog : custom Dialog when success call API
        failDialog : custom Dialog when failed call API
        showDialogFlag : 1 = show successDialog / failDialog with filter
        callback : return of the response
        */

        final String requestBody = jsonBody.toString();

        int method = 0;

        switch(requestMethod.toUpperCase()){

            case "GET" :
                method = Request.Method.GET;
                break;
            case "POST" :
                method = Request.Method.POST;
                break;
            case "PUT" :
                method = Request.Method.PUT;
                break;
            case "DELETE" :
                method = Request.Method.DELETE;
                break;
            default: method = Request.Method.GET;
                break;
        }

        //region initial of stringRequest
        StringRequest stringRequest = new StringRequest(method, REST_URL, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
				Log.d("error response",response);
                // Important Note : need to use try catch when parsing JSONObject, no need when parsing string

                if(response == null || response.equals("null")) {

                    Toast.makeText(context, context.getResources().getString(R.string.api_unauthorized), Toast.LENGTH_LONG).show();
                    callback.onError(context.getResources().getString(R.string.api_unauthorized));
                }

                try {

                    callback.onSuccess(response);
                    ShowCustomDialog(context, showDialogFlag, successDialog);

                } catch (Exception e) {

                    e.printStackTrace();
//                    Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
                    Toast.makeText(context, context.getResources().getString(R.string.api_error), Toast.LENGTH_LONG).show();
                    callback.onError(e.toString());
                }

            }
        }, new Response.ErrorListener() {
            @Override

            public void onErrorResponse(VolleyError error) {
				//Log.d("error response", error.getMessage());
                String message = error.toString();
                if (error instanceof NetworkError) {
                    message = context.getResources().getString(R.string.msg_connection_issue);
                } else if (error instanceof ServerError) {
                    message = context.getResources().getString(R.string.msg_server_issue);
                } else if (error instanceof AuthFailureError) {
                    message = context.getResources().getString(R.string.msg_auth_issue);
                } else if (error instanceof ParseError) {
                    message = context.getResources().getString(R.string.msg_parsing_issue);
                } else if (error instanceof NoConnectionError) {
                    message = context.getResources().getString(R.string.msg_noinet_issue);
                } else if (error instanceof TimeoutError) {
                    message = context.getResources().getString(R.string.msg_timeout_issue);
                }
                callback.onError(message);
                return;
            }
        }) {

            // Request Header
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {

                ItemValidation iv = new ItemValidation();
                String token1 = iv.encodeBase64(t1);
                String token2 = iv.encodeBase64(t2);

                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/json");
                params.put("Client-Service", "frontend-client");
                params.put("Auth-Key", "gmedia_psp");
                params.put("token1", token1);
                params.put("token2", token2);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return String.format("application/json; charset=utf-8");
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return requestBody == null ? null : requestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                            requestBody, "utf-8");
                    return null;
                }
            }
        };
        //endregion
        /*try {
            HttpsURLConnection.setDefaultSSLSocketFactory(
                    getSSLSocketFactory(context));
            HttpsURLConnection.setDefaultHostnameVerifier(getHostnameVerifier());

        } catch (Exception e) {
            e.printStackTrace();
        }*/

        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }

        /*// retry when timeout
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                30*60*1000, *//*DefaultRetryPolicy.DEFAULT_MAX_RETRIES*//*0,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));*/

        // retry when timeout
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                120*1000, -1,DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        stringRequest.setShouldCache(false);
        requestQueue.add(stringRequest);
        requestQueue.getCache().clear();

    }

    // interface for call callback from response API
    public interface VolleyCallback{
        void onSuccess(String result);
        void onError(String result);
    }

    public void ShowCustomDialog(Context context, int flag, String message){
        if(flag == 1){
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    // untuk menangani terkait SSL
    private HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                //return true; // verify always returns true, which could cause insecure network traffic due to trusting TLS/SSL server certificates for wrong hostnames
                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
                if (hostname.equalsIgnoreCase("putmasaripratama.co.id") ||
                        hostname.equalsIgnoreCase("office.putmasaripratama.co.id") ||
                        hostname.equalsIgnoreCase("api.putmasaripratama.co.id") ||
                        hostname.equalsIgnoreCase("reports.crashlytics.com") ||
                        hostname.equalsIgnoreCase("api.crashlytics.com") ||
                        hostname.equalsIgnoreCase("settings.crashlytics.com") ||
                        hostname.equalsIgnoreCase("clients4.google.com") ||
                        hostname.equalsIgnoreCase("www.facebook.com") ||
                        hostname.equalsIgnoreCase("www.instagram.com") ||
                        hostname.equalsIgnoreCase("lh1.googleusercontent.com") ||
                        hostname.equalsIgnoreCase("lh2.googleusercontent.com") ||
                        hostname.equalsIgnoreCase("lh3.googleusercontent.com") ||
                        hostname.equalsIgnoreCase("lh4.googleusercontent.com") ||
                        hostname.equalsIgnoreCase("lh5.googleusercontent.com") ||
                        hostname.equalsIgnoreCase("lh6.googleusercontent.com") ||
                        hostname.equalsIgnoreCase("lh7.googleusercontent.com") ||
                        hostname.equalsIgnoreCase("lh8.googleusercontent.com") ||
                        hostname.equalsIgnoreCase("lh9.googleusercontent.com") ||
                        hostname.equalsIgnoreCase("googleusercontent.com") ||
                        hostname.equalsIgnoreCase("fbcdn.net") ||
                        hostname.equalsIgnoreCase("scontent.xx.fbcdn.net") ||
                        hostname.equalsIgnoreCase("lookaside.facebook.com")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
    }

    private TrustManager[] getWrappedTrustManagers(TrustManager[] trustManagers) {
        final X509TrustManager originalTrustManager = (X509TrustManager) trustManagers[0];
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return originalTrustManager.getAcceptedIssuers();
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0){
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkClientTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkClientTrusted", e.toString());
                        }
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0){
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkServerTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkServerTrusted", e.toString());
                        }
                    }
                }
        };
    }

    private SSLSocketFactory getSSLSocketFactory(Context context)
            throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = context.getResources().openRawResource(R.raw.ca2); // this cert file stored in \app\src\main\res\raw folder path

        Certificate ca = cf.generateCertificate(caInput);
        caInput.close();

        KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        TrustManager[] wrappedTrustManagers = getWrappedTrustManagers(tmf.getTrustManagers());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, wrappedTrustManagers, null);

        return sslContext.getSocketFactory();
    }
}
