package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import com.mylove.okhttp.listener.OnDownloadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author myLove
 */

class DownloadObservable {
    @SuppressLint("StaticFieldLeak")
    private static DownloadObservable instance;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private String filePath;
    private String fileName;
    private static OkHttpClient okHttpClient;

    private DownloadObservable() {
    }

    static DownloadObservable getInstance(Context context) {
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
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .sslSocketFactory(SSLConfig.createSSLSocketFactory())//支持HTTPS请求，跳过证书验证
                            .build();
                }
            }
        }
        mContext = context;
        return instance;
    }

    void request(String url, String filePath, String fileName, final OnDownloadListener onDownloadListener) {
        this.filePath = filePath;
        this.fileName = fileName;
        getObservable(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<DownloadBean>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(DownloadBean bean) {
                        if (OkHttpInfo.isLOG) {
                            LogHelper.v(bean);
                        }
                        if (bean.status == 1) {
                            onDownloadListener.onSuccess(bean.filePath);
                        } else {
                            onDownloadListener.onDownloading();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (OkHttpInfo.isLOG)
                            LogHelper.e(e.getMessage());
                        onDownloadListener.onFailure(e);
                    }

                    @Override
                    public void onComplete() {
                        if (OkHttpInfo.isLOG)
                            LogHelper.v("*****");
                        onDownloadListener.onCompleted();
                    }
                });
    }

    private Observable<DownloadBean> getObservable(final String url) {
        return Observable.create(new ObservableOnSubscribe<DownloadBean>() {
            @Override
            public void subscribe(ObservableEmitter<DownloadBean> e) {
                send(url, e);
            }
        });
    }

    private void send(final String url, ObservableEmitter<DownloadBean> subscriber) {
        InternetBean bean = Internet.ifInternet(mContext);
        if (bean.getStatus()) {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = okHttpClient.newCall(request);
            sendCall(url, call, subscriber);
        } else {
            subscriber.onError(new Exception(bean.getMsg()));
        }
    }

    /**
     * 请求
     */
    private void sendCall(final String url, Call call, final ObservableEmitter<DownloadBean> subscriber) {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (OkHttpInfo.isLOG)
                    LogHelper.e(e.getMessage());
                subscriber.onError(e);
                subscriber.onComplete();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                DownloadBean bean = new DownloadBean();
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                String savePath = FileUtil.isExistDir(filePath);
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file;
                    if (FormatUtil.isEmpty(fileName)) {
                        file = new File(savePath, FileUtil.getNameFromUrl(url));
                    } else {
                        file = new File(savePath, fileName);
                    }
                    bean.filePath = file.getAbsolutePath();
                    if (OkHttpInfo.isLOG)
                        LogHelper.d(filePath);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        bean.status = 0;
                        if (OkHttpInfo.isLOG)
                            LogHelper.d(bean);
                        // 下载中
                        subscriber.onNext(bean);
                    }
                    fos.flush();
                    // 下载完成
                    bean.status = 1;
                    if (OkHttpInfo.isLOG)
                        LogHelper.d(bean);
                    subscriber.onNext(bean);
                    subscriber.onComplete();
                } catch (Exception e) {
                    if (OkHttpInfo.isLOG)
                        LogHelper.e(e.getMessage());
                    subscriber.onError(e);
                    subscriber.onComplete();
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                        if (OkHttpInfo.isLOG)
                            LogHelper.e(e.getMessage());
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        if (OkHttpInfo.isLOG)
                            LogHelper.e(e.getMessage());
                    }
                }
            }
        });
    }

}
