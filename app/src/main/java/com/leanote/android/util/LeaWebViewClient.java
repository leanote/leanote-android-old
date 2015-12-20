package com.leanote.android.util;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.HttpAuthHandler;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.leanote.android.Leanote;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.networking.SelfSignedSSLCertsManager;

import org.bson.types.ObjectId;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Created by binnchx on 11/22/15.
 */
public class LeaWebViewClient extends WebViewClient {


    private OnImageLoadListener imageLoadedListener;

    public void setImageLoadedListener(OnImageLoadListener imageLoadedListener) {
        this.imageLoadedListener = imageLoadedListener;
    }

    public LeaWebViewClient() {
        super();
    }



    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        // Found a bug on some pages where there is an incorrect
        // auto-redirect to file:///android_asset/webkit/.
        AppLog.i("enter shouldoverrideurl...");
        if (!url.equals("file:///android_asset/webkit/")) {
            view.loadUrl(url);
        }

        return true;
    }


    @Override
    public void onPageFinished(WebView view, String url) {
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);

    }

    @Override
    public void onReceivedHttpAuthRequest(WebView view, HttpAuthHandler handler, String host, String realm) {
//        if (mBlog != null && mBlog.hasValidHTTPAuthCredentials()) {
//            // Check that the HTTP AUth protected domain is the same of the blog. Do not send current blog's HTTP
//            // AUTH credentials to external site.
//            // NOTE: There is still a small security hole here, since the realm is not considered when getting
//            // the password. Unfortunately the real is not stored when setting up the blog, and we cannot compare it
//            // at this point.
//            String domainFromHttpAuthRequest = UrlUtils.getDomainFromUrl(UrlUtils.addUrlSchemeIfNeeded(host, false));
//            String currentBlogDomain = UrlUtils.getDomainFromUrl(mBlog.getUrl());
//            if (domainFromHttpAuthRequest.equals(currentBlogDomain)) {
//                handler.proceed(mBlog.getHttpuser(), mBlog.getHttppassword());
//                return;
//            }
//        }
        // TODO: If there is no match show the HTTP Auth dialog here. Like a normal browser usually does...
        super.onReceivedHttpAuthRequest(view, handler, host, realm);
    }

    @Override
    public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
        try {
            if (SelfSignedSSLCertsManager.getInstance(view.getContext()).isCertificateTrusted(error.getCertificate())) {
                handler.proceed();
                return;
            }
        } catch (GeneralSecurityException e) {
            // Do nothing
        } catch (IOException e) {
            // Do nothing
        }

        super.onReceivedSslError(view, handler, error);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {

        AppLog.i("intercept url:" + url);
        WebResourceResponse response = null;
        if (url.indexOf("api/file/getImage") > 0 || url.indexOf("file/outputImage") > 0) {
            //处理图片逻辑
            MediaFile mf = Leanote.leaDB.getMediaFileByUrl(url);
            if (mf != null && !TextUtils.isEmpty(mf.getFilePath())) {
                AppLog.i("image from cache");
                if (mf.getFilePath().contains("content://media/external/images")) {
                    getRealImagePath(mf);
                }
                FileInputStream stream = null;

                try {
                    //filepath不能是content provider,把content provider的地址改为sd卡地址
                    //http://stackoverflow.com/questions/11303118/android-set-a-local-image-to-an-img-element-in-webview
                    stream = new FileInputStream(mf.getFilePath());
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                String mimeType = MediaUtils.getMediaFileMimeType(new File(mf.getFilePath()));
                response = new WebResourceResponse(mimeType, "UTF-8", stream);
                return response;
            } else {
                //本地不存在，从api中下载图片
                new DownloadMediaTask(url).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, Uri.parse(url));
            }

        }

        return super.shouldInterceptRequest(view, url);
    }

    private void getRealImagePath(MediaFile mf) {
        String[] projection = new String[]{MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.MIME_TYPE};

        Cursor cur = Leanote.getContext().getContentResolver().query(Uri.parse(mf.getFilePath()),
                projection, null, null, null);

        if (cur != null && cur.moveToFirst()) {
            int dataColumn = cur.getColumnIndex(MediaStore.Images.Media.DATA);
            String thumbData = cur.getString(dataColumn);

            mf.setFilePath(thumbData);
            Leanote.leaDB.saveMediaFile(mf);

        }
    }

    private class DownloadMediaTask extends AsyncTask<Uri, Integer, Uri> {

        private String url;

        public DownloadMediaTask(String url) {
            this.url = url;
        }

        @Override
        protected Uri doInBackground(Uri... uris) {
            Uri imageUri = uris[0];

            if (imageUri.toString().indexOf("file/outputImage") > 0) {
                //换成api地址下载
                String imageApi = imageUri.toString().replace("file/outputImage", "api/file/getImage");
                imageUri = Uri.parse(imageApi);
            }

            //下载图片带上token
            if (imageUri.toString().indexOf("token=") < 0) {
                imageUri = Uri.parse(imageUri.toString() + "&token=" + AccountHelper.getDefaultAccount().getmAccessToken());
            }
            AppLog.i("download image:" + imageUri);
            return MediaUtils.downloadExternalMedia(Leanote.getContext(), imageUri);
        }

        @Override
        protected void onPostExecute(Uri uri) {
            if (uri == null) {
                AppLog.e(AppLog.T.API, "download image uri is null");
                return;
            }

            super.onPostExecute(uri);
            MediaFile mf = new MediaFile();
            mf.setId(new ObjectId().toString());
            String filePath = uri.toString().replace("file://", "");
            mf.setFilePath(filePath);
            mf.setFileURL(url);
            Leanote.leaDB.saveMediaFile(mf);

            if (imageLoadedListener != null) {
                imageLoadedListener.onImageLoaded(mf.getId());
            }

        }
    }

    public interface OnImageLoadListener {

        void onImageLoaded(String localFileId);

    }
}
