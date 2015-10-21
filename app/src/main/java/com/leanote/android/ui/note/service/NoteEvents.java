package com.leanote.android.ui.note.service;

import com.leanote.android.util.StringUtils;

/**
 * Created by binnchx on 10/18/15.
 */
public class NoteEvents {

    public static class PostUploadStarted {
        public final int mLocalBlogId;

        PostUploadStarted(int localBlogId) {
            mLocalBlogId = localBlogId;
        }
    }

    public static class PostUploadEnded {
        public final int mLocalBlogId;
        public final boolean mSucceeded;

        PostUploadEnded(boolean succeeded, int localBlogId) {
            mSucceeded = succeeded;
            mLocalBlogId = localBlogId;
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
        private boolean mFailed;
        private String errorMsg;
        RequestNotes() {
            mFailed = false;
        }

        public boolean getFailed() {
            return mFailed;
        }

        public void setErrorType(String errorMsg) {
            this.errorMsg = errorMsg;
            mFailed = true;
        }

    }

}