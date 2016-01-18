package com.leanote.android.model;

import com.leanote.android.datasets.AccountTable;

/**
 * Class for managing logged in user informations.
 */
public class Account extends AccountModel {

//    public void fetchAccountDetails() {
//        RestRequest.Listener listener = new RestRequest.Listener() {
//            @Override
//            public void onResponse(JSONObject jsonObject) {
//                if (jsonObject != null) {
//                    updateFromRestResponse(jsonObject);
//                    save();
//
//                    ReaderUserTable.addOrUpdateUser(ReaderUser.fromJson(jsonObject));
//                }
//            }
//        };
//
//        RestRequest.ErrorListener errorListener = new RestRequest.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError volleyError) {
//                AppLog.e(T.API, volleyError);
//            }
//        };
//
//        Leanote.getRestClientUtilsV1_1().get("me", listener, errorListener);
//    }


    public void signout() {
        //init();
        clearToken();
        save();
    }

    public void save() {
        AccountTable.save(this);
    }
}
