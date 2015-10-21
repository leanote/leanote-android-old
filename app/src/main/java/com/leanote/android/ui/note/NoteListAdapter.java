package com.leanote.android.ui.note;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.model.NoteDetailList;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.DisplayUtils;
import com.leanote.android.widget.PostListButton;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binnchx on 10/18/15.
 */
public class NoteListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnPostButtonClickListener {
        void onPostButtonClicked(int buttonId, NoteDetail note);
    }

    //private OnLoadMoreListener mOnLoadMoreListener;
    private OnPostsLoadedListener mOnPostsLoadedListener;
    private OnPostSelectedListener mOnPostSelectedListener;
    private OnPostButtonClickListener mOnPostButtonClickListener;

    private final int mPhotonWidth;
    private final int mPhotonHeight;
    private final int mEndlistIndicatorHeight;

    //private final boolean mIsStatsSupported;
    private final boolean mAlwaysShowAllButtons;

    private boolean mIsLoadingPosts;

    private final NoteDetailList mNotes = new NoteDetailList();
    private final List<NoteDetail> mHiddenNotes = new ArrayList<>();

    private final LayoutInflater mLayoutInflater;

    //private final List<PostsListPost> mHiddenPosts = new ArrayList<>();

    private static final long ROW_ANIM_DURATION = 150;

    private static final int VIEW_TYPE_POST_OR_PAGE = 0;
    private static final int VIEW_TYPE_ENDLIST_INDICATOR = 1;

    public NoteListAdapter(Context context) {
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

//    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
//        mOnLoadMoreListener = listener;
//    }

    public void setOnPostsLoadedListener(OnPostsLoadedListener listener) {
        mOnPostsLoadedListener = listener;
    }

    public void setOnPostSelectedListener(OnPostSelectedListener listener) {
        mOnPostSelectedListener = listener;
    }

    public void setOnPostButtonClickListener(OnPostButtonClickListener listener) {
        mOnPostButtonClickListener = listener;
    }

    private NoteDetail getItem(int position) {
        if (isValidPostPosition(position)) {
            return mNotes.get(position);
        }
        return null;
    }

    private boolean isValidPostPosition(int position) {
        return (position >= 0 && position < mNotes.size());
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mNotes.size()) {
            return VIEW_TYPE_ENDLIST_INDICATOR;
        }
        return VIEW_TYPE_POST_OR_PAGE;
    }

    @Override
    public int getItemCount() {
        if (mNotes.size() == 0) {
            return 0;
        } else {
            return mNotes.size() + 1; // +1 for the endlist indicator
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ENDLIST_INDICATOR) {
            View view = mLayoutInflater.inflate(R.layout.endlist_indicator, parent, false);
            view.getLayoutParams().height = mEndlistIndicatorHeight;
            return new EndListViewHolder(view);
        } else {
            View view = mLayoutInflater.inflate(R.layout.post_cardview, parent, false);
            return new NoteViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        // nothing to do if this is the static endlist indicator
        if (getItemViewType(position) == VIEW_TYPE_ENDLIST_INDICATOR) {
            return;
        }

        final NoteDetail note = mNotes.get(position);
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

            configurePostButtons(postHolder, note);
        }

        // load more posts when we near the end
//        if (mOnLoadMoreListener != null && position >= mPosts.size() - 1
//                && position >= PostsListFragment.POSTS_REQUEST_COUNT - 1) {
//            mOnLoadMoreListener.onLoadMore();
//        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NoteDetail selectedNote = getItem(position);
                if (mOnPostSelectedListener != null && selectedNote != null) {
                    mOnPostSelectedListener.onPostSelected(selectedNote);
                }
            }
        });
    }

    /*
     * returns the caption to show in the date header for the passed page - pages with the same
     * caption will be grouped together
     *  - if page is local draft, returns "Local draft"
     *  - if page is scheduled, returns formatted date w/o time
     *  - if created today or yesterday, returns "Today" or "Yesterday"
     *  - if created this month, returns the number of days ago
     *  - if created this year, returns the month name
     *  - if created before this year, returns the month name with year
     */
//    private static String getPageDateHeaderText(Context context, PostsListPost page) {
//        if (page.isLocalDraft()) {
//            return context.getString(R.string.local_draft);
//        } else if (page.getStatusEnum() == PostStatus.SCHEDULED) {
//            return DateUtils.formatDateTime(context, page.getDateCreatedGmt(), DateUtils.FORMAT_ABBREV_ALL);
//        } else {
//            Date dtCreated = new Date(page.getDateCreatedGmt());
//            Date dtNow = DateTimeUtils.nowUTC();
//            int daysBetween = DateTimeUtils.daysBetween(dtCreated, dtNow);
//            if (daysBetween == 0) {
//                return context.getString(R.string.today);
//            } else if (daysBetween == 1) {
//                return context.getString(R.string.yesterday);
//            } else if (DateTimeUtils.isSameMonthAndYear(dtCreated, dtNow)) {
//                return String.format(context.getString(R.string.days_ago), daysBetween);
//            } else if (DateTimeUtils.isSameYear(dtCreated, dtNow)) {
//                return new SimpleDateFormat("MMMM").format(dtCreated);
//            } else {
//                return new SimpleDateFormat("MMMM yyyy").format(dtCreated);
//            }
//        }
//    }

    /*
     * user tapped "..." next to a page, show a popup menu of choices
     */
