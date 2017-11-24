package com.mylove.okhttp;

/**
 * @author myLove
 * @time 2017/11/24 13:57
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

public interface onOkHttpDownload<K, T> extends onOkHttpListener<K, T> {
    void onDownloading(int progress);
}
