package com.leanote.android.ui.note;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;

import com.leanote.android.R;

/**
 * Created by binnchx on 11/7/15.
 */
public class SearchToolbar extends LinearLayout {

    private SearchView mSearchView;

    public SearchToolbar(Context context) {
        super(context);
        initView(context);
    }

    public SearchView getmSearchView() {
        return mSearchView;
    }

    private void initView(Context context) {
        View view = inflate(context, R.layout.search_note, this);

        mSearchView = (SearchView) view.findViewById(R.id.search_note);
        mSearchView.setIconifiedByDefault(true);
        mSearchView.onActionViewExpanded();
        mSearchView.setFocusable(false);
        mSearchView.clearFocus();
        mSearchView.setSubmitButtonEnabled(true);

        mSearchView.setQueryHint("search note");
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
    }
}
