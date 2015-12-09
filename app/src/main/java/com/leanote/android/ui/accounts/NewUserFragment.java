package com.leanote.android.ui.accounts;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.leanote.android.R;
import com.leanote.android.networking.NetworkRequest;
import com.leanote.android.ui.AbstractFragment;
import com.leanote.android.util.AlertUtils;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.EditTextUtils;
import com.leanote.android.util.RegisterResult;
import com.leanote.android.util.UserEmailUtils;
import com.leanote.android.widget.LeaTextView;
import com.leanote.android.widget.PersistentEditTextHelper;

import org.json.JSONObject;
import org.wordpress.emailchecker.EmailChecker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by binnchx on 12/9/15.
 */
public class NewUserFragment extends AbstractFragment implements TextWatcher {
    private EditText mEmailTextField;
    private EditText mPasswordTextField;
    private LeaTextView mSignupButton;
    private LeaTextView mProgressTextSignIn;
    private RelativeLayout mProgressBarSignIn;
    private EmailChecker mEmailChecker;
    private boolean mEmailAutoCorrected;
    private boolean mAutoCompleteUrl;

    public NewUserFragment() {
        mEmailChecker = new EmailChecker();
    }

    @Override
    public void afterTextChanged(Editable s) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (fieldsFilled()) {
            mSignupButton.setEnabled(true);
        } else {
            mSignupButton.setEnabled(false);
        }
    }

    private boolean fieldsFilled() {
        return EditTextUtils.getText(mEmailTextField).trim().length() > 0
                && EditTextUtils.getText(mPasswordTextField).trim().length() > 0;
    }

    protected void startProgress(String message) {
        mProgressBarSignIn.setVisibility(View.VISIBLE);
        mProgressTextSignIn.setVisibility(View.VISIBLE);
        mSignupButton.setVisibility(View.GONE);
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
        mSignupButton.setVisibility(View.VISIBLE);
        mEmailTextField.setEnabled(true);
        mPasswordTextField.setEnabled(true);
    }

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

    protected void onDoneAction() {
        validateAndCreateUserAndBlog();
    }

    private final View.OnClickListener mSignupClickListener = new View.OnClickListener() {
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


    private void finishThisStuff(String username, String password) {
        final Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent();
            intent.putExtra("username", username);
            intent.putExtra("password", password);
            activity.setResult(NewAccountActivity.RESULT_OK, intent);
            activity.finish();
            PersistentEditTextHelper persistentEditTextHelper = new PersistentEditTextHelper(getActivity());
            persistentEditTextHelper.clearSavedText(mEmailTextField, null);
        }
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


    private void validateAndCreateUserAndBlog() {
        if (mSystemService.getActiveNetworkInfo() == null) {
            AlertUtils.showAlert(getActivity(), R.string.no_network_title, R.string.no_network_message);
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

        new RegisterTask(email, password).execute();
        //注册

    }

    private class RegisterTask extends AsyncTask<Void, Void, RegisterResult> {
        private String email;
        private String password;

        public RegisterTask(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        protected RegisterResult doInBackground(Void... params) {
            String api = String.format("http://leanote.com/api/auth/register?email=%s&pwd=%s",
                    email, password);
            RegisterResult result = new RegisterResult(false, "");

            try {
                String response = NetworkRequest.syncGetRequest(api);
                AppLog.i("response:" + response);
                JSONObject json = new JSONObject(response);
                boolean succ = json.getBoolean("Ok");
                result.setSuccess(succ);
                if (!succ) {
                    result.setMsg(json.getString("Msg"));
                }
            } catch (Exception e) {
                e.printStackTrace();
                result.setMsg("unknown error");
            }
            return result;
        }

        @Override
        protected void onPostExecute(RegisterResult result) {
            super.onPostExecute(result);
            endProgress();
            if (isAdded()) {
                if (result.isSuccess()) {
                    finishThisStuff(email, password);
                } else {
                    showError(result.getMsg());
                }
            }

        }
    }


    private void autocorrectEmail() {
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout containing a title and body text.
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.new_account_user_fragment_screen, container, false);

        mSignupButton = (LeaTextView) rootView.findViewById(R.id.signup_button);
        mSignupButton.setOnClickListener(mSignupClickListener);
        mSignupButton.setEnabled(false);

        mProgressTextSignIn = (LeaTextView) rootView.findViewById(R.id.nux_sign_in_progress_text);
        mProgressBarSignIn = (RelativeLayout) rootView.findViewById(R.id.nux_sign_in_progress_bar);

        mEmailTextField = (EditText) rootView.findViewById(R.id.email_address);
        mEmailTextField.setText(UserEmailUtils.getPrimaryEmail(getActivity()));
        mEmailTextField.setSelection(EditTextUtils.getText(mEmailTextField).length());
        mPasswordTextField = (EditText) rootView.findViewById(R.id.password);

        mEmailTextField.addTextChangedListener(this);
        mPasswordTextField.addTextChangedListener(this);



        mEmailTextField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    autocorrectEmail();
                }
            }
        });
        initPasswordVisibilityButton(rootView, mPasswordTextField);
        return rootView;
    }

}