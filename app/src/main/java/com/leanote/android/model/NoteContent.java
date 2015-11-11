package com.leanote.android.model;

import java.io.Serializable;

/**
 * Created by binnchx on 11/3/15.
 */
public class NoteContent implements Serializable {
    private String noteId;
    private String content;
    private String userId;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
