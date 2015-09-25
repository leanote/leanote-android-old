package com.leanote.android.model;

import com.android.volley.VolleyError;
import com.leanote.android.Leanote;
import com.leanote.android.datasets.AccountTable;
import com.leanote.android.datasets.ReaderUserTable;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.AppLog.T;
import com.wordpress.rest.RestRequest;

import org.json.JSONObject;

/**
 * Class for managing logged in user informations.
 */
public class Account extends AccountModel {

    public void fetchAccountDetails() {
        RestRequest.Listener listener = new RestRequest.Listener() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                if (jsonObject != null) {
                    updateFromRestResponse(jsonObject);
                    save();

                    ReaderUserTable.addOrUpdateUser(ReaderUser.fromJson(jsonObject));
                }
            }
        };

        RestRequest.ErrorListener errorListener = new RestRequest.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                AppLog.e(T.API, volleyError);
            }
        };

        Leanote.getRestClientUtilsV1_1().get("me", listener, errorListener);
    }


    public void signout() {
        init();
        save();
    }

    public void save() {
        AccountTable.save(this);
    }
}
