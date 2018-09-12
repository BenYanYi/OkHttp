package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;

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
                            .build();
                }
            }
        }
        mContext = context;
        return instance;
    }

    void request(String url, String filePath, final OnDownloadListener onDownloadListener) {
        this.filePath = filePath;
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
                            onDownloadListener.onDownloading(bean.progress);
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
//            sendCall(url, call, new onOkHttpListener() {
//                @Override
//                public void onCompleted() {
//                    subscriber.onComplete();
//                }
//
//                @Override
//                public <T> void onSuccess(T message) {
//                    DownloadBean bean1 = (DownloadBean) message;
//                    LogHelper.v(bean1.toString());
//                    subscriber.onNext(bean1);
//                }
//
//                @Override
//                public void onFailure(Throwable t) {
//                    LogHelper.e(t.getMessage());
//                    subscriber.onError(t);
//                }
//            });
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
                String savePath = isExistDir(filePath);
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(savePath, getNameFromUrl(url));
                    bean.filePath = file.getAbsolutePath();
                    if (OkHttpInfo.isLOG)
                        LogHelper.d(filePath);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        if (OkHttpInfo.isLOG)
                            LogHelper.i(progress + "%");
                        bean.status = 0;
                        bean.progress = progress;
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

    /**
     * @param saveDir
     * @return
     * @throws IOException 判断下载目录是否存在
     */
    private String isExistDir(String saveDir) throws IOException {
        // 下载位置
        File downloadFile = new File(Environment.getExternalStorageDirectory(), saveDir);
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile();
        }
        return downloadFile.getAbsolutePath();
    }

    /**
     * @param url
     * @return 从下载连接中解析出文件名
     */
    @NonNull
    private String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
