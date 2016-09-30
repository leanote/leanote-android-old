package com.leanote.android.db;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class LeanoteDB extends SQLiteOpenHelper {

    public static final String COLUMN_NAME_ID = "id";
    public static final String COLUMN_NAME_NOTE_ID = "noteID";
    public static final String COLUMN_NAME_FILE_PATH = "filePath";
    public static final String COLUMN_NAME_FILE_NAME = "fileName";
    public static final String COLUMN_NAME_TITLE = "title";
    public static final String COLUMN_NAME_DESCRIPTION = "description";
    public static final String COLUMN_NAME_CAPTION = "caption";
    public static final String COLUMN_NAME_HORIZONTAL_ALIGNMENT = "horizontalAlignment";
    public static final String COLUMN_NAME_WIDTH = "width";
    public static final String COLUMN_NAME_HEIGHT = "height";
    public static final String COLUMN_NAME_MIME_TYPE = "mimeType";
    public static final String COLUMN_NAME_FILE_URL = "fileURL";
    public static final String COLUMN_NAME_THUMBNAIL_URL = "thumbnailURL";
    public static final String COLUMN_NAME_MEDIA_ID = "mediaId";
    public static final String COLUMN_NAME_UPLOAD_STATE = "uploadState";
    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "leanote";

    private static final String CREATE_TABLE_NOTES =
            "create table if not exists notes ("
                    + "id integer primary key autoincrement,"
                    + "noteId text,"
                    + "notebookId text,"
                    + "userId text,"
                    + "title text default '',"
                    + "tags text default '',"
                    + "content text default '',"
                    + "isMarkDown integer default 0,"
                    + "isBlog integer default 0,"
                    + "isTrash integer default 0,"
                    + "files text default '',"
                    + "createdTime text default '',"
                    + "updatedTime text default '',"
                    + "publicTime text default '',"
                    + "usn  integer default 0,"
                    + "desc text default '',"
                    + "note_abstract default '',"
                    + "is_dirty integer default 0,"
                    + "isUploading integer default 0)";

//    private static final String CREATE_TABLE_NOTE_CONTENT =
//            "create table if not exists note_content ("
//                    + "noteId text primary key,"
//                    + "userId text,"
//                    + "content text);";


    private static final String CREATE_TABLE_NOTEBOOKS =
            "create table if not exists notebooks ("
                    + "id integer primary key autoincrement,"
                    + "notebookId text,"
                    + "parentNotebookId text,"
                    + "userId text,"
                    + "title text default '',"
                    + "urlTitle text default '',"
                    + "isBlog integer default 0,"
                    + "isTrash integer default 0,"
                    + "isDeleted integer default 0,"
                    + "createdTime text default '',"
                    + "updatedTime text default '',"
                    + "usn integer,"
                    + "is_dirty integer default 0)";

    public static final String NOTES_TABLE = "notes";

    public static final String ACCOUNT_TABLE = "accounts";

    public static final String NOTEBOOKS_TABLE = "notebooks";

    //private static final String NOTE_CONTENT_TABLE = "note_content";

    public static final String MEDIA_TABLE = "media";

    private static final String CREATE_TABLE_MEDIA = "create table if not exists media "
            + "(id text primary key, "
            + "mediaID text default '',"
            + "noteID text default '', filePath text default '', "
            + "fileName text default '', title text default '', "
            + "fileURL text default '',"
            + "thumbnailURL text default '',"
            + "uploadState text default '',"
            + "description text default '', caption text default '', "
            + "horizontalAlignment integer default 0, width integer default 0, "
            + "height integer default 0, mimeType text default '');";


    private static final String CREATE_ACCOUNTS_TABLE = "CREATE TABLE " + ACCOUNT_TABLE + " ("
            + "local_id                INTEGER PRIMARY KEY DEFAULT 0,"
            + "user_name               TEXT,"
            + "user_id                 TEXT,"
            + "email                   TEXT,"
            + "verified                INTEGER default 0,"
            + "logo                    TEXT,"
            + "access_token            TEXT,"
            + "isMarkDown              INTEGER default 0,"
            + "usn                     INTEGER,"
            + "host                    TEXT default 'https://leanote.com')";

    private SQLiteDatabase db;

    public SQLiteDatabase getDatabase() {
        return db;
    }

    public LeanoteDB(Context ctx) {
        super(ctx, DATABASE_NAME, null, DATABASE_VERSION);
        //db = ctx.openOrCreateDatabase(DATABASE_NAME, 0, null);
        db = this.getWritableDatabase();

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_NOTES);
        db.execSQL(CREATE_TABLE_NOTEBOOKS);
        db.execSQL(CREATE_TABLE_MEDIA);
        db.execSQL(CREATE_ACCOUNTS_TABLE);
    }

    public static void deleteDatabase(Context ctx) {
        ctx.deleteDatabase(DATABASE_NAME);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


}
