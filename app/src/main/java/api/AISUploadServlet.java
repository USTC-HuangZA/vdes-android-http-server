package api;

import static api.DTO.APIResponse.CODE_ERROR;
import static api.DTO.APIResponse.MEDIA_TYPE_APPLICATION_JSON;

import android.util.Log;

import com.elvishew.xlog.XLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import api.DTO.APIResponse;
import configs.DataCenterConfig;
import ro.polak.http.exception.ServletException;
import ro.polak.http.servlet.HttpServlet;
import ro.polak.http.servlet.HttpServletRequest;
import ro.polak.http.servlet.HttpServletResponse;
import org.apache.commons.io.IOUtils;

public class AISUploadServlet extends HttpServlet {

    private String getBody(HttpServletRequest request) throws IOException {
        return request.getPostParameter("json");
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String body = null;
        try {
            body = getBody(request);
            Log.i("AID", body);
            XLog.i("AIS", body);
            DataCenterConfig.doPost("/ais", new JSONObject(body));
            response.setContentType(MEDIA_TYPE_APPLICATION_JSON);
            response.getWriter().print(new APIResponse().toString());
        } catch (JSONException jsonException) {
            XLog.e("JSON parse error",body);
            response.setContentType(MEDIA_TYPE_APPLICATION_JSON);
            response.getWriter().print(new APIResponse(CODE_ERROR, "JSON Error", null).toString());
        } catch (RuntimeException e) {
            XLog.e(Arrays.toString(e.getStackTrace()));
            response.setContentType(MEDIA_TYPE_APPLICATION_JSON);
            response.getWriter().print(new APIResponse(CODE_ERROR, "Network Error", null).toString());
        } catch (IOException e) {
            XLog.e(Arrays.toString(e.getStackTrace()));
            response.setContentType(MEDIA_TYPE_APPLICATION_JSON);
            response.getWriter().print(new APIResponse(CODE_ERROR, "Get post body error", null).toString());
        }

    }
}
