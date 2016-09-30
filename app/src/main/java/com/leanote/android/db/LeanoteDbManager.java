package com.leanote.android.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.leanote.android.Leanote;
import com.leanote.android.model.Account;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.model.NoteDetailList;
import com.leanote.android.model.NoteInfo;
import com.leanote.android.model.NotebookInfo;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.MediaFile;
import com.leanote.android.util.SqlUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yuchuan
 * DATE 7/23/16
 * TIME 08:49
 */
public class LeanoteDbManager {

    public static LeanoteDbManager sLeanoteDbManager;
    private LeanoteDB mLeanoteDB;
    private SQLiteDatabase mDb;

    public LeanoteDbManager() {
        mLeanoteDB = new LeanoteDB(Leanote.getContext());
    }

    public static LeanoteDbManager getInstance() {
        if (sLeanoteDbManager == null) {
            synchronized (LeanoteDbManager.class) {
                if (sLeanoteDbManager == null) {
                    sLeanoteDbManager = new LeanoteDbManager();
                }
            }
        }
        return sLeanoteDbManager;
    }

    public synchronized void saveNotes(List<?> notesList) {
        mDb = mLeanoteDB.getWritableDatabase();
        List<String> noteIds = getLocalNoteIds(AccountHelper.getDefaultAccount().getUserId());
        if (notesList != null && notesList.size() != 0) {
            mDb.beginTransaction();
            try {
                for (int i = 0; i < notesList.size(); i++) {

                    NoteInfo note = (NoteInfo) notesList.get(i);

                    String noteId = note.getNoteId();
                    if (noteIds.contains(noteId)) {
                        continue;
                    }
                    ContentValues values = getContentValuesFromNote(note);
                    mDb.insert(LeanoteDB.NOTES_TABLE, null, values);
                }
                mDb.setTransactionSuccessful();
            } finally {
                mDb.endTransaction();
            }
        }
    }

