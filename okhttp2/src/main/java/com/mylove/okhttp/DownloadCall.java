package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import com.mylove.loglib.JLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import rx.Subscriber;

/**
 * @author myLove
 * @time 2017/11/24 13:46
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

class DownloadCall {
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    private String url;
    private Subscriber<? super String> subscriber;
    private Call call;
    @SuppressLint("StaticFieldLeak")
    private static DownloadCall instance;
    private CallType callType;
    private static OkHttpClient okHttpClient;

    private DownloadCall(String url, Request request, Subscriber<? super String> subscriber, CallType callType) {
        this.url = url;
        this.subscriber = subscriber;
        this.call = okHttpClient.newCall(request);
        this.callType = callType;
    }

    /**
     * okHttpClient初始化，并添加拦截及缓存
     *
     * @param context    上下文
     * @param mCacheUrl  缓存地址
     * @param request    请求
     * @param subscriber 返回
     * @param callType   请求类型
     * @return
     */
    static DownloadCall getInstance(Context context, String mCacheUrl, Request request, Subscriber<? super String> subscriber, CallType callType) {
        if (instance == null) {
            synchronized (DownloadCall.class) {
                if (instance == null) {
                    mContext = context;
                    OkHttpClient httpClient = new OkHttpClient();
                    okHttpClient = httpClient.newBuilder()
                            .addNetworkInterceptor(new CacheInterceptor())
                            .cache(privateCache())
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build();
                    instance = new DownloadCall(mCacheUrl, request, subscriber, callType);
                }
            }
        }
        return instance;
    }

    /**
     * 请求
     */
    void sendCall() {
        JLog.v();
        if (callType == CallType.SYNC) {
            sync();
        } else if (callType == CallType.ASYNC) {
            async();
        }
    }

    /**
     * 异步请求
     */
    private void sync() {
        try {
            Response execute = call.execute();
            if (execute.isSuccessful()) {
                JLog.v();
                save(execute.body());
            } else {
                JLog.v();
                subscriber.onCompleted();
                subscriber.onError(new Error("请求失败"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            JLog.v(e.getMessage());
            subscriber.onCompleted();
            subscriber.onError(new Error(e.getMessage()));
        }
    }

    /**
     * 同步请求
     */
    private void async() {
        call.enqueue(new Callback() {
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                subscriber.onCompleted();
                subscriber.onError(new Error(e.getMessage()));
            }

            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                save(response.body());
            }
        });
    }

    /**
     * 保存
     *
     * @param body
     */
    private void save(ResponseBody body) {
        int lastIndexOf = url.lastIndexOf("/");
        String pathStr = url.subSequence(lastIndexOf + 1, url.length()).toString();
        InputStream is = null;
        FileOutputStream fos = null;
        String path = FileUtil.getSDPath() + "/" + mContext.getPackageName() + "/" + pathStr;
        File file = new File(path);
        try {
//            FileUtil.saveImage(bitmap, path);
            byte[] buf = new byte[2048];
            int len;
//            long total = body.contentLength();
//            long current = 0;
            is = body.byteStream();
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
//                current += len;
                fos.write(buf, 0, len);
            }
            fos.flush();
            subscriber.onNext(path);
        } catch (Exception e) {
            e.printStackTrace();
            subscriber.onError(new Error(e.getMessage()));
        }finally {
            try {
            if (is!=null){
                is.close();
            }
            if (fos!=null){
                fos.close();
            }
            }catch (Exception e){
                e.printStackTrace();
                subscriber.onError(new Error(e.getMessage()));
            }
        }
        subscriber.onCompleted();
    }

    /**
     * 设置缓存路径，以及缓存文件大小
     */
    private static Cache privateCache() {
        return new Cache(mContext.getCacheDir(), 1024 * 1024);
    }

}
