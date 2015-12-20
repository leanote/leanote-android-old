package com.leanote.android.ui.note;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.LeaWebViewClient;
import com.leanote.android.util.MediaFile;
import com.leanote.android.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by binnchx on 10/27/15.
 */
public class EditNotePreviewFragment extends Fragment {
    private EditNoteActivity mActivity;
    private WebView mWebView;
    private TextView mTextView;
    private LoadNotePreviewTask mLoadTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mActivity = (EditNoteActivity)getActivity();

        ViewGroup rootView = (ViewGroup) inflater
                .inflate(R.layout.edit_note_preview_fragment, container, false);
        mWebView = (WebView) rootView.findViewById(R.id.post_preview_webview);
        mWebView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        mWebView.setWebViewClient(new LeaWebViewClient());

        mTextView = (TextView) rootView.findViewById(R.id.post_preview_textview);
        mTextView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (mActivity != null) {
                    loadNote();
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
            loadNote();
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

    public void loadNote() {
        if (mLoadTask == null) {
            mLoadTask = new LoadNotePreviewTask();
            mLoadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private String processNoteMedia(String content) {

        String imageTagsPattern = "<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>";
        Pattern pattern = Pattern.compile(imageTagsPattern);
        Matcher matcher = pattern.matcher(content);

        List<String> imageTags = new ArrayList<>();
        while (matcher.find()) {
            imageTags.add(matcher.group());
        }

        for (String tag : imageTags) {
            Pattern p = Pattern.compile("src=\"([^\"]+)\"");
            Matcher m = p.matcher(tag);
            if (m.find()) {
                String imageUri = m.group(1);
                if (!"".equals(imageUri)) {
                    MediaFile mediaFile = Leanote.leaDB.getMediaFileByUrl(imageUri);

                    if (mediaFile != null) {
                        String localImagePath = mediaFile.getFilePath();

                        if (!TextUtils.isEmpty(localImagePath)) {
                            content = content.replace(tag, String.format("<img src=\"%s\"' />", localImagePath));
                        } else {
                            content = content.replace(tag, "");
                        }
                    }
                }
            }
        }
        return content;
    }


    // Load post content in the background
    private class LoadNotePreviewTask extends AsyncTask<Void, Void, Spanned> {

        private int width;
        private int height;

        public LoadNotePreviewTask() {
            this.width = mTextView.getWidth();
            this.height = mTextView.getHeight();
        }

        @Override
        protected Spanned doInBackground(Void... params) {
            Spanned contentSpannable;

            if (mActivity == null || mActivity.getNote() == null) {
                return null;
            }

            NoteDetail note = mActivity.getNote();

            String postTitle = "<h1>" + note.getTitle() + "</h1>";
            String postContent = postTitle + note.getContent();

            AppLog.i("postcontent:" + postContent);

            //不懂LeaHtml.fromHtml
//            contentSpannable = LeaHtml.fromHtml(
//                    postContent.replaceAll("\uFFFC", ""),
//                    mActivity,
//                    note,
//                    Math.min(width, height)
//            );


//            if (note.getUsn() == 0) {
//                contentSpannable = LeaHtml.fromHtml(
//                        postContent.replaceAll("\uFFFC", ""),
//                        mActivity,
//                        note,
//                        Math.min(width, height)
//                );
//            } else {
//                String htmlText = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"webview.css\" /></head><body><div id=\"container\">%s</div></body></html>";
//                htmlText = String.format(htmlText, StringUtils.addPTags(postContent));
//                contentSpannable = new SpannableString(htmlText);
//            }

            String htmlText = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?><html><head><link rel=\"stylesheet\" type=\"text/css\" href=\"webview.css\" /></head><body><div id=\"container\">%s</div></body></html>";
            htmlText = String.format(htmlText, StringUtils.addPTags(postContent));
            AppLog.i("html:" + htmlText);

            //把图片url替换成本地url, 本地草稿笔记图片不需要替换
            if (note.getUsn() != 0) {
                htmlText = processNoteMedia(htmlText);
            }

            //contentSpannable = new SpannableString(htmlText);

            contentSpannable = new SpannableString(postContent);

            return contentSpannable;
        }



        @Override
        protected void onPostExecute(Spanned spanned) {
            if (mActivity != null && mActivity.getNote() != null && spanned != null) {
//                if (mActivity.getNote().getUsn() == 0) {
//                    mTextView.setVisibility(View.VISIBLE);
//                    mWebView.setVisibility(View.GONE);
//                    mTextView.setText(spanned);
//                } else {
//                    mTextView.setVisibility(View.GONE);
//                    mWebView.setVisibility(View.VISIBLE);
//
//                    mWebView.loadDataWithBaseURL("file:///android_asset/", spanned.toString(),
//                            "text/html", "utf-8", null);
//                }

                String content = spanned.toString().replace("&nbsp;", "").replace("src=\"null\"", "");
                content = content.replaceAll("android-uri", "src");

                content = "<style>img{display: inline;height: auto;max-width: 100%;}</style>" + content;
                AppLog.i("spanned:" + content);
                mTextView.setVisibility(View.GONE);
                mWebView.setVisibility(View.VISIBLE);

//                mWebView.loadDataWithBaseURL("file:///android_asset/", content,
//                        "text/html", "utf-8", null);

                mWebView.loadDataWithBaseURL(null, content,
                        "text/html", "utf-8", null);

            }

            mLoadTask = null;
        }
    }
}
