package com.mylove.okhttp;

import android.util.Log;


/**
 * @author BenYanYi
 * @date 2018/9/12 15:27
 * @email ben@yanyi.red
 * @overview 打印日志
 */
public class LogHelper {

    public static void a(Object object) {
        Log.wtf(OkHttpInfo.TAG, object.toString());
    }

    public static void e(Object object) {
        Log.e(OkHttpInfo.TAG, object.toString());
    }

    public static void d(Object object) {
        Log.d(OkHttpInfo.TAG, object.toString());
    }

    public static void i(Object object) {
        Log.i(OkHttpInfo.TAG, object.toString());
    }

    public static void v(Object object) {
        Log.v(OkHttpInfo.TAG, object.toString());
    }

    public static void w(Object object) {
        Log.w(OkHttpInfo.TAG, object.toString());
    }
}
