package com.leanote.android.task;

import android.net.Uri;
import android.os.AsyncTask;

import com.leanote.android.Leanote;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.LeaWebViewClient;
import com.leanote.android.util.MediaFile;
import com.leanote.android.util.MediaUtils;

import org.bson.types.ObjectId;

/**
 * Created by binnchx on 12/25/15.
 */
public class DownloadMediaTask extends AsyncTask<Uri, Integer, Uri> {

    private String url;
    private LeaWebViewClient.OnImageLoadListener imageLoadedListener;

    public DownloadMediaTask(String url, LeaWebViewClient.OnImageLoadListener imageLoadedListener) {
        this.url = url;
        this.imageLoadedListener = imageLoadedListener;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Uri doInBackground(Uri... uris) {
        Uri imageUri = uris[0];

        AppLog.i("is downloading:" + Leanote.getDownloadingFileUrls().contains(imageUri.toString()));
        if (Leanote.getDownloadingFileUrls().contains(imageUri.toString())) {
            return null;
        }

        Leanote.getDownloadingFileUrls().add(url);

        if (imageUri.toString().indexOf("file/outputImage") > 0) {
            //换成api地址下载
            String imageApi = imageUri.toString().replace("file/outputImage", "api/file/getImage");
            imageUri = Uri.parse(imageApi);
        }

        //下载图片带上token
        if (!imageUri.toString().contains("token=")) {
            imageUri = Uri.parse(imageUri.toString() + "&token=" + AccountHelper.getDefaultAccount().getmAccessToken());
        }
        AppLog.i("download image:" + imageUri);
        return MediaUtils.downloadExternalMedia(Leanote.getContext(), imageUri);
    }

    @Override
    protected void onPostExecute(Uri uri) {
        Leanote.getDownloadingFileUrls().remove(url);
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
        String fileId = url.split("fileId=")[1];
        mf.setMediaId(fileId);
        Leanote.leaDB.saveMediaFile(mf);

        AppLog.i("download ,image listener is null? " + (imageLoadedListener == null));
        if (imageLoadedListener != null) {
            imageLoadedListener.onImageLoaded(mf.getId());
        }

    }
}