package com.mylove.okhttp;

/**
 * @author myLove
 * @time 2017/11/2 14:51
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

public interface onOkHttpListener {
    void onCompleted();

    void onSuccess(ResultMsg requestMsg);

    void onFailure(Throwable t);
}
