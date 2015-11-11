package com.leanote.android.ui.note;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        mNotebookSpinner = (Spinner) mRootView.findViewById(R.id.notebook);

        mTagsEditText = (EditText) mRootView.findViewById(R.id.tags);


        initSettingsFields();
        setListeners();

        return mRootView;
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
            params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.note_public_settings);
            params.addRule(RelativeLayout.ALIGN_LEFT, -1);
            toggleButtonPublicBlog.setLayoutParams(params);
            toggleButtonPublicBlog
                    .setImageResource(R.drawable.progress_thumb_off_selector);
            togglePublicBlog.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }


        ArrayAdapter<String> notebookAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mNotebooks);
        notebookAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

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





    /**
     * Updates post object with content of this fragment
     */
    public void updatePostSettings() {
        if (!isAdded() || mNote == null) {
            return;
        }

        String tags = EditTextUtils.getText(mTagsEditText);


        //mNote.setIsPublicBlog(StringUtils.equals(status, getString(R.string.private_note)) ? false : true);
        mNote.setTags(tags);
        Leanote.leaDB.addNote(mNote);
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
