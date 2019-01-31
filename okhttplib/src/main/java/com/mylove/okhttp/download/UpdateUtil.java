package com.mylove.okhttp.download;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import com.mylove.okhttp.DownloadBean;
import com.mylove.okhttp.LogHelper;
import com.mylove.okhttp.OkHttpUtil;
import com.mylove.okhttp.R;
import com.mylove.okhttp.listener.OnDownloadListener;

import java.io.File;

/**
 * @author BenYanYi
 * @date 2018/11/20 10:23
 * @email ben@yanyi.red
 * @overview
 */
public class UpdateUtil {
    private Activity mActivity;
    private Context mContext;

    private String downloadUrl = "";//app下载地址
    private String title = "升级";
    private String message = "发现新的安装包";
    private static boolean isLimit = false;//是否可以关闭

    private boolean isShowNotice = false;//是否显示通知栏
    private boolean isShowProgress = false;//是否显示下载进度弹窗

    private int icon;

    private NotificationUtil notificationUtil;
    private AlertDialog.Builder progressDialog;
    private ProgressBar progressBar;
    private AlertDialog dialog;

    private Class<?> aClass;

    private static boolean isInstallApk = false;
    private UpdateObserver updateObserver;

    private String filePath;

    private static final int INSTALL_APK = 0x1020;

    public UpdateUtil(Context context, Activity mActivity, String downloadUrl) {
        this.mActivity = mActivity;
        this.mContext = context;
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
     * 是否显示下载进度弹窗
     *
     * @param showProgress
     */
    public UpdateUtil setShowProgress(boolean showProgress) {
        isShowProgress = showProgress;
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
    public UpdateUtil setUpdateObserver(UpdateObserver updateObserver) {
        this.updateObserver = updateObserver;
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
    public void update() {
        if (FormatUtil.isEmpty(title)) {
            title = "提示";
        }
        if (FormatUtil.isEmpty(message)) {
            message = "是否下载";
        }
        notificationUtil = new NotificationUtil(mActivity, icon, title);
        if (aClass != null) {
            notificationUtil.setClass(aClass);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        if (icon != 0) {
            builder.setIcon(icon);
        }
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                download();
            }
        });
        if (!isLimit) {
            builder.setNegativeButton("下次提醒", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (updateObserver != null) {
                        updateObserver.onDialogDismiss();
                    }
                }
            });
        }
        builder.setCancelable(!isLimit);
        builder.show();
    }

    /**
     * 进度条
     */
    private AlertDialog.Builder getProgressDialog() {
        if (progressDialog == null) {
            AlertDialog.Builder progressDialog1 = new AlertDialog.Builder(mContext);
            View mView = LayoutInflater.from(mContext).inflate(R.layout.progress_dialog, null);
            progressDialog1.setView(mView);
            progressDialog1.setTitle(title);
            progressDialog1.setMessage("正在下载");
//        progressDialog.setProgress(0);
            // 设置ProgressDialog 的进度条是否不明确
            progressDialog1.setCancelable(false);
            if (icon != 0)
                progressDialog1.setIcon(icon);
            progressBar = mView.findViewById(R.id.progress);
            progressBar.setMax(100);
            progressDialog = progressDialog1;
        }
        return progressDialog;
    }

    /**
     * 下载
     */
    private void download() {
        if (isShowProgress) {
            dialog = getProgressDialog().show();
        }
        if (isShowNotice) {
            notificationUtil.showNotification(1020);
        }
        OkHttpUtil.getInstance(mContext).downloadFile(downloadUrl).downloads(filePath, new OnDownloadListener() {
            @Override
            public void onDownloading(int progress) {
                LogHelper.v("进度" + progress);
                if (notificationUtil != null && isShowNotice) {
                    notificationUtil.updateProgressText(1020, progress, "已下载" + progress + "%");
                }
                if (isShowProgress) {
                    getProgressDialog().setMessage("已下载" + progress + "%");
//                    getProgressDialog().setProgress(progress);
                    if (progressBar != null) {
                        progressBar.setProgress(progress);
                    }
                }
            }

            @Override
            public void onCompleted() {
                if (updateObserver != null) {
                    updateObserver.onComplete();
                }
            }

            @Override
            public <T> void onSuccess(T message) {
                DownloadBean downloadBean = (DownloadBean) message;
                if (downloadBean != null && downloadBean.status == 1) {
                    if (isShowNotice) {
                        notificationUtil.cancel(1020);
                    }
                    if (isShowProgress) {
                        dialog.dismiss();
                    }
                    if (isInstallApk && FileUtil.ifUrl(downloadBean.filePath, ".apk")) {
                        installApk(new File(downloadBean.filePath));
                    }
                }
                if (updateObserver != null) {
                    updateObserver.onSuccess(downloadBean);
                }
            }

            @Override
            public void onFailure(Throwable t) {
                if (updateObserver != null) {
                    updateObserver.onError(t);
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
            mActivity.startActivityForResult(install, INSTALL_APK);
        } else {
            Intent install = new Intent(Intent.ACTION_VIEW);
            install.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mActivity.startActivityForResult(install, INSTALL_APK);
        }
    }

//    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == INSTALL_APK) {
//            if (isLimit && isInstallApk) {
//
//            } else {
//
//            }
//        }
//    }
}
