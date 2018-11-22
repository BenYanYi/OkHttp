package com.mylove.okhttp.listener;

/**
 * @author BenYanYi
 * @date 2018/11/20 17:05
 * @email ben@yanyi.red
 * @overview
 */
public interface OnDownloadFileCallBack {
    void onDownloading(int progress);

    void onSuccess();
}
