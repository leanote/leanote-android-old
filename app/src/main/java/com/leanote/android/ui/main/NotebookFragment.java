package com.leanote.android.ui.main;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.leanote.android.Constants;
import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.NotebookInfo;
import com.leanote.android.networking.NetworkUtils;
import com.leanote.android.service.NoteSyncService;
import com.leanote.android.ui.ActivityLauncher;
import com.leanote.android.ui.EmptyViewMessageType;
import com.leanote.android.ui.note.NotebookListAdapter;
import com.leanote.android.ui.note.service.NoteEvents;
import com.leanote.android.ui.note.service.NoteUpdateService;
import com.leanote.android.util.AniUtils;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.SwipeToRefreshHelper;
import com.leanote.android.util.ToastUtils;
import com.leanote.android.widget.CustomSwipeRefreshLayout;
import com.leanote.android.widget.PostListButton;
import com.leanote.android.widget.RecyclerItemDecoration;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;

public class NotebookFragment extends Fragment
                implements NotebookListAdapter.OnNotebookButtonClickListener,
                NotebookListAdapter.OnNotebookSelectedListener,
                NotebookListAdapter.OnNotebooksLoadedListener,
                NoteSyncService.OnNotebooksSyncListener {

    private SwipeToRefreshHelper mSwipeToRefreshHelper;
    private NotebookListAdapter mNotebookListAdapter;
    private View mFabView;

    private RecyclerView mRecyclerView;

    private View mEmptyView;
    private ProgressBar mProgressLoadMore;
    private TextView mEmptyViewTitle;
    private ImageView mEmptyViewImage;

    private boolean mIsFetchingNotebooks;

    private final List<NotebookInfo> mTrashedNotes = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

    }

    public static NotebookFragment newInstance() {
        return new NotebookFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.note_list, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        mProgressLoadMore = (ProgressBar) view.findViewById(R.id.progress);
        mFabView = view.findViewById(R.id.fab_button);


        mEmptyView = view.findViewById(R.id.empty_view);
        mEmptyViewTitle = (TextView) mEmptyView.findViewById(R.id.title_empty);
        mEmptyViewImage = (ImageView) mEmptyView.findViewById(R.id.image_empty);

        Context context = getActivity();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        /////////////间隔所在之处
        mRecyclerView.addItemDecoration(new RecyclerItemDecoration(0, 0));
        int spacingVertical = context.getResources().getDimensionPixelSize(R.dimen.reader_card_gutters);
        int spacingHorizontal = context.getResources().getDimensionPixelSize(R.dimen.content_margin);
        //mRecyclerView.addItemDecoration(new RecyclerItemDecoration(spacingHorizontal, spacingVertical));

        mFabView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newNotebook();
            }
        });

        NoteSyncService.setNotebooksSyncListener(this);

        return view;
    }


    private void initSwipeToRefreshHelper() {
        mSwipeToRefreshHelper = new SwipeToRefreshHelper(
                getActivity(),
                (CustomSwipeRefreshLayout) getView().findViewById(R.id.ptr_layout),
                new SwipeToRefreshHelper.RefreshListener() {
                    @Override
                    public void onRefreshStarted() {
                        if (!isAdded()) {
                            return;
                        }
                        if (!NetworkUtils.checkConnection(getActivity())) {
                            setRefreshing(false);
                            updateEmptyView(EmptyViewMessageType.NETWORK_ERROR);
                            return;
                        }
                        //该方法拉取笔记后存在本地的db中，然后通过EventBus通知AsyncTask加载到页面中
                        requestNotes();
                    }
                });
    }

    public NotebookListAdapter getNotebookListAdapter() {
        if (mNotebookListAdapter == null) {
            mNotebookListAdapter = new NotebookListAdapter(getActivity());
            mNotebookListAdapter.setmOnNotebooksLoadedListener(this);
            mNotebookListAdapter.setmOnNotebookSelectedListener(this);
            mNotebookListAdapter.setmOnNotebookButtonClickListener(this);
        }

        return mNotebookListAdapter;
    }

    private boolean isNoteAdapterEmpty() {
        return (mNotebookListAdapter != null && mNotebookListAdapter.getItemCount() == 0);
    }

    private void loadNotebooks() {
        getNotebookListAdapter().loadNotebooks();
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);

        initSwipeToRefreshHelper();
    }

    private void newNotebook() {
        if (!isAdded()) return;

        ActivityLauncher.addNewNotebookForResult(getActivity());
    }

    public void onResume() {
        super.onResume();

        if (mRecyclerView.getAdapter() == null) {
            mRecyclerView.setAdapter(getNotebookListAdapter());
        }

        // always (re)load when resumed to reflect changes made elsewhere
        loadNotebooks();

        // scale in the fab after a brief delay if it's not already showing
        if (mFabView.getVisibility() != View.VISIBLE) {
            long delayMs = getResources().getInteger(R.integer.fab_animation_delay);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (isAdded()) {
                        AniUtils.scaleIn(mFabView, AniUtils.Duration.MEDIUM);
                    }
                }
            }, delayMs);
        }
    }


    private void setRefreshing(boolean refreshing) {
        mSwipeToRefreshHelper.setRefreshing(refreshing);
    }

    private void requestNotes() {
        if (!isAdded() || mIsFetchingNotebooks) {
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            updateEmptyView(EmptyViewMessageType.NETWORK_ERROR);
            return;
        }

        mIsFetchingNotebooks = true;

        NoteUpdateService.startServiceForNote(getActivity());

    }


    private void hideLoadMoreProgress() {
        if (mProgressLoadMore != null) {
            mProgressLoadMore.setVisibility(View.GONE);
        }
    }


    /*
     * PostUpdateService finished a request to retrieve new posts
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(NoteEvents.RequestNotes event) {
        Log.i("listen load note", ".....");
        mIsFetchingNotebooks = false;
        if (isAdded()) {
            setRefreshing(false);
            hideLoadMoreProgress();
            Log.i("is fail:", String.valueOf(event.getFailed()));
            if (!event.getFailed()) {
                loadNotebooks();
            } else {
                updateEmptyView(EmptyViewMessageType.GENERIC_ERROR);
            }
        }
    }

    private void updateEmptyView(EmptyViewMessageType emptyViewMessageType) {
        int stringId;
        switch (emptyViewMessageType) {
            case LOADING:
                stringId = R.string.notes_fetching;
                break;
            case NETWORK_ERROR:
                stringId = R.string.no_network_message;
                break;
            case GENERIC_ERROR:
                stringId = R.string.error_refresh_notes;
                break;
            default:
                return;
        }

        mEmptyViewTitle.setText(getText(stringId));
        mEmptyViewImage.setVisibility(emptyViewMessageType == EmptyViewMessageType.NO_CONTENT ? View.VISIBLE : View.GONE);
        mEmptyView.setVisibility(isNoteAdapterEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }



    /*
     * send the passed post to the trash with undo
     */
    private void trashNotebook(final NotebookInfo notebook) {
        if (!isAdded() || !NetworkUtils.checkConnection(getActivity())) {
            return;
        }

        //final Post fullPost = WordPress.wpDB.getPostForLocalTablePostId(note.getNoteId());

        // remove post from the list and add it to the list of trashed posts
        getNotebookListAdapter().hideNotebook(notebook);
        mTrashedNotes.add(notebook);

        View.OnClickListener undoListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // user undid the trash, so unhide the post and remove it from the list of trashed posts
                mTrashedNotes.remove(notebook);
                getNotebookListAdapter().unhideNotebook(notebook);
            }
        };

        // different undo text if this is a local draft since it will be deleted rather than trashed
