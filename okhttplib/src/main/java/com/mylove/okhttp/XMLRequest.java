package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.Map;

/**
 * @author myLove
 */

public class XMLRequest {
    @SuppressLint("StaticFieldLeak")
    private static XMLRequest instance;
    private static String url;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static RequestType requestType;

    private XMLRequest() {
    }

    public static XMLRequest getInstance(Context context, String str, RequestType type) {
        if (instance == null) {
            synchronized (XMLRequest.class) {
                if (instance == null) {
                    instance = new XMLRequest();
                }
            }
        }
        mContext = context;
        url = str;
        requestType = type;
        return instance;
    }

    public void sync(Map<Object, Object> map, OnOkHttpListener OnOkHttpListener) {
        ObservableRequest.getInstance(mContext, requestType, CallType.SYNC).request(url, map, OnOkHttpListener);
    }

    public void async(Map<Object, Object> map, OnOkHttpListener OnOkHttpListener) {
        ObservableRequest.getInstance(mContext, requestType, CallType.ASYNC).request(url, map, OnOkHttpListener);
    }
}
