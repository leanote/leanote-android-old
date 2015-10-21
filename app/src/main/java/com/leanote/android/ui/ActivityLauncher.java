package com.leanote.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.leanote.android.R;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.networking.SSLCertsViewActivity;
import com.leanote.android.networking.SelfSignedSSLCertsManager;
import com.leanote.android.ui.accounts.NewAccountActivity;
import com.leanote.android.ui.note.EditNoteActivity;
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

        Intent intent = new Intent(context, EditNoteActivity.class);
        intent.putExtra(EditNoteActivity.EXTRA_NOTEID, newNote.getNoteId());
        intent.putExtra(EditNoteActivity.EXTRA_IS_NEW_POST, true);
        context.startActivityForResult(intent, RequestCodes.EDIT_NOTE);
    }

    public static void editNoteForResult(Activity activity, String noteId) {
        Intent intent = new Intent(activity.getApplicationContext(), EditNoteActivity.class);
        intent.putExtra(EditNoteActivity.EXTRA_NOTEID, noteId);
        intent.putExtra(EditNoteActivity.EXTRA_IS_NEW_POST, false);
        activity.startActivityForResult(intent, RequestCodes.EDIT_NOTE);
    }

    /*
     * Load the post preview as an authenticated URL so stats aren't bumped
     */
    public static void browseNote(Context context, NoteDetail note) {
        if (note == null) return;

//        String url = post.getPermaLink();
//        if (-1 == url.indexOf('?')) {
//            url = url.concat("?preview=true");
//        } else {
//            url = url.concat("&preview=true");
//        }
//        WPWebViewActivity.openUrlByUsingBlogCredentials(context, blog, url);
    }

    public static void slideOutToRight(Activity activity) {
        if (activity != null
                && activity.getIntent() != null
                && activity.getIntent().hasExtra(ARG_DID_SLIDE_IN_FROM_RIGHT)) {
            activity.overridePendingTransition(R.anim.do_nothing, R.anim.activity_slide_out_to_right);
        }
    }




}
