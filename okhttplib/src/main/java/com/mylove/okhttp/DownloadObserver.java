package com.mylove.okhttp;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author BenYanYi
 * @date 2018/11/29 17:24
 * @email ben@yanyi.red
 * @overview
 */
public abstract class DownloadObserver implements Observer<DownloadBean> {
    protected Disposable d;

    @Override
    public void onComplete() {

    }

    @Override
    public void onSubscribe(Disposable d) {
        this.d = d;
    }

    @Override
    public void onError(Throwable e) {

    }

}
