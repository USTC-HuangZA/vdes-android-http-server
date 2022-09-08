package api;

import static api.DTO.APIResponse.CODE_ERROR;
import static api.DTO.APIResponse.MEDIA_TYPE_APPLICATION_JSON;

import android.util.Log;

import com.elvishew.xlog.XLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import api.DTO.APIResponse;
import configs.DataCenterConfig;
import ro.polak.http.exception.ServletException;
import ro.polak.http.servlet.HttpServlet;
import ro.polak.http.servlet.HttpServletRequest;
import ro.polak.http.servlet.HttpServletResponse;

public class AISUploadServlet extends HttpServlet {
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String body = request.getInputStream().toString();
        XLog.i("AIS", body);
        try {
            DataCenterConfig.doPost("/ais", new JSONObject(body));
            response.setContentType(MEDIA_TYPE_APPLICATION_JSON);
            response.getWriter().print(new APIResponse().toString());
        } catch (JSONException jsonException) {
            XLog.e(Arrays.toString(jsonException.getStackTrace()));
            response.setContentType(MEDIA_TYPE_APPLICATION_JSON);
            response.getWriter().print(new APIResponse(CODE_ERROR, "JSON Error", null).toString());
        } catch (RuntimeException e) {
            XLog.e(Arrays.toString(e.getStackTrace()));
            response.setContentType(MEDIA_TYPE_APPLICATION_JSON);
            response.getWriter().print(new APIResponse(CODE_ERROR, "Network Error", null).toString());
        }

    }
}
