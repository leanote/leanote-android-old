package com.leanote.android.networking;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.leanote.android.Leanote;
import com.leanote.android.util.AppLog;

import org.json.JSONObject;

import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by binnchx on 11/2/15.
 */
public class NetworkRequest {

    public static String syncGetRequest(String api) throws ExecutionException, InterruptedException {
        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.GET, api, future, future);

        Leanote.requestQueue.add(request);

        try {
            AppLog.i("api:" + api);
            String response = future.get(15, TimeUnit.SECONDS);

            AppLog.i("response:" + response);
            return response;
        } catch (TimeoutException e) {
            e.printStackTrace();
            return null;
        }
    }




    public static boolean syncPostRequest(String api, Map<String, String> params) {
        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, api,  new JSONObject(params), future, future);
        Leanote.requestQueue.add(request);
        AppLog.i("post api:" + api);
        AppLog.i("params:" + params);
        boolean isOk = true;
        try {
            JSONObject response = future.get();
            AppLog.i("sync notebook:" + response.toString());
            //isOk = response.getBoolean("Ok");
        } catch (Exception e) {
            AppLog.i(e);
        }
        return isOk;
    }
}
