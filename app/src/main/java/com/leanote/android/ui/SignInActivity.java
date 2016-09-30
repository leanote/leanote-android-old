package com.leanote.android.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leanote.android.R;
import com.leanote.android.model.Account;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.networking.NetworkUtils;
import com.leanote.android.networking.retrofit.RetrofitUtil;
import com.leanote.android.networking.retrofit.imp.ImpLogin;
import com.leanote.android.networking.retrofit.imp.RetrofitService;
import com.leanote.android.ui.fragment.SignInDialogFragment;
import com.leanote.android.util.ABTestingUtils;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.EditTextUtils;
import com.leanote.android.util.XLog;
import com.leanote.android.widget.LeaTextView;
import com.leanote.android.widget.OpenSansEditText;

import org.apache.commons.lang.StringUtils;
import org.wordpress.emailchecker.EmailChecker;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//登陆界面
public class SignInActivity extends LeaAbstractActivity implements TextWatcher {

    public static final int SIGN_IN_REQUEST = 1;
    public static final int REQUEST_CODE = 5000;
    public static final int ADD_SELF_HOSTED_BLOG = 2;
    public static final int CREATE_ACCOUNT_REQUEST = 3;
    public static final int SHOW_CERT_DETAILS = 4;
    public static String START_FRAGMENT_KEY = "start-fragment";
    public static final String ARG_JETPACK_SITE_AUTH = "ARG_JETPACK_SITE_AUTH";
    public static final String ARG_JETPACK_MESSAGE_AUTH = "ARG_JETPACK_MESSAGE_AUTH";
    public static final String ARG_IS_AUTH_ERROR = "ARG_IS_AUTH_ERROR";

    private static final String DOT_COM_BASE_URL = "https://leanote.com";
    private static final String FORGOT_PASSWORD_RELATIVE_URL = "/findPassword";
    private static final int WPCOM_ERRONEOUS_LOGIN_THRESHOLD = 3;
    private static final String FROM_LOGIN_SCREEN_KEY = "FROM_LOGIN_SCREEN_KEY";

    public static final String ENTERED_URL_KEY = "ENTERED_URL_KEY";
    public static final String ENTERED_USERNAME_KEY = "ENTERED_USERNAME_KEY";

    private OpenSansEditText mUsernameEditText;
    private OpenSansEditText mPasswordEditText;
    private OpenSansEditText mUrlEditText;

    private LeaTextView mSignInButton;
    private LeaTextView mCreateAccountButton;
    private LeaTextView mAddSelfHostedButton;
    private LeaTextView mProgressTextSignIn;
    private LeaTextView mForgotPassword;

    private RelativeLayout mProgressBarSignIn;
    private RelativeLayout mUrlButtonLayout;

    private ImageView mIvPswVisibility;

    private EmailChecker mEmailChecker;

