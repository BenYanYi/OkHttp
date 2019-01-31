package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;

import com.mylove.okhttp.listener.OnDownloadListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

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
    private String filePaths = "";
    private String url;
    private OnDownloadListener downloadListener;

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
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .build();
                }
            }
        }
        mContext = context;
        return instance;
    }

    void request(String url, String filePath, OnDownloadListener onDownloadListener) {
        this.filePath = filePath;
        this.url = url;
        this.downloadListener = onDownloadListener;
        send();
    }

    private void send() {
        InternetBean bean = Internet.ifInternet(mContext);
        if (bean.getStatus()) {
            Request request = new Request.Builder()
                    .url(url)
                    .build();
            Call call = okHttpClient.newCall(request);
            sendCall(url, call);
        } else {
            if (downloadListener != null) {
                downloadListener.onFailure(new Throwable(bean.getMsg()));
            }
        }
    }

    /**
     * 请求
     */
    private void sendCall(final String url, Call call) {

        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (OkHttpInfo.isLOG)
                    LogHelper.e(e.getMessage());
                if (downloadListener != null) {
                    downloadListener.onFailure(e);
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final DownloadBean bean = new DownloadBean();
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                String savePath = FileUtil.isExistDir(filePath);
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    File file = new File(savePath);
                    bean.filePath = savePath;
                    if (OkHttpInfo.isLOG)
                        LogHelper.d(filePath);
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        final int progress = (int) (sum * 1.0f / total * 100);
                        if (OkHttpInfo.isLOG)
                            LogHelper.i(progress + "%");
                        bean.status = 0;
                        bean.progress = progress;
                        if (OkHttpInfo.isLOG)
                            LogHelper.d(bean);
                        // 下载中
                        new Thread() {
                            @Override
                            public void run() {
                                Message message = new Message();
                                message.obj = progress;
                                message.what = 1;
                                mHandler.sendMessage(message);
                            }
                        }.start();
                    }
                    fos.flush();
                    // 下载完成
                    bean.progress = 100;
                    bean.status = 1;
                    if (OkHttpInfo.isLOG)
                        LogHelper.d(bean);
                    filePaths = file.getAbsolutePath();
                    // 下载中
                    new Thread() {
                        @Override
                        public void run() {
                            Message message = new Message();
                            message.obj = bean;
                            message.what = 2;
                            mHandler.sendMessage(message);
                        }
                    }.start();
                } catch (final Exception e) {
                    if (OkHttpInfo.isLOG)
                        LogHelper.e(e.getMessage());
                    new Thread() {
                        @Override
                        public void run() {
                            Message message = new Message();
                            message.obj = e;
                            message.what = 3;
                            mHandler.sendMessage(message);
                        }
                    }.start();
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
                new Thread() {
                    @Override
                    public void run() {
                        mHandler.sendEmptyMessage(0);
                    }
                }.start();
            }
        });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int what = msg.what;
            if (what == 0) {
                if (downloadListener != null) {
                    downloadListener.onCompleted();
                }
            } else if (what == 1) {
                int progress = (int) msg.obj;
                if (downloadListener != null) {
                    downloadListener.onDownloading(progress);
                }
            } else if (what == 2) {
                DownloadBean bean = (DownloadBean) msg.obj;
                if (downloadListener != null) {
                    downloadListener.onDownloading(bean.progress);
                    downloadListener.onSuccess(bean);
                }
            } else if (what == 3) {
                Exception exception = (Exception) msg.obj;
                if (downloadListener != null) {
                    downloadListener.onFailure(exception);
                }
            }
        }
    };
}
