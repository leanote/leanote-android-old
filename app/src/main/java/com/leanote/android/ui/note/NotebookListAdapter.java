package com.leanote.android.ui.note;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.NotebookInfo;
import com.leanote.android.ui.ActivityLauncher;
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
    private List<Integer> mNotebookLevel = new ArrayList<Integer>();
    private List<Boolean> mHasExpanded = new ArrayList<Boolean>();
    private final List<NotebookInfo> mHiddenNotebooks = new ArrayList<>();

    private final LayoutInflater mLayoutInflater;

    //private final List<PostsListPost> mHiddenPosts = new ArrayList<>();

    private static final long ROW_ANIM_DURATION = 150;

    private static final int VIEW_TYPE_NOTEBOOK = 0;
    private static final int VIEW_TYPE_ENDLIST_INDICATOR = 1;
    private static final int VIEW_TYPE_MENU = 2;
    private static final int VIEW_TYPE_CHILD_NOTEBOOK = 3;

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
        return (position >= 0 && position <= mNotebooks.size());
    }

    @Override
    public int getItemViewType(int position) {

        if (position == (mNotebooks.size() + 1)) {
            return VIEW_TYPE_ENDLIST_INDICATOR;
        } else if (position == 0) {
            return VIEW_TYPE_MENU;
        }
        return mNotebookLevel.get(position-1).equals(0) ? VIEW_TYPE_NOTEBOOK : VIEW_TYPE_CHILD_NOTEBOOK;  //??
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
            return new NotebookAddViewHolder(new SearchToolbar(parent.getContext(), "Notebook"));
//        } else if (viewType == ) {

        } else {
            View view = mLayoutInflater.inflate(R.layout.notebookview, parent, false);
            return new NotebookViewHolder(view);
        }
    }


    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        // nothing to do if this is the static endlist indicator
        final int posType = getItemViewType(position);
        if (posType == VIEW_TYPE_ENDLIST_INDICATOR) {
            return;
        } else if (posType == VIEW_TYPE_MENU) {
            return;
        }

        final NotebookInfo notebook = mNotebooks.get(position - 1);
        // TODO: 2015/12/18
        //notebook.setTitle("test" + position);
        final Context context = holder.itemView.getContext();


        NotebookViewHolder postHolder = (NotebookViewHolder) holder;

        if (StringUtils.isNotEmpty(notebook.getTitle())) {
            String tempS = "";
            for (int i = 0; i < mNotebookLevel.get(position - 1); i++) tempS += "    ";
            postHolder.txtTitle.setText(tempS + notebook.getTitle());
        } else {
            postHolder.txtTitle.setText("(" + context.getResources().getText(R.string.untitled) + ")");
        }
        postHolder.arrow.setVisibility(View.VISIBLE);

        if (mNotebookLevel.get(position-1) > 0)
            ((CardView) postHolder.itemView).setCardBackgroundColor(0xFFFFFFFF - Math.min(0x888888, mNotebookLevel.get(position-1)*0x111111));

