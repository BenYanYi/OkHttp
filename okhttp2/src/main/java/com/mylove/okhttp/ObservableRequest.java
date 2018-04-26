package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
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

    private static OkHttpClient okHttpClient;
    private String mCacheUrl = "";
    private MediaType mediaType = MediaType.parse("text/xml; charset=UTF-8");
    private static String urlMsg;
    private static String label;

    static ObservableRequest getInstance(Context context, RequestType type1, CallType type2) {
        if (instance == null) {
            synchronized (ObservableRequest.class) {
                if (instance == null) {
                    instance = new ObservableRequest();
                    OkHttpClient httpClient = new OkHttpClient();
                    okHttpClient = httpClient.newBuilder()
                            .addNetworkInterceptor(new CacheInterceptor())
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

    static ObservableRequest getInstance(String msg, String labelStr, Context context, RequestType type1, CallType type2) {
        if (instance == null) {
            synchronized (ObservableRequest.class) {
                if (instance == null) {
                    instance = new ObservableRequest();
                    OkHttpClient httpClient = new OkHttpClient();
                    okHttpClient = httpClient.newBuilder()
                            .addNetworkInterceptor(new CacheInterceptor())
                            .cache(Cache.privateCache(context))
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
        urlMsg = msg;
        label = labelStr;
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
        return Observable.unsafeCreate(new Observable.OnSubscribe<ResultMsg>() {
            @Override
            public void call(Subscriber<? super ResultMsg> subscriber) {
                send(url, oMap, subscriber);
            }
        });
    }

    private void send(String url, Map<Object, Object> map, Subscriber<? super ResultMsg> subscriber) {
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
                ResultMsg msg = new ResultMsg();
                msg.setCode("404");
                msg.setResult(json);
                subscriber.onNext(msg);
            } else {
                subscriber.onError(new Error(bean.getMsg()));
            }
            subscriber.onCompleted();
        }

    }

    /**
     * 请求
     */
    void sendCall(Call call, Subscriber<? super ResultMsg> subscriber) {
        if (callType == CallType.SYNC) {
            sync(call, subscriber);
        } else if (callType == CallType.ASYNC) {
            async(call, subscriber);
        }
    }

    /**
     * 同步请求
     */
    private void sync(Call call, Subscriber<? super ResultMsg> subscriber) {
        try {
            Response execute = call.execute();
            ResultMsg msg = new ResultMsg();
            int code = execute.code();
            msg.setCode(code + "");
            msg.setResult("");
            if (execute.isSuccessful()) {
                String str = execute.body().string();
                if (OkHttpUtil.isLOG) {
                    Log.v("onResponse-->>>", str);
                }
                msg.setResult(str);
                if (!str.toUpperCase().contains("<!DOCTYPE HTML>")) {
                    if (FormatUtil.isNotEmpty(mCacheUrl)) {
                        CacheUtils.getInstance(mContext).setCacheToLocalJson(mCacheUrl, str);
                    }
                }
                subscriber.onNext(msg);
                subscriber.onCompleted();
            } else {
                String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
                if (FormatUtil.isNotEmpty(json)) {
                    msg.setResult(json);
                    subscriber.onNext(msg);
                } else {
                    subscriber.onError(new Exception("请求失败"));
                }
                subscriber.onCompleted();
            }
        } catch (IOException e) {
            String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
            ResultMsg msg = new ResultMsg();
            msg.setCode("404");
            if (FormatUtil.isNotEmpty(json)) {
                msg.setResult(json);
                subscriber.onNext(msg);
            } else {
                subscriber.onError(e);
            }
            e.printStackTrace();
            subscriber.onCompleted();
        }
    }

    /**
     * 异步请求
     */
    private void async(Call call, final Subscriber<? super ResultMsg> subscriber) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                String json = CacheUtils.getInstance(mContext).getCacheToLocalJson(mCacheUrl);
                ResultMsg msg = new ResultMsg();
                msg.setCode("404");
                if (FormatUtil.isNotEmpty(json)) {
                    msg.setResult(json);
                    subscriber.onNext(msg);
                } else {
                    subscriber.onError(e);
                }
                e.printStackTrace();
                subscriber.onCompleted();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String str = response.body().string();
                if (OkHttpUtil.isLOG) {
                    Log.v("onResponse-->>>", str);
                }
                ResultMsg msg = new ResultMsg();
                int code = response.code();
                msg.setCode(code + "");
                msg.setResult(str);
                if (!str.toUpperCase().contains("<!DOCTYPE HTML>")) {
                    if (FormatUtil.isNotEmpty(mCacheUrl)) {
                        CacheUtils.getInstance(mContext).setCacheToLocalJson(mCacheUrl, str);
                    }
                }
                subscriber.onNext(msg);
                subscriber.onCompleted();
            }
        });
    }

    private Request request(String url, Map<Object, Object> map) {
        FormBody.Builder builder = new FormBody.Builder();
        if (FormatUtil.isMapNotEmpty(map)) {
            for (Map.Entry<Object, Object> entry : map.entrySet()) {
                builder.add(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        FormBody build = builder.build();
        return new Request.Builder()
                .url(url)
                .post(build)
                .build();
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
        if (!FormatUtil.isMapNotEmpty(oMap)) {
            throw new NullPointerException("请求的数据不能为空");
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:").append(label).append("=\"http://www.orion.com/lpz\">");
        sb.append("<soapenv:Header/>");
        sb.append("<soapenv:Body>");
        sb.append("<").append(label).append(":").append(urlMsg).append(">");
        for (Map.Entry<Object, Object> entry : oMap.entrySet()) {
            String key = entry.getKey().toString();
            String value = entry.getValue().toString();
            sb.append("<").append(label).append(":").append(key).append(">").append(value).append("</").append(label).append(":").append(key).append(">");
        }
        sb.append("</").append(label).append(":").append(urlMsg).append(">");
        sb.append("</soapenv:Body>");
        sb.append("</soapenv:Envelope>");
        return new Request.Builder()
                .url(url)
                .post(RequestBody.create(mediaType, sb.toString()))
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
