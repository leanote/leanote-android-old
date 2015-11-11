package com.leanote.android.service;

import com.android.volley.Request;
import com.leanote.android.Leanote;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.model.NoteContent;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.model.NoteDetailList;
import com.leanote.android.model.NotebookInfo;
import com.leanote.android.networking.NetworkRequest;
import com.leanote.android.util.AppLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binnchx on 11/2/15.
 */
public class NoteSyncService {

    private static final int MAX_SYNC_SIZE = 10;

    public static void syncNote() {
        int lastSyncUsn = AccountHelper.getDefaultAccount().getLastSyncUsn();

        int serverUsn = getServerSyncState();
        AppLog.i("lastusn:" + lastSyncUsn + ", serverUsn:" + serverUsn);

        boolean ifNeedSync = lastSyncUsn < serverUsn;
        if (ifNeedSync) {
            String noteApi = String.format("https://leanote.com/api/note/getSyncNotes?token=%s&maxEntry=%s",
                    AccountHelper.getDefaultAccount().getmAccessToken(), MAX_SYNC_SIZE);

            getSyncNote(noteApi, lastSyncUsn);

            String notebookApi = String.format("https://leanote.com/api/notebook/getNotebooks?token=%s&maxEntry=%s",
                    AccountHelper.getDefaultAccount().getmAccessToken(), MAX_SYNC_SIZE);

            getSyncNotebook(notebookApi, lastSyncUsn);
        }

    }

