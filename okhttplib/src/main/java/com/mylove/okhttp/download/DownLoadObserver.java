package com.mylove.okhttp.download;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author BenYanYi
 * @date 2018/11/29 15:17
 * @email ben@yanyi.red
 * @overview
 */
public abstract class DownLoadObserver implements Observer<DownloadInfo> {
    protected Disposable d;//可以用于取消注册的监听者
    protected DownloadInfo downloadInfo;
    @Override
    public void onSubscribe(Disposable d) {
        this.d = d;
    }

    @Override
    public void onNext(DownloadInfo downloadInfo) {
        this.downloadInfo = downloadInfo;
    }

    @Override
    public void onError(Throwable e) {
        e.printStackTrace();
    }
}
