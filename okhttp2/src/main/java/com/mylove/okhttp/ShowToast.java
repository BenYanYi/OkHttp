package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

/**
 * @author myLove
 * @time 2017/10/31 12:24
 * @e-mail mylove.520.y@gmail.com
 * @overview 吐司工具类
 */

class ShowToast {
    @SuppressLint("StaticFieldLeak")
    private static ShowToast instance;
    private static Context mContext;
    private static boolean visibleBoo = true;


    public static ShowToast getInstance(Context context) {
        if (mContext != null) {
            visibleBoo = mContext != context;
        }
        mContext = context;
        if (instance == null) {
            synchronized (ShowToast.class) {
                instance = new ShowToast();
            }
        }
        return instance;
    }

    void show(String msg) {
        if (visibleBoo) {
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        }
    }
}