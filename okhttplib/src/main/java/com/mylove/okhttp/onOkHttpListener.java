package com.mylove.okhttp;

/**
 * @author myLove
 */

public interface onOkHttpListener {
    void onCompleted();

    <T> void onSuccess(T message);

    void onFailure(Throwable t);
}
