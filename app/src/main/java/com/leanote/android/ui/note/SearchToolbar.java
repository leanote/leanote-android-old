package com.leanote.android.ui.note;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leanote.android.R;

/**
 * Created by binnchx on 11/7/15.
 */
public class SearchToolbar extends LinearLayout {

    private TextView mTextview;

    public SearchToolbar(Context context) {
        super(context);
        initView(context);
    }


    private void initView(Context context) {
        View view = inflate(context, R.layout.search_note, this);

        mTextview = (TextView) view.findViewById(R.id.search_note_title);
        mTextview.setText("test");

    }
}