    public synchronized NoteDetailList getNotesList(String userId) {
        mDb = mLeanoteDB.getWritableDatabase();
        NoteDetailList listPosts = new NoteDetailList();
        String[] args = {userId};
        Cursor c = mDb.query(LeanoteDB.NOTES_TABLE, null, "userId=?", args, null, null, "");
        try {
            while (c.moveToNext()) {
                NoteInfo detail = fillNote(c);
                listPosts.add(detail);
            }
            return listPosts;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public synchronized List<String> getLocalNoteIds(String userId) {
        mDb = mLeanoteDB.getWritableDatabase();
        String[] args = {userId};
        //Cursor c = mDb.query(NOTES_TABLE, null, null, null, null, null, "");
        Cursor c = mDb.query(LeanoteDB.NOTES_TABLE, null, "userId=?", args, null, null, "");
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

    public synchronized NoteInfo getLocalNoteById(long localNoteId) {
        mDb = mLeanoteDB.getWritableDatabase();
        String[] args = {String.valueOf(localNoteId)};
        //Cursor c = mDb.query(NOTES_TABLE, null, null, null, null, null, "");
        Cursor c = mDb.query(LeanoteDB.NOTES_TABLE, null, "id=?", args, null, null, "");
        NoteInfo detail = null;
        try {
            if (c.moveToNext()) {
                detail = fillNote(c);
            }
            return detail;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    private NoteInfo fillNote(Cursor c) {
        NoteInfo detail = new NoteInfo();
        detail.setId(c.getLong(0));
        detail.setNoteId(c.getString(1));
        detail.setNoteBookId(c.getString(2));
        detail.setUserId(c.getString(3));
        detail.setTitle(c.getString(4));
        detail.setTags(c.getString(5));
        detail.setContent(c.getString(6));
        detail.setIsMarkDown(c.getInt(7) != 0);
        detail.setIsPublicBlog(c.getInt(8) != 0);
        detail.setIsTrash(c.getInt(9) != 0);
        detail.setFileIds(c.getString(10));
        detail.setCreatedTime(c.getString(11));
        detail.setUpdatedTime(c.getString(12));
        detail.setPublicTime(c.getString(13));
        detail.setUsn(c.getInt(14));
        detail.setDesc(c.getString(15));
        detail.setNoteAbstract(c.getString(16));
        detail.setIsDirty(c.getInt(17) != 0);
        detail.setIsUploading(c.getInt(18) != 0);
        return detail;
    }

//    public NoteContent getNoteContentByNoteId(String noteId) {
//        String[] args = {String.valueOf(noteId)};
//        Cursor c = mDb.query(NOTE_CONTENT_TABLE, null, "noteId=?", args, null, null, "");
//        NoteContent content = new NoteContent();
//        if (c.moveToNext()) {
//            content.setNoteId(noteId);
//            content.setUserId(c.getString(1));
//            content.setNoteId(c.getString(2));
//        }
//        return content;
//    }

    private ContentValues getContentsFromMf(MediaFile mf) {
        ContentValues values = new ContentValues();
        values.put(LeanoteDB.COLUMN_NAME_ID, mf.getId());
        values.put(LeanoteDB.COLUMN_NAME_NOTE_ID, mf.getNoteID());
        values.put(LeanoteDB.COLUMN_NAME_FILE_PATH, mf.getFilePath());
        values.put(LeanoteDB.COLUMN_NAME_FILE_NAME, mf.getFileName());
        values.put(LeanoteDB.COLUMN_NAME_TITLE, mf.getTitle());
        values.put(LeanoteDB.COLUMN_NAME_DESCRIPTION, mf.getDescription());
        values.put(LeanoteDB.COLUMN_NAME_CAPTION, mf.getCaption());
        values.put(LeanoteDB.COLUMN_NAME_HORIZONTAL_ALIGNMENT, mf.getHorizontalAlignment());
        values.put(LeanoteDB.COLUMN_NAME_WIDTH, mf.getWidth());
        values.put(LeanoteDB.COLUMN_NAME_HEIGHT, mf.getHeight());
        values.put(LeanoteDB.COLUMN_NAME_MIME_TYPE, mf.getMimeType());
//        values.put(LeanoteDB.COLUMN_NAME_FEATURED, mf.isFeatured());
//        values.put(LeanoteDB.COLUMN_NAME_IS_FEATURED_IN_POST, mf.isFeaturedInPost());
        values.put(LeanoteDB.COLUMN_NAME_FILE_URL, mf.getFileURL());
        values.put(LeanoteDB.COLUMN_NAME_THUMBNAIL_URL, mf.getThumbnailURL());
        values.put(LeanoteDB.COLUMN_NAME_MEDIA_ID, mf.getMediaId());
        //values.put(COLUMN_NAME_DATE_CREATED_GMT, mf.getDateCreatedGMT());
        if (mf.getUploadState() != null) {
            values.put(LeanoteDB.COLUMN_NAME_UPLOAD_STATE, mf.getUploadState());
        } else {
            values.putNull(LeanoteDB.COLUMN_NAME_UPLOAD_STATE);
        }
        return values;
    }

    public synchronized void saveMediaFile(MediaFile mf) {
        mDb = mLeanoteDB.getWritableDatabase();
        ContentValues values = getContentsFromMf(mf);
        int result = mDb.update(LeanoteDB.MEDIA_TABLE, values, "id=?",
                new String[]{mf.getId()});

        if (result == 0) {
            AppLog.i("insert new media:" + values);
            mDb.insert(LeanoteDB.MEDIA_TABLE, null, values);
        }
    }

    public synchronized void saveNoteContent(String noteId, String content) {
        if (TextUtils.isEmpty(noteId)) {
            return;
        }
        mDb = mLeanoteDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("content", content);
        mDb.update(LeanoteDB.NOTES_TABLE, values, "noteId=?", new String[]{noteId});
    }

    public synchronized long addNote(NoteInfo newNote) {
        mDb = mLeanoteDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("noteId", newNote.getNoteId());
        values.put("notebookId", newNote.getNoteBookId());
        values.put("userId", newNote.getUserId());
        values.put("title", newNote.getTitle());
        values.put("updatedTime", newNote.getUpdatedTime());
        long result = mDb.insert(LeanoteDB.NOTES_TABLE, null, values);
        if (result > 0) {
            newNote.setId(result);
        }
        return result;
    }

    public synchronized void updateNote(NoteInfo note) {
        mDb = mLeanoteDB.getWritableDatabase();
        ContentValues values = getContentValuesFromNote(note);
        mDb.update(LeanoteDB.NOTES_TABLE, values, "id=?", new String[]{String.valueOf(note.getId())});
    }

    public synchronized void updateNoteByNoteId(NoteInfo note) {
        mDb = mLeanoteDB.getWritableDatabase();
        ContentValues values = getContentValuesFromNote(note);
        mDb.update(LeanoteDB.NOTES_TABLE, values, "noteId=?", new String[]{note.getNoteId()});
    }

    private ContentValues getContentValuesFromNote(NoteInfo note) {
        ContentValues values = new ContentValues();
        values.put("noteId", note.getNoteId());
        values.put("notebookId", note.getNoteBookId());
        values.put("userId", note.getUserId());
        values.put("title", note.getTitle());
        values.put("tags", note.getTags());
        values.put("content", note.getContent());
        values.put("isMarkDown", note.isMarkDown() ? 1 : 0);
        values.put("isBlog", note.isPublicBlog() ? 1 : 0);
        values.put("isTrash", note.isTrash() ? 1 : 0);
        values.put("files", note.getFileIds());
        values.put("createdTime", note.getCreatedTime());
        values.put("updatedTime", note.getUpdatedTime());
        values.put("publicTime", note.getPublicTime());
        values.put("usn", note.getUsn());
        values.put("desc", note.getDesc());
        values.put("note_abstract", note.getNoteAbstract());
        values.put("is_dirty", note.isDirty());
        values.put("isUploading", note.isUploading() ? 1 : 0);
        return values;
    }

    public synchronized NoteInfo getLocalNoteByNoteId(String noteId) {
        mDb = mLeanoteDB.getWritableDatabase();
        String[] args = {String.valueOf(noteId)};
        //Cursor c = mDb.query(NOTES_TABLE, null, null, null, null, null, "");
        Cursor c = mDb.query(LeanoteDB.NOTES_TABLE, null, "noteId=?", args, null, null, "");

        NoteInfo detail = null;
        try {
            if (c.moveToNext()) {
                detail = fillNote(c);
            }
            return detail;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public synchronized List<String> getNotebookTitles(String userId) {
        mDb = mLeanoteDB.getWritableDatabase();
        Cursor c = mDb.query(LeanoteDB.NOTEBOOKS_TABLE, null, "userId=?", new String[]{userId}, null, null, "");
        List<String> notebookTitles = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                notebookTitles.add(c.getString(4));
            }
            return notebookTitles;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public synchronized void deleteNote(long id) {
        mDb = mLeanoteDB.getWritableDatabase();
        String sql = "delete from notes where id='" + id + "'";
        mDb.execSQL(sql);
    }

    public synchronized void deleteNoteByNoteId(String noteId) {
        mDb = mLeanoteDB.getWritableDatabase();
        String sql = "delete from notes where noteId='" + noteId + "'";
        mDb.execSQL(sql);
    }

    public synchronized void deleteNoteInLocal(String noteId) {
        mDb = mLeanoteDB.getWritableDatabase();
        String sql = "update notes set isDeleted = 1 and is_dirty = 1 where noteId='" + noteId + "'";
        mDb.execSQL(sql);
    }

    public synchronized void deleteNotebook(String notebookId) {
        mDb = mLeanoteDB.getWritableDatabase();
        String sql = "delete from notebooks where notebookId='" + notebookId + "'";
        mDb.execSQL(sql);
    }

    public synchronized void deletenotebookInLocal(long id) {
        mDb = mLeanoteDB.getWritableDatabase();
        String sql = "delete from notebooks where id = " + id;
        mDb.execSQL(sql);
    }

    public synchronized List<String> getLocalNotebookIds(String userId) {
        mDb = mLeanoteDB.getWritableDatabase();
        Cursor c = mDb.query(LeanoteDB.NOTEBOOKS_TABLE, null, "userId=?", new String[]{userId}, null, null, "");
        List<String> notebookIds = new ArrayList<>();
        try {
            while (c.moveToNext()) {

                notebookIds.add(c.getString(1));
            }
            return notebookIds;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public synchronized void updateNotebook(NotebookInfo serverNotebook) {
        mDb = mLeanoteDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("notebookId", serverNotebook.getNotebookId());
        values.put("parentNotebookId", serverNotebook.getParentNotebookId());
        values.put("userId", serverNotebook.getUserId());
        values.put("title", serverNotebook.getTitle());
        values.put("urlTitle", serverNotebook.getUrlTitle());
        values.put("isBlog", serverNotebook.isBlog() ? 1 : 0);
        values.put("isTrash", serverNotebook.isTrash() ? 1 : 0);
        values.put("isDeleted", serverNotebook.isDeleted() ? 1 : 0);
        values.put("updatedTime", serverNotebook.getUpdateTime());
        values.put("createdTime", serverNotebook.getCreateTime());
        values.put("usn", serverNotebook.getUsn());
        values.put("is_dirty", serverNotebook.isDirty() ? 1 : 0);
//        if (serverNotebook.getId() != 0l) {
//            mDb.update(NOTEBOOKS_TABLE, values, "id=?", new String[]{String.valueOf(serverNotebook.getId())});
//        } else {
//            mDb.update(NOTEBOOKS_TABLE, values, "notebookId=?", new String[]{serverNotebook.getNotebookId()});
//        }
        mDb.update(LeanoteDB.NOTEBOOKS_TABLE, values, "id=?", new String[]{String.valueOf(serverNotebook.getId())});
    }

    public synchronized NotebookInfo getLocalNotebookByNotebookId(String notebookId) {
        mDb = mLeanoteDB.getWritableDatabase();
        String[] args = {String.valueOf(notebookId)};
        //Cursor c = mDb.query(NOTES_TABLE, null, null, null, null, null, "");
        Cursor c = mDb.query(LeanoteDB.NOTEBOOKS_TABLE, null, "notebookId=?", args, null, null, "");

        NotebookInfo notebook = null;
        try {
            if (c.moveToNext()) {
                notebook = getNotebookFromCursor(c);
            }
            return notebook;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public synchronized void saveNotebooks(List<NotebookInfo> newNotebooks) {
        if (newNotebooks != null && newNotebooks.size() != 0) {
            mDb = mLeanoteDB.getWritableDatabase();
            mDb.beginTransaction();
            try {
                for (int i = 0; i < newNotebooks.size(); i++) {
                    ContentValues values = new ContentValues();
                    NotebookInfo notebook = (NotebookInfo) newNotebooks.get(i);
                    values.put("notebookId", notebook.getNotebookId());
                    values.put("parentNotebookId", notebook.getParentNotebookId());
                    values.put("userId", notebook.getUserId());
                    values.put("title", notebook.getTitle());
                    values.put("urlTitle", notebook.getUrlTitle());
                    values.put("isBlog", notebook.isBlog() ? 1 : 0);
                    values.put("isTrash", notebook.isTrash() ? 1 : 0);
                    values.put("title", notebook.getTitle());
                    values.put("updatedTime", notebook.getUpdateTime());
                    values.put("createdTime", notebook.getCreateTime());
                    values.put("usn", notebook.getUsn());
                    mDb.insert(LeanoteDB.NOTEBOOKS_TABLE, null, values);
                }
                mDb.setTransactionSuccessful();
            } finally {
                mDb.endTransaction();
            }
        }
    }

    public synchronized void updateUsn(String userId, int usn) {
        mDb = mLeanoteDB.getWritableDatabase();
        String sql = "update accounts set usn = " + usn + " where user_id = '" + userId + "'";
        mDb.execSQL(sql);
    }

    public synchronized void dangerouslyDeleteAllContent() {
        mDb = mLeanoteDB.getWritableDatabase();
//        mDb.execSQL("drop table " + NOTES_TABLE);
//        mDb.execSQL("drop table " + NOTEBOOKS_TABLE);
//        mDb.execSQL("drop table " + MEDIA_TABLE);
        mDb.delete(LeanoteDB.NOTES_TABLE, null, null);
        mDb.delete(LeanoteDB.NOTEBOOKS_TABLE, null, null);
        mDb.delete(LeanoteDB.MEDIA_TABLE, null, null);
    }

    public synchronized void publicNote(String noteId, boolean isPublic) {
        mDb = mLeanoteDB.getWritableDatabase();
        int publicNote = isPublic ? 1 : 0;
        String sql = "update notes set isBlog = " + publicNote + " where noteId = '" + noteId + "'";
        mDb.execSQL(sql);
    }

    public synchronized void updateMarkdown(boolean useMarkdown, String userId) {
        mDb = mLeanoteDB.getWritableDatabase();
        int mkd = useMarkdown ? 1 : 0;
        String sql = "update accounts set isMarkDown = " + mkd + " where local_id = 0 and user_id='" + userId + "'";
        mDb.execSQL(sql);
    }

    public synchronized List<String> getNoteisBlogIds() {
        mDb = mLeanoteDB.getWritableDatabase();
        String[] st = {"title"};
//        Cursor c = mDb.query(NOTES_TABLE, null, null , null, null, null, "");
        Cursor c = mDb.query(LeanoteDB.NOTES_TABLE, st, "isBlog=1", null, null, null, "");
        List<String> notebookIds = new ArrayList<>();
        try {
            while (c.moveToNext()) {

                notebookIds.add(c.getString(0));
            }
            return notebookIds;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public synchronized  NoteDetailList getNoteisBlogList(String userId) {
        NoteDetailList listPosts = new NoteDetailList();
        mDb = mLeanoteDB.getWritableDatabase();
        String[] st = {"title"};
        //Cursor c = mDb.query(NOTES_TABLE, null, null, null, null, null, "");
        Cursor c = mDb.query(LeanoteDB.NOTES_TABLE, null, "isBlog=1 and userId=?", new String[]{userId}, null, null, "");
        try {
            while (c.moveToNext()) {
                String title = c.getString(4);
                String updateTime = c.getString(12);
                NoteInfo detail = new NoteInfo();

                detail.setId(c.getLong(0));
                detail.setNoteId(c.getString(1));
                detail.setTitle(title);
                detail.setContent(c.getString(6));
                //detail.setContent(getNoteContentByNoteId(c.getString(1)));
                detail.setUpdatedTime(updateTime);
                listPosts.add(detail);
            }
            return listPosts;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public  synchronized List<NotebookInfo> getNotebookList(String userId) {
        mDb = mLeanoteDB.getWritableDatabase();
        Cursor c = mDb.query(LeanoteDB.NOTEBOOKS_TABLE, null, "userId=?", new String[]{userId}, null, null, "");
        List<NotebookInfo> notebooks = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                NotebookInfo notebook = getNotebookFromCursor(c);
                notebooks.add(notebook);
            }
            return notebooks;
        } finally {
            SqlUtils.closeCursor(c);
        }

    }

    private NotebookInfo getNotebookFromCursor(Cursor c) {
        NotebookInfo notebook = new NotebookInfo();
        notebook.setId(c.getLong(0));
        notebook.setNotebookId(c.getString(1));
        notebook.setParentNotebookId(c.getString(2));
        notebook.setUserId(c.getString(3));
        notebook.setTitle(c.getString(4));
        notebook.setUrlTitle(c.getString(5));
        notebook.setIsBlog(c.getInt(6) != 0);
        notebook.setIsTrash(c.getInt(7) != 0);
        notebook.setIsDeleted(c.getInt(8) != 0);
        notebook.setCreateTime(c.getString(9));
        notebook.setUpdateTime(c.getString(10));
        notebook.setUsn(c.getInt(11));
        notebook.setIsDirty(c.getInt(12) != 0);
        return notebook;
    }

    public synchronized void saveNoteSettings(NoteInfo note) {
        mDb = mLeanoteDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isBlog", note.isPublicBlog() ? 1 : 0);
        values.put("notebookId", note.getNoteBookId());
        values.put("tags", note.getTags());
        if (TextUtils.isEmpty(note.getNoteId())) {
            mDb.update(LeanoteDB.NOTES_TABLE, values, "id=?", new String[]{String.valueOf(note.getId())});
        } else {
            mDb.update(LeanoteDB.NOTES_TABLE, values, "noteId=?", new String[]{note.getNoteId()});
        }
    }

    public synchronized List<NoteInfo> getDirtyNotes() {
        mDb = mLeanoteDB.getWritableDatabase();
        String[] args = {"1"};
        //Cursor c = mDb.query(NOTES_TABLE, null, null, null, null, null, "");
        Cursor c = mDb.query(LeanoteDB.NOTES_TABLE, null, "is_dirty=?", args, null, null, "");
        List<NoteInfo> notes = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                NoteInfo note = fillNote(c);

                notes.add(note);
            }
            return notes;
        } finally {
            SqlUtils.closeCursor(c);
        }

    }

    public synchronized List<NotebookInfo> getDirtyNotebooks() {
        mDb = mLeanoteDB.getWritableDatabase();
        String[] args = {"1"};
        //Cursor c = mDb.query(NOTES_TABLE, null, null, null, null, null, "");
        Cursor c = mDb.query(LeanoteDB.NOTEBOOKS_TABLE, null, "is_dirty=?", args, null, null, "");
        List<NotebookInfo> notebooks = new ArrayList<>();
        try {
            while (c.moveToNext()) {
                NotebookInfo notebook = new NotebookInfo();
                notebook.setNotebookId(c.getString(1));
                notebook.setParentNotebookId(c.getString(2));
                notebook.setTitle(c.getString(4));
                notebook.setUrlTitle(c.getString(5));
                notebook.setIsBlog(c.getInt(6) == 0);
                notebook.setIsTrash(c.getInt(7) == 0);
                notebook.setIsDirty(c.getInt(8) == 0);
                notebook.setUpdateTime(c.getString(10));
                notebook.setUsn(c.getInt(11));
                notebooks.add(notebook);
            }
            return notebooks;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public synchronized long addNotebook(NotebookInfo newNotebook) {
        mDb = mLeanoteDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("notebookId", newNotebook.getNotebookId());
        values.put("is_dirty", 0);
        values.put("userId", newNotebook.getUserId());
        long result = mDb.insert(LeanoteDB.NOTEBOOKS_TABLE, null, values);
        if (result > 0) {
            newNotebook.setId(result);
        }
        return result;
    }

    public synchronized NotebookInfo getLocalNotebookById(long localNotebookId) {
        mDb = mLeanoteDB.getWritableDatabase();
        String[] args = {String.valueOf(localNotebookId)};
        //Cursor c = mDb.query(NOTES_TABLE, null, null, null, null, null, "");
        Cursor c = mDb.query(LeanoteDB.NOTEBOOKS_TABLE, null, "id=?", args, null, null, "");
        NotebookInfo notebook = null;
        try {
            if (c.moveToNext()) {
                notebook = getNotebookFromCursor(c);
            }
            return notebook;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public synchronized MediaFile getMediaFile(String imageUri) {
        mDb = mLeanoteDB.getWritableDatabase();
        Cursor c = mDb.query(LeanoteDB.MEDIA_TABLE, null, "fileURL=?",
                new String[]{imageUri}, null, null, null);
        try {
            if (c.moveToFirst()) {
                MediaFile mf = getMediaFileFromCursor(c);

                return mf;
            } else {
                return null;
            }
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    private MediaFile getMediaFileFromCursor(Cursor c) {
        MediaFile mf = new MediaFile();
        mf.setId(c.getString(0));
        mf.setMediaId(c.getString(1));
        mf.setNoteID(c.getString(2));
        mf.setFilePath(c.getString(3));
        mf.setFileName(c.getString(4));
        mf.setTitle(c.getString(5));
        mf.setFileURL(c.getString(6));
        mf.setThumbnailURL(c.getString(7));
        mf.setUploadState(c.getString(8));
        mf.setDescription(c.getString(9));
        mf.setCaption(c.getString(10));
        mf.setHorizontalAlignment(c.getInt(11));
        mf.setWidth(c.getInt(12));
        mf.setHeight(c.getInt(13));
        mf.setMimeType(c.getString(14));
        return mf;
    }

    public synchronized MediaFile getMediaFileById(String id) {
        mDb = mLeanoteDB.getWritableDatabase();
        Cursor c = mDb.query(LeanoteDB.MEDIA_TABLE, null, "id=?",
                new String[]{String.valueOf(id)}, null, null, null);
        try {
            if (c.moveToFirst()) {
                MediaFile mf = getMediaFileFromCursor(c);

                return mf;
            } else {
                return null;
            }
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public synchronized void updateDirtyUsn(String noteId, int usn) {
        mDb = mLeanoteDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("usn", usn);
        values.put("is_dirty", 0);
        mDb.update(LeanoteDB.NOTES_TABLE, values, "noteId=?", new String[]{noteId});
    }

    public synchronized void updateAccountUsn(int serverUsn, String userId) {
        mDb = mLeanoteDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("usn", serverUsn);
        mDb.update(LeanoteDB.ACCOUNT_TABLE, values, "user_id=?", new String[]{userId});
    }

    public synchronized int getAccountUsn(String userId) {
        mDb = mLeanoteDB.getWritableDatabase();
        Cursor c = mDb.query(LeanoteDB.ACCOUNT_TABLE, null, "user_id=?", new String[]{userId}, null, null, "");
        try {
            if (c.moveToNext()) {
                return c.getInt(8);
            }
        } finally {
            SqlUtils.closeCursor(c);
        }
        return 0;
    }

    public synchronized MediaFile getMediaFileByUrl(String url) {
        mDb = mLeanoteDB.getWritableDatabase();
        Cursor c = mDb.query(LeanoteDB.MEDIA_TABLE, null, "fileURL=?",
                new String[]{url}, null, null, null);
        try {
            if (c.moveToNext()) {
                return getMediaFileFromCursor(c);
            } else {
                return null;
            }
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public synchronized void deleteMediaFileByNoteId(String noteId) {
        mDb = mLeanoteDB.getWritableDatabase();
        String sql = "delete from media where noteId='" + noteId + "'";
        mDb.execSQL(sql);
    }

    public synchronized void updateMedia(String localFileId, String serverFileId) {
        mDb = mLeanoteDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(LeanoteDB.COLUMN_NAME_MEDIA_ID, serverFileId);

        mDb.update(LeanoteDB.MEDIA_TABLE, values, "id=?", new String[]{localFileId});
    }

    public synchronized MediaFile getMediaFileByFileId(String fileId) {
        mDb = mLeanoteDB.getWritableDatabase();
        Cursor c = mDb.query(LeanoteDB.MEDIA_TABLE, null, "id=? or mediaId=?",
                new String[]{fileId, fileId}, null, null, null);
        try {
            if (c.moveToNext()) {
                return getMediaFileFromCursor(c);
            } else {
                return null;
            }
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public synchronized NoteDetailList getNotesListInNotebook(Long localNotebookId, String userId) {
        mDb = mLeanoteDB.getWritableDatabase();
        NoteDetailList notelist = new NoteDetailList();

        String notebookId = getNotebookIdByLocalId(localNotebookId);
        String[] args = {String.valueOf(notebookId), userId};
        if (localNotebookId == null) {
            return notelist;
        }
        //Cursor c = mDb.query(NOTES_TABLE, null, null, null, null, null, "");
        Cursor c = mDb.query(LeanoteDB.NOTES_TABLE, null, "notebookId=? and userId=?", args, null, null, "");
        try {
            while (c.moveToNext()) {
                NoteInfo detail = fillNote(c);
                notelist.add(detail);
            }
            return notelist;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public synchronized String getNotebookIdByLocalId(Long localNotebookId) {
        mDb = mLeanoteDB.getWritableDatabase();
        String[] args = {String.valueOf(localNotebookId)};
        Cursor c = mDb.query(LeanoteDB.NOTEBOOKS_TABLE, null, "id=?", args, null, null, "");
        try {
            if (c.moveToNext()) {
                return c.getString(1);
            }
        } finally {
            SqlUtils.closeCursor(c);
        }
        return null;
    }

    public synchronized Account getAccountByLocalId(long localId) {
        mDb = mLeanoteDB.getWritableDatabase();
        Account account = new Account();
        String[] args = {Long.toString(localId)};
        Cursor c = mDb.rawQuery("SELECT * FROM " + LeanoteDB.ACCOUNT_TABLE + " WHERE local_id=?", args);

        try {
            if (c.moveToFirst()) {
                account.setUserName(c.getString(c.getColumnIndex("user_name")));
                account.setUserId(c.getString(c.getColumnIndex("user_id")));
                account.setEmail(c.getString(c.getColumnIndex("email")));
                account.setAvatar(c.getString(c.getColumnIndex("logo")));
                account.setVerified(c.getInt(c.getColumnIndex("verified")) == 0);
                account.setAccessToken(c.getString(c.getColumnIndex("access_token")));
                account.setUseMarkdown(c.getInt(c.getColumnIndex("isMarkDown")) == 0);
                account.setLastSyncUsn(c.getInt(c.getColumnIndex("usn")));
                account.setHost(c.getString(c.getColumnIndex("host")));
            }
            return account;
        } finally {
            SqlUtils.closeCursor(c);
        }
    }

    public synchronized void save(Account account) {
        mDb = mLeanoteDB.getWritableDatabase();
        ContentValues values = new ContentValues();
        // we only support one wpcom user at the moment: local_id is always 0
        values.put("local_id", 0);
        values.put("user_name", account.getUserName());
        values.put("user_id", account.getUserId());
        values.put("email", account.getEmail());
        values.put("verified", account.isVerified() ? 0 : 1);
        values.put("logo", account.getAvatar());
        values.put("access_token", account.getAccessToken());
        values.put("host", account.getHost());

        mDb.insertWithOnConflict(LeanoteDB.ACCOUNT_TABLE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }


}
