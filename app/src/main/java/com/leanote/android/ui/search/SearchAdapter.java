package com.leanote.android.ui.search;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.leanote.android.R;
import com.leanote.android.model.NoteDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by binnchx on 12/10/15.
 */
public class SearchAdapter extends RecyclerView.Adapter<SearchViewHolder> {
    private final LayoutInflater mInflater;
    private final List mList;

    public SearchAdapter(Context context, List models) {
        mInflater = LayoutInflater.from(context);
        mList = new ArrayList(models);
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //final View itemView = mInflater.inflate(R.layout.search_item, parent, false);
        final View itemView = mInflater.inflate(R.layout.post_cardview_link, parent, false);
        return new SearchViewHolder(parent.getContext(), itemView);
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        final Object model = mList.get(position);

        holder.bind(model);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void animateTo(List<NoteDetail> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List newModels) {
        for (int i = mList.size() - 1; i >= 0; i--) {
            final Object model = mList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final Object model = newModels.get(i);
            if (!mList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final Object model = newModels.get(toPosition);
            final int fromPosition = mList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public Object removeItem(int position) {
        final Object model = mList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, Object model) {
        mList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final Object model = mList.remove(fromPosition);
        mList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
}
