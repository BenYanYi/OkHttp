package com.mylove.okhttp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mylove.loglib.JLog;
import com.yanyi.permissionlib.PermissionHelper;
import com.yanyi.permissionlib.PermissionType;


/**
 * @author myLove
 * @time 2017/11/16 19:18
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

public class MainActivity extends AppCompatActivity {
    private PermissionHelper permissionHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] strings = new String[]{PermissionType.STORAGE};
        permissionHelper = new PermissionHelper(this, strings);
        permissionHelper.hasPermission(new PermissionHelper.OnPermissionListener() {
            @Override
            public void onAllPermissionSuccess() {
                init();
            }

            @Override
            public void onAllPermissionFailure() {

            }
        });
    }

    private void init() {
        OkHttpInfo.soapDataTopString = "";
        JLog.init(true);
        String url = "http://www.yanyi.red/bluetooth/ios.pdf";
        String filePath = "/dectector/dfu/";
//        String filePath = Environment.getExternalStorageDirectory().toString() + "/dectector/dfu/";
        OkHttpUtil.getInstance(this).downloadFile(url).downloads(filePath, new OnDownloadListener() {
            @Override
            public void onDownloading(int progress) {
                JLog.d(progress + "");
            }

            @Override
            public void onCompleted() {

            }

            @Override
            public <T> void onSuccess(T message) {
                JLog.v(message);
            }

            @Override
            public void onFailure(Throwable t) {
                JLog.e(t.getMessage());
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionHelper.onActivityResult(requestCode, resultCode, data);
    }
}
