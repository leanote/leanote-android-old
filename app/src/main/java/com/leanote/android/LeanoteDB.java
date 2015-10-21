package com.leanote.android;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.leanote.android.model.NoteDetail;
import com.leanote.android.model.NoteDetailList;
import com.leanote.android.util.SqlUtils;

import java.util.ArrayList;
import java.util.List;

public class LeanoteDB extends SQLiteOpenHelper {


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

    private static final String NOTES_TABLE = "notes";

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
    }



    public static void deleteDatabase(Context ctx) {
        ctx.deleteDatabase(DATABASE_NAME);
    }

    public void saveNotes(List<?> notesList) {
        List<String> noteIds = getLocalNoteIds(Leanote.getUserID());
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
                detail.setTitle(title);
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
}
