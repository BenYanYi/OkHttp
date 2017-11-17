package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;

import com.mylove.baselib.log.JLog;
import com.mylove.baselib.utils.FormatUtil;

import java.io.File;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author myLove
 * @time 2017/11/15 14:19
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

class ObservableRequest {

    @SuppressLint("StaticFieldLeak")
    private static ObservableRequest instance;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static RequestType requestType;
    private static CallType callType;

    static ObservableRequest getInstance(Context context, RequestType type1, CallType type2) {
        if (instance == null) {
            synchronized (ObservableRequest.class) {
                if (instance == null) {
                    instance = new ObservableRequest();
                    mContext = context;
                    requestType = type1;
                    callType = type2;
                }
            }
        }
        return instance;
    }

    void request(String url, Map<Object, Object> oMap, final onOkHttpListener<String, String> onOkHttpListener) {
        getObservable(url, oMap).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    public void onCompleted() {
                        onOkHttpListener.onCompleted();
                    }

                    public void onError(Throwable e) {
                        onOkHttpListener.onFailure(e.getMessage());
                    }

                    public void onNext(String s) {
                        onOkHttpListener.onSuccess(s);
                    }
                });
    }

    private Observable<String> getObservable(final String url, final Map<Object, Object> oMap) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            public void call(Subscriber<? super String> subscriber) {
                send(url, oMap, subscriber);
            }
        });
    }

    private void send(final String url, final Map<Object, Object> oMap, final Subscriber<? super String> subscriber) {
        final String mCacheUrl;
        if (FormatUtil.isMapNotEmpty(oMap)) {
            mCacheUrl = url + oMap.toString();
        } else {
            mCacheUrl = url;
        }
        if (Internet.ifInternet(mContext)) {
            Request request = getRequest(url, oMap);
            OkCall.getInstance(mContext, mCacheUrl, request, subscriber, callType).sendCall();
        } else {
            String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
            if (FormatUtil.isNotEmpty(json)) {
                subscriber.onNext(json);
                subscriber.onCompleted();
            } else {
                subscriber.onError(new Error("网络错误"));
            }
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
            requestType = RequestType.POST;
        }
        switch (requestType) {
            case GET:
                return get(url, oMap);
            case POST:
                return post(url, oMap);
            case UP_FILE:
                return upFile(url, oMap);
            case ALL:
                return upAll(url, oMap);
            default:
                return post(url, oMap);
        }
    }

    /**
     * 上传文件
     *
     * @param url  地址
     * @param oMap 键值
     * @return request
     */
    private Request upFile(String url, Map<Object, Object> oMap) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        if (FormatUtil.isMapNotEmpty(oMap)) {
            for (Map.Entry<Object, Object> entry : oMap.entrySet()) {
                File file = new File(entry.getValue().toString());
                int indexOf = entry.getValue().toString().indexOf("/");
                RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
                String fileName = entry.getKey().toString().substring(indexOf + 1, entry.getKey().toString().length());
                builder.addFormDataPart(entry.getKey().toString(), fileName, requestBody);
            }
        }
        MultipartBody multipartBody = builder.build();
        return new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();
    }

    /**
     * 参数和文件一起上传
     *
     * @param url  地址
     * @param oMap 键值
     * @return request
     */
    private Request upAll(String url, Map<Object, Object> oMap) {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.ALTERNATIVE);
        if (FormatUtil.isMapNotEmpty(oMap)) {
            for (Map.Entry<Object, Object> entry : oMap.entrySet()) {
                File file = new File(entry.getValue().toString());
                int indexOf = entry.getValue().toString().lastIndexOf("/");
                int indexOf1 = entry.getValue().toString().lastIndexOf(".");
                if (indexOf > 0 && indexOf1 > 0) {
                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
                    String fileName = entry.getKey().toString().substring(indexOf + 1, entry.getKey().toString().length());
                    builder.addFormDataPart(entry.getKey().toString(), fileName, requestBody);
                } else {
                    builder.addFormDataPart(entry.getKey().toString(), entry.getValue().toString());
                }
            }
        }
        MultipartBody multipartBody = builder.build();
        return new Request.Builder()
                .url(url)
                .post(multipartBody)
                .build();
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
                JLog.i(entry.getKey() + "\t" + entry.getValue());
            }
        }
        FormBody build = builder.build();
        return new Request.Builder()
                .url(url)
                .post(build)
                .build();
    }
}
