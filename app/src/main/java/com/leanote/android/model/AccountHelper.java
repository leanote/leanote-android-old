package com.leanote.android.model;


import android.text.TextUtils;

import com.leanote.android.db.LeanoteDbManager;


/**
 * The app supports only one WordPress.com account at the moment, so we might use getDefaultAccount() everywhere we
 * need the account data.
 */
public class AccountHelper {

    private static AccountHelper sAccountHelper;

    public static AccountHelper getInstance() {
        if (sAccountHelper == null) {
            synchronized (AccountHelper.class) {
                if (sAccountHelper == null) {
                    sAccountHelper = new AccountHelper();
                }
            }
        }
        return sAccountHelper;
    }

    public AccountHelper() {
        sAccount = new Account();
    }

    public Account getAccount() {
        return sAccount;
    }

    public void setAccount(Account account) {
        AccountHelper.sAccount = account;
        sAccount.save();
    }

    private static Account sAccount;

    public static Account getDefaultAccount() {
        if (sAccount == null) {
            sAccount = LeanoteDbManager.getInstance().getAccountByLocalId(0);
            if (sAccount == null) {
                sAccount = new Account();
            }
        }
        return sAccount;
    }

    public static boolean isSignedIn() {
        return !TextUtils.isEmpty(getDefaultAccount().getAccessToken());
    }


}
