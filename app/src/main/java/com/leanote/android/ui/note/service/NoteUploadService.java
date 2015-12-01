package com.leanote.android.ui.note.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.leanote.android.model.NoteDetail;
import com.leanote.android.util.AppLog;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class NoteUploadService extends Service {
    public NoteUploadService() {
    }

    private static Context mContext;
    private static final ArrayList<NoteDetail> mNoteDetailsList = new ArrayList<NoteDetail>();
    private static NoteDetail mCurrentUploadingNote = null;
    private UploadNoteTask mCurrentTask = null;
    //private FeatureSet mFeatureSet;

    public static void addNoteToUpload(NoteDetail currentNote) {
        synchronized (mNoteDetailsList) {
            mNoteDetailsList.add(currentNote);
        }
    }

    /*
     * returns true if the passed NoteDetail is either uploading or waiting to be uploaded
     */
    public static boolean isNoteUploading(long localNoteId) {
        // first check the currently uploading NoteDetail
        if (mCurrentUploadingNote != null && mCurrentUploadingNote.getId() == localNoteId) {
            return true;
        }

        // then check the list of NoteDetails waiting to be uploaded
        if (mNoteDetailsList.size() > 0) {
            synchronized (mNoteDetailsList) {
                for (NoteDetail note : mNoteDetailsList) {
                    if (note.getId() == localNoteId) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this.getApplicationContext();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cancel current task, it will reset NoteDetail from "uploading" to "local draft"
        if (mCurrentTask != null) {
            AppLog.d(AppLog.T.POSTS, "cancelling current upload task");
            mCurrentTask.cancel(true);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        synchronized (mNoteDetailsList) {
            if (mNoteDetailsList.size() == 0 || mContext == null) {
                stopSelf();
                return START_NOT_STICKY;
            }
        }

        uploadNextNote();
        // We want this service to continue running until it is explicitly stopped, so return sticky.
        return START_STICKY;
    }



    private void uploadNextNote() {
        synchronized (mNoteDetailsList) {
            if (mCurrentTask == null) { //make sure nothing is running
                mCurrentUploadingNote = null;
                if (mNoteDetailsList.size() > 0) {
                    mCurrentUploadingNote = mNoteDetailsList.remove(0);
                    mCurrentTask = new UploadNoteTask();
                    mCurrentTask.execute(mCurrentUploadingNote);
                } else {
                    stopSelf();
                }
            }
        }
    }

    private void NoteUploaded() {
        synchronized (mNoteDetailsList) {
            mCurrentTask = null;
            mCurrentUploadingNote = null;
        }
        uploadNextNote();
    }

    private class UploadNoteTask extends AsyncTask<NoteDetail, Boolean, Boolean> {

        @Override
        protected Boolean doInBackground(NoteDetail... params) {

            return null;
        }
    }

    private File createTempUploadFile(String fileExtension) throws IOException {
        return File.createTempFile("wp-", fileExtension, mContext.getCacheDir());
    }


}
