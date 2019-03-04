package com.mylove.okhttp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.mylove.loglib.JLog;
import com.mylove.okhttp.download.DownLoadObserver;
import com.mylove.okhttp.download.DownloadInfo;
import com.mylove.okhttp.download.DownloadManager;

/**
 * @author BenYanYi
 * @date 2018/11/29 14:59
 * @email ben@yanyi.red
 * @overview
 */
public class DownloadActivity extends AppCompatActivity implements View.OnClickListener {
    private Button downloadBtn1, downloadBtn2, downloadBtn3;
    private Button cancelBtn1, cancelBtn2, cancelBtn3;
    private ProgressBar progress1, progress2, progress3;
    private String url1 = "http://www.yanyi.red/bluetooth/ios.pdf";
    private String url2 = "http://www.yanyi.red/bluetooth/dectector/dectector.apk";
    private String url3 = "http://www.yanyi.red/bluetooth/dectector/dfu_pkg1119.zip";

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_download);
        mContext = this;
//        downloadBtn1 = bindView(R.id.main_btn_down1);
//        downloadBtn2 = bindView(R.id.main_btn_down2);
//        downloadBtn3 = bindView(R.id.main_btn_down3);
//
//        cancelBtn1 = bindView(R.id.main_btn_cancel1);
//        cancelBtn2 = bindView(R.id.main_btn_cancel2);
//        cancelBtn3 = bindView(R.id.main_btn_cancel3);
//
//        progress1 = bindView(R.id.main_progress1);
//        progress2 = bindView(R.id.main_progress2);
//        progress3 = bindView(R.id.main_progress3);
//
//        downloadBtn1.setOnClickListener(this);
//        downloadBtn2.setOnClickListener(this);
//        downloadBtn3.setOnClickListener(this);
//
//        cancelBtn1.setOnClickListener(this);
//        cancelBtn2.setOnClickListener(this);
//        cancelBtn3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_btn_down1:
                DownloadManager.getInstance(mContext).download(url1, new DownLoadObserver() {
                    @Override
                    public void onNext(DownloadInfo downloadInfo) {
                        super.onNext(downloadInfo);
                        JLog.v(downloadInfo.getProgress());
                        progress1.setMax((int) downloadInfo.getTotal());
                        progress1.setProgress((int) downloadInfo.getProgress());
                    }

                    @Override
                    public void onComplete() {

                    }
                });
                break;
            case R.id.main_btn_down2:
                DownloadManager.getInstance(mContext).download(url2, new DownLoadObserver() {
                    @Override
                    public void onNext(DownloadInfo downloadInfo) {
                        JLog.v(downloadInfo.getProgress());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
                break;
            case R.id.main_btn_down3:
                DownloadManager.getInstance(mContext).download(url3, new DownLoadObserver() {
                    @Override
                    public void onNext(DownloadInfo downloadInfo) {
                        JLog.v(downloadInfo.getProgress());
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
                break;
            case R.id.main_btn_cancel1:
                DownloadManager.getInstance(mContext).cancel(url1);
                break;
            case R.id.main_btn_cancel2:
                DownloadManager.getInstance(mContext).cancel(url2);
                break;
            case R.id.main_btn_cancel3:
                DownloadManager.getInstance(mContext).cancel(url3);
                break;
        }
    }

    private <T extends View> T bindView(@IdRes int id) {
        View viewById = findViewById(id);
        return (T) viewById;
    }

}
