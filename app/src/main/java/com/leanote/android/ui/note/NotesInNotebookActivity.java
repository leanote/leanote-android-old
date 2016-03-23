package com.leanote.android.ui.note;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.NotebookInfo;

public class NotesInNotebookActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes_in_notebook);

        final ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setElevation(0.0f);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            //String notebookId = extras.getString(EditNotebookActivity.EXTRA_SERVER_NOTEBOOK_ID);
            Long localotebookId = extras.getLong(EditNotebookActivity.EXTRA_LOCAL_NOTEBOOK_ID);
            NotebookInfo notebook = Leanote.leaDB.getLocalNotebookById(localotebookId);
            setTitle(notebook.getTitle());
        } else {
            setTitle("");
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }
        return true;
    }

}
