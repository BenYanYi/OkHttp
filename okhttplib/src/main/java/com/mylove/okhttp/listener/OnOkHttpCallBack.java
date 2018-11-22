package com.mylove.okhttp.listener;

import okhttp3.Request;

/**
 * @author BenYanYi
 * @date 2018/9/28 15:36
 * @email ben@yanyi.red
 * @overview
 */
public interface OnOkHttpCallBack extends OnOkHttpListener {
    Request setRequest(String url);
}
