package com.leanote.android.util.helper;

import android.os.AsyncTask;

import com.leanote.android.util.AppLog;
import com.leanote.android.util.AppLog.T;

import java.util.List;

/**
 * Created by binnchx on 10/27/15.
 */
public class ApiHelper {

    public enum ErrorType {
        NO_ERROR, UNKNOWN_ERROR, INVALID_CURRENT_BLOG, NETWORK_XMLRPC, INVALID_CONTEXT,
        INVALID_RESULT, NO_UPLOAD_FILES_CAP, CAST_EXCEPTION, TASK_CANCELLED, UNAUTHORIZED
    }


    public static abstract class HelperAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {
        protected String mErrorMessage;
        protected ErrorType mErrorType = ErrorType.NO_ERROR;
        protected Throwable mThrowable;

        protected void setError(ErrorType errorType, String errorMessage) {
            mErrorMessage = errorMessage;
            mErrorType = errorType;
            AppLog.e(T.API, mErrorType.name() + " - " + mErrorMessage);
        }

        protected void setError(ErrorType errorType, String errorMessage, Throwable throwable) {
            mErrorMessage = errorMessage;
            mErrorType = errorType;
            mThrowable = throwable;
            AppLog.e(T.API, mErrorType.name() + " - " + mErrorMessage, throwable);
        }
    }


    public static class EditMediaItemTask extends HelperAsyncTask<List<?>, Void, Boolean> {
        private GenericCallback mCallback;
        private String mMediaId;
        private String mTitle;
        private String mDescription;
        private String mCaption;

        public EditMediaItemTask(String mediaId, String title, String description, String caption,
                                 GenericCallback callback) {
            mMediaId = mediaId;
            mCallback = callback;
            mTitle = title;
            mCaption = caption;
            mDescription = description;
        }
        @Override
        protected Boolean doInBackground(List<?>... params) {
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (mCallback != null) {
                if (mErrorType == ErrorType.NO_ERROR) {
                    mCallback.onSuccess();
                } else {
                    mCallback.onFailure(mErrorType, mErrorMessage, mThrowable);
                }
            }
        }
    }

    public interface GenericErrorCallback {
        public void onFailure(ErrorType errorType, String errorMessage, Throwable throwable);
    }

    public interface GenericCallback extends GenericErrorCallback {
        public void onSuccess();
    }
}
