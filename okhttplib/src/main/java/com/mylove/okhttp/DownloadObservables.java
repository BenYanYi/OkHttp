package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author BenYanYi
 * @date 2018/9/13 17:43
 * @email ben@yanyi.red
 * @overview
 */
public class DownloadObservables {
    @SuppressLint("StaticFieldLeak")
    private static DownloadObservables instance;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private String filePath;
    private static OkHttpClient okHttpClient;

    private DownloadObservables() {
    }

    static DownloadObservables getInstance(Context context) {
        if (instance == null) {
            synchronized (DownloadObservable.class) {
                if (instance == null) {
                    instance = new DownloadObservables();
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
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<DownloadBean>() {
                    private Subscription mSubscription;

                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(1);//设置初始请求数据量为1
                        mSubscription = s;
                    }

                    @Override
                    public void onNext(DownloadBean downloadBean) {
                        if (OkHttpInfo.isLOG) {
                            LogHelper.v(downloadBean);
                        }
                        if (downloadBean.status == 1) {
                            onDownloadListener.onSuccess(downloadBean.filePath);
                        } else {
                            onDownloadListener.onDownloading(downloadBean.progress);
                        }
                        mSubscription.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        if (OkHttpInfo.isLOG)
                            LogHelper.e(t.getMessage());
                        onDownloadListener.onFailure(t);
                    }

                    @Override
                    public void onComplete() {
                        if (OkHttpInfo.isLOG)
                            LogHelper.v("*****");
                        onDownloadListener.onCompleted();
                    }
                });
    }

    private Flowable<DownloadBean> getObservable(final String url) {
        return Flowable.create(new FlowableOnSubscribe<DownloadBean>() {
            @Override
            public void subscribe(FlowableEmitter<DownloadBean> e) throws Exception {
                send(url, e);
            }
        }, BackpressureStrategy.MISSING);
    }

    private void send(final String url, FlowableEmitter<DownloadBean> subscriber) {
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
    private void sendCall(final String url, Call call, final FlowableEmitter<DownloadBean> subscriber) {

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
                    File file = new File(savePath, FileUtil.getNameFromUrl(url));
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
}
