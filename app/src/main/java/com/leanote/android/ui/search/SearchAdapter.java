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
    private final List<NoteDetail> mNote;

    public SearchAdapter(Context context, List<NoteDetail> models) {
        mInflater = LayoutInflater.from(context);
        mNote = new ArrayList<>(models);
    }

    @Override
    public SearchViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //final View itemView = mInflater.inflate(R.layout.search_item, parent, false);
        final View itemView = mInflater.inflate(R.layout.post_cardview_link, parent, false);
        return new SearchViewHolder(parent.getContext(), itemView);
    }

    @Override
    public void onBindViewHolder(SearchViewHolder holder, int position) {
        final NoteDetail model = mNote.get(position);

        holder.bind(model);
    }

    @Override
    public int getItemCount() {
        return mNote.size();
    }

    public void animateTo(List<NoteDetail> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<NoteDetail> newModels) {
        for (int i = mNote.size() - 1; i >= 0; i--) {
            final NoteDetail model = mNote.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<NoteDetail> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final NoteDetail model = newModels.get(i);
            if (!mNote.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<NoteDetail> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final NoteDetail model = newModels.get(toPosition);
            final int fromPosition = mNote.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public NoteDetail removeItem(int position) {
        final NoteDetail model = mNote.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, NoteDetail model) {
        mNote.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final NoteDetail model = mNote.remove(fromPosition);
        mNote.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
}
