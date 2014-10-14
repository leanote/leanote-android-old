package com.leanote.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;

import com.leanote.controller.DataController;

/**
 * A simple note showed in notes list. It contains id, title, time, description and thumbnails only.
 * <p/>
 * Created by jerrychoi on 2014-9-26.
 */
public class SimpleNote extends BaseNoteModel {

    private static final String TAG = SimpleNote.class.getSimpleName();

    private String desc;
    private String thumbnailsSrc;

    public SimpleNote() {
    }

    public SimpleNote(Parcel source) {
        readBaseNote(source);

        desc = source.readString();
        thumbnailsSrc = source.readString();
    }

    public static SimpleNote from(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        SimpleNote simpleNote = new SimpleNote();
        // from base
        simpleNote.id = DataController.parseString(cursor, IBaseNoteColumns.NID);
        simpleNote.userId = DataController.parseString(cursor, IBaseNoteColumns.USER_ID);
        simpleNote.title = DataController.parseString(cursor, IBaseNoteColumns.TITLE);
        simpleNote.createdTime = DataController.parseLong(cursor, IBaseNoteColumns.CREATED_TIME);
        simpleNote.updatedTime = DataController.parseLong(cursor, IBaseNoteColumns.UPDATED_TIME);

        // from self
        simpleNote.desc = DataController.parseString(cursor, NoteColumns.DESC);
        simpleNote.thumbnailsSrc = DataController.parseString(cursor, NoteColumns.THUMBNAILS_SRC);

        return simpleNote;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getThumbnailsSrc() {
        return thumbnailsSrc;
    }

    public void setThumbnailsSrc(String thumbnailsSrc) {
        this.thumbnailsSrc = thumbnailsSrc;
    }

    @Override
    public ContentValues getContentValues() {
        // from parent
        ContentValues cv = super.getContentValues();

        // from self
        cv.put(NoteColumns.DESC, desc);
        cv.put(NoteColumns.THUMBNAILS_SRC, thumbnailsSrc);

        return cv;
    }

    @Override
    public Uri getContentUri() {
        return NoteColumns.SIMPLE_NOTE_CONTENT_URI;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeBaseNote(dest, flags);

        dest.writeString(desc);
        dest.writeString(thumbnailsSrc);
    }

    public static final Creator<SimpleNote> CREATOR = new Creator<SimpleNote>() {
        @Override
        public SimpleNote createFromParcel(Parcel source) {
            return new SimpleNote(source);
        }

        @Override
        public SimpleNote[] newArray(int size) {
            return new SimpleNote[size];
        }
    };

    @Override
    public String toString() {
        return "SimpleNote{" +
                "desc='" + desc + '\'' +
                ", thumbnailsSrc='" + thumbnailsSrc + '\'' +
                "} " + super.toString();
    }

}
