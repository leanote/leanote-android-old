package com.leanote.android.model;

import android.os.AsyncTask;
import android.util.Log;

import com.leanote.android.Leanote;

import java.util.ArrayList;

/**
 * Created by binnchx on 10/18/15.
 */
public class NoteDetailList extends ArrayList<NoteDetail> {

    public boolean isSameList(NoteDetailList noteList) {
        if (noteList == null || this.size() != noteList.size()) {
            return false;
        }

        for (int i = 0; i < noteList.size(); i++) {
            NoteDetail newNote = noteList.get(i);
            NoteDetail currentNote = this.get(i);

            if (newNote.getNoteId() != currentNote.getNoteId())
                return false;
            if (!newNote.getTitle().equals(currentNote.getTitle()))
                return false;
        }

        return true;
    }

    public int indexOfPost(NoteDetail note) {
        if (note == null) {
            return -1;
        }
        for (int i = 0; i < size(); i++) {
            if (this.get(i).getId() == note.getId()) {
                return i;
            }
        }
        return -1;
    }



}
