package com.leanote.android.ui.note;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.leanote.android.R;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.util.HtmlUtils;
import com.leanote.android.util.LeaHtml;
import com.leanote.android.util.LeaWebViewClient;
import com.leanote.android.util.StringUtils;

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
        LeaWebViewClient client = new LeaWebViewClient();
        mWebView.setWebViewClient(client);
        mWebView.getSettings().setJavaScriptEnabled(true);

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


            String noteTitle =  note.getTitle();
            //String noteTitle = "<h1>" + note.getTitle() + "</h1>";
            // String title = (TextUtils.isEmpty(note.getTitle())
            //        ? "(" + getResources().getText(R.string.untitled) + ")"
            //        : StringUtils.unescapeHTML(note.getTitle()));

            String noteContent = note.getContent();
            //String postContent = postTitle + note.getDescription() + "\n\n" + note.getMoreText();
           // String postContent = "";
            // if this is a local draft, remove src="null" from image tags then replace the "android-uri"
            // tag added for local image with a valid "src" tag so local images can be viewed
            if (note.isLocalDraft()) {
                noteContent = noteContent.replace("src=\"null\"", "").replace("android-uri=", "src=");
            }

            String textColorStr = HtmlUtils.colorResToHtmlColor(mActivity, R.color.grey_dark);
            String linkColorStr = HtmlUtils.colorResToHtmlColor(mActivity, R.color.reader_hyperlink);

            int contentMargin = getResources().getDimensionPixelSize(R.dimen.content_margin);
            String marginStr = Integer.toString(contentMargin) + "px";

            String HTML_Content = "<html><head><link href=\"http://cdn.bootcss.com/bootstrap/3.3.4/css/bootstrap.min.css\" rel=\"stylesheet\"><link href=\"http://leanote.com/public/blog/themes/elegant/style.css\" rel=\"stylesheet\"><style>#content {width: 900px;margin: auto;} img, svg { display: block;  margin: auto;  } </style> <script src=\"markdown-to-html.min.js\"></script> </head> " +
                    "<body> " + "<h1>" + note.getTitle() + "</h1>" +
                    "<div id=\"content\"  style=\"word-wrap:break-word\"> hello!~ </div> <script> function f(t) { markdownToHtml(t, document.getElementById('content')) } " +
                    "f('" + note.getContent().replace("\n", "\\n")
                    + "')</script> </body> </html>";

            contentSpannable = new SpannableString(HTML_Content);

            return contentSpannable;
        }

        @Override
        protected void onPostExecute(Spanned spanned) {

            if (mActivity != null && mActivity.getNote() != null && spanned != null) {

                if (mActivity.getNote().isLocalDraft()) {
                    Log.v("myMsg", "mActivity.getNote().isLocalDraft() ");

//                    mTextView.setVisibility(View.VISIBLE);
//                    mWebView.setVisibility(View.GONE);
//                    mTextView.setText(spanned);


                    mTextView.setVisibility(View.GONE);
                    mWebView.setVisibility(View.VISIBLE);


                    //o~
                    mWebView.loadDataWithBaseURL("file:///android_asset/", spanned.toString(),
                            "text/html", "utf-8", null);

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
