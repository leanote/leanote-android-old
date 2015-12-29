package com.leanote.android.ui.note;

import android.content.Context;
import android.os.AsyncTask;
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
import com.leanote.android.model.AccountHelper;
import com.leanote.android.model.NotebookInfo;
import com.leanote.android.networking.NetworkRequest;
import com.leanote.android.networking.NetworkUtils;
import com.leanote.android.service.NoteSyncService;
import com.leanote.android.util.NoteSyncResultEnum;
import com.leanote.android.util.StringUtils;
import com.leanote.android.util.ToastUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by binnchx on 11/12/15.
 */
public class EditNotebookActivity extends AppCompatActivity {

    public static final String EXTRA_IS_NEW_NOTEBOOK = "new_notebook";
    public static final String EXTRA_NEW_NOTEBOOK_ID = "new_notebook_id";
    public static final String EXTRA_SERVER_NOTEBOOK_ID = "server_notebook_id";

    private boolean mIsNewNotebook;
    private NotebookInfo mNotebook;
    private String mOriginalNotebookTitle;

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
                    mOriginalNotebookTitle = mNotebook.getTitle();
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

        if (itemId == R.id.menu_save_notebook || itemId == android.R.id.home) {

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

                if (!notebookTitle.equals(mOriginalNotebookTitle)) {
                    mNotebook.setIsDirty(true);
                    Leanote.leaDB.updateNotebook(mNotebook);

                    NotebookUploadTask uploadTask = new NotebookUploadTask();
                    uploadTask.execute(mNotebook);
                }


            } catch (Exception e) {
                ToastUtils.showToast(this, getString(R.string.save_notebook_fail));
                Log.e("error", "", e);
            }
            setResult(RESULT_OK);
            finish();
            return true;
        }

        return false;
    }

    private class NotebookUploadTask extends AsyncTask<NotebookInfo, Void, NoteSyncResultEnum> {

        @Override
        protected NoteSyncResultEnum doInBackground(NotebookInfo... params) {
            if (params.length == 0) {
                return NoteSyncResultEnum.FAIL;
            }

            NotebookInfo notebook = params[0];
            /*
            1.pull, 2.push. 3.update usn
             */

            //1.pull
            NoteSyncService.syncPullNote();

            //2. push
            String api = null;
            if (!TextUtils.isEmpty(notebook.getNotebookId())) {
                api = String.format("%s/api/notebook/updateNotebook?notebookId=%s&title=%s&parentNotebookid=%s&seq=%s&usn=%s&token=%s",
                    AccountHelper.getDefaultAccount().getHost(),
                    notebook.getNotebookId(),
                    notebook.getTitle(),
                    notebook.getParentNotebookId(),
                    notebook.getSeq(), notebook.getUsn(),
                    AccountHelper.getDefaultAccount().getmAccessToken());

            } else {
                api = String.format("%s/api/notebook/addNotebook?title=%s&parentNotebookid=%s&seq=%s&token=%s",
                        AccountHelper.getDefaultAccount().getHost(),
                        notebook.getTitle(),
                        notebook.getParentNotebookId(),
                        notebook.getSeq(),
                        AccountHelper.getDefaultAccount().getmAccessToken());

            }

            try {
                String response = NetworkRequest.syncGetRequest(api);
                return processResponse(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private NoteSyncResultEnum processResponse(String response) throws Exception {
        JSONObject json = new JSONObject(response);
        boolean ok = false;
        if (json.has("Ok")) {
            ok = json.getBoolean("Ok");
        }
        String notebookId = null;
        if (json.has("NotebookId")) {
            notebookId = json.getString("NotebookId");
        }

        String msg = null;
        if (json.has("msg")) {
            msg = json.getString("msg");
        }

        if (!TextUtils.isEmpty(notebookId)) {
            NotebookInfo notebook = NoteSyncService.parseServerNotebook(json);
            //加上本地id
            Leanote.leaDB.updateNotebook(notebook);
            return NoteSyncResultEnum.SUCCESS;
        } else if (!ok && "conflict".equals(msg)){
            //更新server端笔记本到本地
            handleConflictNotebook(mNotebook.getNotebookId());
            return NoteSyncResultEnum.CONFLICT;
        }


        return NoteSyncResultEnum.FAIL;
    }

    private void handleConflictNotebook(String notebookId) throws Exception {
        String notebookApi = String.format("%s/api/notebook/getNotebooks?token=%s",
                AccountHelper.getDefaultAccount().getHost(),
                AccountHelper.getDefaultAccount().getmAccessToken());

        String notebookRes = NetworkRequest.syncGetRequest(notebookApi);
        JSONArray jsonArray = new JSONArray(notebookRes);
        JSONObject notebook = null;

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject notebookObj = jsonArray.getJSONObject(i);
            if (StringUtils.equals(notebookId, notebookObj.getString("NotebookId"))) {
                notebook = notebookObj;
            }
        }

        NotebookInfo serverNotebook = NoteSyncService.parseServerNotebook(notebook);
        Leanote.leaDB.updateNotebook(serverNotebook);
    }


    @Nullable
    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        return super.onCreateView(name, context, attrs);
    }
}
