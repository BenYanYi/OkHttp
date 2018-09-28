package com.mylove.okhttp;

import okhttp3.Request;

/**
 * @author BenYanYi
 * @date 2018/9/28 15:36
 * @email ben@yanyi.red
 * @overview
 */
public interface onOkHttpCallBack extends onOkHttpListener {
    Request setRequest(String url);
}
