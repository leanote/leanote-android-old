package com.leanote.android.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.editor.EditorFragmentAbstract;
import com.leanote.android.editor.LegacyEditorFragment;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.networking.NetworkRequest;
import com.leanote.android.networking.NetworkUtils;
import com.leanote.android.ui.media.LeaMediaUtils;
import com.leanote.android.ui.note.EditNotePreviewFragment;
import com.leanote.android.ui.note.EditNoteSettingsFragment;
import com.leanote.android.ui.note.service.NoteUploadService;
import com.leanote.android.util.AniUtils;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.DeviceUtils;
import com.leanote.android.util.LeaHtml;
import com.leanote.android.util.LeaImageSpan;
import com.leanote.android.util.MediaFile;
import com.leanote.android.util.MediaUtils;
import com.leanote.android.util.StringUtils;
import com.leanote.android.util.ToastUtils;
import com.leanote.android.util.helper.ImageUtils;
import com.leanote.android.util.helper.MediaGalleryImageSpan;
import com.leanote.android.widget.LeaViewPager;

import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.wordpress.passcodelock.AppLockManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ShareNoteActivity extends AppCompatActivity {

    public static final String EXTRA_NOTEID = "noteId";
    public static final String EXTRA_IS_NEW_NOTE = "isNewNote";

    public static final String STATE_KEY_CURRENT_NOTE = "stateKeyCurrentPost";

    private NoteDetail mNote;

    private Button publishToLeaBlog, sendMailBtn;
//
//    private EditText mailAddrEditText, pwdEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share_note);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0.0f);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        FragmentManager fragmentManager = getFragmentManager();
        Bundle extras = getIntent().getExtras();
        String action = getIntent().getAction();

        if (savedInstanceState == null) {
            if (extras != null) {
                // Load post from the postId passed in extras
                long localNoteId = extras.getLong(EXTRA_NOTEID, 0L);

                mNote = Leanote.leaDB.getLocalNoteById(localNoteId);
                AppLog.i("mnote:" + mNote);
            } else {
                // A postId extra must be passed to this activity
                showErrorAndFinish(R.string.note_not_found);
                return;
            }
        }

        // Ensure we have a valid post
        if (mNote == null) {
            showErrorAndFinish(R.string.note_not_found);
            return;
        }

        setTitle(StringUtils.unescapeHTML("Share Notes"));


        publishToLeaBlog  = (Button) findViewById(R.id.share_note_blog_btn);
        publishToLeaBlog.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNote.setIsPublicBlog(true);
                Leanote.leaDB.updateNote(mNote);
                Toast.makeText(getApplicationContext(), "Publish to Lea++ Blog", Toast.LENGTH_LONG).show();
            }
        });

        sendMailBtn = (Button) findViewById(R.id.share_note_mail_btn);
//        mailAddrEditText = (EditText) findViewById(R.id.sourceview_mail_addr);
//        pwdEditText = (EditText) findViewById(R.id.sourceview_mail_pwd);
        sendMailBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if( mailAddrEditText.getText().length() == 0 ) {
//                    Toast.makeText(getApplicationContext(), "Please input you e-mail address", Toast.LENGTH_LONG).show();
//                }
//
//                if( pwdEditText.getText().length() == 0 ) {
//                    Toast.makeText(getApplicationContext(), "Please input you e-mail password", Toast.LENGTH_LONG).show();
//                }

                Intent data=new Intent(Intent.ACTION_SENDTO);
                data.setData(Uri.parse("mailto:"));
                data.putExtra(Intent.EXTRA_SUBJECT, mNote.getTitle());
                data.putExtra(Intent.EXTRA_TEXT, mNote.getContent());
                startActivity(data);
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(STATE_KEY_CURRENT_NOTE, mNote);

    }


    @Override
    public void onBackPressed() {
        finish();
    }



    public NoteDetail getNote() {
        return mNote;
    }



    private void showErrorAndFinish(int errorMessageId) {
        Toast.makeText(this, getResources().getText(errorMessageId), Toast.LENGTH_LONG).show();
        finish();
    }

}
