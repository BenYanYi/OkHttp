package com.mylove.okhttp;

/**
 * @author myLove
 */

public interface onOkHttpListener {
    void onCompleted();

    void onSuccess(String message);

    void onFailure(Throwable t);
}
