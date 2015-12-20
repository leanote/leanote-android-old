package com.leanote.android.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leanote.android.R;
import com.leanote.android.accounts.helpers.LoginAbstract;
import com.leanote.android.accounts.helpers.LoginLeanote;
import com.leanote.android.accounts.helpers.LoginSelfHost;
import com.leanote.android.networking.NetworkUtils;
import com.leanote.android.ui.accounts.NewAccountActivity;
import com.leanote.android.util.ABTestingUtils;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.EditTextUtils;
import com.leanote.android.widget.LeaTextView;

import org.apache.commons.lang.StringUtils;
import org.wordpress.emailchecker.EmailChecker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignInFragment extends AbstractFragment implements TextWatcher {
    private static final String DOT_COM_BASE_URL = "https://leanote.com";
    private static final String FORGOT_PASSWORD_RELATIVE_URL = "/findPassword";
    private static final int WPCOM_ERRONEOUS_LOGIN_THRESHOLD = 3;
    private static final String FROM_LOGIN_SCREEN_KEY = "FROM_LOGIN_SCREEN_KEY";

    public static final String ENTERED_URL_KEY = "ENTERED_URL_KEY";
    public static final String ENTERED_USERNAME_KEY = "ENTERED_USERNAME_KEY";

    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private EditText mUrlEditText;
    //private EditText mTwoStepEditText;

    private LeaTextView mSignInButton;
    private LeaTextView mCreateAccountButton;
    private LeaTextView mAddSelfHostedButton;
    private LeaTextView mProgressTextSignIn;
    private LeaTextView mForgotPassword;
    private LeaTextView mJetpackAuthLabel;

    private LinearLayout mBottomButtonsLayout;
    private RelativeLayout mUsernameLayout;
    private RelativeLayout mPasswordLayout;
    private RelativeLayout mProgressBarSignIn;
    private RelativeLayout mUrlButtonLayout;
//    private RelativeLayout mTwoStepLayout;
//    private LinearLayout mTwoStepFooter;
//

    private ImageView mInfoButton;
    private ImageView mInfoButtonSecondary;

    private final EmailChecker mEmailChecker;

    private boolean mSelfHosted;
    private boolean mEmailAutoCorrected;
    private boolean mShouldSendTwoStepSMS;
    private int mErroneousLogInCount;
    private String mUsername;
    private String mPassword;
    private String mHostUrl;

    private String mTwoStepCode;
    private String mHttpUsername;
    private String mHttpPassword;

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (fieldsFilled()) {
            mSignInButton.setEnabled(true);
        } else {
            mSignInButton.setEnabled(false);
        }
        mPasswordEditText.setError(null);
        mUsernameEditText.setError(null);
        mUrlEditText.setError(null);

    }

    private boolean fieldsFilled() {
        boolean usernamePass = EditTextUtils.getText(mUsernameEditText).trim().length() > 0
                && EditTextUtils.getText(mPasswordEditText).trim().length() > 0;
        if (!mSelfHosted) {
            return usernamePass;
        } else {
            return usernamePass && EditTextUtils.getText(mUrlEditText).trim().length() > 0;
        }
    }


    @Override
    public void afterTextChanged(Editable s) {

    }

    public SignInFragment() {
        // Required empty public constructor
        mEmailChecker = new EmailChecker();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_sign_in, container, false);

        mUrlButtonLayout = (RelativeLayout) rootView.findViewById(R.id.url_button_layout);
        //mTwoStepLayout = (RelativeLayout) rootView.findViewById(R.id.two_factor_layout);
        //mTwoStepFooter = (LinearLayout) rootView.findViewById(R.id.two_step_footer);
        mUsernameLayout = (RelativeLayout) rootView.findViewById(R.id.nux_username_layout);
        mUsernameLayout.setOnClickListener(mOnLoginFormClickListener);
        mPasswordLayout = (RelativeLayout) rootView.findViewById(R.id.nux_password_layout);
        mPasswordLayout.setOnClickListener(mOnLoginFormClickListener);

        mUsernameEditText = (EditText) rootView.findViewById(R.id.nux_username);
        mUsernameEditText.addTextChangedListener(this);
        mUsernameEditText.setOnClickListener(mOnLoginFormClickListener);

        mPasswordEditText = (EditText) rootView.findViewById(R.id.nux_password);
        mPasswordEditText.addTextChangedListener(this);
        mPasswordEditText.setOnClickListener(mOnLoginFormClickListener);
        mJetpackAuthLabel = (LeaTextView) rootView.findViewById(R.id.nux_jetpack_auth_label);

        mUrlEditText = (EditText) rootView.findViewById(R.id.nux_url);
        mUrlEditText.addTextChangedListener(this);
        mUrlEditText.setOnClickListener(mOnLoginFormClickListener);


        mSignInButton = (LeaTextView) rootView.findViewById(R.id.nux_sign_in_button);
        mSignInButton.setOnClickListener(mSignInClickListener);

        mProgressBarSignIn = (RelativeLayout) rootView.findViewById(R.id.nux_sign_in_progress_bar);
        mProgressTextSignIn = (LeaTextView) rootView.findViewById(R.id.nux_sign_in_progress_text);
        mCreateAccountButton = (LeaTextView) rootView.findViewById(R.id.nux_create_account_button);
        mCreateAccountButton.setOnClickListener(mCreateAccountListener);
        mAddSelfHostedButton = (LeaTextView) rootView.findViewById(R.id.nux_add_selfhosted_button);

        mAddSelfHostedButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUrlButtonLayout.getVisibility() == View.VISIBLE) {
                    mUrlButtonLayout.setVisibility(View.GONE);
                    mAddSelfHostedButton.setText(getString(R.string.nux_add_selfhosted_blog));
                    mSelfHosted = false;
                } else {
                    mUrlButtonLayout.setVisibility(View.VISIBLE);
                    mAddSelfHostedButton.setText(getString(R.string.nux_oops_not_selfhosted_blog));
                    mSelfHosted = true;
                }
            }
        });

        mForgotPassword = (LeaTextView) rootView.findViewById(R.id.forgot_password);
        mForgotPassword.setOnClickListener(mForgotPasswordListener);
        mUsernameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    autocorrectUsername();
                }
            }
        });

        mPasswordEditText.setOnEditorActionListener(mEditorAction);
        mUrlEditText.setOnEditorActionListener(mEditorAction);



        mBottomButtonsLayout = (LinearLayout) rootView.findViewById(R.id.nux_bottom_buttons);
        initPasswordVisibilityButton(rootView, mPasswordEditText);
        //initInfoButtons(rootView);
        //moveBottomButtons();

        return rootView;
    }

    protected void onDoneAction() {
        signIn();
    }


