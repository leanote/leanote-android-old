package com.leanote.android.ui.note;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;

import com.leanote.android.R;

/**
 * Created by binnchx on 10/16/15.
 */
public class NoteActivityLauncher {

    private static final String ARG_DID_SLIDE_IN_FROM_RIGHT = "did_slide_in_from_right";

    public static void showReaderPostDetail(Context context, long noteId) {
        Intent intent = new Intent(context, NoteDetailActivity.class);
        intent.putExtra("noteId", noteId);
        slideInFromRight(context, intent);
    }

    public static void slideInFromRight(Context context, Intent intent) {
        if (context instanceof Activity) {
            intent.putExtra(ARG_DID_SLIDE_IN_FROM_RIGHT, true);
            Activity activity = (Activity) context;
            ActivityOptionsCompat options = ActivityOptionsCompat.makeCustomAnimation(
                    activity,
                    R.anim.activity_slide_in_from_right,
                    R.anim.do_nothing);
            ActivityCompat.startActivity(activity, intent, options.toBundle());
        } else {
            context.startActivity(intent);
        }
    }


}
