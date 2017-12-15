package com.mylove.okhttp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;

/**
 * @author myLove
 * @time 2017/11/7 17:03
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

class CacheUtils {
    private static File realFile;
    @SuppressLint("StaticFieldLeak")
    private static Context mContext;
    @SuppressLint("StaticFieldLeak")
    private static CacheUtils instance;

    public static CacheUtils getInstance(Context context) {
        if (instance == null) {
            instance = new CacheUtils();
            mContext = context;
            File dir = mContext.getExternalFilesDir(null);
            if (dir != null && !dir.exists() && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                dir.mkdirs();
            }
            realFile = dir;
        }
        return instance;
    }

    /**
     * 根据url的MD5作为文件名,进行缓存
     *
     * @param url  文件名
     * @param json
     */
    void setCacheToLocalJson(String url, String json) {
        String urlMD5 = MD5keyUtil.newInstance().getkeyBeanofStr(url);
        String path = realFile.getAbsolutePath() + "/" + urlMD5;
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            FileOutputStream fis = new FileOutputStream(file);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fis));
            long currentTime = System.currentTimeMillis();
            bw.write(currentTime + "");
            bw.newLine();
            bw.write(json);
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 根据缓存地址，从缓存中取出数据
     *
     * @param url
     * @return
     */
    String getCacheToLocalJson(String url) {
        StringBuffer sb = new StringBuffer();
        String urlMD5 = MD5keyUtil.newInstance().getkeyBeanofStr(url);
        // 创建缓存文件夹
        File file = new File(realFile, urlMD5);
        if (file.exists()) {
            try {
                FileReader fr = new FileReader(file);
                BufferedReader br = new BufferedReader(fr);
                br.readLine();
                String temp;
                while ((temp = br.readLine()) != null) {
                    sb.append(temp);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return sb.toString();
    }
}
