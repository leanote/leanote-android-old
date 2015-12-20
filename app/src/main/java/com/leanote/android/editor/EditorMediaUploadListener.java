package com.leanote.android.editor;

/**
 * Created by binnchx on 12/16/15.
 */
public interface EditorMediaUploadListener {
    void onMediaUploadSucceeded(String localId, String remoteId, String remoteUrl);
    void onMediaUploadProgress(String localId, float progress);
    void onMediaUploadFailed(String localId);
}
