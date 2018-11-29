package com.mylove.okhttp.download;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author BenYanYi
 * @date 2018/11/29 16:32
 * @email ben@yanyi.red
 * @overview
 */
public class IOUtil {
    public static void closeAll(Closeable... closeables){
        if(closeables == null){
            return;
        }
        for (Closeable closeable : closeables) {
            if(closeable!=null){
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
