package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * @author myLove
 */

public class OkHttpUtil {
    @SuppressLint("StaticFieldLeak")
    private static OkHttpUtil instance;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static OkHttpUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (OkHttpUtil.class) {
                if (instance == null) {
                    instance = new OkHttpUtil();
                }
            }
        }
        mContext = context;
        return instance;
    }

    public AutoRequest get(String url) {
        return AutoRequest.getInstance(mContext, url, RequestType.GET);
    }

    public AutoRequest post(String url) {
        return AutoRequest.getInstance(mContext, url, RequestType.POST);
    }

    public XMLRequest postXMLToSoap(String url) {
        return XMLRequest.getInstance(mContext, url, RequestType.POST_XML_SOAP);
    }

    public AutoRequest postUpFile(String url) {
        return AutoRequest.getInstance(mContext, url, RequestType.UP_FILE);
    }

    public AutoRequest postAll(String url) {
        return AutoRequest.getInstance(mContext, url, RequestType.UP_FILE);
    }

    public DownloadRequest downloadFile(String url) {
        return DownloadRequest.getInstance(mContext, url, RequestType.GET);
    }
}
