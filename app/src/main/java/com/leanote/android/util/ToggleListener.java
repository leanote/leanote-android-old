package com.leanote.android.util;

import android.content.Context;
import android.view.Gravity;
import android.view.animation.TranslateAnimation;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.leanote.android.Leanote;
import com.leanote.android.R;

/**
 * Created by binnchx on 11/10/15.
 */
public class ToggleListener implements OnCheckedChangeListener {
    private Context context;
    private String settingName;
    private ToggleButton toggle;
    private ImageButton toggle_Button;
    private Object param;

    public ToggleListener(Context context, String settingName,
                          ToggleButton toggle, ImageButton toggle_Button, Object param) {
        this.context = context;
        this.settingName = settingName;
        this.toggle = toggle;
        this.toggle_Button = toggle_Button;
        this.param = param;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //更新toggle button 值
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) toggle_Button
                .getLayoutParams();
        if (isChecked) {

            params.addRule(RelativeLayout.ALIGN_RIGHT, -1);
            if ("public_note".equals(settingName)) {
                params.addRule(RelativeLayout.ALIGN_LEFT, R.id.toggle_public_blog);
                String noteId = (String) param;
                Leanote.leaDB.publicNote(noteId, true);
            } else if ("use_markdown".equals(settingName)) {
                params.addRule(RelativeLayout.ALIGN_LEFT, R.id.switch_markdown);
                Leanote.leaDB.updateMarkdown(true);
            }


            toggle_Button.setLayoutParams(params);
            toggle_Button.setImageResource(R.drawable.progress_thumb_selector);
            toggle.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);

            TranslateAnimation animation = new TranslateAnimation(
                    DisplayUtils.dip2px(context, 40), 0, 0, 0);
            animation.setDuration(200);
            toggle_Button.startAnimation(animation);
        } else {
            if ("public_note".equals(settingName)) {
                params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.toggle_public_blog);
                String noteId = (String) param;
                Leanote.leaDB.publicNote(noteId, false);
            } else if ("use_markdown".equals(settingName)) {
                params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.switch_markdown);
                Leanote.leaDB.updateMarkdown(false);
            }

            params.addRule(RelativeLayout.ALIGN_LEFT, -1);
            toggle_Button.setLayoutParams(params);
            toggle_Button
                    .setImageResource(R.drawable.progress_thumb_off_selector);

            toggle.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            TranslateAnimation animation = new TranslateAnimation(
                    DisplayUtils.dip2px(context, -40), 0, 0, 0);
            animation.setDuration(200);
            toggle_Button.startAnimation(animation);
        }
    }

}