//    private void moveBottomButtons() {
//        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            mBottomButtonsLayout.setOrientation(LinearLayout.HORIZONTAL);
//            if (getResources().getInteger(R.integer.isSW600DP) == 0) {
//                setSecondaryButtonVisible(true);
//            } else {
//                setSecondaryButtonVisible(false);
//            }
//        } else {
//            mBottomButtonsLayout.setOrientation(LinearLayout.VERTICAL);
//            setSecondaryButtonVisible(false);
//        }
//    }

//    private void setSecondaryButtonVisible(boolean visible) {
//        mInfoButtonSecondary.setVisibility(visible ? View.VISIBLE : View.GONE);
//        mInfoButton.setVisibility(visible ? View.GONE : View.VISIBLE);
//    }



    protected void initPasswordVisibilityButton(View rootView, final EditText passwordEditText) {
        final ImageView passwordVisibility = (ImageView) rootView.findViewById(R.id.password_visibility);
        if (passwordVisibility == null) {
            return;
        }
//        passwordVisibility.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mPasswordVisible = !mPasswordVisible;
//                if (mPasswordVisible) {
//                    passwordVisibility.setImageResource(R.drawable.show_pwd);
//                    passwordVisibility.setColorFilter(v.getContext().getResources().getColor(R.color.nux_eye_icon_color_open));
//                    passwordEditText.setTransformationMethod(null);
//                } else {
//                    passwordVisibility.setImageResource(R.drawable.not_show_pwd);
//                    passwordVisibility.setColorFilter(v.getContext().getResources().getColor(R.color.nux_eye_icon_color_closed));
//                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                }
//                passwordEditText.setSelection(passwordEditText.length());
//            }
//        });
    }


