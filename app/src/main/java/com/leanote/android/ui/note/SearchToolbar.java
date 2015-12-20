package com.leanote.android.ui.note;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.leanote.android.R;
import com.leanote.android.ui.ActivityLauncher;
import com.leanote.android.util.AppLog;

/**
 * Created by binnchx on 11/7/15.
 */
public class SearchToolbar extends LinearLayout {

    private TextView mTextview;
    private ImageButton searchButton;
    private String title;

    public SearchToolbar(Context context, String title) {
        super(context);
        this.title = title;
        initView(context);
    }


    private void initView(final Context context) {
//        View view = inflate(context, R.layout.search_note, this);
//
//        mTextview = (TextView) view.findViewById(R.id.search_note_title);
//        mTextview.setText(title);
//
//        searchButton = (ImageButton) view.findViewById(R.id.search_note_button);
//        searchButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //启动SearchActivity
//                AppLog.i("start to search" + context.getClass());
//                Activity activity = (Activity) context;
//                ActivityLauncher.startSearchForResult(activity);
//            }
//        });
    }
}
