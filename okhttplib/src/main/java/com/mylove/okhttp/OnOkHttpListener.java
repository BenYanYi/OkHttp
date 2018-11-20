package com.mylove.okhttp;

/**
 * @author myLove
 */

public interface OnOkHttpListener {
    void onCompleted();

    <T> void onSuccess(T message);

    void onFailure(Throwable t);
}
