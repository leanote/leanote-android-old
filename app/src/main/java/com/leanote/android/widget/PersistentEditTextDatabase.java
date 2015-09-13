package com.leanote.android.widget;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.database.sqlite.SQLiteDatabase.CursorFactory;

/**
 * Created by binnchx on 8/27/15.
 */
public class PersistentEditTextDatabase extends SQLiteOpenHelper {
    private static final String TAG = "PersistentEditText";
    private static final int MAX_ENTRIES = 100;
    private static final String DATABASE_NAME = "persistentedittext.db";
    private static final int DATABASE_VERSION = 1;
    private static final String PERSISTENTEDITTEXT_TABLE = "persistentedittext";

    public PersistentEditTextDatabase(Context context) {
        super(context, "persistentedittext.db", (CursorFactory)null, 1);
    }

    public void onCreate(SQLiteDatabase db) {
        this.createTables(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        this.reset(db);
    }

    public void reset(SQLiteDatabase db) {
        Log.i("PersistentEditText", "resetting persistentedittext table");
        this.dropTables(db);
        this.createTables(db);
    }

    protected void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS persistentedittext (_id\t     INTEGER PRIMARY KEY AUTOINCREMENT,key       TEXT UNIQUE,value     TEXT)");
    }

    protected void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS persistentedittext");
    }

    public void put(String key, String value) {
        ContentValues values = new ContentValues();
        values.put("key", key);
        values.put("value", value);
        this.getWritableDatabase().insertWithOnConflict("persistentedittext", (String)null, values, 5);
    }

    public String get(String key, String defaultResult) {
        Cursor c = this.getReadableDatabase().query("persistentedittext", new String[]{"value"}, "key=?", new String[]{key}, (String)null, (String)null, (String)null);

        try {
            if(c.moveToFirst()) {
                String var4 = c.getString(0);
                return var4;
            }
        } finally {
            if(c != null && !c.isClosed()) {
                c.close();
            }

        }

        return defaultResult;
    }

    public void remove(String key) {
        this.getWritableDatabase().delete("persistentedittext", "key=?", new String[]{key});
        this.purge(100);
    }

    private void purge(int limit) {
        this.getWritableDatabase().delete("persistentedittext", "_id NOT IN (SELECT _id FROM persistentedittext ORDER BY _id DESC LIMIT " + limit + ")", (String[])null);
    }
}