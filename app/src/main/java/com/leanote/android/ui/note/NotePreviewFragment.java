package com.leanote.android.ui.note;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.HtmlUtils;
import com.leanote.android.util.LeaWebViewClient;
import com.leanote.android.util.StringUtils;
import com.leanote.android.util.ToastUtils;

/**
 * Created by binnchx on 11/22/15.
 */
public class NotePreviewFragment extends Fragment
        implements LeaWebViewClient.OnImageLoadListener  {

    private long mLocalNoteId;
    private WebView mWebView;

    public static NotePreviewFragment newInstance(long localNoteId) {
        Bundle args = new Bundle();
        args.putLong(NotePreviewActivity.ARG_LOCAL_NOTE_ID, localNoteId);
        NotePreviewFragment fragment = new NotePreviewFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        mLocalNoteId = args.getLong(NotePreviewActivity.ARG_LOCAL_NOTE_ID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mLocalNoteId = savedInstanceState.getLong(NotePreviewActivity.ARG_LOCAL_NOTE_ID);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(NotePreviewActivity.ARG_LOCAL_NOTE_ID, mLocalNoteId);
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.note_preview_fragment, container, false);

        mWebView = (WebView) view.findViewById(R.id.webView);
        LeaWebViewClient client = new LeaWebViewClient();
        client.setImageLoadedListener(this);

        mWebView.setWebViewClient(client);
        mWebView.getSettings().setJavaScriptEnabled(true);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshPreview();
    }

    void refreshPreview() {
        if (!isAdded()) return;

        new Thread() {
            @Override
            public void run() {
                NoteDetail note = Leanote.leaDB.getLocalNoteById(mLocalNoteId);
                final String htmlContent = formatPostContentForWebView(getActivity(), note);
                AppLog.i("html:" + htmlContent);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded()) return;

                        if (htmlContent != null) {
                            mWebView.loadDataWithBaseURL(
                                    "file:///android_asset/", //null,
                                    htmlContent,
                                    "text/html",
                                    "utf-8",
                                    null);
                        } else {
                            ToastUtils.showToast(getActivity(), R.string.note_not_found);
                        }
                    }
                });
            }
        }.start();
    }

    private String formatPostContentForWebView(Context context, NoteDetail note) {
        if (context == null || note == null) {
            return null;
        }

        String title = (TextUtils.isEmpty(note.getTitle())
                ? "(" + getResources().getText(R.string.untitled) + ")"
                : StringUtils.unescapeHTML(note.getTitle()));

//        String noteContent = PostUtils.collapseShortcodes(note.getDescription());
//        if (!TextUtils.isEmpty(note.getMoreText())) {
//            noteContent += "\n\n" + note.getMoreText();
//        }

        String noteContent = note.getContent();
        // if this is a local draft, remove src="null" from image tags then replace the "android-uri"
        // tag added for local image with a valid "src" tag so local images can be viewed
        if (note.getUsn() == 0) {
            noteContent = noteContent.replace("src=\"null\"", "").replace("android-uri=", "src=");
        } else {
            //已经提交到服务端的笔记

        }

        String textColorStr = HtmlUtils.colorResToHtmlColor(context, R.color.grey_dark);
        String linkColorStr = HtmlUtils.colorResToHtmlColor(context, R.color.reader_hyperlink);

        int contentMargin = getResources().getDimensionPixelSize(R.dimen.content_margin);
        String marginStr = Integer.toString(contentMargin) + "px";

        if( note.isMarkDown() ) {
            return "<html><head><link href=\"http://cdn.bootcss.com/bootstrap/3.3.4/css/bootstrap.min.css\" rel=\"stylesheet\"><link href=\"http://leanote.com/public/blog/themes/elegant/style.css\" rel=\"stylesheet\"><style>#content {width: 900px;margin: auto;} img, svg { display: block;  margin: auto;  } </style> <script src=\"markdown-to-html.min.js\"></script> </head> " +
                    "<body> " + "<h1>" + note.getTitle() + "</h1>" +
                    "<div id=\"content\"  style=\"word-wrap:break-word\"> hello!~ </div> <script> function f(t) { markdownToHtml(t, document.getElementById('content')) } " +
                    "f('" + note.getContent().replace("\n", "\\n")
                    + "')</script> </body> </html>";
        } else{
            return "<!DOCTYPE html><html><head><meta charset='UTF-8' />"
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
                    + "<h1>" + title + "</h1>"
                    + StringUtils.addPTags(noteContent)
                    + "</body></html>";
        }
    }

    @Override
    public void onImageLoaded(String localFileId) {
        //更新note的fileId字段
        NoteDetail note = Leanote.leaDB.getLocalNoteById(mLocalNoteId);
        String fileIds = note.getFileIds();
        if (fileIds != null && fileIds.length() > 0) {
            fileIds += "," + localFileId;
        } else {
            fileIds = localFileId;
        }
        note.setFileIds(fileIds);
        Leanote.leaDB.updateNote(note);

        refreshPreview();
    }


}