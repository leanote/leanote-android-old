package com.leanote.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.EditText;

import com.leanote.android.R;

/**
 * Created by binnchx on 8/27/15.
 */
public class PersistentEditText extends EditText {
    private PersistentEditTextHelper mPersistentEditTextHelper;
    private Boolean mPersistenceEnabled;

    public PersistentEditText(Context context) {
        super(context, (AttributeSet)null);
        this.mPersistentEditTextHelper = new PersistentEditTextHelper(context);
    }

    public PersistentEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.readCustomAttrs(context, attrs);
        this.mPersistentEditTextHelper = new PersistentEditTextHelper(context);
    }

    public PersistentEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.readCustomAttrs(context, attrs);
        this.mPersistentEditTextHelper = new PersistentEditTextHelper(context);
    }

    public PersistentEditTextHelper getAutoSaveTextHelper() {
        return this.mPersistentEditTextHelper;
    }

    public void setPersistenceEnabled(boolean enabled) {
        this.mPersistenceEnabled = Boolean.valueOf(enabled);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.load();
    }

    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        if(text.length() != 0 || lengthBefore != 0) {
            this.save();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.save();
    }

    private void load() {
        if(this.mPersistenceEnabled.booleanValue()) {
            this.getAutoSaveTextHelper().loadString(this);
        }
    }

    private void save() {
        if(this.mPersistenceEnabled.booleanValue()) {
            this.getAutoSaveTextHelper().saveString(this);
        }
    }

    private void readCustomAttrs(Context context, AttributeSet attrs) {
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PersistentEditText, 0, 0);
        if(array != null) {
            this.mPersistenceEnabled = Boolean.valueOf(array.getBoolean(R.styleable.PersistentEditText_persistenceEnabled, false));
        }

    }
}