package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.mylove.okhttp.listener.OnOkHttpCallBack;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

/**
 * @author myLove
 */

class ObservableIsRequest {
    @SuppressLint("StaticFieldLeak")
    private static ObservableIsRequest instance;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static CallType callType;

    private static OkHttpClient okHttpClient;
    private String mCacheUrl = "";

    private OnOkHttpCallBack okHttpCallBack;

    static ObservableIsRequest getInstance(Context context, CallType type2) {
        if (instance == null) {
            synchronized (ObservableIsRequest.class) {
                if (instance == null) {
                    instance = new ObservableIsRequest();
                    OkHttpClient httpClient = new OkHttpClient();
                    okHttpClient = httpClient.newBuilder()
                            .addNetworkInterceptor(new CacheInterceptor())
                            .addInterceptor(Cache.HTTP_LOGGING_INTERCEPTOR)
                            .cache(Cache.privateCache(context))
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .sslSocketFactory(SSLConfig.createSSLSocketFactory())//支持HTTPS请求，跳过证书验证
                            .build();
                }
            }
        }
        mContext = context;
        callType = type2;
        return instance;
    }

    void request(String url, OnOkHttpCallBack OnOkHttpCallBack) {
        this.okHttpCallBack = OnOkHttpCallBack;
        getObservable(url).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .serialize()//保证上游下游同一线程 ，防止不同线程下 onError 通知会跳到(并吞掉)原始Observable发射的数据项前面的错误行为
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String str) {
                        okHttpCallBack.onSuccess(str);
                    }

                    @Override
                    public void onError(Throwable e) {
                        okHttpCallBack.onFailure(e);
                    }

                    @Override
                    public void onComplete() {
                        okHttpCallBack.onCompleted();
                    }
                });
    }

    private Observable<String> getObservable(final String url) {

        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) {
                send(url, e);
            }
        });
    }

    private void send(String url, ObservableEmitter<String> subscriber) {
        mCacheUrl = url;
        InternetBean bean = Internet.ifInternet(mContext);
        if (bean.getStatus()) {
            Call call = okHttpClient.newCall(okHttpCallBack.setRequest(url));
            sendCall(call, subscriber);
        } else {
            String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
            if (FormatUtil.isNotEmpty(json)) {
                subscriber.onNext(json);
                subscriber.onComplete();
            } else {
                subscriber.onError(new Error(bean.getMsg()));
                subscriber.onComplete();
            }
        }

    }

    /**
     * 请求
     */
    private void sendCall(Call call, ObservableEmitter<String> subscriber) {
        if (callType == CallType.SYNC) {
            sync(call, subscriber);
        } else if (callType == CallType.ASYNC) {
            async(call, subscriber);
        }
    }

    /**
     * 同步请求
     */
    private void sync(Call call, ObservableEmitter<String> subscriber) {
        try {
            Response execute = call.execute();
            if (execute.isSuccessful()) {
                String str = execute.body().string();
                if (OkHttpInfo.isLOG) {
                    Log.v(OkHttpInfo.TAG, str);
                }
                if (!str.toUpperCase().contains("<!DOCTYPE HTML>")) {
                    if (FormatUtil.isNotEmpty(mCacheUrl)) {
                        CacheUtils.getInstance(mContext).setCacheToLocalJson(mCacheUrl, str);
                    }
                }
                subscriber.onNext(str);
                subscriber.onComplete();
            } else {
                String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
                if (FormatUtil.isNotEmpty(json)) {
                    subscriber.onNext(json);
                    subscriber.onComplete();
                } else {
                    subscriber.onError(new Exception("请求失败"));
                    subscriber.onComplete();
                }
            }
        } catch (IOException e) {
            String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
            if (FormatUtil.isNotEmpty(json)) {
                subscriber.onNext(json);
                subscriber.onComplete();
            } else {
                subscriber.onError(e);
                subscriber.onComplete();
            }
            e.printStackTrace();
        }
    }

    /**
     * 异步请求
     */
    private void async(Call call, final ObservableEmitter<String> subscriber) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
                if (FormatUtil.isNotEmpty(json)) {
                    subscriber.onNext(json);
                    subscriber.onComplete();
                } else {
                    subscriber.onError(e);
                    subscriber.onComplete();
                }
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
                if (OkHttpInfo.isLOG) {
                    Log.v(OkHttpInfo.TAG, str);
                }
                if (!str.toUpperCase().contains("<!DOCTYPE HTML>")) {
                    if (FormatUtil.isNotEmpty(mCacheUrl)) {
                        CacheUtils.getInstance(mContext).setCacheToLocalJson(mCacheUrl, str);
                    }
                }
                subscriber.onNext(str);
                subscriber.onComplete();
            }
        });
    }
}
