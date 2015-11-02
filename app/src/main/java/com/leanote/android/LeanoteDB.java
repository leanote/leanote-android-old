package com.leanote.android;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.leanote.android.datasets.AccountTable;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.model.NoteDetailList;
import com.leanote.android.util.MediaFile;
import com.leanote.android.util.SqlUtils;
import com.leanote.android.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class LeanoteDB extends SQLiteOpenHelper {

    public static final String COLUMN_NAME_NOTE_ID               = "noteID";
    public static final String COLUMN_NAME_FILE_PATH             = "filePath";
    public static final String COLUMN_NAME_FILE_NAME             = "fileName";
    public static final String COLUMN_NAME_TITLE                 = "title";
    public static final String COLUMN_NAME_DESCRIPTION           = "description";
    public static final String COLUMN_NAME_CAPTION               = "caption";
    public static final String COLUMN_NAME_HORIZONTAL_ALIGNMENT  = "horizontalAlignment";
    public static final String COLUMN_NAME_WIDTH                 = "width";
    public static final String COLUMN_NAME_HEIGHT                = "height";
    public static final String COLUMN_NAME_MIME_TYPE             = "mimeType";
    public static final String COLUMN_NAME_FEATURED              = "featured";
    public static final String COLUMN_NAME_IS_VIDEO              = "isVideo";
    public static final String COLUMN_NAME_IS_FEATURED_IN_POST   = "isFeaturedInPost";
    public static final String COLUMN_NAME_FILE_URL              = "fileURL";
    public static final String COLUMN_NAME_THUMBNAIL_URL         = "thumbnailURL";
    public static final String COLUMN_NAME_MEDIA_ID              = "mediaId";
    public static final String COLUMN_NAME_BLOG_ID               = "blogId";
    public static final String COLUMN_NAME_DATE_CREATED_GMT      = "date_created_gmt";
    public static final String COLUMN_NAME_VIDEO_PRESS_SHORTCODE = "videoPressShortcode";
    public static final String COLUMN_NAME_UPLOAD_STATE          = "uploadState";
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
            + "publicTime text default '')";


    private static final String CREATE_TABLE_NOTEBOOKS =
            "create table if not exists notebooks ("
                    + "id integer primary key autoincrement,"
                    + "notebookId text,"
                    + "userId text,"
                    + "title text default '',"
                    + "createdTime text default '',"
                    + "updatedTime text default '')";

    private static final String NOTES_TABLE = "notes";

    private static final String MEDIA_TABLE = "media";

    private static final String CREATE_TABLE_MEDIA = "create table if not exists media (id integer primary key autoincrement, "
            + "noteID text not null, filePath text default '', fileName text default '', title text default '', description text default '', caption text default '', horizontalAlignment integer default 0, width integer default 0, height integer default 0, mimeType text default '', featured boolean default false, isVideo boolean default false);";


    private SQLiteDatabase db;

    private Context context;

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
        AccountTable.createTables(db);
    }



    public static void deleteDatabase(Context ctx) {
        ctx.deleteDatabase(DATABASE_NAME);
    }

    public void saveNotes(List<?> notesList) {
        List<String> noteIds = getLocalNoteIds(AccountHelper.getDefaultAccount().getmUserId());
        if (notesList != null && notesList.size() != 0) {
            db.beginTransaction();
            try {
                for (int i = 0; i < notesList.size(); i++) {
                    ContentValues values = new ContentValues();
                    NoteDetail note = (NoteDetail) notesList.get(i);

                    String noteId = note.getNoteId();
                    if (noteIds.contains(noteId)) {
                        continue;
                    }

                    values.put("noteId", note.getNoteId());
                    values.put("notebookId", note.getNoteBookId());
                    values.put("userId", note.getUserId());
                    values.put("title", note.getTitle());
                    values.put("updatedTime", note.getUpdatedTime());

                    db.insert(NOTES_TABLE, null, values);
                }

                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        }
    }


    public NoteDetailList getNotesList(String userId) {
        NoteDetailList listPosts = new NoteDetailList();

        String[] args = {userId};
        //Cursor c = db.query(NOTES_TABLE, null, null, null, null, null, "");
        Cursor c = db.query(NOTES_TABLE, null, "userId=?", args, null, null, "");
        try {
            while (c.moveToNext()) {
                String title = c.getString(4);
                String updateTime = c.getString(12);
                NoteDetail detail = new NoteDetail();

                detail.setId(c.getLong(0));
                detail.setNoteId(c.getString(1));
                detail.setTitle(title);
                detail.setContent(c.getString(6));
                detail.setUpdatedTime(updateTime);

                listPosts.add(detail);
            }
            return listPosts;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }




    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    private List<String> getLocalNoteIds(String userId) {

        String[] args = {userId};
        //Cursor c = db.query(NOTES_TABLE, null, null, null, null, null, "");
        Cursor c = db.query(NOTES_TABLE, null, "userId=?", args, null, null, "");
        List<String> noteIds = new ArrayList<>();
        try {
            while (c.moveToNext()) {

                noteIds.add(c.getString(1));
            }
            return noteIds;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public NoteDetail getLocalNoteById(long localNoteId) {
        String[] args = {String.valueOf(localNoteId)};
        //Cursor c = db.query(NOTES_TABLE, null, null, null, null, null, "");
        Cursor c = db.query(NOTES_TABLE, null, "id=?", args, null, null, "");

        NoteDetail detail = null;
        try {
            while (c.moveToNext()) {
                detail = new NoteDetail();
                detail.setNoteId(c.getString(1));
                detail.setTitle(c.getString(4));
                detail.setContent(c.getString(6));
                detail.setUpdatedTime(c.getString(12));
            }
            return detail;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public void saveMediaFile(MediaFile mf) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_NOTE_ID, mf.getNoteID());
        values.put(COLUMN_NAME_FILE_PATH, mf.getFilePath());
        values.put(COLUMN_NAME_FILE_NAME, mf.getFileName());
        values.put(COLUMN_NAME_TITLE, mf.getTitle());
        values.put(COLUMN_NAME_DESCRIPTION, mf.getDescription());
        values.put(COLUMN_NAME_CAPTION, mf.getCaption());
        values.put(COLUMN_NAME_HORIZONTAL_ALIGNMENT, mf.getHorizontalAlignment());
        values.put(COLUMN_NAME_WIDTH, mf.getWidth());
        values.put(COLUMN_NAME_HEIGHT, mf.getHeight());
        values.put(COLUMN_NAME_MIME_TYPE, mf.getMimeType());
        values.put(COLUMN_NAME_FEATURED, mf.isFeatured());
        values.put(COLUMN_NAME_IS_VIDEO, mf.isVideo());
        values.put(COLUMN_NAME_IS_FEATURED_IN_POST, mf.isFeaturedInPost());
        values.put(COLUMN_NAME_FILE_URL, mf.getFileURL());
        values.put(COLUMN_NAME_THUMBNAIL_URL, mf.getThumbnailURL());
        values.put(COLUMN_NAME_MEDIA_ID, mf.getMediaId());
        values.put(COLUMN_NAME_BLOG_ID, mf.getBlogId());
        values.put(COLUMN_NAME_DATE_CREATED_GMT, mf.getDateCreatedGMT());
        values.put(COLUMN_NAME_VIDEO_PRESS_SHORTCODE, mf.getVideoPressShortCode());
        if (mf.getUploadState() != null)
            values.put(COLUMN_NAME_UPLOAD_STATE, mf.getUploadState());
        else
            values.putNull(COLUMN_NAME_UPLOAD_STATE);

        synchronized (this) {
            int result = 0;
            boolean isMarkedForDelete = false;
            if (mf.getMediaId() != null) {
                Cursor cursor = db.rawQuery("SELECT uploadState FROM " + MEDIA_TABLE + " WHERE mediaId=?",
                        new String[]{StringUtils.notNullStr(mf.getMediaId())});
                if (cursor != null && cursor.moveToFirst()) {
                    isMarkedForDelete = "delete".equals(cursor.getString(0));
                    cursor.close();
                }

                if (!isMarkedForDelete)
                    result = db.update(MEDIA_TABLE, values, "blogId=? AND mediaId=?",
                            new String[]{StringUtils.notNullStr(mf.getBlogId()), StringUtils.notNullStr(mf.getMediaId())});
            }

            if (result == 0 && !isMarkedForDelete) {
                result = db.update(MEDIA_TABLE, values, "postID=? AND filePath=?",
                        new String[]{String.valueOf(mf.getNoteID()), StringUtils.notNullStr(mf.getFilePath())});
                if (result == 0)
                    db.insert(MEDIA_TABLE, null, values);
            }
        }
    }

    public void saveNoteContent(String noteId, String content) {
        ContentValues values = new ContentValues();
        values.put("content", content);
        db.update(NOTES_TABLE, values, "noteId=?",
                new String[]{noteId});

    }

    public long saveNote(NoteDetail newNote) {
        ContentValues values = new ContentValues();

        values.put("noteId", newNote.getNoteId());
        values.put("notebookId", newNote.getNoteBookId());
        values.put("userId", newNote.getUserId());
        values.put("title", newNote.getTitle());
        values.put("updatedTime", newNote.getUpdatedTime());

        long result = db.insert(NOTES_TABLE, null, values);
        if (result > 0) {
            newNote.setId(result);
        }
        return result;
    }

    public NoteDetail getLocalNoteByNoteId(String noteId) {
        String[] args = {String.valueOf(noteId)};
        //Cursor c = db.query(NOTES_TABLE, null, null, null, null, null, "");
        Cursor c = db.query(NOTES_TABLE, null, "noteId=?", args, null, null, "");

        NoteDetail detail = null;
        try {
            while (c.moveToNext()) {
                detail = new NoteDetail();
                detail.setId(c.getLong(0));
                detail.setNoteId(c.getString(1));
                detail.setTitle(c.getString(4));
                detail.setContent(c.getString(6));
                detail.setUpdatedTime(c.getString(12));
            }
            return detail;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public String[] getNotebookTitles() {

        return null;
    }

    public void deleteNote(NoteDetail note) {
        String sql = "delete from notes where noteId='" + note.getNoteId() + "'" ;
        db.execSQL(sql);
    }
}