//            postHolder.txtDate.setText(notebook.getUpdateTime());  /////////////
//            postHolder.txtDate.setVisibility(View.VISIBLE);
//            postHolder.btnTrash.setButtonType(PostListButton.BUTTON_TRASH);

        configureNotebookButtons(postHolder, notebook);

        postHolder.arrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotebookInfo selectedNotebook = getItem(position - 1);

                if (mHasExpanded.get(position - 1)) {
                    int level = mNotebookLevel.get(position - 1) + 1;
                    for (int i = position; i < mNotebooks.size() && level == mNotebookLevel.get(position); ) {
                        mNotebooks.remove(position);
                        mNotebookLevel.remove(position);
                        mHasExpanded.remove(position);
                    }
                    mHasExpanded.set(position - 1, false);
                } else {
                    List<NotebookInfo> childNoteBook = Leanote.leaDB.getChildNotebookList(selectedNotebook);
                    if (childNoteBook.size() > 0) {
                        for (int i = 0; i < childNoteBook.size(); i++) {
                            mNotebooks.add(position + i, childNoteBook.get(i));
                            mNotebookLevel.add(position + i, mNotebookLevel.get(position - 1) + 1);
                            mHasExpanded.add(position + i, false);
                        }
                        mHasExpanded.set(position - 1, true);
                        ((NotebookViewHolder) holder).arrow.setImageResource(R.drawable.arrow_face_down);
                        ((NotebookViewHolder) holder).arrow.setVisibility(View.INVISIBLE);
                        Log.e("childs", "expanding");
                    }
                }

                notifyDataSetChanged();
                if (mOnNotebookSelectedListener != null && selectedNotebook != null) {
                    mOnNotebookSelectedListener.onNotebookSelected(selectedNotebook);
                }
            }
        });
        if (mHasExpanded.get(position-1))
            postHolder.arrow.setImageResource(R.drawable.arrow_face_down);
        else
            postHolder.arrow.setImageResource(R.drawable.arrow_face_left);


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NotebookInfo selectedNotebook = getItem(position - 1);
                // TODO: 2015/12/18
                //selectedNotebook.setTitle("clicked" + position);
                notifyDataSetChanged();
                if (mOnNotebookSelectedListener != null && selectedNotebook != null) {
                    mOnNotebookSelectedListener.onNotebookSelected(selectedNotebook);
                    ActivityLauncher.viewNoteFragmentForResult(
                            ((Leanote) Leanote.getContext().getApplicationContext()).getCurrentActivity(), selectedNotebook);
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final NotebookInfo selectedNotebook = getItem(position - 1);
                // TODO: 2015/12/18
                //selectedNotebook.setTitle("long clicked" + position);
                AlertDialog.Builder dialog = new AlertDialog.Builder(context);
                final CharSequence []items = {"Edit", "Trash"};
                final int[] type = {PostListButton.BUTTON_EDIT, PostListButton.BUTTON_TRASH};
                dialog = dialog.setTitle(selectedNotebook.getTitle());
                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //String selectedItem = items[which].toString();
                        int buttonType = type[which];
                        if (buttonType != -1 && mOnNotebookButtonClickListener != null) {
                            mOnNotebookButtonClickListener.onNotebookButtonClicked(buttonType, selectedNotebook);
                        }
                    }
                }).show();

                notifyDataSetChanged();
                if (mOnNotebookSelectedListener != null && selectedNotebook != null) {
                    mOnNotebookSelectedListener.onNotebookSelected(selectedNotebook);
                }
                return true;
            }
        });
    }


    private void configureNotebookButtons(final NotebookViewHolder holder, final NotebookInfo notebook) {
        // posts with local changes have preview rather than view button
//        holder.btnView.setButtonType(PostListButton.BUTTON_VIEW);

        holder.arrow.setVisibility(View.VISIBLE);
//        holder.btnEdit.setVisibility(View.VISIBLE);
//        holder.btnView.setVisibility(View.VISIBLE);
//        holder.btnTrash.setVisibility(View.VISIBLE);




        View.OnClickListener btnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int buttonType = ((PostListButton) view).getButtonType();
                if (mOnNotebookButtonClickListener != null) {
                    mOnNotebookButtonClickListener.onNotebookButtonClicked(buttonType, notebook);
                }
            }
        };