//    private void requestSMSTwoStepCode() {
//        if (!isAdded()) return;
//
//        ToastUtils.showToast(getActivity(), R.string.two_step_sms_sent);
//        mTwoStepEditText.setText("");
//        mShouldSendTwoStepSMS = true;
//
//        signIn();
//    }

//    private boolean fieldsFilled() {
//        return EditTextUtils.getText(mUsernameEditText).trim().length() > 0
//                && EditTextUtils.getText(mPasswordEditText).trim().length() > 0);
//    }

    private final OnClickListener mOnLoginFormClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // Don't change layout if we are performing a network operation
            if (mProgressBarSignIn.getVisibility() == View.VISIBLE) return;

//            if (mTwoStepLayout.getVisibility() == View.VISIBLE) {
//                setTwoStepAuthVisibility(false);
//            }
        }
    };

    private final OnClickListener mSignInClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.i("start to signin...", "");
            signIn();
        }
    };





    private final TextView.OnEditorActionListener mEditorAction = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (mPasswordEditText == v) {
                if (mSelfHosted) {
                    mUrlEditText.requestFocus();
                    return true;
                } else {
                    return onDoneEvent(actionId, event);
                }
            }
            return onDoneEvent(actionId, event);
        }
    };

    protected boolean onDoneEvent(int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || event != null && (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            if (!isUserDataValid()) {
                return true;
            }

            // hide keyboard before calling the done action
            InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

            // call child action
            onDoneAction();
            return true;
        }
        return false;
    }



    private void signIn() {
        if (!isUserDataValid()) {
            return;
        }

        if (!checkNetworkConnectivity()) {
            return;
        }

        mUsername = EditTextUtils.getText(mUsernameEditText).trim();
        mPassword = EditTextUtils.getText(mPasswordEditText).trim();
        if (mSelfHosted) {
            mHostUrl = EditTextUtils.getText(mUrlEditText).trim();
        }


        startProgress(getString(R.string.connecting_wpcom));
        signInServer();

    }

    private void signInServer() {
        LoginAbstract login;

        AppLog.i("isself:" + mSelfHosted);
        if (mSelfHosted) {
            login = new LoginSelfHost(mUsername, mPassword, mHostUrl);
        } else {
            login = new LoginLeanote(mUsername, mPassword);
        }

        login.execute(new LoginAbstract.Callback() {
            @Override
            public void onSuccess() {
                Log.i("login in success", "");

                finishCurrentActivity();
                //NoteUpdateService.startServiceForNote(getActivity());
            }

            @Override
            public void onError() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        signInError(R.string.username_or_password_incorrect, "client response");
                        endProgress();
                        return;
                    }
                });
            }
        });
    }

    protected void endProgress() {
        mProgressBarSignIn.setVisibility(View.GONE);
        mProgressTextSignIn.setVisibility(View.GONE);
        mSignInButton.setVisibility(View.VISIBLE);
        mUsernameEditText.setEnabled(true);
        mPasswordEditText.setEnabled(true);
        mUrlEditText.setEnabled(true);
        mAddSelfHostedButton.setEnabled(true);
        mCreateAccountButton.setEnabled(true);
        mForgotPassword.setEnabled(true);
    }

    private void finishCurrentActivity() {
        if (!isAdded()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getActivity().setResult(Activity.RESULT_OK);
                getActivity().finish();
            }
        });
    }

    protected void signInError(int messageId, String clientResponse) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        SignInDialogFragment nuxAlert;
        if (messageId == com.leanote.android.R.string.username_or_password_incorrect) {
            handleInvalidUsernameOrPassword(messageId);
            return;
        } else if (messageId == com.leanote.android.R.string.invalid_url_message) {
            showUrlError(messageId);
            endProgress();
            return;
        } else {
            AppLog.e(AppLog.T.NUX, "Server response: " + clientResponse);
            nuxAlert = SignInDialogFragment.newInstance(getString(com.leanote.android.R.string.nux_cannot_log_in),
                    getString(messageId), R.drawable.noticon_alert_big, 3,
                    getString(R.string.cancel), "contact us", getString(R.string.reader_title_applog),
                    SignInDialogFragment.ACTION_OPEN_SUPPORT_CHAT,
                    SignInDialogFragment.ACTION_OPEN_APPLICATION_LOG);
        }
        ft.add(nuxAlert, "alert");
        ft.commitAllowingStateLoss();
        endProgress();
    }

    protected void handleInvalidUsernameOrPassword(int messageId) {
        mErroneousLogInCount += 1;
        if (mErroneousLogInCount >= WPCOM_ERRONEOUS_LOGIN_THRESHOLD) {
            // Clear previous errors
            mPasswordEditText.setError(null);
            mUsernameEditText.setError(null);
            showInvalidUsernameOrPasswordDialog();
        } else {
            showUsernameError(messageId);
            showPasswordError(messageId);
        }
        endProgress();
    }

    protected void showInvalidUsernameOrPasswordDialog() {
        // Show a dialog
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        SignInDialogFragment nuxAlert;
        if (ABTestingUtils.isFeatureEnabled(ABTestingUtils.Feature.HELPSHIFT)) {
            // create a 3 buttons dialog ("Contact us", "Forget your password?" and "Cancel")
            nuxAlert = SignInDialogFragment.newInstance(getString(com.leanote.android.R.string.nux_cannot_log_in),
                    getString(com.leanote.android.R.string.username_or_password_incorrect),
                    com.leanote.android.R.drawable.noticon_alert_big, 3, getString(
                            com.leanote.android.R.string.cancel), getString(
                            com.leanote.android.R.string.forgot_password), "", SignInDialogFragment.ACTION_OPEN_URL,
                    SignInDialogFragment.ACTION_OPEN_SUPPORT_CHAT);
        } else {
            // create a 2 buttons dialog ("Forget your password?" and "Cancel")
            nuxAlert = SignInDialogFragment.newInstance(getString(com.leanote.android.R.string.nux_cannot_log_in),
                    getString(com.leanote.android.R.string.username_or_password_incorrect),
                    com.leanote.android.R.drawable.noticon_alert_big, 2, getString(
                            com.leanote.android.R.string.cancel), getString(
                            com.leanote.android.R.string.forgot_password), null, SignInDialogFragment.ACTION_OPEN_URL,
                    0);
        }

        // Put entered url and entered username args, that could help our support team
        Bundle bundle = nuxAlert.getArguments();
        bundle.putString(SignInDialogFragment.ARG_OPEN_URL_PARAM, getForgotPasswordURL());
        bundle.putString(ENTERED_URL_KEY, EditTextUtils.getText(mUrlEditText));
        bundle.putString(ENTERED_USERNAME_KEY, EditTextUtils.getText(mUsernameEditText));
        nuxAlert.setArguments(bundle);
        ft.add(nuxAlert, "alert");
        ft.commitAllowingStateLoss();
    }

    protected void startProgress(String message) {
        mProgressBarSignIn.setVisibility(View.VISIBLE);
        mProgressTextSignIn.setVisibility(View.VISIBLE);
        mSignInButton.setVisibility(View.GONE);
        mProgressBarSignIn.setEnabled(false);
        mProgressTextSignIn.setText(message);
        mUsernameEditText.setEnabled(false);
        mPasswordEditText.setEnabled(false);
        mUrlEditText.setEnabled(false);
        mAddSelfHostedButton.setEnabled(false);
        mCreateAccountButton.setEnabled(false);
        mForgotPassword.setEnabled(false);
    }



    private boolean isLeaComLogin() {
        String selfHostedUrl = EditTextUtils.getText(mUrlEditText).trim();
        return !mSelfHosted || TextUtils.isEmpty(selfHostedUrl) || selfHostedUrl.contains("leanote.com");
    }

    private boolean checkNetworkConnectivity() {
        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            SignInDialogFragment nuxAlert;
            nuxAlert = SignInDialogFragment.newInstance(getString(R.string.no_network_title),
                    getString(R.string.no_network_message),
                    R.drawable.noticon_alert_big,
                    getString(R.string.cancel));
            ft.add(nuxAlert, "alert");
            ft.commitAllowingStateLoss();
            return false;
        }
        return true;
    }

    private final View.OnClickListener mCreateAccountListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Activity activity = getActivity();
            if (activity != null) {
                ActivityLauncher.newAccountForResult(activity);
            }
        }
    };

    public static void newAccountForResult(Activity activity) {
        Intent intent = new Intent(activity, NewAccountActivity.class);
        activity.startActivityForResult(intent, SignInActivity.CREATE_ACCOUNT_REQUEST);
    }

    public void signInDotComUser(String username, String password) {
        if (username != null && password != null) {
            mUsernameEditText.setText(username);
            mPasswordEditText.setText(password);
            signIn();
        }
    }



    protected boolean isUserDataValid() {
        final String username = EditTextUtils.getText(mUsernameEditText).trim();
        final String password = EditTextUtils.getText(mPasswordEditText).trim();

        boolean retValue = true;

        if (username.equals("")) {
            mUsernameEditText.setError(getString(R.string.required_field));
            mUsernameEditText.requestFocus();
            retValue = false;
        }

        if (StringUtils.isEmpty(password)) {
            mPasswordEditText.setError(getString(R.string.required_field));
            mPasswordEditText.requestFocus();
            retValue = false;
        }

        if (password.length() < 6) {
            mPasswordEditText.setError(getString(R.string.pwd_size_alert));
            mPasswordEditText.requestFocus();
            retValue = false;
        }

        if (mSelfHosted) {
            final String host = EditTextUtils.getText(mUrlEditText).trim();
            if (TextUtils.isEmpty(host)) {
                mUrlEditText.requestFocus();
                retValue = false;
            }
        }

        return retValue;
    }

    private void showPasswordError(int messageId) {
        mPasswordEditText.setError(getString(messageId));
        mPasswordEditText.requestFocus();
    }

    private void showUsernameError(int messageId) {
        mUsernameEditText.setError(getString(messageId));
        mUsernameEditText.requestFocus();
    }

    private void showUrlError(int messageId) {
        mUrlEditText.setError(getString(messageId));
        mUrlEditText.requestFocus();
    }

