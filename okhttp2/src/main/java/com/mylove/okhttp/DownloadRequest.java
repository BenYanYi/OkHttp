package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.Map;

/**
 * @author myLove
 * @time 2017/11/24 13:36
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

public class DownloadRequest {
    @SuppressLint("StaticFieldLeak")
    private static DownloadRequest instance;
    private static String url;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static RequestType requestType;

    public static DownloadRequest getInstance(Context context, String str, RequestType type) {
        if (instance == null) {
            synchronized (DownloadRequest.class) {
                if (instance == null) {
                    instance = new DownloadRequest();
                    mContext = context;
                    url = str;
                    requestType = type;
                }
            }
        }
        return instance;
    }

    public void sync(Map<Object, Object> oMap, onOkHttpListener onOkHttpListener) {
        DownloadObservable.getInstance(mContext, requestType, CallType.SYNC).request(url, oMap, onOkHttpListener);
    }

    public void sync(Map<Object, Object> oMap, String fileName, onOkHttpListener onOkHttpListener) {
        DownloadObservable.getInstance(mContext, fileName, requestType, CallType.SYNC).request(url, oMap, onOkHttpListener);
    }

    public void async(Map<Object, Object> oMap, onOkHttpListener onOkHttpListener) {
        DownloadObservable.getInstance(mContext, requestType, CallType.ASYNC).request(url, oMap, onOkHttpListener);
    }

    public void async(Map<Object, Object> oMap, String fileName, onOkHttpListener onOkHttpListener) {
        DownloadObservable.getInstance(mContext, fileName, requestType, CallType.ASYNC).request(url, oMap, onOkHttpListener);
    }
}
