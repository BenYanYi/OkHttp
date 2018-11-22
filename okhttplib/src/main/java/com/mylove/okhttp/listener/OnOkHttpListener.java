package com.mylove.okhttp.listener;

/**
 * @author myLove
 */

public interface OnOkHttpListener {
    void onCompleted();

    <T> void onSuccess(T message);

    void onFailure(Throwable t);
}
