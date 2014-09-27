package com.leanote.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.leanote.model.NoteColumns;
import com.leanote.model.NotebookColumns;
import com.leanote.util.MLog;

/**
 * Database helper
 * <p/>
 * Created by jerrychoi on 14-9-22.
 */
public class LeanoteDatabase extends SQLiteOpenHelper {

    private static final String TAG = "LeanoteDatabase";

    private static final String DATABASE_NAME = "leanote_main.db";
    private static final int DATABASE_VERSION = 1;

    private final Context mContext;

    public LeanoteDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();

        db.execSQL(NotebookColumns.CREATE_TABLE);
        db.execSQL(NoteColumns.CREATE_TABLE);

        db.setTransactionSuccessful();
        db.endTransaction();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        switch (oldVersion) {
            // TODO
        }

        // drop old tables
        if (MLog.LOG_DB) {
            Log.e(TAG, "Destroying all old data.");
        }
        // TODO

        // create new tables
        onCreate(db);
    }
}
