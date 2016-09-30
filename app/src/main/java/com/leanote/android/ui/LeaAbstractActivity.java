package com.leanote.android.ui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.android.volley.VolleyError;
import com.leanote.android.R;
import com.leanote.android.ui.fragment.SignInDialogFragment;
import com.leanote.android.util.AppLog;
import com.wordpress.rest.RestRequest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by yuchuan
 * DATE 7/27/16
 * TIME 15:48
 */
public abstract class LeaAbstractActivity extends Activity {

    protected ConnectivityManager mSystemService;
    protected boolean mPasswordVisible;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        mSystemService = (ConnectivityManager) getApplicationContext().
                getSystemService(Context.CONNECTIVITY_SERVICE);

        setupViews();
    }

    protected abstract int getLayoutId();

    protected abstract void setupViews();

    protected abstract void onDoneAction();

    //用户登录注册信息是否符合要求
    protected abstract boolean isUserDataValid();

    protected void startProgress(String message) {
    }

    protected void updateProgress(String message) {
    }

    protected void endProgress() {
    }

    protected boolean onDoneEvent(int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || event != null && (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            if (!isUserDataValid()) {
                return true;
            }

            // hide keyboard before calling the done action
            InputMethodManager inputManager = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            View view = getCurrentFocus();
            if (view != null) {
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

            // call child action
            onDoneAction();
            return true;
        }
        return false;
    }

    protected void initPasswordVisibilityButton(final ImageView ivPswVisibility, final EditText passwordEditText) {
        if (ivPswVisibility == null) {
            return;
        }
        ivPswVisibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPasswordVisible = !mPasswordVisible;
                if (mPasswordVisible) {
                    ivPswVisibility.setImageResource(R.drawable.show_pwd);
                    ivPswVisibility.setColorFilter(v.getContext().getResources().getColor(R.color.nux_eye_icon_color_open));
                    passwordEditText.setTransformationMethod(null);
                } else {
                    ivPswVisibility.setImageResource(R.drawable.not_show_pwd);
                    ivPswVisibility.setColorFilter(v.getContext().getResources().getColor(R.color.nux_eye_icon_color_closed));
                    passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                passwordEditText.setSelection(passwordEditText.length());
            }
        });
    }

    protected boolean specificShowError(int messageId) {
        return false;
    }

    protected void showError(int messageId) {
        if (specificShowError(messageId)) {
            return;
        }
        // Failback if it's not a specific error
        showError(getString(messageId));
    }

    protected void showError(String message) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        SignInDialogFragment nuxAlert = SignInDialogFragment.newInstance(getString(R.string.error), message,
                R.drawable.noticon_alert_big, getString(R.string.nux_tap_continue));
        ft.add(nuxAlert, "alert");
        ft.commitAllowingStateLoss();
    }

    protected ErrorType getErrorType(int messageId) {
        if (messageId == R.string.username_only_lowercase_letters_and_numbers ||
                messageId == R.string.username_required || messageId == R.string.username_not_allowed ||
                messageId == R.string.username_must_be_at_least_four_characters ||
                messageId == R.string.username_contains_invalid_characters ||
                messageId == R.string.username_must_include_letters || messageId == R.string.username_exists ||
                messageId == R.string.username_reserved_but_may_be_available ||
                messageId == R.string.username_invalid) {
            return ErrorType.USERNAME;
        } else if (messageId == R.string.password_invalid) {
            return ErrorType.PASSWORD;
        } else if (messageId == R.string.email_cant_be_used_to_signup || messageId == R.string.email_invalid ||
                messageId == R.string.email_not_allowed || messageId == R.string.email_exists ||
                messageId == R.string.email_reserved) {
            return ErrorType.EMAIL;
        } else if (messageId == R.string.blog_name_required || messageId == R.string.blog_name_not_allowed ||
                messageId == R.string.blog_name_must_be_at_least_four_characters ||
                messageId == R.string.blog_name_must_be_less_than_sixty_four_characters ||
                messageId == R.string.blog_name_contains_invalid_characters ||
                messageId == R.string.blog_name_cant_be_used ||
                messageId == R.string.blog_name_only_lowercase_letters_and_numbers ||
                messageId == R.string.blog_name_must_include_letters || messageId == R.string.blog_name_exists ||
                messageId == R.string.blog_name_reserved ||
                messageId == R.string.blog_name_reserved_but_may_be_available ||
                messageId == R.string.blog_name_invalid) {
            return ErrorType.SITE_URL;
        } else if (messageId == R.string.blog_title_invalid) {
            return ErrorType.TITLE;
        }
        return ErrorType.UNDEFINED;
    }

    protected int getErrorMessageForErrorCode(String errorCode) {
        if (errorCode.equals("username_only_lowercase_letters_and_numbers")) {
            return R.string.username_only_lowercase_letters_and_numbers;
        }
        if (errorCode.equals("username_required")) {
            return R.string.username_required;
        }
        if (errorCode.equals("username_not_allowed")) {
            return R.string.username_not_allowed;
        }
        if (errorCode.equals("email_cant_be_used_to_signup")) {
            return R.string.email_cant_be_used_to_signup;
        }
        if (errorCode.equals("username_must_be_at_least_four_characters")) {
            return R.string.username_must_be_at_least_four_characters;
        }
        if (errorCode.equals("username_contains_invalid_characters")) {
            return R.string.username_contains_invalid_characters;
        }
        if (errorCode.equals("username_must_include_letters")) {
            return R.string.username_must_include_letters;
        }
        if (errorCode.equals("email_invalid")) {
            return R.string.email_invalid;
        }
        if (errorCode.equals("email_not_allowed")) {
            return R.string.email_not_allowed;
        }
        if (errorCode.equals("username_exists")) {
            return R.string.username_exists;
        }
        if (errorCode.equals("email_exists")) {
            return R.string.email_exists;
        }
        if (errorCode.equals("username_reserved_but_may_be_available")) {
            return R.string.username_reserved_but_may_be_available;
        }
        if (errorCode.equals("email_reserved")) {
            return R.string.email_reserved;
        }
        if (errorCode.equals("blog_name_required")) {
            return R.string.blog_name_required;
        }
        if (errorCode.equals("blog_name_not_allowed")) {
            return R.string.blog_name_not_allowed;
        }
        if (errorCode.equals("blog_name_must_be_at_least_four_characters")) {
            return R.string.blog_name_must_be_at_least_four_characters;
        }
        if (errorCode.equals("blog_name_must_be_less_than_sixty_four_characters")) {
            return R.string.blog_name_must_be_less_than_sixty_four_characters;
        }
        if (errorCode.equals("blog_name_contains_invalid_characters")) {
            return R.string.blog_name_contains_invalid_characters;
        }
        if (errorCode.equals("blog_name_cant_be_used")) {
            return R.string.blog_name_cant_be_used;
        }
        if (errorCode.equals("blog_name_only_lowercase_letters_and_numbers")) {
            return R.string.blog_name_only_lowercase_letters_and_numbers;
        }
        if (errorCode.equals("blog_name_must_include_letters")) {
            return R.string.blog_name_must_include_letters;
        }
        if (errorCode.equals("blog_name_exists")) {
            return R.string.blog_name_exists;
        }
        if (errorCode.equals("blog_name_reserved")) {
            return R.string.blog_name_reserved;
        }
        if (errorCode.equals("blog_name_reserved_but_may_be_available")) {
            return R.string.blog_name_reserved_but_may_be_available;
        }
        if (errorCode.equals("password_invalid")) {
            return R.string.password_invalid;
        }
        if (errorCode.equals("blog_name_invalid")) {
            return R.string.blog_name_invalid;
        }
        if (errorCode.equals("blog_title_invalid")) {
            return R.string.blog_title_invalid;
        }
        if (errorCode.equals("username_invalid")) {
            return R.string.username_invalid;
        }
        return 0;
    }

    protected enum ErrorType {USERNAME, PASSWORD, SITE_URL, EMAIL, TITLE, UNDEFINED}

    public class ErrorListener implements RestRequest.ErrorListener {
        @Override
        public void onErrorResponse(VolleyError error) {
            String message = null;
            int messageId;
            AppLog.e(AppLog.T.NUX, error);
            if (error.networkResponse != null && error.networkResponse.data != null) {
                AppLog.e(AppLog.T.NUX, String.format("Error message: %s", new String(error.networkResponse.data)));
                String jsonString = new String(error.networkResponse.data);
                try {
                    JSONObject errorObj = new JSONObject(jsonString);
                    messageId = getErrorMessageForErrorCode((String) errorObj.get("error"));
                    if (messageId == 0) {
                        // Not one of our common errors. Show the error message from the server.
                        message = (String) errorObj.get("message");
                    }
                } catch (JSONException e) {
                    AppLog.e(AppLog.T.NUX, e);
                    messageId = R.string.error_generic;
                }
            } else {
                if (error.getMessage() != null) {
                    if (error.getMessage().contains("Limit reached")) {
                        messageId = R.string.limit_reached;
                    } else {
                        messageId = R.string.error_generic;
                    }
                } else {
                    messageId = R.string.error_generic;
                }
            }
            endProgress();
            if (messageId == 0) {
                showError(message);
            } else {
                showError(messageId);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
