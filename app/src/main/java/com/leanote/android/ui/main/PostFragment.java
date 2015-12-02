package com.leanote.android.ui.main;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.leanote.android.R;
import com.leanote.android.networking.NetworkUtils;
import com.leanote.android.ui.ActivityLauncher;
import com.leanote.android.ui.EmptyViewMessageType;
import com.leanote.android.ui.LeaMainActivity;

import com.leanote.android.ui.note.service.NoteEvents;

import com.leanote.android.ui.note.service.NoteUpdateService;
import com.leanote.android.ui.post.PostAdapter;
import com.leanote.android.util.SwipeToRefreshHelper;
import com.leanote.android.widget.CustomSwipeRefreshLayout;
import com.leanote.android.widget.RecyclerItemDecoration;

import de.greenrobot.event.EventBus;

public class PostFragment extends Fragment
            implements LeaMainActivity.OnScrollToTopListener,
            PostAdapter.OnVisitBlogClickListener{


    private View mFabView;
    private int mFabTargetYTranslation;

    private RecyclerView mRecyclerView;

    private View mEmptyView;
    private ProgressBar mProgressLoadMore;
    private TextView mEmptyViewTitle;
    private SwipeToRefreshHelper mSwipeToRefreshHelper;
    private PostAdapter mNoteListAdapter;
    private boolean mIsFetchingPostList;

    private ImageView mEmptyViewImage;

    private boolean mIsFetchingPosts;

    public static PostFragment newInstance() {
        return new PostFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    public PostAdapter getNoteListAdapter() {
        if (mNoteListAdapter == null) {
            mNoteListAdapter = new PostAdapter(getActivity());
            mNoteListAdapter.setOnVisitBlogClickListener((PostAdapter.OnVisitBlogClickListener) this);
        }
        return mNoteListAdapter;
    }

    private void loadNotes() {
        getNoteListAdapter().loadNotes();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mRecyclerView.getAdapter() == null) {
            mRecyclerView.setAdapter(getNoteListAdapter());
        }
        loadNotes();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.post_fragment, container, false);

        int fabHeight = getResources().getDimensionPixelSize(R.dimen.fab_size_normal);
        int fabMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        mFabTargetYTranslation = (fabHeight + fabMargin) * 2;
        mFabView = rootView.findViewById(R.id.fab_button);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);

        mProgressLoadMore = (ProgressBar) rootView.findViewById(R.id.progress);
        mEmptyView = rootView.findViewById(R.id.empty_view);
        mEmptyViewTitle = (TextView) mEmptyView.findViewById(R.id.title_empty);
        mEmptyViewImage = (ImageView) mEmptyView.findViewById(R.id.image_empty);

        Context context = getActivity();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        int spacingVertical = context.getResources().getDimensionPixelSize(R.dimen.reader_card_gutters);
        int spacingHorizontal = context.getResources().getDimensionPixelSize(R.dimen.content_margin);
        mRecyclerView.addItemDecoration(new RecyclerItemDecoration(spacingHorizontal, spacingVertical));

        //mEmptyViewImage.setVisibility(View.VISIBLE);
        //mEmptyView.setVisibility(View.VISIBLE);



        return rootView;
    }

    private void showSitePicker() {
    }

    private void setRefreshing(boolean refreshing) {
        mSwipeToRefreshHelper.setRefreshing(refreshing);
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

                        requestPosts();


                    }
                });
    }


    private void requestPosts() {
        if (!isAdded() || mIsFetchingPosts) {

            return;
        }

        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            updateEmptyView(EmptyViewMessageType.NETWORK_ERROR);
            return;
        }


        mIsFetchingPosts = true;


        NoteUpdateService.startServiceForNote(getActivity());

    }

    @Override
    public void onActivityCreated(Bundle bundle){
        super.onActivityCreated(bundle);

        initSwipeToRefreshHelper();
//        List<String> localNotebookIds = Leanote.leaDB.getNoteisBlogIds();
//        Log.v("postIds", localNotebookIds.toString());
        

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showAlert(View view) {

    }

    private void refreshBlogDetails() {

    }


    @Override
    public void onScrollToTop() {
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }


    private void hideLoadMoreProgress() {
        if (mProgressLoadMore != null) {
            mProgressLoadMore.setVisibility(View.GONE);
        }
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(NoteEvents.RequestNotes event) {
        Log.i("listen load note", ".....");
        mIsFetchingPosts = false;
        if (isAdded()) {
            setRefreshing(false);
            hideLoadMoreProgress();
            Log.i("is fail:", String.valueOf(event.getFailed()));
            if (!event.getFailed()) {
                loadNotes();
            } else {
//                ApiHelper.ErrorType errorType = event.getErrorType();
//                if (errorType != null && errorType != ErrorType.TASK_CANCELLED && errorType != ErrorType.NO_ERROR) {
//                    switch (errorType) {
//                        case UNAUTHORIZED:
//                            updateEmptyView(EmptyViewMessageType.PERMISSION_ERROR);
//                            break;
//                        default:
//                            updateEmptyView(EmptyViewMessageType.GENERIC_ERROR);
//                            break;
//                    }
//                }
                updateEmptyView(EmptyViewMessageType.GENERIC_ERROR);
            }
        }
    }

    private boolean isPostAdapterEmpty() {
        //return (mNoteListAdapter != null && mNoteListAdapter.getItemCount() == 0);    //--add this
        return false;
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
        mEmptyView.setVisibility(isPostAdapterEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onVisitBlogButtonClicked() {
        ActivityLauncher.visitBlog(getActivity());
    }
}
