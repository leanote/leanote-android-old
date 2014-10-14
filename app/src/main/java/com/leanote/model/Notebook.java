package com.leanote.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;

import com.leanote.controller.DataController;

/**
 * Created by jerrychoi on 14-9-27.
 */
public class Notebook extends BaseNoteModel {

    private static final String TAG = Notebook.class.getSimpleName();

    private String parentNotebookId;
    private int seq;
    private boolean isTrash;
    private boolean isBlog;

    public Notebook() {
    }

    public Notebook(Parcel source) {
        readBaseNote(source);

        parentNotebookId = source.readString();
        seq = source.readInt();
        isTrash = source.readInt() == 0 ? false : true;
        isBlog = source.readInt() == 0 ? false : true;
    }

    public static Notebook from(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        Notebook notebook = new Notebook();
        // from parent
        notebook.id = DataController.parseString(cursor, IBaseNoteColumns.NID);
        notebook.userId = DataController.parseString(cursor, IBaseNoteColumns.USER_ID);
        notebook.title = DataController.parseString(cursor, IBaseNoteColumns.TITLE);
        notebook.createdTime = DataController.parseLong(cursor, IBaseNoteColumns.CREATED_TIME);
        notebook.updatedTime = DataController.parseLong(cursor, IBaseNoteColumns.UPDATED_TIME);

        // from self
        notebook.parentNotebookId = DataController.parseString(cursor, NotebookColumns.PARENT_NOTEBOOK_ID);
        notebook.seq = DataController.parseInt(cursor, NotebookColumns.SEQ);
        notebook.isTrash = DataController.parseBoolean(cursor, NotebookColumns.IS_TRASH);
        notebook.isBlog = DataController.parseBoolean(cursor, NotebookColumns.IS_BLOG);

        return notebook;
    }

    @Override
    public ContentValues getContentValues() {
        // from parent
        ContentValues cv = super.getContentValues();

        // from self
        cv.put(NotebookColumns.PARENT_NOTEBOOK_ID, parentNotebookId);
        cv.put(NotebookColumns.SEQ, seq);
        cv.put(NotebookColumns.IS_TRASH, isTrash);
        cv.put(NotebookColumns.IS_BLOG, isBlog);

        return cv;
    }

    @Override
    public Uri getContentUri() {
        return NotebookColumns.CONTENT_URI;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeBaseNote(dest, flags);

        dest.writeString(parentNotebookId);
        dest.writeInt(seq);
        dest.writeInt(isTrash ? 1 : 0);
        dest.writeInt(isBlog ? 1 : 0);
    }

    public static final Creator<Notebook> CREATOR = new Creator<Notebook>() {
        @Override
        public Notebook createFromParcel(Parcel source) {
            return new Notebook(source);
        }

        @Override
        public Notebook[] newArray(int size) {
            return new Notebook[size];
        }
    };

    public String getParentNotebookId() {
        return parentNotebookId;
    }

    public void setParentNotebookId(String parentNotebookId) {
        this.parentNotebookId = parentNotebookId;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public boolean isTrash() {
        return isTrash;
    }

    public void setTrash(boolean isTrash) {
        this.isTrash = isTrash;
    }

    public boolean isBlog() {
        return isBlog;
    }

    public void setBlog(boolean isBlog) {
        this.isBlog = isBlog;
    }

    @Override
    public String toString() {
        return "Notebook{" +
                "parentNotebookId='" + parentNotebookId + '\'' +
                ", seq=" + seq +
                ", isTrash=" + isTrash +
                ", isBlog=" + isBlog +
                "} " + super.toString();
    }
}
