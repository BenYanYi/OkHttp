package com.mylove.okhttp.download;

/**
 * @author BenYanYi
 * @date 2018/11/29 16:29
 * @email ben@yanyi.red
 * @overview
 */
public interface OnDownloadCallBack {

    void onNext(DownloadInfo downloadInfo);

    void onError(Throwable e);

    void onComplete();
}
