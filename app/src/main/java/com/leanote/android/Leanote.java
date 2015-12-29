package com.leanote.android;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.android.volley.RequestQueue;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.google.android.gcm.GCMRegistrar;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.networking.SelfSignedSSLCertsManager;
import com.leanote.android.ui.AppPrefs;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.BitmapLruCache;
import com.leanote.android.util.CoreEvents;
import com.leanote.android.util.PackageUtils;
import com.leanote.android.util.VolleyUtils;
import com.wordpress.rest.RestRequest;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

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

    private static String mUserAgent;

    private static boolean isFirstSync = true;

    private static List<String> downloadingFileUrls;

    private static final String USER_AGENT_APPNAME = "leanote-android";

    public static Context getContext() {
        return mContext;
    }


    private Activity mCurrentActivity = null;

    public Activity getCurrentActivity(){
        return mCurrentActivity;
    }
    public void setCurrentActivity(Activity mCurrentActivity){
        this.mCurrentActivity = mCurrentActivity;
    }



    @Override
    public void onCreate() {
        super.onCreate();

        mContext = this;

        EventBus.TAG = "Leanote-EVENT";
        EventBus.builder()
                .logNoSubscriberMessages(false)
                .sendNoSubscriberEvent(false)
                .throwSubscriberException(true)
                .installDefaultEventBus();
        EventBus.getDefault().register(this);

        setupVolleyQueue();

        leaDB = new LeanoteDB(this);

        if (downloadingFileUrls != null && downloadingFileUrls.size() > 0) {
            downloadingFileUrls.clear();
        }

        downloadingFileUrls = new CopyOnWriteArrayList<>();
    }


    public static List<String> getDownloadingFileUrls() {
        return downloadingFileUrls;
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


//    public static RestClientUtils getRestClientUtilsV1_1() {
//        if (mRestClientUtilsVersion1_1 == null) {
//            OAuthAuthenticator authenticator = OAuthAuthenticatorFactory.instantiate();
//            mRestClientUtilsVersion1_1 = new RestClientUtils(requestQueue, authenticator, mOnAuthFailedListener, RestClient.REST_CLIENT_VERSIONS.V1_1);
//        }
//        return mRestClientUtilsVersion1_1;
//    }

    public static String getUserAgent() {
        if (mUserAgent == null) {
            mUserAgent = USER_AGENT_APPNAME + "/" + PackageUtils.getVersionName(getContext())
                    + " (Android " + Build.VERSION.RELEASE + "; "
                    + Locale.getDefault().toString() + "; "
                    + Build.MANUFACTURER + " " + Build.MODEL + "/" + Build.PRODUCT + ")";
        }
        return mUserAgent;
    }

    public static void leanotecomSignOut(Context context) {
        // Keep the analytics tracking at the beginning, before the account data is actual removed.

        removeWpComUserRelatedData(context);

        // broadcast an event: wpcom user signed out
        EventBus.getDefault().post(new CoreEvents.UserSignedOutWordPressCom());

        // broadcast an event only if the user is completely signed out
        if (!AccountHelper.isSignedIn()) {
            EventBus.getDefault().post(new CoreEvents.UserSignedOutCompletely());
        }
    }

    public static void removeWpComUserRelatedData(Context context) {
        // cancel all Volley requests - do this before unregistering push since that uses
        // a Volley request
        VolleyUtils.cancelAllRequests(requestQueue);

        try {
            GCMRegistrar.checkDevice(context);
            GCMRegistrar.unregister(context);
        } catch (Exception e) {
            AppLog.v(AppLog.T.NOTIFS, "Could not unregister for GCM: " + e.getMessage());
        }

        // delete wpcom blogs

        // reset default account
        AccountHelper.getDefaultAccount().signout();

        // reset all reader-related prefs & data
        AppPrefs.reset();
        //ReaderDatabase.reset();

        // Reset Stats Data
        //StatsDatabaseHelper.getDatabase(context).reset();
        //StatsWidgetProvider.updateWidgetsOnLogout(context);

        // Reset Simperium buckets (removes local data)
        //SimperiumUtils.resetBucketsAndDeauthorize();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CoreEvents.UserSignedOutCompletely event) {
        try {
            SelfSignedSSLCertsManager.getInstance(getContext()).emptyLocalKeyStoreFile();
        } catch (GeneralSecurityException e) {
            AppLog.e(AppLog.T.UTILS, "Error while cleaning the Local KeyStore File", e);
        } catch (IOException e) {
            AppLog.e(AppLog.T.UTILS, "Error while cleaning the Local KeyStore File", e);
        }


        // dangerously delete all content!
        leaDB.dangerouslyDeleteAllContent();
    }

    public static void LeanoteComSignOut(Context context) {

        removeWpComUserRelatedData(context);

        // broadcast an event: wpcom user signed out
        EventBus.getDefault().post(new CoreEvents.UserSignedOutWordPressCom());

        // broadcast an event only if the user is completely signed out
        if (!AccountHelper.isSignedIn()) {
            EventBus.getDefault().post(new CoreEvents.UserSignedOutCompletely());
        }
    }

    public static boolean isFirstSync() {
        return isFirstSync;
    }

    public static void setIsFirstSync(boolean firstSync) {
        isFirstSync = firstSync;
    }
}
