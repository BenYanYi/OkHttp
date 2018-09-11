package com.mylove.okhttp;

/**
 * @author BenYanYi
 * @date 2018/9/11 11:27
 * @email ben@yanyi.red
 * @overview
 */
public interface OnDownloadListener extends onOkHttpListener {
    /**
     * @param progress 下载进度
     */
    void onDownloading(int progress);
}