//        holder.btnEdit.setOnClickListener(btnClickListener);
//        holder.btnView.setOnClickListener(btnClickListener);
//        holder.btnTrash.setOnClickListener(btnClickListener);

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
//        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 0f);
//        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0f);
//        ObjectAnimator animOut = ObjectAnimator.ofPropertyValuesHolder(holder.layoutButtons, scaleX, scaleY);
//        animOut.setDuration(ROW_ANIM_DURATION);
//        animOut.setInterpolator(new AccelerateInterpolator());
//
//        animOut.addListener(new AnimatorListenerAdapter() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                // row 1
////                holder.btnEdit.setVisibility(showRow1 ? View.VISIBLE : View.GONE);
////                holder.btnView.setVisibility(showRow1 ? View.VISIBLE : View.GONE);
//                //holder.btnMore.setVisibility(showRow1 ? View.VISIBLE : View.GONE);
//                // row 2
//                //holder.btnStats.setVisibility(!showRow1 && canShowStatsForPost(note) ? View.VISIBLE : View.GONE);
////                holder.btnTrash.setVisibility(!showRow1 ? View.VISIBLE : View.GONE);
//                //holder.btnBack.setVisibility(!showRow1 ? View.VISIBLE : View.GONE);
//
//                PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, 0f, 1f);
//                PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, 0f, 1f);
//                ObjectAnimator animIn = ObjectAnimator.ofPropertyValuesHolder(holder.layoutButtons, scaleX, scaleY);
//                animIn.setDuration(ROW_ANIM_DURATION);
//                animIn.setInterpolator(new DecelerateInterpolator());
//                animIn.start();
//            }
//        });
//
//        animOut.start();
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
            if (mNotebooks.get(i).getNotebookId().equals(notebook.getNotebookId())) {
                position = i;
                break;
            }
        }

        if (position > -1) {
            mNotebooks.remove(position);
            mNotebookLevel.remove(position);
            mHasExpanded.remove(position);
            if (mNotebooks.size() > 0) {
                notifyItemRemoved(position+1);
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
        private final ImageView arrow;
//        private final TextView txtDate;
//
//        private final PostListButton btnEdit;
//        private final PostListButton btnView;
//        private final FrameLayout btnLayout;
//
//        private final PostListButton btnTrash;
//
//        private final ViewGroup layoutButtons;

        public NotebookViewHolder(View view) {
            super(view);

            txtTitle = (TextView) view.findViewById(R.id.text_title);
            arrow =  (ImageView) view.findViewById(R.id.notebook_arrow);
//            txtDate = (TextView) view.findViewById(R.id.text_date);
//
//            btnEdit = (PostListButton) view.findViewById(R.id.btn_edit);
//            btnView = (PostListButton) view.findViewById(R.id.btn_view);
//            btnLayout = (FrameLayout) view.findViewById(R.id.__framelayout__);
//
//            btnTrash = (PostListButton) view.findViewById(R.id.btn_trash);
//            layoutButtons = (ViewGroup) view.findViewById(R.id.layout_buttons);
        }
    }



    class EndListViewHolder extends RecyclerView.ViewHolder {
        public EndListViewHolder(View view) {
            super(view);
        }
    }

    class NotebookAddViewHolder extends RecyclerView.ViewHolder {
        private final SearchToolbar mSearchToolbar;
        public NotebookAddViewHolder(View itemView) {
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



    private class LoadNotebooksTask extends AsyncTask<Void, Void, Boolean> {
        private List<NotebookInfo> tmpNotebooks;
        private List<Integer> tmpNotebookLevel;
        private List<Boolean> tmpHasExpanded;

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

            tmpNotebooks = Leanote.leaDB.getRootNotebookList();
            tmpNotebookLevel = new ArrayList<Integer>();
            tmpHasExpanded = new ArrayList<Boolean>();
            AppLog.i("loading notebooks:" + tmpNotebooks);
            for (NotebookInfo hiddenNote : mHiddenNotebooks) {
                int index = -1;
                for (int i = 0; i < tmpNotebooks.size(); i++) {
                    if (tmpNotebooks.get(i).getNotebookId().equals(hiddenNote.getNotebookId())) {
                        index = i;
                        break;
                    }
                }
                tmpNotebooks.remove(index);
            }
            for (int i = 0; i < tmpNotebooks.size(); i++) {
                tmpNotebookLevel.add(0);
                tmpHasExpanded.add(false);
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                mNotebooks.clear();
                mNotebookLevel.clear();
                mHasExpanded.clear();
                mNotebooks.addAll(tmpNotebooks);
                mNotebookLevel.addAll(tmpNotebookLevel);
                mHasExpanded.addAll(tmpHasExpanded);
                notifyDataSetChanged();
            }

            mIsLoadingNotebooks = false;

            if (mOnNotebooksLoadedListener != null) {
                mOnNotebooksLoadedListener.onNotebooksLoaded(mNotebooks.size());
            }
        }
    }

}
