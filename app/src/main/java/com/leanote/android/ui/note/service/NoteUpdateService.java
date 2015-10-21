package com.leanote.android.ui.note.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.leanote.android.Leanote;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.model.NoteDetailList;

import org.json.JSONArray;
import org.json.JSONObject;

import de.greenrobot.event.EventBus;

/**
 * Created by binnchx on 10/19/15.
 */
public class NoteUpdateService extends Service {


    public static void startServiceForNote(Context context) {
        Intent intent = new Intent(context, NoteUpdateService.class);
        context.startService(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        new Thread() {
            @Override
            public void run() {
                fetchNotes();
            }
        }.start();

        return START_NOT_STICKY;
    }

    private void fetchNotes() {
        String noteApi = String.format("http://leanote.com/api/note/getSyncNotes?token=%s", Leanote.getAccessToken());

        RequestFuture<String> future = RequestFuture.newFuture();

        StringRequest noteReq = new StringRequest(Request.Method.GET,
                noteApi,
                future,
                future);

        Leanote.requestQueue.add(noteReq);

        String response = "";
        NoteDetailList noteList = new NoteDetailList();
        NoteEvents.RequestNotes event = new NoteEvents.RequestNotes();

        try {
            response = future.get();
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                NoteDetail note = new NoteDetail();
                note.setTitle(item.getString("Title"));
                note.setUpdatedTime(item.getString("UpdatedTime"));
                note.setUserId(item.getString("UserId"));
                note.setNoteId(item.getString("NoteId"));
                note.setNoteBookId(item.getString("NotebookId"));

                noteList.add(note);
            }
            Log.i("response note size:", String.valueOf(noteList.size()));

            Leanote.leaDB.saveNotes(noteList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventBus.getDefault().post(event);
    }


}
