package com.leanote.android.networking;

import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.leanote.android.Leanote;

import java.util.concurrent.ExecutionException;

/**
 * Created by binnchx on 11/2/15.
 */
public class NetworkRequest {

    public static String syncRequest(String api, int method) throws ExecutionException, InterruptedException {
        RequestFuture<String> future = RequestFuture.newFuture();

        StringRequest request = new StringRequest(method, api, future, future);

        Leanote.requestQueue.add(request);

        return future.get();
    }


}