//        String text = getString(R.string.note_trashed);

        Snackbar.make(getView().findViewById(R.id.coordinator), R.string.note_trashed, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, undoListener)
                .show();

        // wait for the undo snackbar to disappear before actually deleting the post
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // if the post no longer exists in the list of trashed posts it's because the
                // user undid the trash, so don't perform the deletion
                if (!mTrashedNotes.contains(notebook)) {
                    AppLog.d(AppLog.T.POSTS, "user undid trashing");
                    return;
                }

                //delete note in local
                Leanote.leaDB.deletenotebookInLocal(notebook.getNotebookId());
                //delete note in server
                new DeleteNotebookTask().execute(notebook.getNotebookId());
            }
        }, Constants.SNACKBAR_LONG_DURATION_MS);
    }

    @Override
    public void onNotebookButtonClicked(int buttonType, NotebookInfo notebook) {
        if (!isAdded()) return;

        //Post fullPost = WordPress.wpDB.getPostForLocalTablePostId(post.getPostId());
        //load note detail
        NotebookInfo fullNotebook = Leanote.leaDB.getLocalNotebookByNotebookId(notebook.getNotebookId());
        if (fullNotebook == null) {
            ToastUtils.showToast(getActivity(), R.string.note_not_found);
            return;
        }

        switch (buttonType) {
            case PostListButton.BUTTON_EDIT:
                ActivityLauncher.editNotebookForResult(getActivity(), fullNotebook.getId());
                break;
            case PostListButton.BUTTON_VIEW:
            case PostListButton.BUTTON_PREVIEW:
                //ActivityLauncher.viewNotebookForResult(getActivity(), fullNotebook.getId());
                break;
            case PostListButton.BUTTON_TRASH:
            case PostListButton.BUTTON_DELETE:
                // prevent deleting post while it's being uploaded
                trashNotebook(fullNotebook);
                break;
        }

    }

    @Override
    public void onNotebookSelected(NotebookInfo note) {
        onNotebookButtonClicked(PostListButton.BUTTON_PREVIEW, note);
    }

    @Override
    public void onNotebooksLoaded(int notebookCount) {
        if (!isAdded()) {
            return;
        }

        if (notebookCount == 0 && !mIsFetchingNotebooks) {
            if (NetworkUtils.isNetworkAvailable(getActivity())) {
                updateEmptyView(EmptyViewMessageType.NO_CONTENT);
            } else {
                updateEmptyView(EmptyViewMessageType.NETWORK_ERROR);
            }
        } else if (notebookCount > 0) {
            mEmptyView.setVisibility(View.GONE);

        }

    }

    @Override
    public void onNotebooksPullDone(Boolean result) {
        if (result) {
            loadNotebooks();
            ToastUtils.showToast(getActivity(), getString(R.string.save_notebook_succ));
        } else {
            ToastUtils.showToast(getActivity(), getString(R.string.save_notebook_fail));
        }

    }

    private class DeleteNotebookTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);


        }

        @Override
        protected Boolean doInBackground(String... params) {
            String notebookiId = params[0];
            if (TextUtils.isEmpty(notebookiId)) {
                ToastUtils.showToast(getActivity(), getString(R.string.notebook_not_found));
                return false;
            }


            return false;
        }
    }

}
