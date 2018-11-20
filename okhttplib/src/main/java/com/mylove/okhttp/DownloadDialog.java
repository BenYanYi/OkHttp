package com.mylove.okhttp;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * @author BenYanYi
 * @date 2018/11/20 09:45
 * @email ben@yanyi.red
 * @overview
 */
public class DownloadDialog {
    private String title = "升级";
    private String message = "发现新的安装包";
    private boolean isLimit = false;//是否可以关闭

    public DownloadDialog() {
    }

    public DownloadDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public DownloadDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public DownloadDialog setLimit(boolean limit) {
        isLimit = limit;
        return this;
    }

    void init(Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        if (!isLimit) {
            builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }
        builder.setCancelable(isLimit);
        builder.show();
    }
}
