package com.leanote.android.ui.note;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.model.NotebookInfo;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.EditTextUtils;
import com.leanote.android.util.ToggleListener;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Created by binnchx on 10/27/15.
 */
public class EditNoteSettingsFragment extends Fragment
        implements View.OnClickListener, TextView.OnEditorActionListener {

    private static final int ACTIVITY_REQUEST_CODE_SELECT_CATEGORIES = 5;

    private NoteDetail mNote;

    private LinearLayout notePublicSettings;
    private ToggleButton togglePublicBlog;
    private ImageButton toggleButtonPublicBlog;

    private Spinner mNotebookSpinner;
    private EditText mTagsEditText;
    private ViewGroup mRootView;

    private List<String> mNotebooks;
    private List<NotebookInfo> mNotebookInfos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mNote = ((EditNoteActivity) getActivity()).getNote();
        mRootView = (ViewGroup) inflater.inflate(R.layout.edit_note_settings_fragment, container, false);

        if (mRootView == null || mNote == null) {
            return null;
        }

        notePublicSettings = (LinearLayout) mRootView.findViewById(R.id.note_public_settings);
        togglePublicBlog = (ToggleButton) mRootView.findViewById(R.id.toggle_public_blog);
        toggleButtonPublicBlog = (ImageButton) mRootView.findViewById(R.id.toggleButton_public_blog);

        mNotebooks = Leanote.leaDB.getNotebookTitles();
        mNotebookInfos = Leanote.leaDB.getNotebookList();

        Leanote.leaDB.getNotebookList();
        mNotebookSpinner = (Spinner) mRootView.findViewById(R.id.notebook);
        mNotebookSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        saveSettingsAndSaveButton();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                }
        );
        mTagsEditText = (EditText) mRootView.findViewById(R.id.tags);


        initSettingsFields();
        setListeners();

        return mRootView;
    }

    private void saveSettingsAndSaveButton() {
        AppLog.i("begin to save setting:" + isAdded());
        if (isAdded()) {
            updateNoteSettings();
            getActivity().invalidateOptionsMenu();
        }
    }

    private void setListeners() {

        togglePublicBlog.setOnCheckedChangeListener(new ToggleListener(getActivity(),
                "public_note", togglePublicBlog, toggleButtonPublicBlog, mNote.getNoteId()));


        View.OnClickListener clickToToggleListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                togglePublicBlog.toggle();
            }
        };

        toggleButtonPublicBlog.setOnClickListener(clickToToggleListener);
        notePublicSettings.setOnClickListener(clickToToggleListener);

    }

    private void initSettingsFields() {
        boolean isPublic = mNote.isPublicBlog();
        togglePublicBlog.setChecked(isPublic);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toggleButtonPublicBlog
                .getLayoutParams();


        if (isPublic) {
            params.addRule(RelativeLayout.ALIGN_RIGHT, -1);
            params.addRule(RelativeLayout.ALIGN_LEFT,
                    R.id.toggleButton_public_blog);
            toggleButtonPublicBlog.setLayoutParams(params);
            toggleButtonPublicBlog
                    .setImageResource(R.drawable.progress_thumb_selector);
            togglePublicBlog.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else {
            params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.toggle_public_blog);
            params.addRule(RelativeLayout.ALIGN_LEFT, -1);
            toggleButtonPublicBlog.setLayoutParams(params);
            toggleButtonPublicBlog
                    .setImageResource(R.drawable.progress_thumb_off_selector);
            togglePublicBlog.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }


        ArrayAdapter<String> notebookAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mNotebooks);
        notebookAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNotebookSpinner.setAdapter(notebookAdapter);

        String tags = mNote.getTags();

        if (StringUtils.isNotEmpty(tags)) {
            mTagsEditText.setText(tags);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        if (data != null || ((requestCode == RequestCodes.TAKE_PHOTO ||
//                requestCode == RequestCodes.TAKE_VIDEO))) {
//            Bundle extras;
//
//            switch (requestCode) {
//                case ACTIVITY_REQUEST_CODE_SELECT_CATEGORIES:
//                    extras = data.getExtras();
//                    if (extras != null && extras.containsKey("selectedCategories")) {
//                        mNoteBook = (ArrayList<String>) extras.getSerializable("selectedCategories");
//                        populateSelectedCategories();
//                    }
//                    break;
//            }
//        }
    }


    @Override
    public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
//        boolean handled = false;
//        int id = view.getId();
//        if (id == R.id.searchLocationText && actionId == EditorInfo.IME_ACTION_SEARCH) {
//            searchLocation();
//            handled = true;
//        }
//        return handled;
        return false;
    }


    @Override
    public void onClick(View view) {

    }

    public void updateNoteSettings() {
        if (!isAdded() || mNote == null) {
            return;
        }


        String tags = EditTextUtils.getText(mTagsEditText);


        mNote.setIsPublicBlog(togglePublicBlog.isChecked());
        mNote.setTags(tags);

        //保存notebookid和notebook name 关联
        NotebookInfo notebook = mNotebookInfos.get(mNotebookSpinner.getSelectedItemPosition());
        mNote.setNoteBookId(notebook.getNotebookId());

        Leanote.leaDB.saveNoteSettings(mNote);
        ((EditNoteActivity)getActivity()).reloadNote();
    }


}
