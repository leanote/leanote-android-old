package com.leanote.android.ui.note;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.leanote.android.R;

/**
 * Created by binnchx on 11/5/15.
 */
public class SearchNoteViewHolder extends RecyclerView.ViewHolder {
    private final TextView noteTitle;

    public SearchNoteViewHolder(View itemView) {
        super(itemView);

        noteTitle = (TextView) itemView.findViewById(R.id.search_note_title);
    }

    public void bind(String title) {
        noteTitle.setText(title);
    }

}
