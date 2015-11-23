package com.leanote.android.ui.main;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.leanote.android.R;
import com.leanote.android.ui.LeaMainActivity;
import com.leanote.android.util.CoreEvents;
import com.leanote.android.widget.RecyclerItemDecoration;

import de.greenrobot.event.EventBus;

public class PostFragment extends Fragment
            implements LeaMainActivity.OnScrollToTopListener {


    private View mFabView;
    private int mFabTargetYTranslation;

    private RecyclerView mRecyclerView;

    private View mEmptyView;
    private ProgressBar mProgressLoadMore;
    private TextView mEmptyViewTitle;
    private ImageView mEmptyViewImage;

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

    @Override
    public void onResume() {
        super.onResume();

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
        return rootView;
    }

    private void showSitePicker() {
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

    //unused
    public void onEventMainThread(CoreEvents.MainViewPagerScrolled event) {
        mFabView.setTranslationY(mFabTargetYTranslation * event.mXOffset);
    }

}
