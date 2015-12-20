package com.leanote.android.ui.note;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.NotebookInfo;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.DisplayUtils;
import com.leanote.android.widget.PostListButton;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binnchx on 11/11/15.
 */
public class NotebookListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>  {

    public interface OnNotebookButtonClickListener {
        void onNotebookButtonClicked(int buttonId, NotebookInfo notebook);
    }

    private OnNotebooksLoadedListener mOnNotebooksLoadedListener;
    private OnNotebookSelectedListener mOnNotebookSelectedListener;
    private OnNotebookButtonClickListener mOnNotebookButtonClickListener;

    private final int mEndlistIndicatorHeight;

    private boolean mIsLoadingNotebooks;

    private List<NotebookInfo> mNotebooks = new ArrayList<>();
    private final List<NotebookInfo> mHiddenNotebooks = new ArrayList<>();

    private final LayoutInflater mLayoutInflater;

    //private final List<PostsListPost> mHiddenPosts = new ArrayList<>();

    private static final long ROW_ANIM_DURATION = 150;

    private static final int VIEW_TYPE_NOTEBOOK = 0;
    private static final int VIEW_TYPE_ENDLIST_INDICATOR = 1;
    private static final int VIEW_TYPE_MENU = 2;

    public NotebookListAdapter(Context context) {
        mLayoutInflater = LayoutInflater.from(context);

        mEndlistIndicatorHeight = DisplayUtils.dpToPx(context, 74);

    }

    public void setmOnNotebooksLoadedListener(OnNotebooksLoadedListener mOnNotebooksLoadedListener) {
        this.mOnNotebooksLoadedListener = mOnNotebooksLoadedListener;
    }

    public void setmOnNotebookSelectedListener(OnNotebookSelectedListener mOnNotebookSelectedListener) {
        this.mOnNotebookSelectedListener = mOnNotebookSelectedListener;
    }

    public void setmOnNotebookButtonClickListener(OnNotebookButtonClickListener mOnNotebookButtonClickListener) {
        this.mOnNotebookButtonClickListener = mOnNotebookButtonClickListener;
    }

    private NotebookInfo getItem(int position) {
        if (isValidPostPosition(position)) {
            return mNotebooks.get(position);
        }
        return null;
    }

    private boolean isValidPostPosition(int position) {
        return (position >= 0 && position < mNotebooks.size());
    }

    @Override
    public int getItemViewType(int position) {

        if (position == (mNotebooks.size() + 1)) {
            return VIEW_TYPE_ENDLIST_INDICATOR;
        } else if (position == 0) {
            return VIEW_TYPE_MENU;
        }
        return VIEW_TYPE_NOTEBOOK;
    }

