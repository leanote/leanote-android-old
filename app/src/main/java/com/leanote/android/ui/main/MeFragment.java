package com.leanote.android.ui.main;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Outline;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.leanote.android.Leanote;

import com.leanote.android.model.Account;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.ui.ActivityLauncher;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.GravatarUtils;
import com.leanote.android.util.ToggleListener;
import com.leanote.android.widget.LeaNetworkImageView;

import java.lang.ref.WeakReference;
import com.leanote.android.R;

public class MeFragment extends Fragment {

    private static final String IS_DISCONNECTING = "IS_DISCONNECTING";

    private ViewGroup mAvatarFrame;
    private LeaNetworkImageView mAvatarImageView;
    private TextView mDisplayNameTextView;
    private TextView mUsernameTextView;
    private TextView mLoginLogoutTextView;
    private ProgressDialog mDisconnectProgressDialog;

    private LinearLayout markdown_editor_setting;
    private ToggleButton switch_markdown;
    private ImageButton switch_markdown_button;


    public static MeFragment newInstance() {
        return new MeFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.me_fragment, container, false);

        mAvatarFrame = (ViewGroup) rootView.findViewById(R.id.frame_avatar);
        mAvatarImageView = (LeaNetworkImageView) rootView.findViewById(R.id.me_avatar);
        mDisplayNameTextView = (TextView) rootView.findViewById(R.id.me_display_name);
        mUsernameTextView = (TextView) rootView.findViewById(R.id.me_username);
        mLoginLogoutTextView = (TextView) rootView.findViewById(R.id.me_login_logout_text_view);

        markdown_editor_setting = (LinearLayout) rootView.findViewById(R.id.markdown_editor_setting);
        switch_markdown = (ToggleButton) rootView.findViewById(R.id.switch_markdown);
        switch_markdown_button = (ImageButton) rootView.findViewById(R.id.switch_markdown_button);

        addDropShadowToAvatar();
        refreshAccountDetails();

        initSettingsFields();
        setListeners();

        rootView.findViewById(R.id.row_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (AccountHelper.isSignedIn()) {
                    signOutWordPressComWithConfirmation();
                } else {
                    ActivityLauncher.showSignInForResult(getActivity());
                }
            }
        });

        if (savedInstanceState != null && savedInstanceState.getBoolean(IS_DISCONNECTING, false)) {
            showDisconnectDialog(getActivity());
        }

        return rootView;
    }



    private void setListeners() {

        switch_markdown.setOnCheckedChangeListener(new ToggleListener(getActivity(),
                "use_markdown", switch_markdown, switch_markdown_button, null));


        View.OnClickListener clickToToggleListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switch_markdown.toggle();
                AccountHelper.getDefaultAccount().setUseMarkdown(switch_markdown.isChecked());
            }
        };

        switch_markdown_button.setOnClickListener(clickToToggleListener);
        markdown_editor_setting.setOnClickListener(clickToToggleListener);

    }


    private void initSettingsFields() {
        boolean isMarkdown = AccountHelper.getDefaultAccount().isUseMarkdown();
        AppLog.i("ismarkdown:" + isMarkdown);
        switch_markdown.setChecked(isMarkdown);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) switch_markdown_button
                .getLayoutParams();


        if (isMarkdown) {
            params.addRule(RelativeLayout.ALIGN_RIGHT, -1);
            params.addRule(RelativeLayout.ALIGN_LEFT,
                    R.id.toggleButton_public_blog);
            switch_markdown_button.setLayoutParams(params);
            switch_markdown_button
                    .setImageResource(R.drawable.progress_thumb_selector);
            switch_markdown.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        } else {
            params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.switch_markdown);
            params.addRule(RelativeLayout.ALIGN_LEFT, -1);
            switch_markdown_button.setLayoutParams(params);
            switch_markdown_button
                    .setImageResource(R.drawable.progress_thumb_off_selector);
            switch_markdown.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mDisconnectProgressDialog != null) {
            outState.putBoolean(IS_DISCONNECTING, true);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        if (mDisconnectProgressDialog != null) {
            mDisconnectProgressDialog.dismiss();
            mDisconnectProgressDialog = null;
        }
        super.onDestroy();
    }

    /**
     * adds a circular drop shadow to the avatar's parent view (Lollipop+ only)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void addDropShadowToAvatar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mAvatarFrame.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, view.getWidth(), view.getHeight());
                }
            });
            mAvatarFrame.setElevation(mAvatarFrame.getResources().getDimensionPixelSize(R.dimen.card_elevation));
        }
    }

    private void refreshAccountDetails() {
        // we only want to show user details for WordPress.com users
        if (AccountHelper.isSignedIn()) {
            Account defaultAccount = AccountHelper.getDefaultAccount();

            mDisplayNameTextView.setVisibility(View.VISIBLE);
            mUsernameTextView.setVisibility(View.VISIBLE);
            mAvatarFrame.setVisibility(View.VISIBLE);

            int avatarSz = getResources().getDimensionPixelSize(R.dimen.avatar_sz_large);
            String avatarUrl = GravatarUtils.fixGravatarUrl(defaultAccount.getmAvatar(), avatarSz);
            mAvatarImageView.setImageUrl(avatarUrl, LeaNetworkImageView.ImageType.AVATAR);

            mUsernameTextView.setText("@" + defaultAccount.getmUserName());
            mLoginLogoutTextView.setText(R.string.me_disconnect_from_leanote_com);

            String displayName = defaultAccount.getmUserName();
            if (!TextUtils.isEmpty(displayName)) {
                mDisplayNameTextView.setText(displayName);
            } else {
                mDisplayNameTextView.setText(defaultAccount.getmUserName());
            }
        } else {
            mDisplayNameTextView.setVisibility(View.GONE);
            mUsernameTextView.setVisibility(View.GONE);
            mAvatarFrame.setVisibility(View.GONE);
            mLoginLogoutTextView.setText(R.string.me_connect_to_leanote_com);
        }
    }

    private void signOutWordPressComWithConfirmation() {
        String message = String.format(getString(R.string.sign_out_leacom_confirm), AccountHelper.getDefaultAccount()
                .getmUserName());

        new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton(R.string.signout, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        signOutWordPressCom();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(true)
                .create().show();
    }

    private void signOutWordPressCom() {
        // note that signing out sends a CoreEvents.UserSignedOutWordPressCom EventBus event,
        // which will cause the main activity to recreate this fragment
        (new SignOutWordPressComAsync(getActivity())).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private void showDisconnectDialog(Context context) {
        mDisconnectProgressDialog = ProgressDialog.show(context, null, context.getText(R.string.signing_out), false);
    }

    private class SignOutWordPressComAsync extends AsyncTask<Void, Void, Void> {
        WeakReference<Context> mWeakContext;

        public SignOutWordPressComAsync(Context context) {
            mWeakContext = new WeakReference<Context>(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Context context = mWeakContext.get();
            if (context != null) {
                showDisconnectDialog(context);
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            Context context = mWeakContext.get();
            if (context != null) {
                //WordPress.WordPressComSignOut(context);
                Leanote.LeanoteComSignOut(context);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (mDisconnectProgressDialog != null && mDisconnectProgressDialog.isShowing()) {
                mDisconnectProgressDialog.dismiss();
            }
            mDisconnectProgressDialog = null;
        }
    }


}
