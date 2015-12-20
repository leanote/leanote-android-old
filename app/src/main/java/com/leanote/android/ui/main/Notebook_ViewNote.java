package com.leanote.android.ui.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.model.NoteDetailList;
import com.leanote.android.model.NotebookInfo;
import com.leanote.android.networking.NetworkUtils;
import com.leanote.android.service.NoteSyncService;
import com.leanote.android.ui.ActivityLauncher;
import com.leanote.android.ui.note.NoteListAdapter;
import com.leanote.android.ui.note.service.NoteEvents;
import com.leanote.android.ui.note.service.NoteUploadService;
import com.leanote.android.util.AniUtils;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.StringUtils;
import com.leanote.android.util.SwipeToRefreshHelper;
import com.leanote.android.widget.RecyclerItemDecoration;

import de.greenrobot.event.EventBus;

public class Notebook_ViewNote extends AppCompatActivity {

    public static final String EXTRA_NOTEBOOK = "notebook";

    private SwipeToRefreshHelper mSwipeToRefreshHelper;
    private NoteListAdapter mNoteListAdapter;
    private View mFabView;

    private RecyclerView mRecyclerView;

    private View mEmptyView;
    private ProgressBar mProgressLoadMore;
    private TextView mEmptyViewTitle;
    private ImageView mEmptyViewImage;

    private boolean mIsFetchingPosts;

    private String notebookId;

    private final NoteDetailList mTrashedNotes = new NoteDetailList();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_preview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(true);
        }

        if (savedInstanceState != null) {
            notebookId = savedInstanceState.getString(EXTRA_NOTEBOOK);
        } else {
            notebookId = getIntent().getStringExtra(EXTRA_NOTEBOOK);
        }

//        if (savedInstanceState != null) {
//            mLocalNoteId = savedInstanceState.getLong(ARG_LOCAL_NOTE_ID);
//        } else {
//            mLocalNoteId = getIntent().getLongExtra(ARG_LOCAL_NOTE_ID, 0);
//        }

        setTitle(Leanote.leaDB.getLocalNotebookByNotebookId(notebookId).getTitle());
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        if (hasPreviewFragment()) {
            refreshPreview();
        } else {
            showViewNoteFragment();
        }
    }
    private void refreshPreview() {
        if (!isFinishing()) {
            ViewNoteFragment fragment = getPreviewFragment();
            if (fragment != null) {
//                fragment.refreshPreview();
            }
        }
    }
    public void onEventMainThread(NoteEvents.PostUploadStarted event) {
    }

    public void onEventMainThread(NoteEvents.PostUploadEnded event) {
    }


    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
        ActivityLauncher.slideOutToRight(this);
    }

    private void showViewNoteFragment() {
        FragmentManager fm = getFragmentManager();
        fm.executePendingTransactions();

        String tagForFragment = getString(R.string.fragment_tag_note_preview);
        Fragment fragment = ViewNoteFragment.newInstance();

        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment, tagForFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commitAllowingStateLoss();
    }

    private boolean hasPreviewFragment() {
        return (getPreviewFragment() != null);
    }

    private ViewNoteFragment getPreviewFragment() {
        String tagForFragment = getString(R.string.fragment_tag_note_preview);
        Fragment fragment = getFragmentManager().findFragmentByTag(tagForFragment);
        if (fragment != null) {
            return (ViewNoteFragment) fragment;
        } else {
            return null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //outState.putLong(ARG_LOCAL_NOTE_ID, mLocalNoteId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    String getNotebookId() { return notebookId; }


    public static Notebook_ViewNote newInstance() {
        return new Notebook_ViewNote();
    }

}
