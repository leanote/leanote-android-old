package com.leanote.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.util.Log;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.model.NotebookInfo;
import com.leanote.android.networking.SSLCertsViewActivity;
import com.leanote.android.networking.SelfSignedSSLCertsManager;
import com.leanote.android.ui.accounts.NewAccountActivity;
import com.leanote.android.ui.main.Notebook_ViewNote;
import com.leanote.android.ui.note.EditNoteActivity;
import com.leanote.android.ui.note.EditNotebookActivity;
import com.leanote.android.ui.note.NoteListActivity;
import com.leanote.android.ui.note.NotePreviewActivity;
import com.leanote.android.ui.post.BlogHomeActivity;
import com.leanote.android.ui.search.SearchActivity;
import com.leanote.android.util.AppLog;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Created by binnchx on 8/27/15.
 */
public class ActivityLauncher {

    private static final String ARG_DID_SLIDE_IN_FROM_RIGHT = "did_slide_in_from_right";

    public static void showSignInForResult(Activity activity) {
        Intent intent = new Intent(activity, SignInActivity.class);
        activity.startActivityForResult(intent, RequestCodes.ADD_ACCOUNT);
    }

    public static void newAccountForResult(Activity activity) {
        Intent intent = new Intent(activity, NewAccountActivity.class);
        activity.startActivityForResult(intent, SignInActivity.CREATE_ACCOUNT_REQUEST);
    }



    public static void viewSSLCerts(Context context) {
        try {
            Intent intent = new Intent(context, SSLCertsViewActivity.class);
            SelfSignedSSLCertsManager selfSignedSSLCertsManager = SelfSignedSSLCertsManager.getInstance(context);
            String lastFailureChainDescription =
                    selfSignedSSLCertsManager.getLastFailureChainDescription().replaceAll("\n", "<br/>");
            intent.putExtra(SSLCertsViewActivity.CERT_DETAILS_KEYS, lastFailureChainDescription);
            context.startActivity(intent);
        } catch (GeneralSecurityException e) {
            AppLog.e(AppLog.T.API, e);
        } catch (IOException e) {
            AppLog.e(AppLog.T.API, e);
        }
    }

    public static void addNewNoteForResult(Activity context) {

        // Create a new post object
        NoteDetail newNote = new NoteDetail();
        //WordPress.wpDB.savePost(newPost);
        Leanote.leaDB.addNote(newNote);
        Intent intent = new Intent(context, EditNoteActivity.class);
        intent.putExtra(EditNoteActivity.EXTRA_NOTEID, newNote.getId());
        intent.putExtra(EditNoteActivity.EXTRA_IS_NEW_NOTE, true);
        context.startActivityForResult(intent, RequestCodes.EDIT_NOTE);
    }

    public static void editNoteForResult(Activity activity, long noteId) {
        Intent intent = new Intent(activity.getApplicationContext(), EditNoteActivity.class);
        intent.putExtra(EditNoteActivity.EXTRA_NOTEID, noteId);
        intent.putExtra(EditNoteActivity.EXTRA_IS_NEW_NOTE, false);
        activity.startActivityForResult(intent, RequestCodes.EDIT_NOTE);
    }

    public static void shareNoteForResult(Activity activity, long noteId) {
        Intent intent = new Intent(activity.getApplicationContext(), ShareNoteActivity.class);
        intent.putExtra(ShareNoteActivity.EXTRA_NOTEID, noteId);
        activity.startActivityForResult(intent, RequestCodes.SHARE_NOTE);
    }

    public static void editNotebookForResult(Activity activity, long localNotebookId) {
        Intent intent = new Intent(activity.getApplicationContext(), EditNotebookActivity.class);
        intent.putExtra(EditNotebookActivity.EXTRA_NEW_NOTEBOOK_ID, localNotebookId);
        intent.putExtra(EditNotebookActivity.EXTRA_IS_NEW_NOTEBOOK, false);
        activity.startActivityForResult(intent, RequestCodes.EDIT_NOTE);
    }
    public static void viewNoteFragmentForResult(Activity activity, NotebookInfo notebook) {
        Intent intent = new Intent(activity.getApplicationContext(), Notebook_ViewNote.class);
        intent.putExtra(Notebook_ViewNote.EXTRA_NOTEBOOK, notebook.getNotebookId());
        activity.startActivityForResult(intent, RequestCodes.NOTEBOOK_VIEW_NOTE);
    }

//    public static void viewNotebookForResult(Activity activity, long localNotebookId) {
//        Intent intent = new Intent(activity.getApplicationContext(), NoteListActivity.class);
//        intent.putExtra(NoteListActivity.EXTRA_NOTEBOOK_ID, localNotebookId);
//        activity.startActivityForResult(intent, RequestCodes.EDIT_NOTE);
//    }


    public static void slideOutToRight(Activity activity) {
        if (activity != null
                && activity.getIntent() != null
                && activity.getIntent().hasExtra(ARG_DID_SLIDE_IN_FROM_RIGHT)) {
            activity.overridePendingTransition(R.anim.do_nothing, R.anim.activity_slide_out_to_right);
        }
    }


    public static void addNewNotebookForResult(Activity context) {
        NotebookInfo newNotebook = new NotebookInfo();
        //WordPress.wpDB.savePost(newPost);
        Leanote.leaDB.addNotebook(newNotebook);
        Intent intent = new Intent(context, EditNotebookActivity.class);
        intent.putExtra(EditNotebookActivity.EXTRA_NEW_NOTEBOOK_ID, newNotebook.getId());
        intent.putExtra(EditNotebookActivity.EXTRA_IS_NEW_NOTEBOOK, true);
        context.startActivityForResult(intent, RequestCodes.EDIT_NOTE);
    }

    public static void previewNoteForResult(Activity activity, Long id) {
        NoteDetail note = Leanote.leaDB.getLocalNoteById(id);
        if (note == null) return;

        Intent intent = new Intent(activity, NotePreviewActivity.class);
        intent.putExtra(NotePreviewActivity.ARG_LOCAL_NOTE_ID, id);
        slideInFromRightForResult(activity, intent, RequestCodes.PREVIEW_NOTE);

    }

    public static void slideInFromRightForResult(Activity activity, Intent intent, int requestCode) {
        intent.putExtra(ARG_DID_SLIDE_IN_FROM_RIGHT, true);
        ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(
                activity,
                R.anim.activity_slide_in_from_right,
                R.anim.do_nothing);
        ActivityCompat.startActivityForResult(activity, intent, requestCode, options.toBundle());
    }

    public static void visitBlog(Activity activity){
        Intent intent = new Intent(activity,BlogHomeActivity.class);
        activity.startActivity(intent);
    }

    public static void startSearchForResult(Activity context, Long searchType) {  //0: Note, 1: Blog, 2: Notebook
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(SearchActivity.SEARCH_TYPE, searchType);
        context.startActivityForResult(intent, RequestCodes.SEARCH_NOTE);
    }
    public static void startSearchForResult(Activity context) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(SearchActivity.SEARCH_TYPE, new Long(0));
        context.startActivityForResult(intent, RequestCodes.SEARCH_NOTE);
    }
}
