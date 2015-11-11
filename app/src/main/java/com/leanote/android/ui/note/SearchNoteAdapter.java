package com.leanote.android.ui.note;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.leanote.android.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binnchx on 11/5/15.
 */
public class SearchNoteAdapter extends RecyclerView.Adapter<SearchNoteViewHolder> {

    private final LayoutInflater mInflater;
    private List<String> mTitles;

    public SearchNoteAdapter(Context context, List<String> titles) {
        mInflater = LayoutInflater.from(context);
        mTitles = new ArrayList<>(titles);
    }

    @Override
    public SearchNoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View itemView = mInflater.inflate(R.layout.seach_note, parent, false);
        return new SearchNoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SearchNoteViewHolder holder, int position) {
        final String title = mTitles.get(position);
        holder.bind(title);
    }

    @Override
    public int getItemCount() {
        return mTitles.size();
    }

    public void setModels(List<String> titles) {
        mTitles = new ArrayList<>(titles);
    }

    public String removeItem(int position) {
        final String model = mTitles.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, String model) {
        mTitles.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final String model = mTitles.remove(fromPosition);
        mTitles.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void animateTo(List<String> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }


    private void applyAndAnimateRemovals(List<String> newModels) {
        for (int i = mTitles.size() - 1; i >= 0; i--) {
            final String model = mTitles.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<String> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final String model = newModels.get(i);
            if (!mTitles.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<String> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final String model = newModels.get(toPosition);
            final int fromPosition = mTitles.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

}
