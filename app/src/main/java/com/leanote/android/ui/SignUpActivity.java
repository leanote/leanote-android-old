package com.leanote.android.ui;


import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leanote.android.R;
import com.leanote.android.networking.retrofit.RetrofitUtil;
import com.leanote.android.networking.retrofit.bean.SuccessBean;
import com.leanote.android.networking.retrofit.imp.ImpRegister;
import com.leanote.android.util.AlertUtils;
import com.leanote.android.util.EditTextUtils;
import com.leanote.android.util.UserEmailUtils;
import com.leanote.android.widget.LeaTextView;
import com.leanote.android.widget.OpenSansEditText;
import com.leanote.android.widget.PersistentEditTextHelper;

import org.wordpress.emailchecker.EmailChecker;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends LeaAbstractActivity implements TextWatcher {

    private OpenSansEditText mEmailTextField;
    private OpenSansEditText mPasswordTextField;
    private LeaTextView mSignUpButton;
    private LeaTextView mProgressTextSignIn;
    private RelativeLayout mProgressBarSignIn;
    private EmailChecker mEmailChecker;
    private ImageView mIvPswVisibility;
    private boolean mEmailAutoCorrected;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_account;
    }

    @Override
    protected void setupViews() {
        mEmailChecker = new EmailChecker();
        mSignUpButton = (LeaTextView) findViewById(R.id.signup_button);
        mSignUpButton.setOnClickListener(mSignUpClickListener);
        mSignUpButton.setEnabled(false);

        mProgressTextSignIn = (LeaTextView) findViewById(R.id.nux_sign_in_progress_text);
        mProgressBarSignIn = (RelativeLayout) findViewById(R.id.nux_sign_in_progress_bar);

        mEmailTextField = (OpenSansEditText) findViewById(R.id.email_address);
        mEmailTextField.setText(UserEmailUtils.getPrimaryEmail(this));
        mEmailTextField.setSelection(EditTextUtils.getText(mEmailTextField).length());
        mPasswordTextField = (OpenSansEditText) findViewById(R.id.password);
        mIvPswVisibility = (ImageView) findViewById(R.id.password_visibility);

        mEmailTextField.addTextChangedListener(this);
        mPasswordTextField.addTextChangedListener(this);

        mEmailTextField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    autoCorrectEmail();
                }
            }
        });
        initPasswordVisibilityButton(mIvPswVisibility, mPasswordTextField);
    }

    private final View.OnClickListener mSignUpClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            validateAndCreateUserAndBlog();
        }
    };

    private final TextView.OnEditorActionListener mEditorAction = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            return onDoneEvent(actionId, event);
        }
    };

    @Override
    protected void onDoneAction() {
        validateAndCreateUserAndBlog();
    }

    @Override
    protected boolean isUserDataValid() {
        // try to create the user
        final String email = EditTextUtils.getText(mEmailTextField).trim();
        final String password = EditTextUtils.getText(mPasswordTextField).trim();
        boolean retValue = true;

        if (email.equals("")) {
            showEmailError(R.string.required_field);
            retValue = false;
        }

        final Pattern emailRegExPattern = Patterns.EMAIL_ADDRESS;
        Matcher matcher = emailRegExPattern.matcher(email);
        if (!matcher.find() || email.length() > 100) {
            showEmailError(R.string.invalid_email_message);
            retValue = false;
        }


        if (password.equals("")) {
            showPasswordError(R.string.required_field);
            retValue = false;
        }

        if (password.length() < 6) {
            showPasswordError(R.string.invalid_password_message);
            retValue = false;
        }

        return retValue;
    }

    //判断账号密码是否为空
    private boolean fieldsFilled() {
        return EditTextUtils.getText(mEmailTextField).trim().length() > 0
                && EditTextUtils.getText(mPasswordTextField).trim().length() > 0;
    }

    protected void startProgress(String message) {
        mProgressBarSignIn.setVisibility(View.VISIBLE);
        mProgressTextSignIn.setVisibility(View.VISIBLE);
        mSignUpButton.setVisibility(View.GONE);
        mProgressBarSignIn.setEnabled(false);
        mProgressTextSignIn.setText(message);
        mEmailTextField.setEnabled(false);
        mPasswordTextField.setEnabled(false);
    }

    protected void updateProgress(String message) {
        mProgressTextSignIn.setText(message);
    }

    protected void endProgress() {
        mProgressBarSignIn.setVisibility(View.GONE);
        mProgressTextSignIn.setVisibility(View.GONE);
        mSignUpButton.setVisibility(View.VISIBLE);
        mEmailTextField.setEnabled(true);
        mPasswordTextField.setEnabled(true);
    }

    private void finishThisStuff(String username, String password) {
        Intent intent = new Intent();
        intent.putExtra("username", username);
        intent.putExtra("password", password);
        setResult(RESULT_OK, intent);
        finish();
        PersistentEditTextHelper persistentEditTextHelper = new PersistentEditTextHelper(SignUpActivity.this);
        persistentEditTextHelper.clearSavedText(mEmailTextField, null);
    }

    protected boolean specificShowError(int messageId) {
        switch (getErrorType(messageId)) {
            case PASSWORD:
                showPasswordError(messageId);
                return true;
            case EMAIL:
                showEmailError(messageId);
                return true;
        }
        return false;
    }

    private void showPasswordError(int messageId) {
        mPasswordTextField.setError(getString(messageId));
        mPasswordTextField.requestFocus();
    }

    private void showEmailError(int messageId) {
        mEmailTextField.setError(getString(messageId));
        mEmailTextField.requestFocus();
    }

    private void autoCorrectEmail() {
        if (mEmailAutoCorrected) {
            return;
        }
        final String email = EditTextUtils.getText(mEmailTextField).trim();
        String suggest = mEmailChecker.suggestDomainCorrection(email);
        if (suggest.compareTo(email) != 0) {
            mEmailAutoCorrected = true;
            mEmailTextField.setText(suggest);
            mEmailTextField.setSelection(suggest.length());
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (fieldsFilled()) {
            mSignUpButton.setEnabled(true);
        } else {
            mSignUpButton.setEnabled(false);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private void validateAndCreateUserAndBlog() {
        if (mSystemService.getActiveNetworkInfo() == null) {
            AlertUtils.showAlert(SignUpActivity.this, R.string.no_network_title, R.string.no_network_message);
            return;
        }
        if (!isUserDataValid()) {
            return;
        }

        // Prevent double tapping of the "done" btn in keyboard for those clients that don't dismiss the keyboard.
        // Samsung S4 for example
        if (View.VISIBLE == mProgressBarSignIn.getVisibility()) {
            return;
        }

        startProgress(getString(R.string.validating_user_data));

        final String email = EditTextUtils.getText(mEmailTextField).trim();
        final String password = EditTextUtils.getText(mPasswordTextField).trim();
        Map<String, String> map = getParams(email, password);
        RetrofitUtil.getInstance()
                .setBaseUrl(RetrofitUtil.RETROFITUTIL_BASE_URL)
                .setTimeout(100000)
                .build()
                .register(map, new ImpRegister() {
                    @Override
                    public void onSuccess(SuccessBean bean) {
                        endProgress();
                        finishThisStuff(email, password);
                    }

                    @Override
                    public void onFail(String msg) {
                        endProgress();
                        showError(msg);
                    }
                });

    }

    private Map<String, String> getParams(String email, String pwd) {
        Map<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("pwd", pwd);
        return map;
    }

}
