package com.leanote.android.networking;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.leanote.android.util.AppLog;

import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by binnchx on 11/26/15.
 */
public class MultipartRequest extends Request<JSONObject> {

    private MultipartEntity entity = new MultipartEntity();

    private final Response.Listener<JSONObject> mListener;
    private final Map<String, Object> mParams;


    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("Charset", "utf-8");
        headers.put("Accept-Encoding", "gzip,deflate");
        return headers;

    }

    public MultipartRequest(String url, Response.ErrorListener errorListener, Response.Listener<JSONObject> listener,
                            Map<String, Object> params) {
        super(Method.POST, url, errorListener);

        mListener = listener;
        mParams = params;
        try {
            buildMultipartEntity();
        } catch (UnsupportedEncodingException e) {
            Log.e("upload note error", ":", e);
        }
    }

    private void buildMultipartEntity() throws UnsupportedEncodingException {
        AppLog.i("mparams:" + mParams);
        for (Map.Entry<String, Object> entry : mParams.entrySet()) {
            String key = entry.getKey();

            if (key.contains("FileDatas")) {
                File file = (File) entry.getValue();
                AppLog.i("upload file size:" + file.length()/1024);
                entity.addPart(key, new FileBody(file));
            } else {
                String strValue = String.valueOf(entry.getValue());
                entity.addPart(key, new StringBody(strValue, Charset.forName("UTF-8")));
            }

        }

    }

    public static int calculateInSampleSize(BitmapFactory.Options options,int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height/ (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }


    public static Bitmap getSmallBitmap(String filePath) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 480, 800);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(filePath, options);
    }

    public static String bitmapToString(String filePath) {

        Bitmap bm = getSmallBitmap(filePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 40, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }


    @Override
    public String getBodyContentType()
    {
        return entity.getContentType().getValue();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try
        {
            entity.writeTo(bos);
        }
        catch (IOException e)
        {
            Log.e("upload", "IOException writing to ByteArrayOutputStream", e);
        }
        return bos.toByteArray();
    }

    @Override
    protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonStr = new String(response.data, "UTF-8");
            return Response.success(new JSONObject(jsonStr), getCacheEntry());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void deliverResponse(JSONObject response) {
        mListener.onResponse(response);
    }

}
