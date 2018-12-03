package com.mylove.okhttp.download;

import com.mylove.okhttp.DownloadBean;
import com.mylove.okhttp.DownloadObserver;
import com.mylove.okhttp.LogHelper;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * @author BenYanYi
 * @date 2018/12/03 10:18
 * @email ben@yanyi.red
 * @overview
 */
public abstract class UpdateObserver extends DownloadObserver implements Observer<DownloadBean> {
    @Override
    public void onSubscribe(Disposable d) {
        super.onSubscribe(d);
    }

    @Override
    public void onComplete() {
        super.onComplete();
    }

    @Override
    public void onError(Throwable e) {
        super.onError(e);
        LogHelper.e("UpdateUtil--->>" + e.getMessage());
    }

    protected void onDialogDismiss() {
    }
}
