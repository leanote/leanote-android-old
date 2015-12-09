package com.leanote.android.util;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Created by binnchx on 12/9/15.
 */

public class AlertUtils {
    /**
     * Show Alert Dialog
     * @param context
     * @param titleId
     * @param messageId
     */
    public static void showAlert(Context context, int titleId, int messageId) {
        Dialog dlg = new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setPositiveButton(android.R.string.ok, null)
                .setMessage(messageId)
                .create();

        dlg.show();
    }

    /**
     * Show Alert Dialog
     * @param context
     * @param titleId
     */
    public static void showAlert(Context context, int titleId, String message) {
        Dialog dlg = new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setPositiveButton(android.R.string.ok, null)
                .setMessage(message)
                .create();

        dlg.show();
    }

    /**
     * Show Alert Dialog
     * @param context
     * @param titleId
     * @param messageId
     * @param positiveButtontxt
     * @param positiveListener
     * @param negativeButtontxt
     * @param negativeListener
     */
    public static void showAlert(Context context, int titleId, int messageId,
                                 CharSequence positiveButtontxt, DialogInterface.OnClickListener positiveListener,
                                 CharSequence negativeButtontxt, DialogInterface.OnClickListener negativeListener) {
        Dialog dlg = new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setPositiveButton(positiveButtontxt, positiveListener)
                .setNegativeButton(negativeButtontxt, negativeListener)
                .setMessage(messageId)
                .setCancelable(false)
                .create();

        dlg.show();
    }

    /**
     * Show Alert Dialog
     * @param context
     * @param titleId
     * @param positiveButtontxt
     * @param positiveListener
     */
    public static void showAlert(Context context, int titleId, String message,
                                 CharSequence positiveButtontxt, DialogInterface.OnClickListener positiveListener) {
        Dialog dlg = new AlertDialog.Builder(context)
                .setTitle(titleId)
                .setPositiveButton(positiveButtontxt, positiveListener)
                .setMessage(message)
                .setCancelable(false)
                .create();

        dlg.show();
    }
}