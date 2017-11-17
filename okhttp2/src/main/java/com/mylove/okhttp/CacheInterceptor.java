package com.mylove.okhttp;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author myLove
 * @time 2017/11/7 16:26
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

 class CacheInterceptor implements Interceptor {
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = chain.proceed(request);
        Response build = response.newBuilder()
                .removeHeader("Pragma")
                .removeHeader("GlideCache-Control")
                .header("GlideCache-Control", "max-age=" + 60)
                .build();
        return build;
    }
}
