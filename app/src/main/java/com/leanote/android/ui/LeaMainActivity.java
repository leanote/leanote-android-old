package com.leanote.android.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.model.NoteDetail;
import com.leanote.android.networking.NetworkUtils;
import com.leanote.android.service.NoteSyncService;
import com.leanote.android.ui.main.LeaMainTabAdapter;
import com.leanote.android.ui.main.LeaMainTabLayout;
import com.leanote.android.ui.note.service.NoteUploadService;
import com.leanote.android.util.AniUtils;
import com.leanote.android.util.CoreEvents;
import com.leanote.android.widget.LeaViewPager;

import de.greenrobot.event.EventBus;
import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by binnchx on 8/26/15.
 */
public class LeaMainActivity extends AppCompatActivity {

    private LeaViewPager mViewPager;
    private LeaMainTabLayout mTabLayout;
    private LeaMainTabAdapter mTabAdapter;
    private TextView mConnectionBar;


    public static final String ARG_OPENED_FROM_PUSH = "opened_from_push";


    private SensorManager sensorManager;
    private Vibrator vibrator;

    private static final String TAG = "TestSensorActivity";
    private static final int SENSOR_SHAKE = 10;
    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SENSOR_SHAKE:
                    //NoteSyncService.sendNotebookChanges();  //会导致出错

                    List<NoteDetail> dirtyNotes = Leanote.leaDB.getDirtyNotes();
                    for (NoteDetail note : dirtyNotes) {
                        NoteUploadService.addNoteToUpload(note);
                    }
                    startService(new Intent(LeaMainActivity.this, NoteUploadService.class));
                    //Toast.makeText(LeaMainActivity.this, "同步中", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "检测到摇晃，执行操作！");
                    break;
            }
        }

    };

    static long time = 0;
    static boolean allowShake() {
        long tempTime = System.currentTimeMillis();
        long dTime = tempTime - time;
        if (dTime < 0 || dTime > 800) {
            time = tempTime;
            return true;
        }
        return false;
    }

    private SensorEventListener sensorEventListener = new SensorEventListener() {

        @Override
        public void onSensorChanged(SensorEvent event) {
            // 传感器信息改变时执行该方法
            float[] values = event.values;
            float x = values[0]; // x轴方向的重力加速度，向右为正
            float y = values[1]; // y轴方向的重力加速度，向前为正
            float z = values[2]; // z轴方向的重力加速度，向上为正
            Log.i(TAG, "x轴方向的重力加速度" + x +  "；y轴方向的重力加速度" + y +  "；z轴方向的重力加速度" + z);
            // 一般在这三个方向的重力加速度达到40就达到了摇晃手机的状态。
            int medumValue = 19;// 三星 i9250怎么晃都不会超过20，没办法，只设置19了


            if (!allowShake()) return;

            if (Math.abs(x) > medumValue || Math.abs(y) > medumValue || Math.abs(z) > medumValue) {
                vibrator.vibrate(200);
                Message msg = new Message();
                msg.what = SENSOR_SHAKE;
                handler.sendMessage(msg);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    public interface OnScrollToTopListener {
        void onScrollToTop();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setStatusBarColor() {
    }


    private Menu mOptionsMenu;  //应该是整个menu文件夹
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mOptionsMenu = menu;
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);

//        MenuItem searchItem = menu.findItem(R.id.searchBar);
//        MenuItemCompat.setOnActionExpandListener(searchItem, new MenuItemCompat.OnActionExpandListener() {
//            @Override
//            public boolean onMenuItemActionCollapse(MenuItem item) {
//                // Do something when collapsed
//                Toast.makeText(LeaMainActivity.this, "hello expand", Toast.LENGTH_LONG).show();
//                return true;  // Return true to collapse action view
//            }
//
//            @Override
//            public boolean onMenuItemActionExpand(MenuItem item) {
//                // Do something when expanded
//                Toast.makeText(LeaMainActivity.this, "hello expand", Toast.LENGTH_LONG).show();
//                return true;  // Return true to expand action view
//            }
//
//        });
        //http://www.bubuko.com/infodetail-662767.html

        //http://www.cnblogs.com/yc-755909659/p/4290784.html  点击事件参考
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.searchBar:
                //Toast.makeText(this, "你点击了“搜索”按键！", Toast.LENGTH_SHORT).show();
                ActivityLauncher.startSearchForResult(LeaMainActivity.this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    public void setRefreshActionButtonState(boolean refreshing) {
//        if (mOptionsMenu == null) {
//            return;
//        }
//
//        final MenuItem searchItem = mOptionsMenu.findItem(R.id.searchBar);
//        if (searchItem != null) {
//            if (refreshing) {
//                MenuItemCompat.setActionView(searchItem, R.layout.meaddlayout);
//            } else {
//                MenuItemCompat.setActionView(searchItem, null);
//            }
//        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.status_bar_tint));
            super.onCreate(savedInstanceState);
        }


        setContentView(R.layout.main_activity);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Leanote");

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

                String[] mPageTitle = {"Recent Notes", "Blog", "Notebook", "About me"};
                LeaMainActivity.this.getSupportActionBar().setTitle(mPageTitle[tab.getPosition()]);

                if (mOptionsMenu != null) {
                    mOptionsMenu.clear();
                    if (tab.getPosition() < 3)
                        getMenuInflater().inflate(R.menu.toolbar, mOptionsMenu);
                }
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



        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    @Override
    protected void onDestroy() {
        clearReferences();
        super.onDestroy();
    }

    private void clearReferences() {
        Leanote lea = (Leanote) this.getApplicationContext();
        Activity currActivity = lea.getCurrentActivity();
        if (this.equals(currActivity))
            lea.setCurrentActivity(null);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.ADD_ACCOUNT && resultCode == RESULT_OK) {
            resetFragments();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Leanote app = (Leanote)this.getApplicationContext();
        app.setCurrentActivity(this);


        if (sensorManager != null) {// 注册监听器
            sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            // 第一个参数是Listener，第二个参数是所得传感器类型，第三个参数值获取传感器信息的频率
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

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


    @SuppressWarnings("unused")
    public void onEventMainThread(CoreEvents.UserSignedOutCompletely event) {
        ActivityLauncher.showSignInForResult(this);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CoreEvents.UserSignedOutWordPressCom event) {
        resetFragments();
    }



}