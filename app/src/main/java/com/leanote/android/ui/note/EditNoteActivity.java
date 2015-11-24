package com.leanote.android.ui.note;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.SuggestionSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Toast;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.editor.EditorFragmentAbstract;
import com.leanote.android.editor.LegacyEditorFragment;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.networking.NetworkRequest;
import com.leanote.android.networking.NetworkUtils;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.LeaHtml;
import com.leanote.android.util.LeaImageSpan;
import com.leanote.android.util.MediaFile;
import com.leanote.android.util.StringUtils;
import com.leanote.android.util.ToastUtils;
import com.leanote.android.util.helper.ApiHelper;
import com.leanote.android.util.helper.ImageUtils;
import com.leanote.android.util.helper.MediaGalleryImageSpan;
import com.leanote.android.widget.LeaViewPager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditNoteActivity extends AppCompatActivity implements EditorFragmentAbstract.EditorFragmentListener {

    public static final String EXTRA_NOTEID = "noteId";
    public static final String EXTRA_IS_NEW_NOTE = "isNewNote";

    public static final String EXTRA_IS_QUICKPRESS = "isQuickPress";
    public static final String EXTRA_SAVED_AS_LOCAL_DRAFT = "savedAsLocalDraft";
    public static final String STATE_KEY_CURRENT_NOTE = "stateKeyCurrentPost";
    public static final String STATE_KEY_ORIGINAL_NOTE = "stateKeyOriginalPost";
    public static final String STATE_KEY_EDITOR_FRAGMENT = "editorFragment";

    // Context menu positioning
    private static final int SELECT_PHOTO_MENU_POSITION = 0;
    private static final int CAPTURE_PHOTO_MENU_POSITION = 1;
    private static final int SELECT_VIDEO_MENU_POSITION = 2;
    private static final int CAPTURE_VIDEO_MENU_POSITION = 3;
    private static final int ADD_GALLERY_MENU_POSITION = 4;
    private static final int SELECT_LIBRARY_MENU_POSITION = 5;
    private static final int NEW_PICKER_MENU_POSITION = 6;

    private static final String ANALYTIC_PROP_NUM_LOCAL_PHOTOS_ADDED = "number_of_local_photos_added";
    private static final String ANALYTIC_PROP_NUM_WP_PHOTOS_ADDED = "number_of_wp_library_photos_added";

    private static int PAGE_CONTENT = 0;
    private static int PAGE_SETTINGS = 1;
    private static int PAGE_PREVIEW = 2;

    // Moved from EditPostContentFragment
    public static final String NEW_MEDIA_GALLERY = "NEW_MEDIA_GALLERY";
    public static final String NEW_MEDIA_GALLERY_EXTRA_IDS = "NEW_MEDIA_GALLERY_EXTRA_IDS";
    public static final String NEW_MEDIA_POST = "NEW_MEDIA_POST";
    public static final String NEW_MEDIA_POST_EXTRA = "NEW_MEDIA_POST_ID";

    private boolean mIsNewNote;

    private boolean mHasSetNoteContent;

    private NoteDetail mNote;

    private NoteDetail mOriginalNote;

    private EditorFragmentAbstract mEditorFragment;

    private EditNoteSettingsFragment mEditNoteSettingsFragment;

    private EditNotePreviewFragment mEditNotePreviewFragment;

    SectionsPagerAdapter mSectionsPagerAdapter;

    LeaViewPager mViewPager;

    private int mMaxThumbWidth = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_note);

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
                mIsNewNote = extras.getBoolean(EXTRA_IS_NEW_NOTE);

                mNote = Leanote.leaDB.getLocalNoteById(localNoteId);

                mOriginalNote = Leanote.leaDB.getLocalNoteById(localNoteId);
            } else {
                // A postId extra must be passed to this activity
                showErrorAndFinish(R.string.note_not_found);
                return;
            }
        } else {
            if (savedInstanceState.containsKey(STATE_KEY_ORIGINAL_NOTE)) {
                try {

                    mNote = (NoteDetail) savedInstanceState.getSerializable(STATE_KEY_CURRENT_NOTE);
                    mOriginalNote = (NoteDetail) savedInstanceState.getSerializable(STATE_KEY_ORIGINAL_NOTE);
                } catch (ClassCastException e) {
                    Log.e("error", ":", e);
                    mNote = null;
                }
            }
            mEditorFragment = (EditorFragmentAbstract) fragmentManager.getFragment(savedInstanceState, STATE_KEY_EDITOR_FRAGMENT);
        }

        if (mHasSetNoteContent = mEditorFragment != null) {
            mEditorFragment.setImageLoader(Leanote.imageLoader);
        }


        // Ensure we have a valid post
