package com.leanote.android.ui.note.service;

import com.leanote.android.util.NoteSyncResultEnum;
import com.leanote.android.util.StringUtils;

/**
 * Created by binnchx on 10/18/15.
 */
public class NoteEvents {

    public static class PostUploadStarted {

        PostUploadStarted() {}
    }

    public static class NotebookUploadStarted {

        NotebookUploadStarted() {}
    }

    public static class PostUploadEnded {
        public final NoteSyncResultEnum result;

        PostUploadEnded(NoteSyncResultEnum result) {
            this.result = result;
        }
    }

    public static class NotebookUploadEnded {
        public final NoteSyncResultEnum result;

        NotebookUploadEnded(NoteSyncResultEnum result) {
            this.result = result;
        }
    }



    public static class PostMediaInfoUpdated {
        private long mMediaId;
        private String mMediaUrl;

        PostMediaInfoUpdated(long mediaId, String mediaUrl) {
            mMediaId = mediaId;
            mMediaUrl = mediaUrl;
        }
        public long getMediaId() {
            return mMediaId;
        }
        public String getMediaUrl() {
            return StringUtils.notNullStr(mMediaUrl);
        }
    }

    public static class RequestNotes {
        private boolean mFailed = false;
        private String errorMsg;
        RequestNotes() {
            mFailed = false;
        }

        public boolean ismFailed() {
            return mFailed;
        }

        public void setmFailed(boolean mFailed) {
            this.mFailed = mFailed;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }
    }

}