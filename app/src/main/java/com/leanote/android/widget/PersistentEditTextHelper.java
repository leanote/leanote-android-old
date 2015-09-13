package com.leanote.android.widget;

import android.content.Context;
import android.view.View;
import android.widget.EditText;

/**
 * Created by binnchx on 8/27/15.
 */
public class PersistentEditTextHelper {
    private String mUniqueId;
    private PersistentEditTextDatabase mPersistentEditTextDatabase;

    public PersistentEditTextHelper(Context context) {
        this.mPersistentEditTextDatabase = new PersistentEditTextDatabase(context);
    }

    public void setUniqueId(String uniqueId) {
        this.mUniqueId = uniqueId;
    }

    public String getUniqueId() {
        return this.mUniqueId;
    }

    public void clearSavedText(View view) {
        this.clearSavedText(view, this.mUniqueId);
    }

    public void loadString(EditText editText) {
        String text = this.mPersistentEditTextDatabase.get(getViewPathId(editText) + this.mUniqueId, "");
        if(!text.isEmpty()) {
            editText.setText(text);
            editText.setSelection(text.length());
        }

    }

    public void saveString(EditText editText) {
        if(editText.getText() != null) {
            this.mPersistentEditTextDatabase.put(getViewPathId(editText) + this.mUniqueId, editText.getText().toString());
        }
    }

    public void clearSavedText(View view, String uniqueId) {
        this.mPersistentEditTextDatabase.remove(getViewPathId(view) + uniqueId);
    }

    protected static String getViewPathId(View view) {
        StringBuilder sb = new StringBuilder();

        for(View currentView = view; currentView != null && currentView.getParent() != null && currentView.getParent() instanceof View; currentView = (View)currentView.getParent()) {
            sb.append(currentView.getId());
        }

        return sb.toString();
    }
}