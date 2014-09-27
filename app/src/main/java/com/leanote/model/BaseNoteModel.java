package com.leanote.model;

import android.content.ContentValues;
import android.os.Parcel;

/**
 * Base model of notebook and note
 *
 * Created by jerrychoi on 2014-9-26.
 */
public abstract class BaseNoteModel implements IModel {

    protected String id;    // note or notebook id
    protected String userId;    // related user id
    protected String title; // title
    protected long createdTime; // created time
    protected long updatedTime; // updated time

    protected BaseNoteModel() {
    }

    protected void readBaseNote(Parcel source) {
        id = source.readString();
        userId = source.readString();
        title = source.readString();

        createdTime = source.readLong();
        updatedTime = source.readLong();
    }

    protected void writeBaseNote(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(userId);
        dest.writeString(title);

        dest.writeLong(createdTime);
        dest.writeLong(updatedTime);
    }

    @Override
    public ContentValues getContentValues() {
        ContentValues cv = new ContentValues();

        cv.put(IBaseNoteColumns.NID, id);
        cv.put(IBaseNoteColumns.USER_ID, userId);
        cv.put(IBaseNoteColumns.TITLE, title);
        cv.put(IBaseNoteColumns.CREATED_TIME, createdTime);
        cv.put(IBaseNoteColumns.UPDATED_TIME, updatedTime);

        return cv;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(long createdTime) {
        this.createdTime = createdTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
    }

    @Override
    public String toString() {
        return "BaseNoteModel{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", createdTime=" + createdTime +
                ", updatedTime=" + updatedTime +
                '}';
    }
}
