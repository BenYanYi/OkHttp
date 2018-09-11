package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import org.json.XML;

import java.io.File;
import java.io.IOException;
import java.util.Map;
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
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * @author myLove
 */

class ObservableRequests<T> {
    @SuppressLint("StaticFieldLeak")
    private static ObservableRequests instance;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static RequestType requestType;
    private static CallType callType;

    private static OkHttpClient okHttpClient;
    private String mCacheUrl = "";

    public Class<T> tClass;

    static ObservableRequests getInstance(Context context, RequestType type1, CallType type2) {
        if (instance == null) {
            synchronized (ObservableRequests.class) {
                if (instance == null) {
                    instance = new ObservableRequests();
                    OkHttpClient httpClient = new OkHttpClient();
                    okHttpClient = httpClient.newBuilder()
                            .addNetworkInterceptor(new CacheInterceptor())
                            .addInterceptor(Cache.HTTP_LOGGING_INTERCEPTOR)
                            .cache(Cache.privateCache(context))
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build();
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
                .serialize().subscribe(new Observer<T>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(T t) {
                onOkHttpListener.onSuccess(t);
            }

            @Override
            public void onError(Throwable e) {
                onOkHttpListener.onFailure(e);
            }

            @Override
            public void onComplete() {
                onOkHttpListener.onCompleted();
            }
        });
    }

    private Observable<T> getObservable(final String url, final Map<Object, Object> oMap) {
        return Observable.create(new ObservableOnSubscribe<T>() {
            @Override
            public void subscribe(ObservableEmitter<T> e) {
                send(url, oMap, e);
            }
        });
    }

    private void send(String url, Map<Object, Object> map, ObservableEmitter<T> subscriber) {
        if (FormatUtil.isMapNotEmpty(map)) {
            mCacheUrl = url + map.toString();
        } else {
            mCacheUrl = url;
        }
        InternetBean bean = Internet.ifInternet(mContext);
        if (bean.getStatus()) {
            Call call = okHttpClient.newCall(getRequest(url, map));
            sendCall(call, subscriber);
        } else {
            String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
            if (FormatUtil.isNotEmpty(json)) {
                T t = new Gson().fromJson(json, tClass);
                subscriber.onNext(t);
            } else {
                subscriber.onError(new Error(bean.getMsg()));
            }
        }
        subscriber.onComplete();
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
            } else {
                String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
                if (FormatUtil.isNotEmpty(json)) {
                    T t = new Gson().fromJson(json, tClass);
                    subscriber.onNext(t);
                } else {
                    subscriber.onError(new Exception("请求失败"));
                }
            }
        } catch (IOException e) {
            String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
            if (FormatUtil.isNotEmpty(json)) {
                T t = new Gson().fromJson(json, tClass);
                subscriber.onNext(t);
            } else {
                subscriber.onError(e);
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
                } else {
                    subscriber.onError(e);
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
            }
        });
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
            case POST_XML_SOAP:
                return postXMLToSoap(url, oMap);
            case UP_FILE:
                return upFile(url, oMap);
            case ALL:
                return upAll(url, oMap);
            case POST:
            default:
                return post(url, oMap);
        }
    }

    private Request postXMLToSoap(String url, Map<Object, Object> oMap) {
        if (FormatUtil.isEmpty(OkHttpInfo.soapDataTopString)) {
            throw new NullPointerException("OkHttpInfo.soapDataTopString不能为空");
        }
        if (FormatUtil.isEmpty(OkHttpInfo.soapDataBottomString)) {
            throw new NullPointerException("OkHttpInfo.soapDataBottomString不能为空");
        }
        StringBuilder sb = new StringBuilder();
        if (FormatUtil.isMapNotEmpty(oMap)) {
            for (Map.Entry<Object, Object> entry : oMap.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                sb.append("<").append(key).append(">").append(value).append("</").append(key).append(">");
            }
        }
        MediaType mediaType = MediaType.parse(OkHttpInfo.soapMediaType);
        String str = OkHttpInfo.soapDataTopString + sb + OkHttpInfo.soapDataBottomString;
        if (OkHttpInfo.isLOG) {
            Log.v(OkHttpInfo.TAG, str);
        }
        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(mediaType, str))
                .build();
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
                int indexOf = entry.getValue().toString().lastIndexOf("/");
                int indexOf1 = entry.getValue().toString().lastIndexOf(".");
                if (indexOf > 0 && indexOf1 > 0) {
                    File file = new File(entry.getValue().toString());
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
                str.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
            }
            str = new StringBuilder(str.substring(0, str.length() - 1));
        }
        if (OkHttpInfo.isLOG) {
            Log.v(OkHttpInfo.TAG, str.toString());
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
