package com.leanote.android.ui.note;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.widget.TextView;

import com.leanote.android.R;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.util.LeaHtml;

/**
 * Created by binnchx on 10/27/15.
 */
public class EditNotePreviewFragment extends Fragment {
    private EditNoteActivity mActivity;
    private WebView mWebView;
    private TextView mTextView;
    private LoadPostPreviewTask mLoadTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = (EditNoteActivity)getActivity();

        Log.i("enter", " preview editor");
        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.edit_note_preview_fragment, container, false);
        mWebView = (WebView) rootView.findViewById(R.id.post_preview_webview);
        mTextView = (TextView) rootView.findViewById(R.id.post_preview_textview);
        mTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mActivity != null) {
                    loadPost();
                }
                mTextView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mActivity != null && !mTextView.isLayoutRequested()) {
            loadPost();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mLoadTask != null) {
            mLoadTask.cancel(true);
            mLoadTask = null;
        }
    }

    public void loadPost() {
        if (mLoadTask == null) {
            mLoadTask = new LoadPostPreviewTask();
            mLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    // Load post content in the background
    private class LoadPostPreviewTask extends AsyncTask<Void, Void, Spanned> {
        @Override
        protected Spanned doInBackground(Void... params) {
            Spanned contentSpannable;

            if (mActivity == null || mActivity.getNote() == null) {
                return null;
            }

            NoteDetail note = mActivity.getNote();

            String postTitle = "<h1>" + note.getTitle() + "</h1>";
            //String postContent = postTitle + note.getDescription() + "\n\n" + note.getMoreText();
            String postContent = "";

            contentSpannable = LeaHtml.fromHtml(
                    postContent.replaceAll("\uFFFC", ""),
                    mActivity,
                    note,
                    Math.min(mTextView.getWidth(), mTextView.getHeight())
            );


//            if (post.isLocalDraft()) {
//                contentSpannable = WPHtml.fromHtml(
//                        postContent.replaceAll("\uFFFC", ""),
//                        mActivity,
//                        post,
//                        Math.min(mTextView.getWidth(), mTextView.getHeight())
//                );
//            } else {
//                String htmlText = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"webview.css\" /></head><body><div id=\"container\">%s</div></body></html>";
//                htmlText = String.format(htmlText, StringUtils.addPTags(postContent));
//                contentSpannable = new SpannableString(htmlText);
//            }

            return contentSpannable;
        }

        @Override
        protected void onPostExecute(Spanned spanned) {
            if (mActivity != null && mActivity.getNote() != null && spanned != null) {
                if (mActivity.getNote().isLocalDraft()) {
                    mTextView.setVisibility(View.VISIBLE);
                    mWebView.setVisibility(View.GONE);
                    mTextView.setText(spanned);
                } else {
                    mTextView.setVisibility(View.GONE);
                    mWebView.setVisibility(View.VISIBLE);

                    mWebView.loadDataWithBaseURL("file:///android_asset/", spanned.toString(),
                            "text/html", "utf-8", null);
                }
            }

            mLoadTask = null;
        }
    }
}
