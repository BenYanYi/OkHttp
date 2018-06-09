package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author myLove
 */

class DownloadObservable {
    @SuppressLint("StaticFieldLeak")
    private static DownloadObservable instance;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private static RequestType requestType;
    private static CallType callType;
    private static String fileName;
    private static OkHttpClient okHttpClient;

    private DownloadObservable() {
    }

    static DownloadObservable getInstance(Context context, String file, RequestType type1, CallType type2) {
        if (instance == null) {
            synchronized (DownloadObservable.class) {
                if (instance == null) {
                    instance = new DownloadObservable();
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
        fileName = file;
        return instance;
    }

    static DownloadObservable getInstance(Context context, RequestType type1, CallType type2) {
        if (instance == null) {
            synchronized (DownloadObservable.class) {
                if (instance == null) {
                    instance = new DownloadObservable();
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

    void request(String url, Map<Object, Object> oMap, final onOkHttpListener onOkHttpListener) {
        getObservable(url, oMap).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(String str) {
                        onOkHttpListener.onSuccess(str);
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

    private Observable<String> getObservable(final String url, final Map<Object, Object> oMap) {
        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) {
                send(url, oMap, e);
            }
        });
    }

    private void send(final String url, final Map<Object, Object> oMap, final ObservableEmitter<String> subscriber) {
        InternetBean bean = Internet.ifInternet(mContext);
        if (bean.getStatus()) {
            Call call = okHttpClient.newCall(getRequest(url, oMap));
            sendCall(url, call, subscriber);
        } else {
            subscriber.onError(new Exception(bean.getMsg()));
            subscriber.onComplete();
        }
    }

    /**
     * 请求
     */
    private void sendCall(String url, Call call, ObservableEmitter<String> subscriber) {
        if (callType == CallType.SYNC) {
            sync(url, call, subscriber);
        } else if (callType == CallType.ASYNC) {
            async(url, call, subscriber);
        }
    }

    /**
     * 异步请求
     */
    private void sync(final String url, Call call, final ObservableEmitter<String> subscriber) {
        try {
            Response execute = call.execute();
            if (execute.isSuccessful()) {
                save(execute.body(), url, subscriber);
            } else {
                subscriber.onError(new Error("请求失败"));
            }
        } catch (IOException e) {
            subscriber.onError(e);
        }
        subscriber.onComplete();
    }

    /**
     * 同步请求
     */
    private void async(final String url, Call call, final ObservableEmitter<String> subscriber) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                subscriber.onError(e);
                subscriber.onComplete();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                save(response.body(), url, subscriber);
            }
        });
    }

    /**
     * 保存
     *
     * @param body
     */
    private void save(ResponseBody body, String url, ObservableEmitter<String> subscriber) {
        if (null == fileName && "".equals(fileName) && fileName.length() <= 0) {
            int lastIndexOf = url.lastIndexOf("/");
            String pathStr = url.subSequence(lastIndexOf + 1, url.length()).toString();
            fileName = FileUtil.getSDPath() + "/" + mContext.getPackageName() + "/" + pathStr;
        }
        File file = new File(fileName);
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            byte[] buf = new byte[2048];
            int len;
            is = body.byteStream();
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                fos.write(buf, 0, len);
            }
            fos.flush();
            subscriber.onNext(fileName);
        } catch (Exception e) {
            subscriber.onError(e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                subscriber.onError(e);
            }
        }
        subscriber.onComplete();
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
