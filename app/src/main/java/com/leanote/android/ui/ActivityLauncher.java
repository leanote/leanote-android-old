package com.leanote.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.leanote.android.networking.SSLCertsViewActivity;
import com.leanote.android.networking.SelfSignedSSLCertsManager;
import com.leanote.android.ui.accounts.NewAccountActivity;
import com.leanote.android.util.AppLog;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Created by binnchx on 8/27/15.
 */
public class ActivityLauncher {

    public static void showSignInForResult(Activity activity) {
        Intent intent = new Intent(activity, SignInActivity.class);
        activity.startActivityForResult(intent, RequestCodes.ADD_ACCOUNT);
    }

    public static void newAccountForResult(Activity activity) {
        Intent intent = new Intent(activity, NewAccountActivity.class);
        activity.startActivityForResult(intent, SignInActivity.CREATE_ACCOUNT_REQUEST);
    }

    public static void viewSSLCerts(Context context) {
        try {
            Intent intent = new Intent(context, SSLCertsViewActivity.class);
            SelfSignedSSLCertsManager selfSignedSSLCertsManager = SelfSignedSSLCertsManager.getInstance(context);
            String lastFailureChainDescription =
                    selfSignedSSLCertsManager.getLastFailureChainDescription().replaceAll("\n", "<br/>");
            intent.putExtra(SSLCertsViewActivity.CERT_DETAILS_KEYS, lastFailureChainDescription);
            context.startActivity(intent);
        } catch (GeneralSecurityException e) {
            AppLog.e(AppLog.T.API, e);
        } catch (IOException e) {
            AppLog.e(AppLog.T.API, e);
        }
    }

}
