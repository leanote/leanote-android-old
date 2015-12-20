package com.leanote.android.ui.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.model.NotebookInfo;
import com.leanote.android.ui.main.Notebook_ViewNote;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binnchx on 12/10/15.
 */
public class SearchFragment extends Fragment implements SearchView.OnQueryTextListener {

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }

    private RecyclerView mRecyclerView;
    private SearchAdapter mAdapter;
    private List<NoteDetail> mNotes;
    private List<NoteDetail> allNotes;

    private List<NotebookInfo> mNotebooks;
    private List<NotebookInfo> allNotebooks;

    private long searchType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_search, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);

        searchType = ((SearchActivity)getActivity()).getSeachType();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (searchType == 0) {
            mNotes = new ArrayList<>();
            allNotes = Leanote.leaDB.getNotesList(AccountHelper.getDefaultAccount().getmUserId());
        }
        else if (searchType == 2) {
            mNotebooks = new ArrayList<>();
            allNotebooks = Leanote.leaDB.getNotebookList();
        }


        mAdapter = new SearchAdapter(getActivity(), mNotes);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onQueryTextChange(String query) {

        final List<NoteDetail> filteredModelList = filter(allNotes, query);
        mAdapter.animateTo(filteredModelList);
        mRecyclerView.scrollToPosition(0);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    private List<NoteDetail> filter(List<NoteDetail> models, String query) {
        query = query.toLowerCase();

        final List<NoteDetail> filteredModelList = new ArrayList<>();
        if (TextUtils.isEmpty(query)) {
            return filteredModelList;
        }

        for (NoteDetail model : models) {
            if (model.getContent() == null || model.getTitle()==null) continue;  /////////////
            final String content = model.getContent().toLowerCase();
            final String title = model.getTitle().toLowerCase();

            if (title.contains(query) || (!TextUtils.isEmpty(content) && content.contains(query))) {
                filteredModelList.add(model);
            }

        }
        return filteredModelList;
    }


}
