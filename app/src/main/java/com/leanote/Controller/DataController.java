package com.leanote.controller;

import android.database.Cursor;

/**
 * Created by jerrychoi on 2014-9-26.
 */
public class DataController {

    private static final String TAG = DataController.class.getSimpleName();

    public static int parseInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(columnName));
    }

    public static long parseLong(Cursor cursor, String columnName) {
        return cursor.getLong(cursor.getColumnIndexOrThrow(columnName));
    }

    public static String parseString(Cursor cursor, String columnName) {
        return cursor.getString(cursor.getColumnIndexOrThrow(columnName));
    }

    public static boolean parseBoolean(Cursor cursor, String columnName) {
        return cursor.getInt(cursor.getColumnIndexOrThrow(columnName)) != 0;
    }

}
