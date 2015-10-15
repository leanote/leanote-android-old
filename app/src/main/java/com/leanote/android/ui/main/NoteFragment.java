package com.leanote.android.ui.main;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.ui.LeaMainActivity;
import com.leanote.android.util.CoreEvents;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

public class NoteFragment extends Fragment
            implements LeaMainActivity.OnScrollToTopListener {

    private View mFabView;
    private int mFabTargetYTranslation;

    private ListView noteListView;


    public static NoteFragment newInstance() {
        return new NoteFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //setUpListView();
    }

    private void setUpListView() {
        //noteListView.setAdapter(getNoteAdapter());

    }

    private ListAdapter getNoteAdapter() {

        return null;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.note_fragment, container, false);

        int fabHeight = getResources().getDimensionPixelSize(R.dimen.fab_size_normal);
        int fabMargin = getResources().getDimensionPixelSize(R.dimen.fab_margin);
        mFabTargetYTranslation = (fabHeight + fabMargin) * 2;
        mFabView = rootView.findViewById(R.id.fab_button);

        noteListView = (ListView) rootView.findViewById(R.id.note_list);

        //获取笔记api：https://leanote.com/api/note/getSyncNotes?token=
        //生成适配器的Item和动态数组对应的元素

        SimpleAdapter listItemAdapter = new SimpleAdapter(getActivity(),getNoteData(),//数据源
                R.layout.note_list,//ListItem的XML实现
                new String[] {"note_title", "note_time", "notebook"},
                new int[] {R.id.note_title,R.id.note_time, R.id.notebook}
        );

        //添加并且显示
        noteListView.setAdapter(listItemAdapter);

        //添加点击
//        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
//                                    long arg3) {
//                setTitle("点击第"+arg2+"个项目");
//            }
//        });

        //添加长按点击
        noteListView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,ContextMenu.ContextMenuInfo menuInfo) {
                menu.setHeaderTitle("长按菜单-ContextMenu");
                menu.add(0, 0, 0, "弹出长按菜单0");
                menu.add(0, 1, 0, "弹出长按菜单1");
            }
        });
        //noteListView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.simple, getData()));

        return rootView;
    }

    private ArrayList<HashMap<String, Object>> getNoteData() {
        ArrayList<HashMap<String, Object>> listItem = new ArrayList<>();

        String note_url = String.format("http://leanote.com/api/note/getSyncNotes?token=%s", Leanote.getAccessToken());

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest request = new JsonObjectRequest(note_url, null, future, future);
        Leanote.requestQueue.add(request);

        try {
            JSONObject json = future.get(50, TimeUnit.SECONDS);
            Log.i("note json:" , json.toString());
        } catch (Exception e) {
            Log.e("get note data error", String.valueOf(e));
        }

        return listItem;
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

    @SuppressWarnings("unused")
    public void onEventMainThread(CoreEvents.MainViewPagerScrolled event) {
        mFabView.setTranslationY(mFabTargetYTranslation * event.mXOffset);
    }


}
