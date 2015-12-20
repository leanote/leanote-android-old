package com.leanote.android.ui.note;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.networking.NetworkUtils;
import com.leanote.android.service.NoteSyncService;
import com.leanote.android.ui.ActivityLauncher;
import com.leanote.android.ui.note.service.NoteEvents;
import com.leanote.android.ui.note.service.NoteUploadService;
import com.leanote.android.util.AniUtils;
import com.leanote.android.util.AppLog;

import de.greenrobot.event.EventBus;

public class NotePreviewActivity extends AppCompatActivity {

    public static final String ARG_LOCAL_NOTE_ID = "local_note_id";

    private long mLocalNoteId;
    private boolean mIsUpdatingNote;

    private NoteDetail mNote;

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
            mLocalNoteId = savedInstanceState.getLong(ARG_LOCAL_NOTE_ID);
        } else {
            mLocalNoteId = getIntent().getLongExtra(ARG_LOCAL_NOTE_ID, 0);
        }

        setTitle(getString(R.string.preview_note));
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        mNote = Leanote.leaDB.getLocalNoteById(mLocalNoteId);
        if (hasPreviewFragment()) {
            refreshPreview();
        } else {
            showPreviewFragment();
        }
        showMessageViewIfNecessary();
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

    private void showPreviewFragment() {
        FragmentManager fm = getFragmentManager();
        fm.executePendingTransactions();

        String tagForFragment = getString(R.string.fragment_tag_note_preview);
        Fragment fragment = NotePreviewFragment.newInstance(mLocalNoteId);

        fm.beginTransaction()
                .replace(R.id.fragment_container, fragment, tagForFragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commitAllowingStateLoss();
    }

    private boolean hasPreviewFragment() {
        return (getPreviewFragment() != null);
    }

    private NotePreviewFragment getPreviewFragment() {
        String tagForFragment = getString(R.string.fragment_tag_note_preview);
        Fragment fragment = getFragmentManager().findFragmentByTag(tagForFragment);
        if (fragment != null) {
            return (NotePreviewFragment) fragment;
        } else {
            return null;
        }
    }

    private void refreshPreview() {
        if (!isFinishing()) {
            NotePreviewFragment fragment = getPreviewFragment();
            if (fragment != null) {
                fragment.refreshPreview();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(ARG_LOCAL_NOTE_ID, mLocalNoteId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.note_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (item.getItemId() == R.id.menu_edit) {
            ActivityLauncher.editNoteForResult(this, mLocalNoteId);
            return true;
        } else if (item.getItemId() == R.id.menu_share) {
            ActivityLauncher.shareNoteForResult(this, mLocalNoteId);
            return  true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    /*
     * if this is a local draft or has local changes, show the message explaining what these
     * states mean, and hook up the publish and revert buttons
     */
    private void showMessageViewIfNecessary() {
        final ViewGroup messageView = (ViewGroup) findViewById(R.id.message_container);

        if (mNote == null
                || mIsUpdatingNote
                || NoteUploadService.isNoteUploading(mNote.getId())
                || (!mNote.isDirty() && mNote.getUsn() > 0)) {
            messageView.setVisibility(View.GONE);
            return;
        }

        TextView messageText = (TextView) messageView.findViewById(R.id.message_text);
        if (mNote.isDirty()) {
            messageText.setText(R.string.local_changes_explainer);
        } else if (mNote.getUsn() == 0) {
            messageText.setText(R.string.local_draft_explainer);
        }

        // publish applies to both local draft and local changes
        View btnPublish = messageView.findViewById(R.id.btn_publish);
        btnPublish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AniUtils.animateBottomBar(messageView, false);
                publishPost();
            }
        });

        // revert applies to only local changes
        View btnRevert = messageView.findViewById(R.id.btn_revert);
        btnRevert.setVisibility(mNote.getUsn() == 0 ? View.GONE : View.VISIBLE);
        if (mNote.isDirty()) {
            btnRevert.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AniUtils.animateBottomBar(messageView, false);
                    revertNote();
                }
            });
        }

        // first set message bar to invisible so it takes up space, then animate it in
        // after a brief delay to give time for preview to render first
        messageView.setVisibility(View.INVISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!isFinishing() && messageView.getVisibility() != View.VISIBLE) {
                    AniUtils.animateBottomBar(messageView, true);
                }
            }
        }, 1000);
    }

    /*
     * reverts local changes for this post, replacing it with the latest version from the server
     */
    private void revertNote() {
        if (isFinishing() || !NetworkUtils.checkConnection(this)) {
            return;
        }

        if (mIsUpdatingNote) {
            AppLog.d(AppLog.T.POSTS, "post preview > already updating post");
        } else {
            new RevertNoteTask().execute(mNote.getNoteId());
        }
    }

    private void publishPost() {
        if (!isFinishing() && NetworkUtils.checkConnection(this)) {

            NoteUploadService.addNoteToUpload(mNote);
            startService(new Intent(this, NoteUploadService.class));
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NoteEvents.PostUploadStarted event) {
            showProgress();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(NoteEvents.PostUploadEnded event) {
        hideProgress();
        refreshPreview();
    }

    private void showProgress() {
        if (!isFinishing()) {
            findViewById(R.id.progress).setVisibility(View.VISIBLE);
        }
    }

    private void hideProgress() {
        if (!isFinishing()) {
            findViewById(R.id.progress).setVisibility(View.GONE);
        }
    }

    private class RevertNoteTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsUpdatingNote = true;
            showProgress();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mIsUpdatingNote = false;
            hideProgress();
        }

        @Override
        protected Boolean doInBackground(String... noteIds) {
            //fetch note from server
            String noteId = noteIds[0];
            NoteDetail serverNote = NoteSyncService.getServerNote(noteId);
            Leanote.leaDB.updateNoteByNoteId(serverNote);
            //NoteSyncService.syncPullNote();
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!isFinishing()) {
                hideProgress();
                if (result) {
                    refreshPreview();
                }
            }
            mIsUpdatingNote = false;
        }
    }
}
