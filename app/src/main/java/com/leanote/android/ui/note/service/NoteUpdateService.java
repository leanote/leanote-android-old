package com.leanote.android.ui.note.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.leanote.android.model.AccountHelper;
import com.leanote.android.service.NoteSyncService;

import de.greenrobot.event.EventBus;

/**
 * Created by binnchx on 10/19/15.
 */
public class NoteUpdateService extends Service {


    public static void startServiceForNote(Context context) {
        if (!AccountHelper.isSignedIn()) {
            return;
        }

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
        Log.v("fetchNotes", "haha");



        NoteEvents.RequestNotes event = new NoteEvents.RequestNotes();
        NoteSyncService.syncPullNote();


        EventBus.getDefault().post(event);
    }


}
