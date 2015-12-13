package com.leanote.android.ui.search;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leanote.android.R;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.ui.ActivityLauncher;

/**
 * Created by binnchx on 12/10/15.
 */
public class SearchViewHolder extends RecyclerView.ViewHolder {

    private final TextView noteTitle;
    private final LinearLayout layout;
    private Context context;

    public SearchViewHolder(Context context, View itemView) {
        super(itemView);
        this.context = context;
        noteTitle = (TextView) itemView.findViewById(R.id.text_title);
        layout = (LinearLayout) itemView.findViewById(R.id.post_layout);
    }

    public void bind(final NoteDetail note) {
        noteTitle.setText(note.getTitle());

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) context;
                ActivityLauncher.previewNoteForResult(activity, note.getId());
            }
        };

        layout.setOnClickListener(listener);

    }
}
