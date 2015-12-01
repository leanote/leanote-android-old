package com.leanote.android.ui.note;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import com.leanote.android.R;

/**
 * Created by binnchx on 11/7/15.
 */
public class SearchToolbar extends LinearLayout {

    private TextView mTextview;
    private SearchView mSearchView;

    public SearchToolbar(Context context) {
        super(context);
        initView(context);
    }


    private void initView(Context context) {
        View view = inflate(context, R.layout.search_note, this);

        //mSearchView = (SearchView) view.findViewById(R.id.search_note);
//        mSearchView = (SearchView) view.findViewById(R.id.search_note_title);
//        mSearchView.setIconifiedByDefault(true);
//        mSearchView.onActionViewExpanded();
//        mSearchView.setFocusable(false);
//        mSearchView.clearFocus();
//        mSearchView.setSubmitButtonEnabled(true);
//
//        mSearchView.setQueryHint(hintext);
		//mSearchView.setIconifiedByDefault(true);


//        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextChange(String queryText) {
//
//                return true;
//            }
//
//            @Override
//            public boolean onQueryTextSubmit(String queryText) {
//                return true;
//            }
//        });

        mTextview = (TextView) view.findViewById(R.id.search_note_title);
        mTextview.setText("test");


    }
}
