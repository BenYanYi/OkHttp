package com.mylove.okhttp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;

import com.mylove.okhttp.listener.OnDownloadCallBack;

import java.io.File;

/**
 * @author BenYanYi
 * @date 2018/11/20 10:23
 * @email ben@yanyi.red
 * @overview
 */
public class UpdateUtil {
    private Activity mActivity;

    private String downloadUrl = "";//app下载地址
    private String title = "升级";
    private String message = "发现新的安装包";
    private boolean isLimit = false;//是否可以关闭

    private boolean isShowNotice = false;//是否显示通知栏

    private int icon;

    private NotificationUtil notificationUtil;

    private Class<?> aClass;

    private ProgressDialog progressDialog;

    private boolean isInstallApk = false;
    private OnDownloadCallBack onDownloadCallBack;

    private String filePath;

    public UpdateUtil(Activity mActivity, String downloadUrl) {
        this.mActivity = mActivity;
        setDownloadUrl(downloadUrl);
    }

    public UpdateUtil setDownloadUrl(String downloadUrl) {
        if (FormatUtil.isEmpty(downloadUrl)) {
            throw new NullPointerException("");
        }
        this.downloadUrl = downloadUrl;
        return this;
    }

    /**
     * 设置弹窗标题
     */
    public UpdateUtil setTitle(String title) {
        if (FormatUtil.isNotEmpty(title)) {
            this.title = title;
        }
        return this;
    }

    /**
     * 设置弹窗内容
     */
    public UpdateUtil setMessage(String message) {
        if (FormatUtil.isNotEmpty(message)) {
            this.message = message;
        }
        return this;
    }

    /**
     * 是否需要强制下载
     */
    public UpdateUtil setLimit(boolean limit) {
        isLimit = limit;
        return this;
    }

    /**
     * 是否显示通知弹窗
     */
    public UpdateUtil setShowNotice(boolean showNotice) {
        isShowNotice = showNotice;
        return this;
    }

    /**
     * 通知弹窗跳转页面
     *
     * @param aClass
     */
    public UpdateUtil setStartClass(Class<?> aClass) {
        this.aClass = aClass;
        return this;
    }

    /**
     * 设置图标
     */
    public UpdateUtil setIcon(@DrawableRes int icon) {
        this.icon = icon;
        return this;
    }

    /**
     * 是否需要安装
     */
    public UpdateUtil setInstallApk(boolean installApk) {
        isInstallApk = installApk;
        return this;
    }

    /**
     * 下载监听
     */
    public UpdateUtil setDownloadCallBack(OnDownloadCallBack downloadCallBack) {
        this.onDownloadCallBack = downloadCallBack;
        return this;
    }

    /**
     * 存储目录
     */
    public UpdateUtil setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    /**
     * 升级
     */
    public void request() {
        if (FormatUtil.isEmpty(title)) {
            title = "提示";
        }
        if (FormatUtil.isEmpty(message)) {
            message = "是否下载";
        }
        if (isShowNotice) {
            notificationUtil = new NotificationUtil(mActivity, icon, title);
            if (aClass != null) {
                notificationUtil.setClass(aClass);
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        if (icon != 0) {
            builder.setIcon(icon);
        }
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                progressDialog();
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
        builder.setCancelable(!isLimit);
        builder.show();

    }

    private void progressDialog() {
        progressDialog = new ProgressDialog(mActivity);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(title);
        progressDialog.setMessage("正在下载");
        progressDialog.setMax(100);
        progressDialog.setProgress(0);
        // 设置ProgressDialog 的进度条是否不明确
        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        if (icon != 0)
            progressDialog.setIcon(icon);
        progressDialog.show();
        notificationUtil.showNotification(1020);
        download();
    }

    private void download() {
        if (FormatUtil.isEmpty(filePath)) {
            filePath = mActivity.getPackageName();
        }

        OkHttpUtil.getInstance(mActivity).downloadFile(downloadUrl).downloads(filePath, new DownloadObserver() {
            @Override
            public void onNext(DownloadBean downloadBean) {
                if (downloadBean == null) {
                    if (onDownloadCallBack != null) {
                        onDownloadCallBack.onFailure(new Throwable("下载失败"));
                    }
                } else if (downloadBean.status == 0) {
                    if (notificationUtil != null && isShowNotice) {
                        notificationUtil.updateProgressText(1020, downloadBean.progress, "已下载" + downloadBean.progress + "%");
                    }
                    progressDialog.setProgress(downloadBean.progress);
                    progressDialog.setMax(100);
                    progressDialog.setMessage("已下载" + downloadBean.progress + "%");
                    if (onDownloadCallBack != null) {
                        onDownloadCallBack.onDownloading(downloadBean.progress);
                    }
                } else if (downloadBean.status == 1) {
                    d.dispose();
                    progressDialog.dismiss();
                    notificationUtil.cancel(1020);
                    if (isInstallApk && FileUtil.ifUrl(downloadBean.filePath, ".apk")) {
                        installApk(new File(downloadBean.filePath));
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                super.onError(e);
                if (onDownloadCallBack != null) {
                    onDownloadCallBack.onFailure(e);
                }
            }
        });
    }

    /**
     * 安装下载完成的安装包
     *
     * @param file
     */
    private void installApk(File file) {
        //判读版本是否在7.0以上
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            //在AndroidManifest中的android:authorities值
            LogHelper.a(file.getAbsolutePath());
            Uri apkUri = FileProvider.getUriForFile(mActivity, mActivity.getPackageName() + ".fileProvider", file);
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            install.setDataAndType(apkUri, "application/vnd.android.package-archive");
            mActivity.startActivity(install);
        } else {
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mActivity.startActivity(install);
        }
    }
}
