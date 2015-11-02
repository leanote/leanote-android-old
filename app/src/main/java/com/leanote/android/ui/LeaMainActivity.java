package com.leanote.android.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.leanote.android.R;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.networking.NetworkUtils;
import com.leanote.android.ui.main.LeaMainTabAdapter;
import com.leanote.android.ui.main.LeaMainTabLayout;
import com.leanote.android.util.AniUtils;
import com.leanote.android.util.CoreEvents;
import com.leanote.android.widget.LeaViewPager;

import de.greenrobot.event.EventBus;

/**
 * Created by binnchx on 8/26/15.
 */
public class LeaMainActivity extends Activity {

    private LeaViewPager mViewPager;
    private LeaMainTabLayout mTabLayout;
    private LeaMainTabAdapter mTabAdapter;
    private TextView mConnectionBar;

    public static final String ARG_OPENED_FROM_PUSH = "opened_from_push";


    public interface OnScrollToTopListener {
        void onScrollToTop();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_tint));
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStatusBarColor();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        mViewPager = (LeaViewPager) findViewById(R.id.viewpager_main);
        mTabAdapter = new LeaMainTabAdapter(getFragmentManager());
        mViewPager.setAdapter(mTabAdapter);

        mConnectionBar = (TextView) findViewById(R.id.connection_bar);
        mConnectionBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // slide out the bar on click, then re-check connection after a brief delay
                AniUtils.animateBottomBar(mConnectionBar, false);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isFinishing()) {
                            checkConnection();
                        }
                    }
                }, 2000);
            }
        });
        mTabLayout = (LeaMainTabLayout) findViewById(R.id.tab_layout);
        mTabLayout.createTabs();

        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //  nop
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // scroll the active fragment to the top, if available
                Fragment fragment = mTabAdapter.getFragment(tab.getPosition());
                if (fragment instanceof OnScrollToTopListener) {
                    ((OnScrollToTopListener) fragment).onScrollToTop();
                }
            }
        });

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                AppPrefs.setMainTabIndex(position);

//                switch (position) {
//                    case LeaMainTabAdapter.TAB_NOTIFS:
//                        if (getNotificationListFragment() != null) {
//                            getNotificationListFragment().updateLastSeenTime();
//                            mTabLayout.showNoteBadge(false);
//                        }
//                        break;
//                }
                trackLastVisibleTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // noop
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                // fire event if the "My Site" page is being scrolled so the fragment can
                // animate its fab to match
                if (position == LeaMainTabAdapter.TAB_NOTE) {
                    EventBus.getDefault().post(new CoreEvents.MainViewPagerScrolled(positionOffset));
                }
            }
        });

        if (savedInstanceState == null) {
            if (AccountHelper.isSignedIn()) {
                // open note detail if activity called from a push, otherwise return to the tab
                // that was showing last time
                boolean openedFromPush = (getIntent() != null && getIntent().getBooleanExtra(ARG_OPENED_FROM_PUSH,
                        false));

                if (openedFromPush) {
                    getIntent().putExtra(ARG_OPENED_FROM_PUSH, false);
                    //launchWithNoteId();
                } else {
                    int position = AppPrefs.getMainTabIndex();
                    if (mTabAdapter.isValidPosition(position) && position != mViewPager.getCurrentItem()) {
                        mViewPager.setCurrentItem(position);
                    }
                }
            } else {
                ActivityLauncher.showSignInForResult(this);
            }
        }
//        if (AccountUtils.isSignedIn()) {
//
//
//        } else {
//            ActivityLauncher.showSignInForResult(this);
//        }

        //ActivityLauncher.showSignInForResult(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.ADD_ACCOUNT && resultCode == RESULT_OK) {
            resetFragments();
        }
    }

    private void resetFragments() {
        int position = mViewPager.getCurrentItem();
        mTabAdapter = new LeaMainTabAdapter(getFragmentManager());
        mViewPager.setAdapter(mTabAdapter);

        // restore previous position
        if (mTabAdapter.isValidPosition(position)) {
            mViewPager.setCurrentItem(position);
        }
    }


    private void trackLastVisibleTab(int position) {
        switch (position) {
            case LeaMainTabAdapter.TAB_NOTE:
                ActivityId.trackLastActivity(ActivityId.NOTE);
                //AnalyticsTracker.track(AnalyticsTracker.Stat.MY_SITE_ACCESSED);
                break;
            case LeaMainTabAdapter.TAB_POST:
                ActivityId.trackLastActivity(ActivityId.POST);
                //AnalyticsTracker.track(AnalyticsTracker.Stat.READER_ACCESSED);
                break;
            case LeaMainTabAdapter.TAB_CATEGORY:
                ActivityId.trackLastActivity(ActivityId.NOTIFICATIONS);
                //AnalyticsTracker.track(AnalyticsTracker.Stat.NOTIFICATIONS_ACCESSED);
                break;
            case LeaMainTabAdapter.TAB_ME:
                ActivityId.trackLastActivity(ActivityId.ME);
                //AnalyticsTracker.track(AnalyticsTracker.Stat.ME_ACCESSED);
                break;
            default:
                break;
        }
    }

    private void checkConnection() {
        updateConnectionBar(NetworkUtils.isNetworkAvailable(this));
    }

    private void updateConnectionBar(boolean isConnected) {
        if (isConnected && mConnectionBar.getVisibility() == View.VISIBLE) {
            AniUtils.animateBottomBar(mConnectionBar, false);
        } else if (!isConnected && mConnectionBar.getVisibility() != View.VISIBLE) {
            AniUtils.animateBottomBar(mConnectionBar, true);
        }
    }

}