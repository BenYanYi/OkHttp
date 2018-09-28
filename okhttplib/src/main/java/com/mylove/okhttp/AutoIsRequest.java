package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * @author myLove
 */

public class AutoIsRequest {
    @SuppressLint("StaticFieldLeak")
    private static AutoIsRequest instance;
    private static String url;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static AutoIsRequest getInstance(Context context, String str) {
        if (instance == null) {
            synchronized (AutoIsRequest.class) {
                if (instance == null) {
                    instance = new AutoIsRequest();
                }
            }
        }
        mContext = context;
        url = str;
        return instance;
    }

    public void sync(onOkHttpCallBack onOkHttpCallBack) {
        ObservableIsRequest.getInstance(mContext, CallType.SYNC).request(url, onOkHttpCallBack);
    }

    public <T> void sync(Class<T> tClass, onOkHttpCallBack onOkHttpCallBack) {
        ObservableIsRequests request = ObservableIsRequests.getInstance(mContext, CallType.SYNC);
        request.tClass = tClass;
        request.request(url, onOkHttpCallBack);
    }

    public void async(onOkHttpCallBack onOkHttpCallBack) {
        ObservableIsRequest.getInstance(mContext, CallType.ASYNC).request(url, onOkHttpCallBack);
    }

    public <T> void async(Class<T> tClass, onOkHttpCallBack onOkHttpCallBack) {
        ObservableIsRequests request = ObservableIsRequests.getInstance(mContext, CallType.ASYNC);
        request.tClass = tClass;
        request.request(url, onOkHttpCallBack);
    }
}
