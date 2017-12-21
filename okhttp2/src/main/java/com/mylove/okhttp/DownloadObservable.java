package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Request;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author myLove
 * @time 2017/11/24 13:38
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

class DownloadObservable {
    @SuppressLint("StaticFieldLeak")
    private static DownloadObservable instance;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static RequestType requestType;
    private static CallType callType;
    private static String fileName;

    private DownloadObservable() {
    }

    static DownloadObservable getInstance(Context context, String file, RequestType type1, CallType type2) {
        if (instance == null) {
            synchronized (DownloadObservable.class) {
                if (instance == null) {
                    instance = new DownloadObservable();
                    mContext = context;
                    requestType = type1;
                    callType = type2;
                    fileName = file;
                }
            }
        }
        return instance;
    }

    static DownloadObservable getInstance(Context context, RequestType type1, CallType type2) {
        if (instance == null) {
            synchronized (DownloadObservable.class) {
                if (instance == null) {
                    instance = new DownloadObservable();
                }
            }
        }
        mContext = context;
        requestType = type1;
        callType = type2;
        return instance;
    }

    void request(String url, Map<Object, Object> oMap, final onOkHttpListener onOkHttpListener) {
        getObservable(url, oMap).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResultMsg>() {
                    @Override
                    public void onCompleted() {
                        onOkHttpListener.onCompleted();
                    }

                    @Override
                    public void onError(Throwable e) {
                        onOkHttpListener.onFailure(e);
                    }

                    @Override
                    public void onNext(ResultMsg s) {
                        onOkHttpListener.onSuccess(s);
                    }
                });
    }

    private Observable<ResultMsg> getObservable(final String url, final Map<Object, Object> oMap) {
        return Observable.create(new Observable.OnSubscribe<ResultMsg>() {
            @Override
            public void call(Subscriber<? super ResultMsg> subscriber) {
                send(url, oMap, subscriber);
            }
        });
    }

    private void send(final String url, final Map<Object, Object> oMap, final Subscriber<? super ResultMsg> subscriber) {
        InternetBean bean = Internet.ifInternet(mContext);
        if (bean.getStatus()) {
            Request request = getRequest(url, oMap);
            if (null != fileName && !"".equals(fileName) && fileName.length() > 0) {
                DownloadCall.getInstance(mContext, url, request, subscriber, callType).setFileName(fileName).sendCall();
            } else {
                DownloadCall.getInstance(mContext, url, request, subscriber, callType).sendCall();
            }
        } else {
            subscriber.onError(new Exception(bean.getMsg()));
            subscriber.onCompleted();
        }
    }

    /**
     * 判断请求方式
     *
     * @param url  地址
     * @param oMap 键值
     * @return request
     */
    private Request getRequest(String url, Map<Object, Object> oMap) {
        if (requestType == null) {
            requestType = RequestType.GET;
        }
        switch (requestType) {
            case GET:
                return get(url, oMap);
            case POST:
                return post(url, oMap);
            default:
                return get(url, oMap);
        }
    }

    /**
     * get上传参数
     *
     * @param url  地址
     * @param oMap 键值
     * @return request
     */
    private Request get(String url, Map<Object, Object> oMap) {
        StringBuilder str = new StringBuilder(url);
        if (FormatUtil.isMapNotEmpty(oMap)) {
            str.append("?");
            for (Map.Entry<Object, Object> entry : oMap.entrySet()) {
                str.append(entry.getKey()).append("=").append(entry).append("&");
            }
            str = new StringBuilder(str.substring(0, str.length() - 1));
        }
        return new Request.Builder()
                .url(str.toString())
                .get()
                .build();
    }

    /**
     * post上传参数
     *
     * @param url  地址
     * @param oMap 键值
     * @return request
     */
    private Request post(String url, Map<Object, Object> oMap) {
        FormBody.Builder builder = new FormBody.Builder();
        if (FormatUtil.isMapNotEmpty(oMap)) {
            for (Map.Entry<Object, Object> entry : oMap.entrySet()) {
                builder.add(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        FormBody build = builder.build();
        return new Request.Builder()
                .url(url)
                .post(build)
                .build();
    }
}
