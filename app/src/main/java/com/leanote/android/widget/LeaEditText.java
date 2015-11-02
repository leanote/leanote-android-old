package com.leanote.android.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

/**
 * Created by binnchx on 10/22/15.
 */
public class LeaEditText extends EditText {
    private EditTextImeBackListener mOnImeBack;
    private OnSelectionChangedListener onSelectionChangedListener;

    public LeaEditText(Context context) {
        super(context);
    }

    public LeaEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LeaEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        if (onSelectionChangedListener != null) {
            onSelectionChangedListener.onSelectionChanged();
        }
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnImeBack != null)
                mOnImeBack.onImeBack(this, this.getText().toString());
        }

        return super.onKeyPreIme(keyCode, event);
    }

    public void setOnEditTextImeBackListener(EditTextImeBackListener listener) {
        mOnImeBack = listener;
    }

    public interface EditTextImeBackListener {
        public abstract void onImeBack(LeaEditText ctrl, String text);
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        onSelectionChangedListener = listener;
    }

    public interface OnSelectionChangedListener {
        public abstract void onSelectionChanged();
    }
}