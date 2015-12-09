package com.leanote.android.datasets;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.leanote.android.Leanote;
import com.leanote.android.util.SqlUtils;
import com.leanote.android.model.Account;


public class AccountTable {
    // Warning: the "accounts" table in WordPressDB is actually where blogs are stored.
    private static final String ACCOUNT_TABLE = "accounts";

    private static SQLiteDatabase getReadableDb() {
        return Leanote.leaDB.getDatabase();
    }
    private static SQLiteDatabase getWritableDb() {
        return Leanote.leaDB.getDatabase();
    }

    public static void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + ACCOUNT_TABLE + " ("
                + "local_id                INTEGER PRIMARY KEY DEFAULT 0,"
                + "user_name               TEXT,"
                + "user_id                 TEXT,"
                + "email                   TEXT,"
                + "verified                INTEGER default 0,"
                + "logo                    TEXT,"
                + "access_token            TEXT,"
                + "isMarkDown              INTEGER default 0,"
                + "usn                     INTEGER,"
                + "host                    TEXT default 'https://leanote.com')");
    }

    private static void dropTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + ACCOUNT_TABLE);
    }

    public static void save(Account account) {
        save(account, getWritableDb());
    }

    public static void save(Account account, SQLiteDatabase database) {
        ContentValues values = new ContentValues();
        // we only support one wpcom user at the moment: local_id is always 0
        values.put("local_id", 0);
        values.put("user_name", account.getmUserName());
        values.put("user_id", account.getmUserId());
        values.put("email", account.getmEmail());
        values.put("verified", account.isVerified() ? 0 : 1);
        values.put("logo", account.getmAvatar());
        values.put("access_token", account.getmAccessToken());
        values.put("host", account.getHost());

        database.insertWithOnConflict(ACCOUNT_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public static Account getDefaultAccount() {
        return getAccountByLocalId(0);
    }

    private static Account getAccountByLocalId(long localId) {
        Account account = new Account();

        String[] args = {Long.toString(localId)};
        Cursor c = getReadableDb().rawQuery("SELECT * FROM " + ACCOUNT_TABLE + " WHERE local_id=?", args);

        try {
            if (c.moveToFirst()) {
                account.setmUserName(c.getString(c.getColumnIndex("user_name")));
                account.setmUserId(c.getString(c.getColumnIndex("user_id")));
                account.setmEmail(c.getString(c.getColumnIndex("email")));
                account.setmAvatar(c.getString(c.getColumnIndex("logo")));
                account.setVerified(c.getInt(c.getColumnIndex("verified")) == 0 ? false : true);
                account.setmAccessToken(c.getString(c.getColumnIndex("access_token")));
                account.setUseMarkdown(c.getInt(c.getColumnIndex("isMarkDown")) == 0 ? false : true);
                account.setLastSyncUsn(c.getInt(c.getColumnIndex("usn")));
                account.setHost(c.getString(c.getColumnIndex("host")));
            }
            return account;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

}
