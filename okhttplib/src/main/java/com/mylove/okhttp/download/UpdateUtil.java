package com.mylove.okhttp.download;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.widget.ProgressBar;

import com.mylove.okhttp.LogHelper;

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

    private int icon;

    private NotificationUtil notificationUtil;

    private Class<?> aClass;

    private ProgressDialog progressDialog;
    private ProgressBar progressBar;

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
                progressDialog();
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
    private void progressDialog() {
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setTitle(title);
        progressDialog.setMessage("正在下载");
//        progressDialog.setMax(100);
//        progressDialog.setProgress(0);
//        // 设置ProgressDialog 的进度条是否不明确
//        progressDialog.setIndeterminate(false);
        progressDialog.setCancelable(false);
        if (icon != 0)
            progressDialog.setIcon(icon);
        progressDialog.show();
//        progressDialog = new AlertDialog.Builder(mActivity);
//        progressDialog.setTitle(title);
//        progressDialog.setMessage("正在下载");
//        View mView = LayoutInflater.from(mActivity).inflate(R.layout.progress_dialog, null);
//        progressBar = mView.findViewById(R.id.progress);
////        progressBar.setMax(100);
////        progressBar.setProgress(0);
//        progressDialog.setView(mView);
//        if (icon != 0) {
//            progressDialog.setIcon(icon);
//        }
//        progressDialog.setCancelable(false);
//        progressDialog.show();
        notificationUtil.showNotification(1020);
        download();
    }

    /**
     * 下载
     */
    private void download() {
        if (FormatUtil.isEmpty(filePath)) {
            filePath = mActivity.getPackageName();
        }
        DownloadManager.getInstance(mContext).download(downloadUrl, new DownLoadObserver() {
            @Override
            public void onComplete() {

            }

            @Override
            public void onNext(DownloadInfo downloadInfo) {
                super.onNext(downloadInfo);
                LogHelper.v(downloadInfo);
                int progress = (int) (downloadInfo.getProgress() * 1.0f / downloadInfo.getTotal() * 100);
                if (notificationUtil != null && isShowNotice) {
                    notificationUtil.updateProgressText(1020, progress, "已下载" + progress + "%");
                }
                LogHelper.v("进度" + progress);
                progressDialog.setMessage("已下载" + progress + "%");
                progressDialog.setMax(100);
                progressDialog.setProgress(progress);
                if (downloadInfo.getProgress() == downloadInfo.getTotal()) {
                    d.dispose();
                    if (isInstallApk && FileUtil.ifUrl(downloadInfo.getFile().getPath(), ".apk")) {
                        installApk(downloadInfo.getFile());
                    }
                }
//                    Message message = new Message();
//                    message.obj = downloadBean;
//                    mHandler.sendMessage(message);
            }
        });
//        OkHttpUtil.getInstance(mActivity).downloadFile(downloadUrl).downloads(filePath, new DownloadObserver() {
//            @Override
//            public void onNext(DownloadBean downloadBean) {
//                LogHelper.v(downloadBean);
//                if (downloadBean != null && downloadBean.status == 0) {
//                    if (notificationUtil != null && isShowNotice) {
//                        notificationUtil.updateProgressText(1020, downloadBean.progress, "已下载" + downloadBean.progress + "%");
//                    }
//                    LogHelper.v("进度" + downloadBean.progress);
//                    progressDialog.setMessage("已下载" + downloadBean.progress + "%");
//                    progressDialog.setMax(100);
//                    progressDialog.setProgress(downloadBean.progress);
////                    Message message = new Message();
////                    message.obj = downloadBean;
////                    mHandler.sendMessage(message);
//                } else if (downloadBean != null && downloadBean.status == 1) {
//                    d.dispose();
//                    notificationUtil.cancel(1020);
//                    if (isInstallApk && FileUtil.ifUrl(downloadBean.filePath, ".apk")) {
//                        installApk(new File(downloadBean.filePath));
//                    }
//                }
//                if (updateObserver != null) {
//                    updateObserver.onNext(downloadBean);
//                }
//            }
//
//            @Override
//            public void onError(Throwable e) {
//                super.onError(e);
//                if (updateObserver != null) {
//                    updateObserver.onError(e);
//                }
//            }
//
//            @Override
//            public void onComplete() {
//                super.onComplete();
//                if (updateObserver != null) {
//                    updateObserver.onComplete();
//                }
//            }
//
//            @Override
//            public void onSubscribe(Disposable d) {
//                super.onSubscribe(d);
//                if (updateObserver != null) {
//                    updateObserver.onSubscribe(d);
//                }
//            }
//        });
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

    public static void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == INSTALL_APK) {
            if (isLimit && isInstallApk) {

            } else {

            }
        }
    }
}
