package com.mylove.okhttp;

import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

/**
 * @author BenYanYi
 * @date 2019/01/30 22:38
 * @email ben@yanyi.red
 * @overview
 */
class SSLConfig {
    /**
     * 生成安全套接字工厂，用于https请求的证书跳过
     *
     * @return
     */
    static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }
        return ssfFactory;
    }
}
