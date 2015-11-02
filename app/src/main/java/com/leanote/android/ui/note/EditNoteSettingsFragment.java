package com.leanote.android.ui.note;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.util.EditTextUtils;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;

/**
 * Created by binnchx on 10/27/15.
 */
public class EditNoteSettingsFragment extends Fragment
        implements View.OnClickListener, TextView.OnEditorActionListener {

    private static final int ACTIVITY_REQUEST_CODE_SELECT_CATEGORIES = 5;

    private NoteDetail mNote;

    private Spinner mStatusSpinner;
    private Spinner mNotebookSpinner;
    private EditText mTagsEditText;
    private ViewGroup mRootView;
    private Integer isNotePublic;

    private ArrayList<String> mNoteBook;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mNote = ((EditNoteActivity) getActivity()).getNote();
        mRootView = (ViewGroup) inflater.inflate(R.layout.edit_note_settings_fragment, container, false);

        if (mRootView == null || mNote == null) {
            return null;
        }

        mNoteBook = new ArrayList<String>();


        mStatusSpinner = (Spinner) mRootView.findViewById(R.id.status);
        mStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updatePostSettingsAndSaveButton();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mNotebookSpinner = (Spinner) mRootView.findViewById(R.id.notebook);


        mTagsEditText = (EditText) mRootView.findViewById(R.id.tags);


        initSettingsFields();


        return mRootView;
    }

    private void initSettingsFields() {

        String[] items = new String[]{ getResources().getString(R.string.publish_note),
                getResources().getString(R.string.private_note)};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStatusSpinner.setAdapter(adapter);
        mStatusSpinner.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return false;
                    }
                }
        );

        String[] notebookItems = new String[]{};

        ArrayAdapter<String> notebookAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, items);
        notebookAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStatusSpinner.setAdapter(adapter);
        mStatusSpinner.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        return false;
                    }
                }
        );


        if (mNote.isPublicBlog()) {
            mStatusSpinner.setSelection(0, true);
        } else {
            mStatusSpinner.setSelection(1, true);
        }



        String tags = mNote.getTags();

        if (StringUtils.isNotEmpty(tags)) {
            mTagsEditText.setText(tags);
        }
    }

    private String getPostStatusForSpinnerPosition(int position) {
        if (position == 0) {
            return getString(R.string.private_note);
        } else {
            return getString(R.string.publish_note);
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





    /**
     * Updates post object with content of this fragment
     */
    public void updatePostSettings() {
        if (!isAdded() || mNote == null) {
            return;
        }

        String tags = EditTextUtils.getText(mTagsEditText);

        String status = getPostStatusForSpinnerPosition(mStatusSpinner.getSelectedItemPosition());;
//        if (mStatusSpinner != null) {
//            status = getPostStatusForSpinnerPosition(mStatusSpinner.getSelectedItemPosition());
//        } else {
//            status = mNote.getPostStatus();
//        }


        mNote.setIsPublicBlog(StringUtils.equals(status, getString(R.string.private_note)) ? false : true);
        mNote.setTags(tags);
        Leanote.leaDB.saveNote(mNote);
    }

    /*
     * Saves settings to post object and updates save button text in the ActionBar
     */
    private void updatePostSettingsAndSaveButton() {
        if (isAdded()) {
            updatePostSettings();
            getActivity().invalidateOptionsMenu();
        }
    }

    @Override
    public void onClick(View view) {

    }

    public void updateNoteSettings() {

    }
}
