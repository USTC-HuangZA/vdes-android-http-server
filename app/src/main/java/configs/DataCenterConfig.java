package configs;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.elvishew.xlog.XLog;

import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ro.polak.webserver.MainActivity;

public class DataCenterConfig {
    public static volatile RequestQueue requestQueue;
    //后端ip
    private static String backendURL = "https://192.168.10.28:443";
    private static MainActivity activity;

    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("TLS");
            // trustAllCerts信任所有的证书
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

        public static void singletonInit(Context context, MainActivity inActivity) {
        activity = inActivity;
        if (requestQueue == null){
            synchronized (RequestQueue.class){
                try {
                    handleSSLHandshake();
                    requestQueue = Volley.newRequestQueue(context);
                }catch (Exception e) {
                    Log.e("CONNECTION", Arrays.toString(e.getStackTrace()));
                }
            }
        }
    }

    public static void doPost(String suffixURL, JSONObject content) throws RuntimeException{
        //TODO: 队列要持久化
        if(requestQueue!=null){
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST,
                    backendURL + suffixURL,
                    content,
                    (Response.Listener<JSONObject>) response -> activity.println("message:"+content.toString() + "\nres:"+ response.toString()),
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String errorMessage = "ERROR on message:"+content.toString() +
                                    "\n" +
                                    error.getMessage();
                            activity.println(errorMessage);
                            XLog.e("AIS RESPONSE", errorMessage);
                        }
            });
            request.setRetryPolicy(new DefaultRetryPolicy(100000, 10, 1));
            requestQueue.add(request);
            requestQueue.start();
        }else {
            XLog.e("backend connection not init");
            throw new RuntimeException("backend connection not init");
        }
    }

}
