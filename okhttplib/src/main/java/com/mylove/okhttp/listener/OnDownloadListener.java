package com.mylove.okhttp.listener;

/**
 * @author BenYanYi
 * @date 2018/9/11 11:27
 * @email ben@yanyi.red
 * @overview
 */
public interface OnDownloadListener extends OnOkHttpListener {
    /**
     * 下载中
     */
    void onDownloading();
}
