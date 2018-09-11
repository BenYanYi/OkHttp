package com.mylove.okhttp;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


/**
 * @author myLove
 * @time 2017/11/16 19:18
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {
        OkHttpInfo.soapDataTopString = "";
        String url = "http://www.yanyi.red/bluetooth/dfu_pkg0904.zip";
        String filePath = Environment.getExternalStorageDirectory().toString() + "/dectector/dfu/";
        OkHttpUtil.getInstance(this).downloadFile(url).download(filePath, new OnDownloadListener() {
            @Override
            public void onDownloading(int progress) {
                Log.d("进度", progress + "");
            }

            @Override
            public void onCompleted() {

            }

            @Override
            public <T> void onSuccess(T message) {

            }

            @Override
            public void onFailure(Throwable t) {

            }
        });
//        val url = "http://www.yanyi.red/bluetooth/dfu_pkg0904.zip"
//        val filePath = Environment.getExternalStorageDirectory().toString() + "/dectector/dfu/"
//        OkHttpUtil.getInstance(mContext).downloadFile(url).download(filePath, object : OnDownloadListener {
//            override fun <String : Any?> onSuccess(message: String) {
//                JLog.v("路径$message")
//            }
//
//            override fun onFailure(t: Throwable?) {
//                JLog.e(t!!.message)
//            }
//
//            override fun onDownloading(progress: Int) {
//                JLog.d("下载进度$progress%")
//            }
//
//            override fun onCompleted() {
//            }
//        })
    }
}
