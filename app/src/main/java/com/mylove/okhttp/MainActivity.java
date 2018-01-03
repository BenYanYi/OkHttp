package com.mylove.okhttp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mylove.loglib.JLog;

import didikee.com.permissionshelper.PermissionsHelper;
import didikee.com.permissionshelper.permission.DangerousPermissions;


/**
 * @author myLove
 * @time 2017/11/16 19:18
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

public class MainActivity extends AppCompatActivity {

    /**
     * app所需要的全部危险权限
     */
    static final String[] PERMISSIONS = new String[]{DangerousPermissions.STORAGE};
    private PermissionsHelper permissionsHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setPermissions();
    }

    private void setPermissions() {
        permissionsHelper = new PermissionsHelper(this, PERMISSIONS, true);
        if (permissionsHelper.checkAllPermissions(PERMISSIONS)) {
            permissionsHelper.onDestroy();
            data();
        } else {
            //申请权限
            permissionsHelper.startRequestNeedPermissions();
        }
        permissionsHelper.setonAllNeedPermissionsGrantedListener(new PermissionsHelper.onAllNeedPermissionsGrantedListener() {
            //全部许可了,已经获得了所有权限
            @Override
            public void onAllNeedPermissionsGranted() {
                //做原先的业务代码
                JLog.d();
                data();
            }

            //被拒绝了,只要有一个权限被拒绝那么就会调用
            @Override
            public void onPermissionsDenied() {
                //拒绝了,如何处理?(视情况而定)
                JLog.d();
                permissionsHelper.setParams(null);
            }

            //用户已经永久的拒绝了
            @Override
            public void hasLockForever() {
                JLog.i("hasLockForever");
                permissionsHelper.setParams(null);
            }

            //被拒绝后,在最后一次申请权限之前
            @Override
            public void onBeforeRequestFinalPermissions(PermissionsHelper helper) {
                JLog.i();
                helper.continueRequestPermissions();
            }
        });
    }

    private void data() {
//        sendTest();
        sendTest2();
    }

//    private void sendTest() {
//        OkHttpUtil.getInstance(this).post("http://www.yanyi.red/api/AppMsg.php").async(null, new onOkHttpListener() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onSuccess(ResultMsg requestMsg) {
//                JLog.v(requestMsg);
//            }
//
//            @Override
//            public void onFailure(Throwable t) {
//
//            }
//        });
//        OkHttpUtil.getInstance(this).post("http://www.yanyi.red/api/TestApi.php").async(null, new onOkHttpListener() {
//            @Override
//            public void onCompleted() {
//                JLog.v();
//            }
//
//            @Override
//            public void onSuccess(ResultMsg resultMsg) {
//                JLog.v(resultMsg);
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                throwable.printStackTrace();
//            }
//        });
//    }

    private void sendTest2() {
        OkHttpUtil.getInstance(this).post("http://www.yanyi.red/api/AppMsg.php").async(null, new onOkHttpListener() {
            @Override
            public void onCompleted() {
                JLog.v();
            }

            @Override
            public void onSuccess(ResultMsg resultMsg) {
                JLog.v(resultMsg);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
        OkHttpUtil.getInstance(this).post("http://www.yanyi.red/api/TestApi.php").async(null, new onOkHttpListener() {
            @Override
            public void onCompleted() {
                JLog.v();
            }

            @Override
            public void onSuccess(ResultMsg resultMsg) {
                JLog.v(resultMsg);
            }

            @Override
            public void onFailure(Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        permissionsHelper.onActivityResult(requestCode, resultCode, data);
    }
}
