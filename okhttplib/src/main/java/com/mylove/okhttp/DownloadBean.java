package com.mylove.okhttp;

/**
 * @author BenYanYi
 * @date 2018/9/12 15:32
 * @email ben@yanyi.red
 * @overview 下载状态
 */
class DownloadBean {
    public int progress;
    public int status = 0;
    public String filePath;

    @Override
    public String toString() {
        return "DownloadBean{" +
                "progress=" + progress +
                ", status=" + status +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
