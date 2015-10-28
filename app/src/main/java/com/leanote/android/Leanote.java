package com.leanote.android;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.leanote.android.networking.OAuthAuthenticator;
import com.leanote.android.networking.OAuthAuthenticatorFactory;
import com.leanote.android.networking.RestClientUtils;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.BitmapLruCache;
import com.leanote.android.util.CoreEvents;
import com.leanote.android.util.PackageUtils;
import com.leanote.android.util.VolleyUtils;
import com.wordpress.rest.RestClient;
import com.wordpress.rest.RestRequest;

import java.util.Locale;

import de.greenrobot.event.EventBus;

/**
 * Created by binnchx on 8/28/15.
 */
public class Leanote extends Application {

    public static RequestQueue requestQueue;

    public static ImageLoader imageLoader;

    public static LeanoteDB leaDB;

    private static Context mContext;
    private static BitmapLruCache mBitmapCache;

    private static RestClientUtils mRestClientUtils;
    private static RestClientUtils mRestClientUtilsVersion1_1;
    private static String mUserAgent;

    private static String accessToken;

    private static final String USER_AGENT_APPNAME = "leanote-android";

    public static Context getContext() {
        return mContext;
    }

    private static String userID;

    private static String userName;


    public static String getUserID() {
        return userID;
    }

    public static void setUserID(String uID) {
        userID = uID;
    }

    public static void setAccessToken(String accessToken) {
        Leanote.accessToken = accessToken;
    }

    public static String getAccessToken() {
        return accessToken;

    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        Leanote.userName = userName;
    }


    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;
        setupVolleyQueue();

        setUserID("54a47c6b38f4116e57000339");
        setAccessToken("5627b49938f4110bc5000464");
        setUserName("binchx@gmail.com");
        leaDB = new LeanoteDB(this);
        //LeanoteDB.deleteDatabase(this);

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

    private static RestRequest.OnAuthFailedListener mOnAuthFailedListener = new RestRequest.OnAuthFailedListener() {
        @Override
        public void onAuthFailed() {
            if (getContext() == null) return;
            // If this is called, it means the WP.com token is no longer valid.
            EventBus.getDefault().post(new CoreEvents.RestApiUnauthorized());
        }
    };


    public static RestClientUtils getRestClientUtilsV1_1() {
        if (mRestClientUtilsVersion1_1 == null) {
            OAuthAuthenticator authenticator = OAuthAuthenticatorFactory.instantiate();
            mRestClientUtilsVersion1_1 = new RestClientUtils(requestQueue, authenticator, mOnAuthFailedListener, RestClient.REST_CLIENT_VERSIONS.V1_1);
        }
        return mRestClientUtilsVersion1_1;
    }

    public static String getUserAgent() {
        if (mUserAgent == null) {
            mUserAgent = USER_AGENT_APPNAME + "/" + PackageUtils.getVersionName(getContext())
                    + " (Android " + Build.VERSION.RELEASE + "; "
                    + Locale.getDefault().toString() + "; "
                    + Build.MANUFACTURER + " " + Build.MODEL + "/" + Build.PRODUCT + ")";
        }
        return mUserAgent;
    }





}
