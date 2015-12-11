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

            String HTML_Content =  "<!DOCTYPE html><html><head><meta charset='UTF-8' />"
                    + "<meta name='viewport' content='width=device-width, initial-scale=1'>"
                    + "<link href='file:///android_asset/merriweather.css' rel='stylesheet' type='text/css'>"
                    + "<style type='text/css'>"
                    + "  html { margin-left: " + marginStr + "; margin-right: " + marginStr + "; }"
                    + "  body { font-family: Merriweather, serif; font-weight: 400; padding: 0px; margin: 0px; width: 100%; color: " + textColorStr + "; }"
                    + "  body, p, div { max-width: 100% !important; word-wrap: break-word; }"
                    + "  p, div { line-height: 1.6em; font-size: 0.95em; }"
                    + "  h1 { font-size: 1.2em; font-family: Merriweather, serif; font-weight: 700; }"
                    + "  img { max-width: 100%; height: auto; }"
                    + "  a { text-decoration: none; color: " + linkColorStr + "; }"
                    + "</style></head><body>"
                    + "<h1>" + noteTitle + "</h1>"
                    + StringUtils.addPTags(noteContent)
                    + "</body></html>";


//            contentSpannable = LeaHtml.fromHtml(
//                    //HTML_Content.replaceAll("\uFFFC", ""),
//                    HTML_Content,
//                    mActivity,
//                    note,
//                    300
//                    //Math.min(mTextView.getWidth(), mTextView.getHeight())
//            );


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
