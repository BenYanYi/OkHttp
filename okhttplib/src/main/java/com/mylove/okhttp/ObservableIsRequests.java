package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.mylove.okhttp.listener.OnOkHttpCallBack;

import org.json.XML;

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

class ObservableIsRequests<T> {
    @SuppressLint("StaticFieldLeak")
    private static ObservableIsRequests instance;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static CallType callType;

    private static OkHttpClient okHttpClient;
    private String mCacheUrl = "";

    private OnOkHttpCallBack okHttpCallBack;

    public Class<T> tClass;

    static ObservableIsRequests getInstance(Context context, CallType type2) {
        if (instance == null) {
            synchronized (ObservableIsRequests.class) {
                if (instance == null) {
                    instance = new ObservableIsRequests();
                    OkHttpClient httpClient = new OkHttpClient();
                    okHttpClient = httpClient.newBuilder()
                            .addNetworkInterceptor(new CacheInterceptor())
                            .addInterceptor(Cache.HTTP_LOGGING_INTERCEPTOR)
                            .cache(Cache.privateCache(context))
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
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
                .serialize().subscribe(new Observer<T>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(T t) {
                okHttpCallBack.onSuccess(t);
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

    private Observable<T> getObservable(final String url) {
        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> e) {
                send(url, e);
            }
        });
    }

    private void send(String url, ObservableEmitter<T> subscriber) {
        mCacheUrl = url;
        InternetBean bean = Internet.ifInternet(mContext);
        if (bean.getStatus()) {
            Call call = okHttpClient.newCall(okHttpCallBack.setRequest(url));
            sendCall(call, subscriber);
        } else {
            String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
            if (FormatUtil.isNotEmpty(json)) {
                T t = new Gson().fromJson(json, tClass);
                subscriber.onNext(t);
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
    private void sendCall(Call call, ObservableEmitter<T> subscriber) {
        if (callType == CallType.SYNC) {
            sync(call, subscriber);
        } else if (callType == CallType.ASYNC) {
            async(call, subscriber);
        }
    }

    /**
     * 同步请求
     */
    private void sync(Call call, ObservableEmitter<T> subscriber) {
        try {
            Response execute = call.execute();
            if (execute.isSuccessful()) {
                String str = execute.body().string();
                if (OkHttpInfo.isLOG) {
                    Log.v(OkHttpInfo.TAG, str);
                }
                if ((str.substring(0, 1).equals("<") || str.substring(0, 1).equals("["))
                        && (str.substring(1, 2).equals("\"") || str.substring(1, 2).equals("["))) {
                    try {
                        str = XML.toJSONObject(str).toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!str.toUpperCase().contains("<!DOCTYPE HTML>")) {
                        if (FormatUtil.isNotEmpty(mCacheUrl)) {
                            CacheUtils.getInstance(mContext).setCacheToLocalJson(mCacheUrl, str);
                        }
                    }
                }
                T t = new Gson().fromJson(str, tClass);
                subscriber.onNext(t);
                subscriber.onComplete();
            } else {
                String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
                if (FormatUtil.isNotEmpty(json)) {
                    T t = new Gson().fromJson(json, tClass);
                    subscriber.onNext(t);
                    subscriber.onComplete();
                } else {
                    subscriber.onError(new Exception("请求失败"));
                    subscriber.onComplete();
                }
            }
        } catch (IOException e) {
            String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
            if (FormatUtil.isNotEmpty(json)) {
                T t = new Gson().fromJson(json, tClass);
                subscriber.onNext(t);
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
    private void async(Call call, final ObservableEmitter<T> subscriber) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
                if (FormatUtil.isNotEmpty(json)) {
                    T t = new Gson().fromJson(json, tClass);
                    subscriber.onNext(t);
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
                if ((str.substring(0, 1).equals("<") || str.substring(0, 1).equals("["))
                        && (str.substring(1, 2).equals("\"") || str.substring(1, 2).equals("["))) {
                    try {
                        str = XML.toJSONObject(str).toString();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (!str.toUpperCase().contains("<!DOCTYPE HTML>")) {
                        if (FormatUtil.isNotEmpty(mCacheUrl)) {
                            CacheUtils.getInstance(mContext).setCacheToLocalJson(mCacheUrl, str);
                        }
                    }
                }
                T t = new Gson().fromJson(str, tClass);
                subscriber.onNext(t);
                subscriber.onComplete();
            }
        });
    }
}