//        if (mNote == null) {
//            showErrorAndFinish(R.string.note_not_found);
//            return;
//        }

//        if (mIsNewNote) {
//            trackEditorCreatedPost(action, getIntent());
//        }

        setTitle(StringUtils.unescapeHTML("leanote"));

        mSectionsPagerAdapter = new SectionsPagerAdapter(fragmentManager);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (LeaViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setPagingEnabled(false);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {

                invalidateOptionsMenu();
                if (position == PAGE_CONTENT) {
                    setTitle(AccountHelper.getDefaultAccount().getmUserName());
                } else if (position == PAGE_SETTINGS) {
                    setTitle(R.string.post_settings);
                } else if (position == PAGE_PREVIEW) {
                    setTitle(R.string.preview_note);
                    saveNote(true);
                    if (mEditNotePreviewFragment != null) {
                        mEditNotePreviewFragment.loadPost();
                    }
                }
            }
        });

    }

    @Override
    public void onEditorFragmentInitialized() {
        fillContentEditorFields();
    }

    @Override
    public void onSettingsClicked() {
        mViewPager.setCurrentItem(PAGE_SETTINGS);
    }

    @Override
    public void onAddMediaClicked() {

    }

    @Override
    public void saveMediaFile(MediaFile mediaFile) {

    }

    private int getMaximumThumbnailWidthForEditor() {
        if (mMaxThumbWidth == 0) {
            mMaxThumbWidth = ImageUtils.getMaximumThumbnailWidthForEditor(this);
        }
        return mMaxThumbWidth;
    }

    public void reloadNote() {
        mNote = Leanote.leaDB.getLocalNoteByNoteId(mNote.getNoteId());
    }


    private class LoadNoteContentTask extends AsyncTask<Void, Spanned, Spanned> {

        @Override
        protected Spanned doInBackground(Void... voids) {
            String content = fetchNoteContent(getNote().getNoteId());

            if (org.apache.commons.lang.StringUtils.isEmpty(content)) {
                return new SpannableString("");
            }
            return LeaHtml.fromHtml(content, EditNoteActivity.this, getNote(), getMaximumThumbnailWidthForEditor());
        }

        @Override
        protected void onPostExecute(Spanned spanned) {
            if (spanned != null) {
                mEditorFragment.setContent(spanned);
            }
        }
    }

    private String fetchNoteContent(String noteId) {
        NoteDetail note = Leanote.leaDB.getLocalNoteByNoteId(noteId);
        if (note != null && org.apache.commons.lang.StringUtils.isNotEmpty(note.getContent())) {
            return note.getContent();
        }

        String noteApi = String.format("%sapi/note/getNoteContent?noteId=%s&token=%s",
                AccountHelper.getDefaultAccount().getHost(), noteId,
                AccountHelper.getDefaultAccount().getmAccessToken());


        String response;
        String content = null;
        try {
            response = NetworkRequest.syncGetRequest(noteApi);
            JSONObject json = new JSONObject(response);
            content = json.getString("Content");
            Leanote.leaDB.saveNoteContent(noteId, content);

        } catch (Exception e) {
            AppLog.e(AppLog.T.API, "fetch note content error", e);
            ToastUtils.showToast(this, R.string.fetch_note_content_fail);
        }
        return content;
    }


    private void fillContentEditorFields() {
        // Needed blog settings needed by the editor
        if (AccountHelper.getDefaultAccount().getmUserId() != null) {
            mEditorFragment.setFeaturedImageSupported(true);
            //mEditorFragment.setBlogSettingMaxImageWidth(WordPress.getCurrentBlog().getMaxImageWidth());
        }

        // Set post title and content
        NoteDetail note = getNote();
        if (note != null) {
            if (!mHasSetNoteContent) {
                mHasSetNoteContent = true;
                if (!mIsNewNote && org.apache.commons.lang.StringUtils.isEmpty(note.getContent())) {
                    // Load local post content in the background, as it may take time to generate images
//                    new LoadNoteContentTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//                            note.getContent().replaceAll("\uFFFC", ""));
                    new LoadNoteContentTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                }
                else if (!TextUtils.isEmpty(note.getContent())){
                    mEditorFragment.setContent(note.getContent().replaceAll("\uFFFC", ""));
                }
            }
            if (!TextUtils.isEmpty(note.getTitle())) {
                mEditorFragment.setTitle(note.getTitle());
            }
            // TODO: postSettingsButton.setText(post.isPage() ? R.string.page_settings : R.string.post_settings);
            //mEditorFragment.setLocalDraft(note.isLocalDraft());
        }


    }


    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    // TODO: switch between legacy and new editor here (AB test?)
                    return new LegacyEditorFragment();
                case 1:
                    return new EditNoteSettingsFragment();
                default:
                    return new EditNotePreviewFragment();
            }
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);

            switch (position) {
                case 0:
                    mEditorFragment = (EditorFragmentAbstract) fragment;
                    break;
                case 1:
                    mEditNoteSettingsFragment = (EditNoteSettingsFragment) fragment;
                    break;
                case 2:
                    mEditNotePreviewFragment = (EditNotePreviewFragment) fragment;
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    private void trackEditorCreatedPost(String action, Intent intent) {
        Map<String, Object> properties = new HashMap<String, Object>();
        // Post created from the post list (new post button).
        String normalizedSourceName = "post-list";
        if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
            // Post created with share with WordPress
            normalizedSourceName = "shared-from-external-app";
        }
        if (EditNoteActivity.NEW_MEDIA_GALLERY.equals(action) || EditNoteActivity.NEW_MEDIA_POST.equals(
                action)) {
            // Post created from the media library
            normalizedSourceName = "media-library";
        }
        if (intent != null && intent.hasExtra(EXTRA_IS_QUICKPRESS)) {
            // Quick press
            normalizedSourceName = "quick-press";
        }
        if (intent != null && intent.getIntExtra("quick-media", -1) > -1) {
            // Quick photo or quick video
            normalizedSourceName = "quick-media";
        }
        properties.put("created_post_source", normalizedSourceName);
        //AnalyticsTracker.track(AnalyticsTracker.Stat.EDITOR_CREATED_POST, properties);
    }

    private void showErrorAndFinish(int errorMessageId) {
        Toast.makeText(this, getResources().getText(errorMessageId), Toast.LENGTH_LONG).show();
        finish();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_note, menu);

        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saves both post objects so we can restore them in onCreate()
        saveNote(true);
        outState.putSerializable(STATE_KEY_CURRENT_NOTE, mNote);
        outState.putSerializable(STATE_KEY_ORIGINAL_NOTE, mOriginalNote);

        if (mEditorFragment != null) {
            getFragmentManager().putFragment(outState, STATE_KEY_EDITOR_FRAGMENT, mEditorFragment);
        }
    }

    private void updateNoteObject(boolean isAutosave) {
        if (mNote == null) {
            AppLog.e(AppLog.T.POSTS, "Attempted to save an invalid Post.");
            return;
        }

        // Update post object from fragment fields
        if (mEditorFragment != null) {
            updateNoteContent(isAutosave);
        }
        if (mEditNoteSettingsFragment != null) {
            mEditNoteSettingsFragment.updateNoteSettings();
        }
    }


    private void updateNoteContent(boolean isAutoSave) {
        NoteDetail note = getNote();

        if (note == null) {
            return;
        }
        String title = StringUtils.notNullStr((String) mEditorFragment.getTitle());
        SpannableStringBuilder postContent;
        if (mEditorFragment.getSpannedContent() != null) {
            // needed by the legacy editor to save local drafts
            try {
                postContent = new SpannableStringBuilder(mEditorFragment.getSpannedContent());
            } catch (IndexOutOfBoundsException e) {
                // A core android bug might cause an out of bounds exception, if so we'll just use the current editable
                // See https://code.google.com/p/android/issues/detail?id=5164
                postContent = new SpannableStringBuilder(StringUtils.notNullStr((String) mEditorFragment.getContent()));
            }
        } else {
            postContent = new SpannableStringBuilder(StringUtils.notNullStr((String) mEditorFragment.getContent()));
        }

        String content;
        if (note.isLocalDraft()) {
            // remove suggestion spans, they cause craziness in WPHtml.toHTML().
            CharacterStyle[] characterStyles = postContent.getSpans(0, postContent.length(), CharacterStyle.class);
            for (CharacterStyle characterStyle : characterStyles) {
                if (characterStyle instanceof SuggestionSpan) {
                    postContent.removeSpan(characterStyle);
                }
            }
            content = LeaHtml.toHtml(postContent);
            // replace duplicate <p> tags so there's not duplicates, trac #86
            content = content.replace("<p><p>", "<p>");
            content = content.replace("</p></p>", "</p>");
            content = content.replace("<br><br>", "<br>");
            // sometimes the editor creates extra tags
            content = content.replace("</strong><strong>", "").replace("</em><em>", "").replace("</u><u>", "")
                    .replace("</strike><strike>", "").replace("</blockquote><blockquote>", "");
        } else {
            if (!isAutoSave) {
                // Add gallery shortcode
                MediaGalleryImageSpan[] gallerySpans = postContent.getSpans(0, postContent.length(),
                        MediaGalleryImageSpan.class);
                for (MediaGalleryImageSpan gallerySpan : gallerySpans) {
                    int start = postContent.getSpanStart(gallerySpan);
                    postContent.removeSpan(gallerySpan);
                    postContent.insert(start, LeaHtml.getGalleryShortcode(gallerySpan));
                }
            }

            LeaImageSpan[] imageSpans = postContent.getSpans(0, postContent.length(), LeaImageSpan.class);
            if (imageSpans.length != 0) {
                for (LeaImageSpan wpIS : imageSpans) {
                    MediaFile mediaFile = wpIS.getMediaFile();
                    if (mediaFile == null)
                        continue;
                    if (mediaFile.getMediaId() != null) {
                        //updateMediaFileOnServer(wpIS);
                    } else {
                        mediaFile.setFileName(wpIS.getImageSource().toString());
                        mediaFile.setFilePath(wpIS.getImageSource().toString());
                        Leanote.leaDB.saveMediaFile(mediaFile);
                    }

                    int tagStart = postContent.getSpanStart(wpIS);
                    if (!isAutoSave) {
                        postContent.removeSpan(wpIS);

                        // network image has a mediaId
                        if (mediaFile.getMediaId() != null && mediaFile.getMediaId().length() > 0) {
                            postContent.insert(tagStart, LeaHtml.getContent(wpIS));
                        } else {
                            // local image for upload
                            postContent.insert(tagStart,
                                    "<img android-uri=\"" + wpIS.getImageSource().toString() + "\" />");
                        }
                    }
                }
            }
            content = postContent.toString();
        }

        String moreTag = "<!--more-->";

        note.setTitle(title);
        // split up the post content if there's a more tag

    }

    private void updateMediaFileOnServer(LeaImageSpan wpIS) {
        if (wpIS == null)
            return;

        MediaFile mf = wpIS.getMediaFile();

        final String mediaId = mf.getMediaId();
        final String title = mf.getTitle();
        final String description = mf.getDescription();
        final String caption = mf.getCaption();

        ApiHelper.EditMediaItemTask task = new ApiHelper.EditMediaItemTask(mf.getMediaId(), mf.getTitle(),
                mf.getDescription(), mf.getCaption(),
                new ApiHelper.GenericCallback() {
                    @Override
                    public void onSuccess() {
                        //String localBlogTableIndex = String.valueOf(WordPress.getCurrentBlog().getLocalTableBlogId());
                        //WordPress.wpDB.updateMediaFile(localBlogTableIndex, mediaId, title, description, caption);
                    }

                    @Override
                    public void onFailure(ApiHelper.ErrorType errorType, String errorMessage, Throwable throwable) {
                        Toast.makeText(EditNoteActivity.this, R.string.media_edit_failure, Toast.LENGTH_LONG).show();
                    }
                });

        List<Object> apiArgs = new ArrayList<Object>();
        //apiArgs.add(currentBlog);
        task.execute(apiArgs);
    }

    public NoteDetail getNote() {
        return mNote;
    }


    private void saveNote(boolean isAutosave) {
        saveNote(isAutosave, true);
    }

    private void saveNote(boolean isAutosave, boolean updatePost) {
        if (updatePost) {
            updateNoteObject(isAutosave);
        }

        //Leanote.leaDB.updateNote(mNote);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.menu_save_post) {
            // If the post is new and there are no changes, don't publish
            updateNoteObject(false);
//            if (!mPost.isPublishable()) {
//                ToastUtils.showToast(this, R.string.error_publish_empty_post, Duration.SHORT);
//                return false;
//            }

            saveNote(false, false);
            //trackSavePostAnalytics();

            if (!NetworkUtils.isNetworkAvailable(this)) {
                ToastUtils.showToast(this, R.string.no_network_message, ToastUtils.Duration.SHORT);
                return false;
            }

            //PostUploadService.addPostToUpload(mPost);
            //startService(new Intent(this, PostUploadService.class));
            setResult(RESULT_OK);
            finish();
            return true;
        } else if (itemId == R.id.menu_preview_post) {
            mViewPager.setCurrentItem(PAGE_PREVIEW);
        } else if (itemId == android.R.id.home) {
            if (mViewPager.getCurrentItem() > PAGE_CONTENT) {
                mViewPager.setCurrentItem(PAGE_CONTENT);
                invalidateOptionsMenu();
            } else {
                saveAndFinish();
            }
            return true;
        }
        return false;
    }

    private void saveAndFinish() {
        saveNote(true);
        if (mEditorFragment != null && hasEmptyContentFields()) {
            // new and empty post? delete it
            if (mIsNewNote) {
                //WordPress.wpDB.deletePost(mPost);
            }
        } else if (mOriginalNote != null && !mNote.hasChanges(mOriginalNote)) {
            // if no changes have been made to the post, set it back to the original don't save it
            //WordPress.wpDB.updatePost(mOriginalPost);
        } else {
            // changes have been made, save the post and ask for the post list to refresh.
            // We consider this being "manual save", it will replace some Android "spans" by an html
            // or a shortcode replacement (for instance for images and galleries)
            saveNote(false);
            Intent i = new Intent();
            i.putExtra(EXTRA_SAVED_AS_LOCAL_DRAFT, true);
            //i.putExtra(EXTRA_IS_PAGE, mIsPage);
            setResult(RESULT_OK, i);

            ToastUtils.showToast(this, R.string.editor_toast_changes_saved);
        }
        finish();
    }

    private boolean hasEmptyContentFields() {
        return TextUtils.isEmpty(mEditorFragment.getTitle()) && TextUtils.isEmpty(mEditorFragment.getContent());
    }


}
