package com.leanote.android.ui.note;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.NotebookInfo;
import com.leanote.android.networking.NetworkUtils;
import com.leanote.android.service.NoteSyncService;
import com.leanote.android.util.StringUtils;
import com.leanote.android.util.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by binnchx on 11/12/15.
 */
public class EditNotebookActivity extends AppCompatActivity {

    public static final String EXTRA_IS_NEW_NOTEBOOK = "new_notebook";
    public static final String EXTRA_NEW_NOTEBOOK_ID = "new_notebook_id";

    private boolean mIsNewNotebook;
    private NotebookInfo mNotebook;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_notebook);


        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0.0f);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        Bundle extras = getIntent().getExtras();


        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);


        if (savedInstanceState == null) {
            if (extras != null) {
                // Load post from the postId passed in extras
                long notebookId = extras.getLong(EXTRA_NEW_NOTEBOOK_ID);
                mIsNewNotebook = extras.getBoolean(EXTRA_IS_NEW_NOTEBOOK);
                mNotebook = Leanote.leaDB.getLocalNotebookById(notebookId);

                if (!mIsNewNotebook) {
                    ((TextView) viewGroup.findViewById(R.id.edit_notebook_title)).setText(mNotebook.getTitle());
                }

            } else {
                // A postId extra must be passed to this activity
                showErrorAndFinish(R.string.notebook_not_found);
                return;
            }
        }

        if (mIsNewNotebook) {
            setTitle(StringUtils.unescapeHTML(getString(R.string.add_new_notebook)));
        } else {
            setTitle(StringUtils.unescapeHTML(getString(R.string.edit_notebook_title)));
        }

    }

    private void showErrorAndFinish(int errorMessageId) {
        Toast.makeText(this, getResources().getText(errorMessageId), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_notebook, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);

        String notebookTitle = ((EditText)viewGroup.findViewById(R.id.edit_notebook_title)).getText().toString();

        if (itemId == R.id.menu_save_notebook) {

            if (TextUtils.isEmpty(notebookTitle)) {
                ToastUtils.showToast(this, getString(R.string.empty_notebook_title));
                return false;
            }

            if (!NetworkUtils.isNetworkAvailable(this)) {
                ToastUtils.showToast(this, R.string.no_network_message, ToastUtils.Duration.SHORT);
                return false;
            }


            try {
                mNotebook.setTitle(notebookTitle);
                saveNotebook(mNotebook);

            } catch (Exception e) {
                ToastUtils.showToast(this, getString(R.string.save_notebook_fail));
                Log.e("error", "", e);
            }
            setResult(RESULT_OK);
            finish();
            return true;
        } else if (itemId == android.R.id.home) {
            setResult(RESULT_OK);
            finish();

            return true;
        }
        return false;
    }

    private void saveNotebook(NotebookInfo notebook) throws Exception {
        notebook.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        notebook.setIsDirty(true);
        NoteSyncService.updateNotebook(notebook);
    }


    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }
}
