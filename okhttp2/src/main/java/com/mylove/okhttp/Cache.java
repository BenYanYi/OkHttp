package com.mylove.okhttp;

import android.content.Context;

/**
 * @author myLove
 * @time 2017/12/26 17:23
 * @e-mail love@yanyi.red
 * @overview
 */

class Cache {
    /**
     * 设置缓存路径，以及缓存文件大小
     */
    static okhttp3.Cache privateCache(Context mContext) {
        return new okhttp3.Cache(mContext.getCacheDir(), 1024 * 1024);
    }
}
