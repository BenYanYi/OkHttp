package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.Map;

/**
 * @author myLove
 * @time 2018/1/4 15:14
 * @e-mail love@yanyi.red
 * @overview
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

    public void sync(String urlMsg, String labelStr, Map<Object, Object> map, onOkHttpListener onOkHttpListener) {
        ObservableRequest.getInstance(urlMsg, labelStr, mContext, requestType, CallType.SYNC).request(url, map, onOkHttpListener);
    }

    public void async(String urlMsg, String labelStr, Map<Object, Object> map, onOkHttpListener onOkHttpListener) {
        ObservableRequest.getInstance(urlMsg, labelStr, mContext, requestType, CallType.ASYNC).request(url, map, onOkHttpListener);
    }
}