    private boolean mSelfHosted;
    private boolean mEmailAutoCorrected;
    private int mErroneousLogInCount;
    private String mUsername;
    private String mPassword;
    private String mHostUrl;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_sign_in;
    }

    @Override
    protected void setupViews() {
        // Required empty public constructor
        mEmailChecker = new EmailChecker();
        mUrlButtonLayout = (RelativeLayout) findViewById(R.id.url_button_layout);

        mUsernameEditText = (OpenSansEditText) findViewById(R.id.nux_username);
        mUsernameEditText.addTextChangedListener(this);

        mPasswordEditText = (OpenSansEditText) findViewById(R.id.nux_password);
        mPasswordEditText.addTextChangedListener(this);

        mUrlEditText = (OpenSansEditText) findViewById(R.id.nux_url);
        mUrlEditText.addTextChangedListener(this);

        mSignInButton = (LeaTextView) findViewById(R.id.nux_sign_in_button);
        mSignInButton.setOnClickListener(mSignInClickListener);

        mProgressBarSignIn = (RelativeLayout) findViewById(R.id.nux_sign_in_progress_bar);
        mProgressTextSignIn = (LeaTextView) findViewById(R.id.nux_sign_in_progress_text);
        mCreateAccountButton = (LeaTextView) findViewById(R.id.nux_create_account_button);
        mCreateAccountButton.setOnClickListener(mCreateAccountListener);
        mAddSelfHostedButton = (LeaTextView) findViewById(R.id.nux_add_selfhosted_button);
        mIvPswVisibility = (ImageView) findViewById(R.id.password_visibility);
        mAddSelfHostedButton.setOnClickListener(new View.OnClickListener() {
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

        mForgotPassword = (LeaTextView) findViewById(R.id.forgot_password);
        mForgotPassword.setOnClickListener(mForgotPasswordListener);
        mUsernameEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    autoCorrectUsername();
                }
            }
        });

        mPasswordEditText.setOnEditorActionListener(mEditorAction);
        mUrlEditText.setOnEditorActionListener(mEditorAction);

        initPasswordVisibilityButton(mIvPswVisibility, mPasswordEditText);

        mSignInButton.setEnabled(true);
    }

    @Override
    protected void onDoneAction() {
        signIn();
    }

    @Override
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String username = data.getStringExtra("username");
            String password = data.getStringExtra("password");
            if (username != null) {
                signInDotComUser(username, password);
            }
        }
    }

    public void signInDotComUser(String username, String password) {
        if (username != null && password != null) {
            mUsernameEditText.setText(username);
            mPasswordEditText.setText(password);
            signIn();
        }
    }

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

    @Override
    public void afterTextChanged(Editable s) {

    }

    //判断账号密码不为空
    private boolean fieldsFilled() {
        boolean usernamePass = EditTextUtils.getText(mUsernameEditText).trim().length() > 0
                && EditTextUtils.getText(mPasswordEditText).trim().length() > 0;
        if (!mSelfHosted) {
            return usernamePass;
        } else {
            return usernamePass && EditTextUtils.getText(mUrlEditText).trim().length() > 0;
        }
    }

    private final View.OnClickListener mSignInClickListener = new View.OnClickListener() {
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

    //登录到服务器
    private void signInServer() {
        String loginUrl;
        AppLog.i("isself:" + mSelfHosted);
        if (mSelfHosted) {
            loginUrl = mHostUrl;
            if (!TextUtils.isEmpty(loginUrl) && !loginUrl.endsWith("/")) {
                loginUrl = loginUrl + "/";
            }
        } else {
            loginUrl = RetrofitService.BASE_URL;
        }
        XLog.e(XLog.getTag(), XLog.TAG_GU + loginUrl);
        XLog.e(XLog.getTag(), XLog.TAG_GU + mUsername);
        XLog.e(XLog.getTag(), XLog.TAG_GU + mPassword);
        Map<String, String> map = new HashMap<>();
        map.put("email", mUsername);
        map.put("pwd", mPassword);
        RetrofitUtil.getInstance()
                .setBaseUrl(loginUrl)
                .setTimeout(10000)
                .build()
                .login(map, new ImpLogin() {
                    @Override
                    public void onSuccess(Account account) {
                        XLog.e(XLog.getTag(), XLog.TAG_GU + account.toString());
                        if (account.isOk()) {
                            AccountHelper.getInstance().setAccount(account);
                            XLog.e(XLog.getTag(), XLog.TAG_GU + "login success");
                            finishCurrentActivity();
                        } else {
                            XLog.e(XLog.getTag(), XLog.TAG_GU + account.getMsg());
                            signInError(R.string.username_or_password_incorrect, "client response");
                            endProgress();
                        }
                    }

                    @Override
                    public void onFail() {
                        signInError(R.string.username_or_password_incorrect, "client response");
                        endProgress();
                    }
                });

    }

    //结束加载框
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
        setResult(Activity.RESULT_OK);
        finish();
    }

    //登陆错误提示
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

    //显示登录不成功的提示
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

    private boolean checkNetworkConnectivity() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
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
            ActivityLauncher.newAccountForResult(SignInActivity.this);
        }
    };

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

    //忘记密码操作
    private final View.OnClickListener mForgotPasswordListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getForgotPasswordURL()));
            startActivity(intent);
        }
    };

    //获取忘记密码的地址
    private String getForgotPasswordURL() {
        String baseUrl = DOT_COM_BASE_URL;
        return baseUrl + FORGOT_PASSWORD_RELATIVE_URL;
    }

    private void autoCorrectUsername() {
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
