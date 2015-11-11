package com.leanote.android.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.leanote.android.R;
import com.leanote.android.ui.main.MeFragment;
import com.leanote.android.ui.main.NoteListFragment;
import com.leanote.android.ui.main.NotebookFragment;
import com.leanote.android.ui.main.PostFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binnchx on 11/4/15.
 */
public class LeaMainActivity2 extends FragmentActivity {
    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List mFragments = new ArrayList();


    /**
     * 底部四个按钮
     */
    private LinearLayout mTabBtnNote;
    private LinearLayout mTabBtnNotebook;
    private LinearLayout mTabBtnPost;
    private LinearLayout mTabBtnMe;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mViewPager = (ViewPager) findViewById(R.id.id_viewpager);

        initView();

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager())
        {

            @Override
            public int getCount()
            {
                return mFragments.size();
            }

            @Override
            public Fragment getItem(int arg0)
            {
                return (Fragment) mFragments.get(arg0);
            }
        };

        mViewPager.setAdapter(mAdapter);


        mViewPager.setOnPageChangeListener(new OnPageChangeListener()
        {

            private int currentIndex;

            @Override
            public void onPageSelected(int position)
            {
                resetTabBtn();
                switch (position)
                {
                    case 0:
                        ((ImageButton) mTabBtnNote.findViewById(R.id.btn_tab_bottom_note))
                                .setImageResource(R.drawable.ic_tab_note_pressed);
                        break;
                    case 1:
                        ((ImageButton) mTabBtnNotebook.findViewById(R.id.btn_tab_bottom_notebook))
                                .setImageResource(R.drawable.ic_tab_category_pressed);
                        break;
                    case 2:
                        ((ImageButton) mTabBtnPost.findViewById(R.id.btn_tab_bottom_post))
                                .setImageResource(R.drawable.ic_tab_note_pressed);
                        break;
                    case 3:
                        ((ImageButton) mTabBtnMe.findViewById(R.id.btn_tab_bottom_me))
                                .setImageResource(R.drawable.ic_tab_me_pressed);
                        break;
                }

                currentIndex = position;
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2)
            {

            }

            @Override
            public void onPageScrollStateChanged(int arg0)
            {
            }
        });

    }

    protected void resetTabBtn()
    {
        ((ImageButton) mTabBtnNote.findViewById(R.id.btn_tab_bottom_note))
                .setImageResource(R.drawable.ic_tab_note_normal);
        ((ImageButton) mTabBtnNotebook.findViewById(R.id.btn_tab_bottom_notebook))
                .setImageResource(R.drawable.ic_tab_category_normal);
        ((ImageButton) mTabBtnPost.findViewById(R.id.btn_tab_bottom_post))
                .setImageResource(R.drawable.ic_tab_note_normal);
        ((ImageButton) mTabBtnMe.findViewById(R.id.btn_tab_bottom_me))
                .setImageResource(R.drawable.ic_tab_me_normal);
    }

    private void initView() {

        mTabBtnNote = (LinearLayout) findViewById(R.id.id_tab_bottom_note);
        mTabBtnNote.setOnClickListener(new TabOnclickListener(0));

        mTabBtnNotebook = (LinearLayout) findViewById(R.id.id_tab_bottom_notebook);
        mTabBtnNotebook.setOnClickListener(new TabOnclickListener(1));

        mTabBtnPost = (LinearLayout) findViewById(R.id.id_tab_bottom_post);
        mTabBtnPost.setOnClickListener(new TabOnclickListener(2));

        mTabBtnMe = (LinearLayout) findViewById(R.id.id_tab_bottom_me);
        mTabBtnMe.setOnClickListener(new TabOnclickListener(3));

        NoteListFragment noteTab = new NoteListFragment();
        NotebookFragment notebookTab = new NotebookFragment();
        PostFragment postTab = new PostFragment();
        MeFragment meTab = new MeFragment();

        mFragments.add(noteTab);
        mFragments.add(notebookTab);
        mFragments.add(postTab);
        mFragments.add(meTab);

    }


    private class TabOnclickListener implements View.OnClickListener {

        private int index = 0;

        public TabOnclickListener(int i) {
            this.index = i;
        }
        @Override
        public void onClick(View v) {
            resetTabBtn();
            mViewPager.setCurrentItem(index);

        }
    }
}
