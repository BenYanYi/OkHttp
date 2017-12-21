package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

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
    private static String url;
    private static Subscriber<? super ResultMsg> subscriber;
    private static Call call;
    @SuppressLint("StaticFieldLeak")
    private static DownloadCall instance;
    private static CallType callType;
    private static OkHttpClient okHttpClient;
    private String fileName;

    private DownloadCall() {

    }

    /**
     * okHttpClient初始化，并添加拦截及缓存
     *
     * @param context     上下文
     * @param str         缓存地址
     * @param request     请求
     * @param subscriber1 返回
     * @param type        请求类型
     * @return
     */
    static DownloadCall getInstance(Context context, String str, Request request, Subscriber<? super ResultMsg> subscriber1, CallType type) {
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
                    instance = new DownloadCall();
                }
            }
        }
        url = str;
        subscriber = subscriber1;
        call = okHttpClient.newCall(request);
        callType = type;
        return instance;
    }

    DownloadCall setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * 请求
     */
    void sendCall() {
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
                save(execute.body());
            } else {
                subscriber.onError(new Error("请求失败"));
            }
        } catch (IOException e) {
            subscriber.onError(e);
        }
        subscriber.onCompleted();
    }

    /**
     * 同步请求
     */
    private void async() {
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                subscriber.onError(e);
                subscriber.onCompleted();
            }

            @Override
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
            ResultMsg msg = new ResultMsg();
            msg.setCode("200");
            msg.setResult(fileName);
            subscriber.onNext(msg);
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
        subscriber.onCompleted();
    }

    /**
     * 设置缓存路径，以及缓存文件大小
     */
    private static Cache privateCache() {
        return new Cache(mContext.getCacheDir(), 1024 * 1024);
    }

}
