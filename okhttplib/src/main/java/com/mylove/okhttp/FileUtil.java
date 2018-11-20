package com.mylove.okhttp;

import android.graphics.Bitmap;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author myLove
 */

class FileUtil {
    /**
     * @param saveDir
     * @return
     * @throws IOException 判断下载目录是否存在
     */
    static String isExistDir(String saveDir) throws IOException {
        // 下载位置
        File downloadFile = new File(Environment.getExternalStorageDirectory(), saveDir);
        if (!downloadFile.mkdirs()) {
            downloadFile.createNewFile();
        }
        String savePath = downloadFile.getAbsolutePath();
        return savePath;
    }

    /**
     * 图片保存
     */
    static String saveImage(Bitmap bitmap, String path) {
        File file = new File(path);
        try {
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(file);
            String substring = path.substring(path.length() - 3, path.length());
            if (substring.equals("png")) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            }
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        return path;
    }

    static String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist) {//判断sd卡是否存在
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }

    /**
     * @param url
     * @return 从下载连接中解析出文件名
     */
    @NonNull
    static String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    /**
     * 判断当前url下载的文件是否为自己所需的
     *
     * @param url
     * @param condition 判断条件
     * @return
     */
    static boolean ifUrl(String url, String condition) {
        String str = url.substring(url.lastIndexOf("."));
        if (FormatUtil.isNotEmpty(str)) {
            return str.equals(condition);
        } else {
            return false;
        }
    }
}
