package com.mylove.okhttp.download;

import com.mylove.okhttp.DownloadBean;

/**
 * @author BenYanYi
 * @date 2018/12/03 10:18
 * @email ben@yanyi.red
 * @overview
 */
public abstract class UpdateObserver {

    protected abstract void onSuccess(DownloadBean downloadBean);

    protected void onComplete() {

    }

    public abstract void onError(Throwable e);

    protected void onDialogDismiss() {
    }
}
