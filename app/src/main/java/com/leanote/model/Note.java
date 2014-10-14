package com.leanote.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;

import com.leanote.controller.DataController;

/**
 * Created by jerrychoi on 14-9-27.
 */
public class Note extends BaseNoteModel {

    private static final String TAG = Note.class.getSimpleName();

    private String createdUserId;
    private String notebookId;
    private String desc;
    private String content;
    private String thumbnailsSrc;
    private String tags;

    private boolean isMarkdown;
    private boolean isTrash;
    private boolean isTop;

    // for blog only
    private boolean isBlog;
    private String summary;

    // for share
    private String updatedUserId;

    public Note() {
    }

    public Note(Parcel source) {
        readBaseNote(source);

        createdUserId = source.readString();
        notebookId = source.readString();
        desc = source.readString();
        content = source.readString();
        thumbnailsSrc = source.readString();
        tags = source.readString();

        isMarkdown = source.readInt() == 0 ? false : true;
        isTrash = source.readInt() == 0 ? false : true;
        isTop = source.readInt() == 0 ? false : true;

        isBlog = source.readInt() == 0 ? false : true;
        summary = source.readString();

        updatedUserId = source.readString();
    }

    public static Note from(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        Note note = new Note();
        // from base
        note.id = DataController.parseString(cursor, IBaseNoteColumns.NID);
        note.userId = DataController.parseString(cursor, IBaseNoteColumns.USER_ID);
        note.title = DataController.parseString(cursor, IBaseNoteColumns.TITLE);
        note.createdTime = DataController.parseLong(cursor, IBaseNoteColumns.CREATED_TIME);
        note.updatedTime = DataController.parseLong(cursor, IBaseNoteColumns.UPDATED_TIME);

        // from self
        note.createdUserId = DataController.parseString(cursor, NoteColumns.CREATED_USER_ID);
        note.notebookId = DataController.parseString(cursor, NoteColumns.NOTEBOOK_ID);
        note.desc = DataController.parseString(cursor, NoteColumns.DESC);
        note.content = DataController.parseString(cursor, NoteColumns.CONTENT);
        note.thumbnailsSrc = DataController.parseString(cursor, NoteColumns.THUMBNAILS_SRC);
        note.tags = DataController.parseString(cursor, NoteColumns.TAGS);

        note.isMarkdown = DataController.parseBoolean(cursor, NoteColumns.IS_MARKDOWN);
        note.isTrash = DataController.parseBoolean(cursor, NoteColumns.IS_TRASH);
        note.isTop = DataController.parseBoolean(cursor, NoteColumns.IS_TOP);

        note.isBlog = DataController.parseBoolean(cursor, NoteColumns.IS_BLOG);
        note.summary = DataController.parseString(cursor, NoteColumns.SUMMARY);

        note.updatedUserId = DataController.parseString(cursor, NoteColumns.UPDATED_USER_ID);

        return note;
    }

    @Override
    public ContentValues getContentValues() {
        // from parent
        ContentValues cv = super.getContentValues();

        // from self
        cv.put(NoteColumns.CREATED_USER_ID, createdUserId);
        cv.put(NoteColumns.NOTEBOOK_ID, notebookId);
        cv.put(NoteColumns.DESC, desc);
        cv.put(NoteColumns.CONTENT, content);
        cv.put(NoteColumns.THUMBNAILS_SRC, thumbnailsSrc);
        cv.put(NoteColumns.TAGS, tags);

        cv.put(NoteColumns.IS_MARKDOWN, isMarkdown);
        cv.put(NoteColumns.IS_TRASH, isTrash);
        cv.put(NoteColumns.IS_TOP, isTop);

        cv.put(NoteColumns.IS_BLOG, isBlog);
        cv.put(NoteColumns.SUMMARY, summary);

        cv.put(NoteColumns.UPDATED_USER_ID, updatedUserId);

        return cv;
    }

    @Override
    public Uri getContentUri() {
        return NoteColumns.CONTENT_URI;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        writeBaseNote(dest, flags);

        dest.writeString(createdUserId);
        dest.writeString(notebookId);
        dest.writeString(desc);
        dest.writeString(content);
        dest.writeString(thumbnailsSrc);
        dest.writeString(tags);

        dest.writeInt(isMarkdown ? 1 : 0);
        dest.writeInt(isTrash ? 1 : 0);
        dest.writeInt(isTop ? 1 : 0);

        dest.writeInt(isBlog ? 1 : 0);
        dest.writeString(summary);

        dest.writeString(updatedUserId);
    }

    public static final Creator<Note> CREATOR = new Creator<Note>() {
        @Override
        public Note createFromParcel(Parcel source) {
            return new Note(source);
        }

        @Override
        public Note[] newArray(int size) {
            return new Note[size];
        }
    };

    public String getCreatedUserId() {
        return createdUserId;
    }

    public void setCreatedUserId(String createdUserId) {
        this.createdUserId = createdUserId;
    }

    public String getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(String notebookId) {
        this.notebookId = notebookId;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getThumbnailsSrc() {
        return thumbnailsSrc;
    }

    public void setThumbnailsSrc(String thumbnailsSrc) {
        this.thumbnailsSrc = thumbnailsSrc;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public boolean isMarkdown() {
        return isMarkdown;
    }

    public void setMarkdown(boolean isMarkdown) {
        this.isMarkdown = isMarkdown;
    }

    public boolean isTrash() {
        return isTrash;
    }

    public void setTrash(boolean isTrash) {
        this.isTrash = isTrash;
    }

    public boolean isTop() {
        return isTop;
    }

    public void setTop(boolean isTop) {
        this.isTop = isTop;
    }

    public boolean isBlog() {
        return isBlog;
    }

    public void setBlog(boolean isBlog) {
        this.isBlog = isBlog;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getUpdatedUserId() {
        return updatedUserId;
    }

    public void setUpdatedUserId(String updatedUserId) {
        this.updatedUserId = updatedUserId;
    }

    @Override
    public String toString() {
        return "Note{" +
                "createdUserId='" + createdUserId + '\'' +
                ", notebookId='" + notebookId + '\'' +
                ", desc='" + desc + '\'' +
                ", content='" + content + '\'' +
                ", thumbnailsSrc='" + thumbnailsSrc + '\'' +
                ", tags='" + tags + '\'' +
                ", isMarkdown=" + isMarkdown +
                ", isTrash=" + isTrash +
                ", isTop=" + isTop +
                ", isBlog=" + isBlog +
                ", summary='" + summary + '\'' +
                ", updatedUserId='" + updatedUserId + '\'' +
                "} " + super.toString();
    }

}
