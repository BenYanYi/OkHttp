package com.mylove.okhttp;

import android.app.Application;
import android.content.Context;

import com.mylove.loglib.JLog;

/**
 * @author myLove
 * @time 2017/11/16 19:11
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

public class AppContext extends Application {

    public static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        JLog.init(true);
        mContext = this;
    }
}
