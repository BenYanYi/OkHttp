package com.mylove.okhttp;

import android.content.Context;
import android.support.annotation.NonNull;

import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author myLove
 */

class Cache {
    /**
     * 设置缓存路径，以及缓存文件大小
     */
    static okhttp3.Cache privateCache(Context mContext) {
        return new okhttp3.Cache(mContext.getCacheDir(), 1024 * 1024);
    }

    static HttpLoggingInterceptor HTTP_LOGGING_INTERCEPTOR =
            new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(@NonNull String message) {
                    if (OkHttpInfo.isLOG) {
                        LogHelper.d( message);
                    }
                }
            }).setLevel(HttpLoggingInterceptor.Level.BODY);
}
