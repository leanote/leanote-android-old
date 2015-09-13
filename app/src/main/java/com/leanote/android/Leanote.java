package com.leanote.android;

import android.app.Application;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.BitmapLruCache;
import com.leanote.android.util.VolleyUtils;

/**
 * Created by binnchx on 8/28/15.
 */
public class Leanote extends Application {

    public static RequestQueue requestQueue;

    public static ImageLoader imageLoader;

    private static Context mContext;
    private static BitmapLruCache mBitmapCache;


    @Override
    public void onCreate() {
        super.onCreate();
        setupVolleyQueue();
    }

    public static void setupVolleyQueue() {
        requestQueue = Volley.newRequestQueue(mContext, VolleyUtils.getHTTPClientStack(mContext));
        imageLoader = new ImageLoader(requestQueue, getBitmapCache());
        VolleyLog.setTag(AppLog.TAG);
        // http://stackoverflow.com/a/17035814
        imageLoader.setBatchedResponseDelay(0);
    }

    public static BitmapLruCache getBitmapCache() {
        if (mBitmapCache == null) {
            // The cache size will be measured in kilobytes rather than
            // number of items. See http://developer.android.com/training/displaying-bitmaps/cache-bitmap.html
            int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int cacheSize = maxMemory / 16;  //Use 1/16th of the available memory for this memory cache.
            mBitmapCache = new BitmapLruCache(cacheSize);
        }
        return mBitmapCache;
    }


}
