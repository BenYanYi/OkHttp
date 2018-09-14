package com.mylove.okhttp;

/**
 * @author BenYanYi
 * @date 2018/9/14 10:24
 * @email ben@yanyi.red
 * @overview
 */
public interface OnDownloadCallBack {
    void onDownloading(int progress);

    void onSuccess(String filePath);

    void onFailure(Throwable throwable);
}
