package com.leanote.android.ui.note.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.leanote.android.Leanote;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.model.NotebookInfo;
import com.leanote.android.networking.NetworkRequest;
import com.leanote.android.service.NoteSyncService;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.NoteSyncResultEnum;
import com.leanote.android.util.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by binnchx on 1/26/16.
 */
public class NotebookUploadService extends Service {
    public NotebookUploadService() {}

    private static Context mContext;
    private static final ArrayList<NotebookInfo> mNotebookList = new ArrayList<>();
    private static NotebookInfo mCurrentUploadingNotebook = null;
    private NotebookUploadTask mCurrentTask = null;
    //private FeatureSet mFeatureSet;

    public static void addNotebookToUpload(NotebookInfo currentNotebook) {
        synchronized (mNotebookList) {
            mNotebookList.add(currentNotebook);
        }
    }

    /*
     * returns true if the passed NoteDetail is either uploading or waiting to be uploaded
     */
    public static boolean isNotebookUploading(long localNoteId) {
        // first check the currently uploading NoteDetail
        if (mCurrentUploadingNotebook != null && mCurrentUploadingNotebook.getId() == localNoteId) {
            return true;
        }

        // then check the list of NoteDetails waiting to be uploaded
        if (mNotebookList.size() > 0) {
            synchronized (mNotebookList) {
                for (NotebookInfo notebook : mNotebookList) {
                    if (notebook.getId() == localNoteId) {
                        return true;
                    }
                }
            }
        }
        return false;
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

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AppLog.i("upload size:" + mNotebookList.size());

        synchronized (mNotebookList) {
            if (mNotebookList.size() == 0 || mContext == null) {
                stopSelf();
                return START_NOT_STICKY;
            }
        }

        uploadNextNote();
        // We want this service to continue running until it is explicitly stopped, so return sticky.
        return START_STICKY;
    }



    private void uploadNextNote() {
        synchronized (mNotebookList) {
            if (mCurrentTask == null) { //make sure nothing is running
                mCurrentUploadingNotebook = null;
                if (mNotebookList.size() > 0) {
                    mCurrentUploadingNotebook = mNotebookList.remove(0);
                    mCurrentTask = new NotebookUploadTask();
                    mCurrentTask.execute(mCurrentUploadingNotebook);
                } else {
                    stopSelf();
                }
            }
        }
    }


    private class NotebookUploadTask extends AsyncTask<NotebookInfo, Void, NoteSyncResultEnum> {

        @Override
        protected NoteSyncResultEnum doInBackground(NotebookInfo... params) {
            if (params.length == 0) {
                return NoteSyncResultEnum.FAIL;
            }

            NotebookInfo notebook = params[0];
            /*
            1.pull, 2.push. 3.update usn
             */

            //1.pull
            NoteSyncService.syncPullNote();

            //2. push
            String api = null;
            if (!TextUtils.isEmpty(notebook.getNotebookId())) {
                api = String.format("%s/api/notebook/updateNotebook?notebookId=%s&title=%s&parentNotebookid=%s&seq=%s&usn=%s&token=%s",
                        AccountHelper.getDefaultAccount().getHost(),
                        notebook.getNotebookId(),
                        notebook.getTitle(),
                        notebook.getParentNotebookId(),
                        notebook.getSeq(), notebook.getUsn(),
                        AccountHelper.getDefaultAccount().getmAccessToken());

            } else {
                api = String.format("%s/api/notebook/addNotebook?title=%s&parentNotebookid=%s&seq=%s&token=%s",
                        AccountHelper.getDefaultAccount().getHost(),
                        notebook.getTitle(),
                        notebook.getParentNotebookId(),
                        notebook.getSeq(),
                        AccountHelper.getDefaultAccount().getmAccessToken());

            }

            try {
                EventBus.getDefault().post(new NoteEvents.NotebookUploadStarted());
                String response = NetworkRequest.syncGetRequest(api);
                return processResponse(response, notebook.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(NoteSyncResultEnum result) {
            super.onPostExecute(result);
            //refresh notebook list
            notebookUploaded();
            AppLog.i("upload result:" + result);
            EventBus.getDefault().post(new NoteEvents.NotebookUploadEnded(result));
        }
    }

    private void notebookUploaded() {
        synchronized (mNotebookList) {
            mCurrentTask = null;
            mCurrentUploadingNotebook = null;
        }
        uploadNextNote();
    }


    private NoteSyncResultEnum processResponse(String response, Long localNotebookId) throws Exception {
        JSONObject json = new JSONObject(response);
        boolean ok = false;
        if (json.has("Ok")) {
            ok = json.getBoolean("Ok");
        }
        String notebookId = null;
        if (json.has("NotebookId")) {
            notebookId = json.getString("NotebookId");
        }

        String msg = null;
        if (json.has("msg")) {
            msg = json.getString("msg");
        }

        if (!TextUtils.isEmpty(notebookId)) {
            NotebookInfo notebook = NoteSyncService.parseServerNotebook(json);
            //加上本地id
            notebook.setId(localNotebookId);
            Leanote.leaDB.updateNotebook(notebook);
            return NoteSyncResultEnum.SUCCESS;
        } else if (!ok && "conflict".equals(msg)){
            //更新server端笔记本到本地
            handleConflictNotebook(notebookId);
            return NoteSyncResultEnum.CONFLICT;
        }


        return NoteSyncResultEnum.FAIL;
    }

    private void handleConflictNotebook(String notebookId) throws Exception {
        String notebookApi = String.format("%s/api/notebook/getNotebooks?token=%s",
                AccountHelper.getDefaultAccount().getHost(),
                AccountHelper.getDefaultAccount().getmAccessToken());

        String notebookRes = NetworkRequest.syncGetRequest(notebookApi);
        JSONArray jsonArray = new JSONArray(notebookRes);
        JSONObject notebook = null;

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject notebookObj = jsonArray.getJSONObject(i);
            if (StringUtils.equals(notebookId, notebookObj.getString("NotebookId"))) {
                notebook = notebookObj;
            }
        }

        NotebookInfo serverNotebook = NoteSyncService.parseServerNotebook(notebook);
        Leanote.leaDB.updateNotebook(serverNotebook);
    }

}
