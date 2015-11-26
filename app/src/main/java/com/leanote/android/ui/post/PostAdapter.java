package com.leanote.android.ui.post;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.model.NoteDetailList;
import com.leanote.android.ui.note.NoteListAdapter;
import com.leanote.android.ui.note.SearchToolbar;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.DisplayUtils;
import com.leanote.android.widget.PostListButton;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yun on 11/23/15.
 */
public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private final int mPhotonWidth;
    private final int mPhotonHeight;
    private final int mEndlistIndicatorHeight;

    //private final boolean mIsStatsSupported;
    private final boolean mAlwaysShowAllButtons;
    private boolean mIsLoadingPosts;
    private NoteDetailList mNotes = new NoteDetailList();
    private final List<NoteDetail> mHiddenNotes = new ArrayList<>();

    private NoteListAdapter.OnPostsLoadedListener mOnPostsLoadedListener;

    public PostAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);

        int displayWidth = DisplayUtils.getDisplayPixelWidth(context);
        int contentSpacing = context.getResources().getDimensionPixelSize(R.dimen.content_margin);
        mPhotonWidth = displayWidth - (contentSpacing * 2);
        mPhotonHeight = context.getResources().getDimensionPixelSize(R.dimen.reader_featured_image_height);
        // endlist indicator height is hard-coded here so that its horz line is in the middle of the fab
        mEndlistIndicatorHeight = DisplayUtils.dpToPx(context, 74);
        // on larger displays we can always show all buttons
        mAlwaysShowAllButtons = (displayWidth >= 1080);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mLayoutInflater.inflate(R.layout.post_cardview, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final NoteDetail note = mNotes.get(position);  //not clear
        Log.i("note", note.toString());
        Context context = holder.itemView.getContext();

        if (holder instanceof NoteViewHolder) {
            NoteViewHolder postHolder = (NoteViewHolder) holder;

            if (StringUtils.isNotEmpty(note.getTitle())) {
                postHolder.txtTitle.setText(note.getTitle());
            } else {
                postHolder.txtTitle.setText("(" + context.getResources().getText(R.string.untitled) + ")");
            }


            postHolder.txtDate.setText(note.getUpdatedTime());
            postHolder.txtDate.setVisibility(View.VISIBLE);
            postHolder.btnTrash.setButtonType(PostListButton.BUTTON_TRASH);

            //configurePostButtons(postHolder, note);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("notewe", position+"");
              //  NoteDetail selectedNote = getItem(position);
              //  if (mOnPostSelectedListener != null && selectedNote != null) {
              //      mOnPostSelectedListener.onPostSelected(selectedNote);
              //  }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mNotes.size() == 0) {
            return 0;
        } else {
            return mNotes.size() ; // +1 for the endlist indicator
        }
    }


    class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtTitle;
        private final TextView txtDate;

        private final PostListButton btnEdit;
        private final PostListButton btnView;

        private final PostListButton btnTrash;

        private final ViewGroup layoutButtons;

        public NoteViewHolder(View view) {
            super(view);

            txtTitle = (TextView) view.findViewById(R.id.text_title);
            txtDate = (TextView) view.findViewById(R.id.text_date);

            btnEdit = (PostListButton) view.findViewById(R.id.btn_edit);
            btnView = (PostListButton) view.findViewById(R.id.btn_view);

            btnTrash = (PostListButton) view.findViewById(R.id.btn_trash);
            layoutButtons = (ViewGroup) view.findViewById(R.id.layout_buttons);
        }
    }


    public void loadNotes() {
        if (mIsLoadingPosts) {
            AppLog.d(AppLog.T.POSTS, "post adapter > already loading posts");
        } else {
            //load note
            new LoadNotesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class LoadNotesTask extends AsyncTask<Void, Void, Boolean> {
        private NoteDetailList tmpNotes;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsLoadingPosts = true;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mIsLoadingPosts = false;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            tmpNotes = Leanote.leaDB.getNoteisBlogList(AccountHelper.getDefaultAccount().getmUserId());
            Log.i("load notes from local:", String.valueOf(tmpNotes.size()));
            // make sure we don't return any hidden posts
            Log.i("hidden note size:", String.valueOf(mHiddenNotes.size()));
            for (NoteDetail hiddenNote : mHiddenNotes) {
                int index = tmpNotes.indexOfPost(hiddenNote);
                tmpNotes.remove(index);
            }
            Log.i("after remove, size:", String.valueOf(tmpNotes.size()));
            return true;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                mNotes.clear();
                mNotes.addAll(tmpNotes);
                notifyDataSetChanged();
            }

            mIsLoadingPosts = false;
            if (mOnPostsLoadedListener != null) {
                mOnPostsLoadedListener.onPostsLoaded(mNotes.size());
            }
        }
    }
}
