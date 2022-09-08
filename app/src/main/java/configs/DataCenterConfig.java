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

import java.util.Arrays;

import ro.polak.webserver.MainActivity;

public class DataCenterConfig {
    public static volatile RequestQueue requestQueue;
    private static String backendURL = "http://localhost:8080";
    private static MainActivity activity;

    public static void singletonInit(Context context, MainActivity inActivity) {
        activity = inActivity;
        if (requestQueue == null){
            synchronized (RequestQueue.class){
                try {
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
                                    Arrays.toString(error.getStackTrace());
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