    private static void getSyncNotebook(String apiPrefix, int lastSyncUsn) {

        String notebookApi;
        if (lastSyncUsn == 0) {
            notebookApi = apiPrefix;
        } else {
            notebookApi = apiPrefix + "&afterUsn=" + lastSyncUsn;
        }


        List<String> localNotebookIds = Leanote.leaDB.getLocalNotebookIds();
        try {
            for (int m = 0; m < Integer.MAX_VALUE; m++) {

                String response = NetworkRequest.syncRequest(notebookApi, Request.Method.GET);
                JSONArray jsonArray = new JSONArray(response);
                AppLog.i("notebook response:" + response);
                updateNotebookToLocal(jsonArray, localNotebookIds);

                if (jsonArray.length() == MAX_SYNC_SIZE) {
                    int afterUsn = jsonArray.getJSONObject(MAX_SYNC_SIZE - 1).getInt("Usn");
                    notebookApi = apiPrefix + "&afterUsn=" + afterUsn;
                } else {
                    break;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void updateNotebookToLocal(JSONArray array, List<String> localNotebookIds) throws Exception {
        List<NotebookInfo> newNotebooks = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.getJSONObject(i);

            boolean isDeleted = item.getBoolean("IsDeleted");
            String notebookId = item.getString("NotebookId");
            if (isDeleted) {
                Leanote.leaDB.deleteNotebook(notebookId);
                continue;
            }

            NotebookInfo serverNotebook = new NotebookInfo();
            serverNotebook.setNotebookId(notebookId);
            serverNotebook.setTitle(item.getString("Title"));
            serverNotebook.setUserId(item.getString("UserId"));
            serverNotebook.setParentNotebookId(item.getString("ParentNotebookId"));
            serverNotebook.setSeq(item.getInt("Seq"));
            serverNotebook.setUrlTitle(item.getString("UrlTitle"));
            serverNotebook.setIsBlog(item.getBoolean("IsBlog"));
            serverNotebook.setCreateTime(item.getString("CreatedTime"));
            serverNotebook.setUpdateTime(item.getString("UpdatedTime"));
            serverNotebook.setUsn(item.getInt("Usn"));
            serverNotebook.setIsDeleted(item.getBoolean("IsDeleted"));

            if (localNotebookIds.contains(notebookId)) {
                if (Leanote.leaDB.getLocalNotebookByNotebookId(notebookId).isDirty()) {
                    //conflict
                    AppLog.i("conflict :" + notebookId);
                } else {
                    //更新本地笔记
                    Leanote.leaDB.updateNotebook(serverNotebook);
                }
            } else {
                //本地新增笔记
                newNotebooks.add(serverNotebook);

            }
        }

        Leanote.leaDB.saveNotebooks(newNotebooks);
    }


    public static int getServerSyncState() {

        String noteApi = String.format("http://leanote.com/api/user/getSyncState?token=%s",
                AccountHelper.getDefaultAccount().getmAccessToken());

        try {
            String response = NetworkRequest.syncRequest(noteApi, Request.Method.GET);
            JSONObject json = new JSONObject(response);
            AppLog.i("sync state:" + json);
            int serverUsn = json.getInt("LastSyncUsn");
            Leanote.leaDB.updateUsn(AccountHelper.getDefaultAccount().getmUserId(), serverUsn);
            return serverUsn;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    public static void getSyncNote(String apiPrefix, int lastSyncUsn) {

        String noteApi;

        if (lastSyncUsn == 0) {
            noteApi = apiPrefix;
        } else {
            noteApi = apiPrefix + "&afterUsn=" + lastSyncUsn;
        }

        try {
            for (int m = 0; m < Integer.MAX_VALUE; m++) {

                String response = NetworkRequest.syncRequest(noteApi, Request.Method.GET);
                JSONArray jsonArray = new JSONArray(response);

                List<String> localNoteIds = Leanote.leaDB.getLocalNoteIds(AccountHelper.getDefaultAccount().getmUserId());
                updateNoteToLocal(jsonArray, localNoteIds);

                if (jsonArray.length() == MAX_SYNC_SIZE) {
                    int afterUsn = jsonArray.getJSONObject(MAX_SYNC_SIZE - 1).getInt("Usn");
                    noteApi = apiPrefix + "&afterUsn=" + afterUsn;
                } else {
                    break;
                }

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void updateNoteToLocal(JSONArray jsonArray, List<String> localNoteIds) throws Exception {
        NoteDetailList syncNotes = new NoteDetailList();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject item = jsonArray.getJSONObject(i);

            boolean isDeleted = item.getBoolean("IsDeleted");
            String noteId = item.getString("NoteId");
            if (isDeleted) {
                Leanote.leaDB.deleteNote(noteId);
                continue;
            }

            NoteDetail serverNote = new NoteDetail();

            serverNote.setNoteId(noteId);
            serverNote.setNoteBookId(item.getString("NotebookId"));
            String userId = item.getString("UserId");
            serverNote.setUserId(userId);
            serverNote.setTitle(item.getString("Title"));

            if (item.getString("Tags") != null) {
                //serverNote.setTags(item.getJSONArray("Tags").toString().replaceAll("[\\[\\]]", ""));
                serverNote.setTags(item.getString("Tags").replaceAll("[\\[\\]]", ""));
            }

            serverNote.setIsPublicBlog(item.getBoolean("IsBlog"));
            serverNote.setUpdatedTime(item.getString("UpdatedTime"));
            serverNote.setUserId(item.getString("UserId"));

            String noteContentApi = String.format("https://leanote.com/api/note/getNoteAndContent?token=%s&noteId=%s",
                    AccountHelper.getDefaultAccount().getmAccessToken(), noteId);

            String contentRes = NetworkRequest.syncRequest(noteContentApi, Request.Method.GET);
            JSONObject json = new JSONObject(contentRes);

            NoteContent noteContent = new NoteContent();
            noteContent.setNoteId(noteId);
            noteContent.setUserId(userId);
            noteContent.setContent(json.getString("Content"));
            serverNote.setContent(noteContent);
            serverNote.setUsn(item.getInt("Usn"));

            if (localNoteIds.contains(noteId)) {
                if (Leanote.leaDB.getLocalNoteByNoteId(noteId).isDirty()) {
                    //conflict
                    AppLog.i("conflict :" + noteId);
                } else {
                    //更新本地笔记
                    Leanote.leaDB.updateNote(serverNote);
                }
            } else {
                //本地新增笔记
                syncNotes.add(serverNote);

            }

        }
        Leanote.leaDB.saveNotes(syncNotes);
    }
}
