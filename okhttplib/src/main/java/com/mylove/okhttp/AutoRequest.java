package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.Map;

/**
 * @author myLove
 */

public class AutoRequest {
    @SuppressLint("StaticFieldLeak")
    private static AutoRequest instance;
    private static String url;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static RequestType requestType;

    public static AutoRequest getInstance(Context context, String str, RequestType type) {
        if (instance == null) {
            synchronized (AutoRequest.class) {
                if (instance == null) {
                    instance = new AutoRequest();
                }
            }
        }
        mContext = context;
        url = str;
        requestType = type;
        return instance;
    }

    public void sync(Map<Object, Object> oMap, onOkHttpListener onOkHttpListener) {
        ObservableRequest.getInstance(mContext, requestType, CallType.SYNC).request(url, oMap, onOkHttpListener);
    }

//    public <T> void sync(Map<Object, Object> oMap, Class<T> tClass, onOkHttpListener onOkHttpListener) {
//        ObservableRequests request = ObservableRequests.getInstance(mContext, requestType, CallType.SYNC);
//        request.tClass = tClass;
//        request.request(url, oMap, onOkHttpListener);
//    }

    public void async(Map<Object, Object> oMap, onOkHttpListener onOkHttpListener) {
        ObservableRequest.getInstance(mContext, requestType, CallType.ASYNC).request(url, oMap, onOkHttpListener);
    }

//    public <T> void async(Map<Object, Object> oMap, Class<T> tClass, onOkHttpListener onOkHttpListener) {
//        ObservableRequests request = ObservableRequests.getInstance(mContext, requestType, CallType.ASYNC);
//        request.tClass = tClass;
//        request.request(url, oMap, onOkHttpListener);
//    }
}