//    private void showPagePopupMenu(View view, final PostsListPost page) {
//        Context context = view.getContext();
//        final ListPopupWindow listPopup = new ListPopupWindow(context);
//        listPopup.setAnchorView(view);
//
//        listPopup.setWidth(context.getResources().getDimensionPixelSize(R.dimen.menu_item_width));
//        listPopup.setModal(true);
//        listPopup.setAdapter(new PageMenuAdapter(context, page));
//        listPopup.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                listPopup.dismiss();
//                if (mOnPostButtonClickListener != null) {
//                    int buttonId = (int) id;
//                    mOnPostButtonClickListener.onPostButtonClicked(buttonId, page);
//                }
//            }
//        });
//        listPopup.show();
//    }



    private void configurePostButtons(final NoteViewHolder holder,
                                      final NoteDetail note) {
        // posts with local changes have preview rather than view button
        holder.btnView.setButtonType(PostListButton.BUTTON_VIEW);

        //boolean canShowStatsButton = canShowStatsForPost(post);
        //int numVisibleButtons = (canShowStatsButton ? 4 : 3);
        int numVisibleButtons = 3;
        // edit / view are always visible

        holder.btnEdit.setVisibility(View.VISIBLE);
        holder.btnView.setVisibility(View.VISIBLE);
        holder.btnTrash.setVisibility(View.VISIBLE);

        // if we have enough room to show all buttons, hide the back/more buttons and show stats/trash
//        if (mAlwaysShowAllButtons || numVisibleButtons <= 3) {
//            holder.btnMore.setVisibility(View.GONE);
//            holder.btnBack.setVisibility(View.GONE);
//            holder.btnTrash.setVisibility(View.VISIBLE);
//            holder.btnStats.setVisibility(canShowStatsButton ? View.VISIBLE : View.GONE);
//        } else {
//            holder.btnMore.setVisibility(View.VISIBLE);
//            holder.btnBack.setVisibility(View.GONE);
//            holder.btnTrash.setVisibility(View.GONE);
//            holder.btnStats.setVisibility(View.GONE);
//        }

        View.OnClickListener btnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // handle back/more here, pass other actions to activity/fragment
                int buttonType = ((PostListButton) view).getButtonType();
                switch (buttonType) {
                    case PostListButton.BUTTON_MORE:
                        animateButtonRows(holder, note, false);
                        break;
                    case PostListButton.BUTTON_BACK:
                        animateButtonRows(holder, note, true);
                        break;
                    default:
                        if (mOnPostButtonClickListener != null) {
                            mOnPostButtonClickListener.onPostButtonClicked(buttonType, note);
                        }
                        break;
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
    private void animateButtonRows(final NoteViewHolder holder,
                                   final NoteDetail note,
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

    public void loadNotes() {
        if (mIsLoadingPosts) {
            AppLog.d(AppLog.T.POSTS, "post adapter > already loading posts");
        } else {
            //load note
            new LoadNotesTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    /*
     * hides the post - used when the post is trashed by the user but the network request
     * to delete the post hasn't completed yet
     */
    public void hidePost(NoteDetail note) {
        mHiddenNotes.add(note);

        int position = mNotes.indexOfPost(note);
        if (position > -1) {
            mNotes.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void unhidePost(NoteDetail note) {
        if (mHiddenNotes.remove(note)) {
            loadNotes();
        }
    }

//    public interface OnLoadMoreListener {
//        void onLoadMore();
//    }

    public interface OnPostSelectedListener {
        void onPostSelected(NoteDetail note);
    }

    public interface OnPostsLoadedListener {
        void onPostsLoaded(int postCount);
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



    class EndListViewHolder extends RecyclerView.ViewHolder {
        public EndListViewHolder(View view) {
            super(view);
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
            //Leanote.getUserID();
            //5620f9af38f411671600050d
            tmpNotes = Leanote.leaDB.getNotesList(Leanote.getUserID());
            Log.i("load notes from local:", String.valueOf(tmpNotes.size()));
            // make sure we don't return any hidden posts
            for (NoteDetail hiddenNote : mHiddenNotes) {
                tmpNotes.remove(hiddenNote);
            }

//            List<NoteDetail> notes = new ArrayList<>();
//            NoteDetail note = new NoteDetail();
//            Random rand = new Random();
//            note.setTitle(String.valueOf(rand.nextInt(100)));
//            note.setUpdatedTime("2015/10/20");
//            notes.add(note);
//
//            tmpNotes.addAll(notes);

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