package com.mylove.okhttp;

/**
 * @author myLove
 * @time 2017/11/2 14:51
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

public interface onOkHttpListener<K, T> {
    void onCompleted();

    void onSuccess(K k);

    void onFailure(T t);
}
