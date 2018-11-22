package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;

import com.mylove.okhttp.listener.OnOkHttpCallBack;

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

    public void sync(OnOkHttpCallBack OnOkHttpCallBack) {
        ObservableIsRequest.getInstance(mContext, CallType.SYNC).request(url, OnOkHttpCallBack);
    }

    public <T> void sync(Class<T> tClass, OnOkHttpCallBack OnOkHttpCallBack) {
        ObservableIsRequests request = ObservableIsRequests.getInstance(mContext, CallType.SYNC);
        request.tClass = tClass;
        request.request(url, OnOkHttpCallBack);
    }

    public void async(OnOkHttpCallBack OnOkHttpCallBack) {
        ObservableIsRequest.getInstance(mContext, CallType.ASYNC).request(url, OnOkHttpCallBack);
    }

    public <T> void async(Class<T> tClass, OnOkHttpCallBack OnOkHttpCallBack) {
        ObservableIsRequests request = ObservableIsRequests.getInstance(mContext, CallType.ASYNC);
        request.tClass = tClass;
        request.request(url, OnOkHttpCallBack);
    }
}
