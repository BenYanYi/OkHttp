package com.mylove.okhttp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mylove.loglib.JLog;
import com.mylove.okhttp.listener.OnOkHttpListener;
import com.yanyi.permissionlib.PermissionHelper;
import com.yanyi.permissionlib.PermissionType;

import java.util.HashMap;
import java.util.Map;


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
//                init();
                downloadDFU();
            }

            @Override
            public void onAllPermissionFailure() {

            }
        });
    }

    private void init() {
        String url = "http://192.168.3.188/DTP/BPO_DTPInterfaceYYC.asmx/DTPInterfaceYYC";
        Map<Object, Object> oMap = new HashMap<>();
        oMap.put("UserID", "110");
        oMap.put("TypeID", "1");
        oMap.put("Mac", "");
        oMap.put("CodeID", "");
        oMap.put("Status", "");
        oMap.put("DataSet", "");
        OkHttpUtil.getInstance(this).post(url).async(oMap, new OnOkHttpListener() {
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
    }

    private void downloadDFU() {
        OkHttpInfo.soapDataTopString = "";
        JLog.init(true);
        String url = "http://www.yanyi.red/bluetooth/dectector/dectector.apk";
//        String filePath = "/dectector/dfu/";
////        String filePath = Environment.getExternalStorageDirectory().toString() + "/dectector/dfu/";
//        OkHttpUtil.getInstance(this).downloadFile(url).downloads(filePath, new OnDownloadCallBack() {
//            @Override
//            public void onDownloading(int progress) {
//                JLog.d(progress + "");
//            }
//
//            @Override
//            public void onSuccess(String message) {
//                JLog.v(message);
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//                JLog.e(t.getMessage());
//            }
//        });
        UpdateUtil updateUtil = new UpdateUtil(this, url)
                .setIcon(R.mipmap.ic_launcher)
                .setMessage("更新")
                .setTitle("更新测试")
                .setLimit(false)
                .setShowNotice(true)
                .setInstallApk(true)
                .setStartClass(MainActivity.class);
        updateUtil.request();
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
