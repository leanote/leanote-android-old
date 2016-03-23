package com.leanote.android.ui.post;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.model.NoteDetailList;
import com.leanote.android.ui.ActivityLauncher;
import com.leanote.android.ui.note.NoteListAdapter;
import com.leanote.android.ui.note.SearchToolbar;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.Constant;
import com.leanote.android.util.DisplayUtils;
import com.leanote.android.widget.PostListButton;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yun on 11/23/15.
 */
public class PostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    public interface OnVisitBlogClickListener {
        void onVisitBlogButtonClicked();
    }

    public interface OnPostLoadedListener {
        void onPostLoaded(int postCount);
    }

    private OnVisitBlogClickListener mOnVisitBlogClickListener;


    private NoteListAdapter.OnNotesSelectedListener mOnNotesSelectedListener;
    private final LayoutInflater mLayoutInflater;
    private final int mPhotonWidth;
    private final int mPhotonHeight;
    private final int mEndlistIndicatorHeight;

    //private final boolean mIsStatsSupported;
    private final boolean mAlwaysShowAllButtons;
    private boolean mIsLoadingPosts;
    private NoteDetailList mPosts = new NoteDetailList();
    private final List<NoteDetail> mHiddenPosts = new ArrayList<>();

    private static final long ROW_ANIM_DURATION = 150;

    private static final int VIEW_TYPE_POST_OR_PAGE = 0;
    private static final int VIEW_TYPE_ENDLIST_INDICATOR = 1;
    private static final int VIEW_TYPE_SEARCH = 2;
    private static final int VIEW_TYPE_HOME_PAGE = 3;

    private PostAdapter.OnPostLoadedListener mOnPostLoadedListener;

    public void setmOnPostLoadedListener(OnPostLoadedListener mOnPostLoadedListener) {
        this.mOnPostLoadedListener = mOnPostLoadedListener;
    }

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

    public void setOnVisitBlogClickListener(OnVisitBlogClickListener listener) {
        mOnVisitBlogClickListener = listener;
    }

    public void setmOnNotesSelectedListener(NoteListAdapter.OnNotesSelectedListener mOnNotesSelectedListener) {
        this.mOnNotesSelectedListener = mOnNotesSelectedListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        AppLog.i("post size:" + getItemCount());

        if (getItemCount() == 0 || viewType == VIEW_TYPE_ENDLIST_INDICATOR) {
            View view = mLayoutInflater.inflate(R.layout.endlist_indicator, parent, false);
            view.getLayoutParams().height = mEndlistIndicatorHeight;
            return new EndListViewHolder(view);
        } else if (viewType == VIEW_TYPE_SEARCH) {
            return new SearchViewHolder(new SearchToolbar(parent.getContext(),
                    parent.getContext().getString(R.string.post), Constant.BLOG_SEARCH));

        } else if (viewType == VIEW_TYPE_HOME_PAGE) {
            View view = mLayoutInflater.inflate(R.layout.blog_home_page, parent, false);
            //view.getLayoutParams().height = mEndlistIndicatorHeight;
            return new ViewBlogHolder(view);
        } else{
            View view = mLayoutInflater.inflate(R.layout.post_cardview_link, parent, false);
            return new NoteViewHolder(view);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == (mPosts.size() + 2)) {
            return VIEW_TYPE_ENDLIST_INDICATOR;
        } else if (position == 0) {
            return VIEW_TYPE_SEARCH;
        }
          else if (position == 1) {
            return VIEW_TYPE_HOME_PAGE;
        }
        return VIEW_TYPE_POST_OR_PAGE;
    }


    private boolean isValidPostPosition(int position) {
        return (position >= 0 && position < mPosts.size());
    }

    private NoteDetail getItem(int position) {
        if (isValidPostPosition(position)) {
            return mPosts.get(position);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        if (mPosts.size() == 0) {
            return 0;
        } else {
            return mPosts.size()+3 ; // +1 for the endlist indicator
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        int posType = getItemViewType(position);

        if (posType == VIEW_TYPE_ENDLIST_INDICATOR) {
            return;
        }
        else if (posType == VIEW_TYPE_HOME_PAGE) {
            ViewBlogHolder viewBlogHolder = (ViewBlogHolder) holder;
            return;
        } else if (posType == VIEW_TYPE_SEARCH) {
            SearchViewHolder searchViewHolder = (SearchViewHolder) holder;
            //searchViewHolder.mSearchToolbar.getmSearchView().setOnQueryTextListener(new SearchChangeListener());
            return;
        }

        final NoteDetail note = mPosts.get(position - 2);  //not clear

        Context context = holder.itemView.getContext();

        if (holder instanceof NoteViewHolder) {
            NoteViewHolder postHolder = (NoteViewHolder) holder;

            if (StringUtils.isNotEmpty(note.getTitle())) {
                postHolder.txtTitle.setText(note.getTitle());
            } else {
                postHolder.txtTitle.setText("(" + context.getResources().getText(R.string.untitled) + ")");
            }

        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NoteDetail selectedNote = getItem(position - 2);
                if (mOnNotesSelectedListener != null && selectedNote != null) {
                    mOnNotesSelectedListener.onNotesSelected(selectedNote);
                }

            }
        });
    }


    public void loadPosts() {
        if (mIsLoadingPosts) {
            AppLog.d(AppLog.T.POSTS, "post adapter > already loading posts");
        } else {
            //load note
            new LoadPostsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private class LoadPostsTask extends AsyncTask<Void, Void, Boolean> {
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

            // make sure we don't return any hidden posts
            for (NoteDetail hiddenNote : mHiddenPosts) {
                int index = tmpNotes.indexOfPost(hiddenNote);
                tmpNotes.remove(index);
            }

            return true;

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                mPosts.clear();
                mPosts.addAll(tmpNotes);
                notifyDataSetChanged();
            }

            mIsLoadingPosts = false;
            if (mOnPostLoadedListener != null) {
                mOnPostLoadedListener.onPostLoaded(mPosts.size());
            }
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
            mSearchToolbar.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    ActivityLauncher.addNewNoteForResult(
                            ((Leanote) Leanote.getContext().getApplicationContext()).getCurrentActivity());

                    return true;
                }

            });
            mSearchToolbar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    ActivityLauncher.addNewNoteForResult(
                            ((Leanote) Leanote.getContext().getApplicationContext()).getCurrentActivity());

                }
            });
        }
    }


    class NoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtTitle;
        private final TextView txtDate;
        public NoteViewHolder(View view) {
            super(view);
            txtTitle = (TextView) view.findViewById(R.id.text_title);
            txtDate = (TextView) view.findViewById(R.id.text_date);

        }
    }

    class ViewBlogHolder extends RecyclerView.ViewHolder {
        private final PostListButton btnView;
        public ViewBlogHolder(View view) {
            super(view);
            btnView = (PostListButton) view.findViewById(R.id.btn_visit_blog);
            btnView.setVisibility(View.VISIBLE);
            View.OnClickListener viewBlogClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // handle back/more here, pass other actions to activity/fragment
//                    context =
//                    Intent intent = new Intent();
//                    intent.setClass(context,BlogHomeActivity.class);
//                    context.startActivity(intent);

                    mOnVisitBlogClickListener.onVisitBlogButtonClicked();
                }
            };
            btnView.setOnClickListener(viewBlogClickListener);

        }
    }


}
