package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Subscriber;

/**
 * @author myLove
 * @time 2017/11/15 13:52
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

class OkCall {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private String mCacheUrl;
    private Subscriber<? super String> subscriber;
    private Call call;
    @SuppressLint("StaticFieldLeak")
    private static OkCall instance;
    private CallType callType;
    private static OkHttpClient okHttpClient;

    private OkCall(String mCacheUrl, Request request, Subscriber<? super String> subscriber, CallType callType) {
        this.mCacheUrl = mCacheUrl;
        this.subscriber = subscriber;
        this.call = okHttpClient.newCall(request);
        this.callType = callType;
    }

    /**
     * okHttpClient初始化，并添加拦截及缓存
     *
     * @param context    上下文
     * @param mCacheUrl  缓存地址
     * @param request    请求
     * @param subscriber 返回
     * @param callType   请求类型
     * @return
     */
    public static OkCall getInstance(Context context, String mCacheUrl, Request request, Subscriber<? super String> subscriber, CallType callType) {
        if (instance == null) {
            synchronized (OkCall.class) {
                if (instance == null) {
                    mContext = context;
                    OkHttpClient httpClient = new OkHttpClient();
                    okHttpClient = httpClient.newBuilder()
                            .addNetworkInterceptor(new CacheInterceptor())
                            .cache(privateCache())
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build();
                    instance = new OkCall(mCacheUrl, request, subscriber, callType);
                }
            }
        }
        return instance;
    }

    /**
     * 请求
     */
    void sendCall() {
        if (callType == CallType.SYNC) {
            sync();
        } else if (callType == CallType.ASYNC) {
            async();
        }
    }

    /**
     * 异步请求
     */
    private void sync() {
        try {
            Response execute = call.execute();
            if (execute.isSuccessful()) {
                String str = execute.body().string();
                if (str.contains("<!DOCTYPE html>")) {
                    subscriber.onError(new Exception("接口错误"));
                } else {
                    if (FormatUtil.isNotEmpty(mCacheUrl)) {
                        CacheUtils.getInstance(mContext).setCacheToLocalJson(mCacheUrl, str);
                    }
                    subscriber.onNext(str);
                    subscriber.onCompleted();
                }
            } else {
                String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
                if (FormatUtil.isNotEmpty(json)) {
                    subscriber.onNext(json);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new Exception("请求失败"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
            if (FormatUtil.isNotEmpty(json)) {
                subscriber.onNext(json);
                subscriber.onCompleted();
            } else {
                subscriber.onError(new Exception(e));
            }
        }
    }

    /**
     * 同步请求
     */
    private void async() {
        call.enqueue(new Callback() {
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
                if (FormatUtil.isNotEmpty(json)) {
                    subscriber.onNext(json);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new Exception(e));
                }
            }

            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
                if (str.contains("<!DOCTYPE html>")) {
                    subscriber.onError(new Exception("接口错误"));
                } else {
                    if (FormatUtil.isNotEmpty(mCacheUrl)) {
                        CacheUtils.getInstance(mContext).setCacheToLocalJson(mCacheUrl, str);
                    }
                    subscriber.onNext(str);
                    subscriber.onCompleted();
                }
            }
        });
    }

    /**
     * 设置缓存路径，以及缓存文件大小
     */
    private static Cache privateCache() {
        return new Cache(mContext.getCacheDir(), 1024 * 1024);
    }
}