//    private void showTwoStepCodeError(int messageId) {
//        mTwoStepEditText.setError(getString(messageId));
//        mTwoStepEditText.requestFocus();
//    }

    protected boolean specificShowError(int messageId) {
        switch (getErrorType(messageId)) {
            case USERNAME:
            case PASSWORD:
                showUsernameError(messageId);
                showPasswordError(messageId);
                return true;
            default:
                return false;
        }
    }

    private final View.OnClickListener mForgotPasswordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getForgotPasswordURL()));
            startActivity(intent);
        }
    };

    private String getForgotPasswordURL() {
        String baseUrl = DOT_COM_BASE_URL;
        return baseUrl + FORGOT_PASSWORD_RELATIVE_URL;
    }


    private void autocorrectUsername() {
        if (mEmailAutoCorrected) {
            return;
        }
        final String email = EditTextUtils.getText(mUsernameEditText).trim();
        // Check if the username looks like an email address
        final Pattern emailRegExPattern = Patterns.EMAIL_ADDRESS;
        Matcher matcher = emailRegExPattern.matcher(email);
        if (!matcher.find()) {
            return;
        }
        // It looks like an email address, then try to correct it
        String suggest = mEmailChecker.suggestDomainCorrection(email);
        if (suggest.compareTo(email) != 0) {
            mEmailAutoCorrected = true;
            mUsernameEditText.setText(suggest);
            mUsernameEditText.setSelection(suggest.length());
        }
    }


}