    @Override
    public int getItemCount() {
        if (mNotebooks.size() == 0) {
            return 0;
        } else {
            return mNotebooks.size() + 2; // +1 for the endlist indicator
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ENDLIST_INDICATOR) {
            View view = mLayoutInflater.inflate(R.layout.endlist_indicator, parent, false);
            view.getLayoutParams().height = mEndlistIndicatorHeight;
            return new EndListViewHolder(view);
        } else if (viewType == VIEW_TYPE_MENU) {
            return new SearchViewHolder(new SearchToolbar(parent.getContext(), "Notebook"));
        } else{
            View view = mLayoutInflater.inflate(R.layout.post_cardview, parent, false);
            return new NotebookViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        // nothing to do if this is the static endlist indicator
        int posType = getItemViewType(position);
        if (posType == VIEW_TYPE_ENDLIST_INDICATOR) {
            return;
        } else if (posType == VIEW_TYPE_MENU) {
            return;
        }

        final NotebookInfo notebook = mNotebooks.get(position - 1);

        Context context = holder.itemView.getContext();

        if (holder instanceof NotebookViewHolder) {
            NotebookViewHolder postHolder = (NotebookViewHolder) holder;

            if (StringUtils.isNotEmpty(notebook.getTitle())) {
                postHolder.txtTitle.setText(notebook.getTitle());
            } else {
                postHolder.txtTitle.setText("(" + context.getResources().getText(R.string.untitled) + ")");
            }

            postHolder.txtDate.setText(notebook.getUpdateTime());
            postHolder.txtDate.setVisibility(View.VISIBLE);
            postHolder.btnTrash.setButtonType(PostListButton.BUTTON_TRASH);

            configureNotebookButtons(postHolder, notebook);
        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotebookInfo selectedNotebook = getItem(position - 1);
                if (mOnNotebookSelectedListener != null && selectedNotebook != null) {
                    mOnNotebookSelectedListener.onNotebookSelected(selectedNotebook);
                }
            }
        });
    }


    private void configureNotebookButtons(final NotebookViewHolder holder,
                                          final NotebookInfo notebook) {
        // posts with local changes have preview rather than view button
        holder.btnView.setButtonType(PostListButton.BUTTON_VIEW);

        holder.btnEdit.setVisibility(View.VISIBLE);
        holder.btnView.setVisibility(View.VISIBLE);
        holder.btnTrash.setVisibility(View.VISIBLE);


        View.OnClickListener btnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int buttonType = ((PostListButton) view).getButtonType();
                if (mOnNotebookButtonClickListener != null) {
                    mOnNotebookButtonClickListener.onNotebookButtonClicked(buttonType, notebook);
                }
            }
        };

        holder.btnEdit.setOnClickListener(btnClickListener);
        holder.btnView.setOnClickListener(btnClickListener);
        holder.btnTrash.setOnClickListener(btnClickListener);
    }

    /*
     * buttons may appear in two rows depending on display size and number of visible
     * buttons - these rows are toggled through the "more" and "back" buttons - this
     * routine is used to animate the new row in and the old row out
     */
    private void animateButtonRows(final NotebookViewHolder holder,
                                   final NotebookInfo note,
                                   final boolean showRow1) {
        // first animate out the button row, then show/hide the appropriate buttons,
        // then animate the row layout back in
        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0f);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0f);
        ObjectAnimator animOut = ObjectAnimator.ofPropertyValuesHolder(holder.layoutButtons, scaleX, scaleY);
        animOut.setDuration(ROW_ANIM_DURATION);
        animOut.setInterpolator(new AccelerateInterpolator());

        animOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // row 1
                holder.btnEdit.setVisibility(showRow1 ? View.VISIBLE : View.GONE);
                holder.btnView.setVisibility(showRow1 ? View.VISIBLE : View.GONE);
                //holder.btnMore.setVisibility(showRow1 ? View.VISIBLE : View.GONE);
                // row 2
                //holder.btnStats.setVisibility(!showRow1 && canShowStatsForPost(note) ? View.VISIBLE : View.GONE);
                holder.btnTrash.setVisibility(!showRow1 ? View.VISIBLE : View.GONE);
                //holder.btnBack.setVisibility(!showRow1 ? View.VISIBLE : View.GONE);

                PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f);
                PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f);
                ObjectAnimator animIn = ObjectAnimator.ofPropertyValuesHolder(holder.layoutButtons, scaleX, scaleY);
                animIn.setDuration(ROW_ANIM_DURATION);
                animIn.setInterpolator(new DecelerateInterpolator());
                animIn.start();
            }
        });

        animOut.start();
    }

    public void loadNotebooks() {
        if (mIsLoadingNotebooks) {
            AppLog.d(AppLog.T.POSTS, "notebook adapter > already loading posts");
        } else {
            //load note
            new LoadNotebooksTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /*
     * hides the post - used when the post is trashed by the user but the network request
     * to delete the post hasn't completed yet
     */
    public void hideNotebook(NotebookInfo notebook) {
        mHiddenNotebooks.add(notebook);

        int position = -1;
        for (int i = 0; i < mNotebooks.size(); i++) {
            if (mNotebooks.get(i).getId() == notebook.getId()) {
                position = i;
                break;
            }
        }

        if (position > -1) {
            mNotebooks.remove(position);
            if (mNotebooks.size() > 0) {
                notifyItemRemoved(position + 1);
            } else {
                notifyDataSetChanged();
            }

        }
    }

    public void unhideNotebook(NotebookInfo notebook) {
        if (mHiddenNotebooks.remove(notebook)) {
            loadNotebooks();
        }
    }

    public interface OnNotebookSelectedListener {
        void onNotebookSelected(NotebookInfo note);
    }

    public interface OnNotebooksLoadedListener {
        void onNotebooksLoaded(int notebookCount);
    }

    class NotebookViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtTitle;
        private final TextView txtDate;

        private final PostListButton btnEdit;
        private final PostListButton btnView;

        private final PostListButton btnTrash;

        private final ViewGroup layoutButtons;

        public NotebookViewHolder(View view) {
            super(view);

            txtTitle = (TextView) view.findViewById(R.id.text_title);
            txtDate = (TextView) view.findViewById(R.id.text_date);

            btnEdit = (PostListButton) view.findViewById(R.id.btn_edit);
            btnView = (PostListButton) view.findViewById(R.id.btn_view);

            btnTrash = (PostListButton) view.findViewById(R.id.btn_trash);
            layoutButtons = (ViewGroup) view.findViewById(R.id.layout_buttons);
        }
    }



    class EndListViewHolder extends RecyclerView.ViewHolder {
        public EndListViewHolder(View view) {
            super(view);
        }
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {
        private final SearchToolbar mSearchToolbar;
        public SearchViewHolder(View itemView) {
            super(itemView);
            mSearchToolbar = (SearchToolbar) itemView;
        }
    }




    private class LoadNotebooksTask extends AsyncTask<Void, Void, Boolean> {
        private List<NotebookInfo> tmpNotebooks;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mIsLoadingNotebooks = true;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mIsLoadingNotebooks = false;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            tmpNotebooks = Leanote.leaDB.getNotebookList();
            AppLog.i("loading notebooks:" + tmpNotebooks);
            for (NotebookInfo hiddenNote : mHiddenNotebooks) {
                int index = -1;
                for (int i = 0; i < tmpNotebooks.size(); i++) {
                    if (tmpNotebooks.get(i).getNotebookId().equals(hiddenNote.getNotebookId())) {
                        index = i;
                        break;
                    }
                }
                if (index >= 0 && index < tmpNotebooks.size()) {
                    tmpNotebooks.remove(index);
                }
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                mNotebooks.clear();
                mNotebooks.addAll(tmpNotebooks);
                notifyDataSetChanged();
            }

            mIsLoadingNotebooks = false;

            if (mOnNotebooksLoadedListener != null) {
                mOnNotebooksLoadedListener.onNotebooksLoaded(mNotebooks.size());
            }
        }
    }

}
