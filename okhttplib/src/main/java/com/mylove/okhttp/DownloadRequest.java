package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;

import com.mylove.okhttp.listener.OnDownloadListener;

/**
 * @author myLove
 */

public class DownloadRequest {
    @SuppressLint("StaticFieldLeak")
    private static DownloadRequest instance;
    private static String url;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;

    public static DownloadRequest getInstance(Context context, String str) {
        if (instance == null) {
            synchronized (DownloadRequest.class) {
                if (instance == null) {
                    instance = new DownloadRequest();
                }
            }
        }
        mContext = context;
        url = str;
        return instance;
    }

    /**
     * @param filePath           储存下载文件的SDCard目录
     * @param onDownloadListener 监听
     */
    @Deprecated
    public void download(String filePath, OnDownloadListener onDownloadListener) {
        //saveDir判断不能为空
        if (FormatUtil.isEmpty(filePath)) {
            throw new NullPointerException("filePath is the SDCard directory of the downloaded file, cannot be empty.");
        }
        DownloadObservable.getInstance(mContext).request(url, filePath, "", onDownloadListener);
    }

    /**
     * @param filePath           储存下载文件的SDCard目录
     * @param fileName           文件名称
     * @param onDownloadListener 监听
     */
    @Deprecated
    public void download(String filePath, String fileName, OnDownloadListener onDownloadListener) {
        //saveDir判断不能为空
        if (FormatUtil.isEmpty(filePath)) {
            throw new NullPointerException("filePath is the SDCard directory of the downloaded file, cannot be empty.");
        }
        DownloadObservable.getInstance(mContext).request(url, filePath, fileName, onDownloadListener);
    }

}
