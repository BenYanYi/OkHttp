package com.mylove.okhttp;

import android.content.Context;

import com.mylove.loglib.JLog;


/**
 * @author myLove
 * @time 2017/11/15 13:42
 * @e-mail mylove.520.y@gmail.com
 * @overview
 */

class Internet {
    /**
     * 判断网络
     */
    static boolean ifInternet(Context mContext) {
        switch (InternetUtil.getConnectedType(mContext)) {
            case -1:
                JLog.d();
                ShowToast.getInstance(mContext).show("网络异常");
                return false;
            case 0:
                if (!InternetUtil.isNetWorkConnected(mContext)) {
                    ShowToast.getInstance(mContext).show("网络异常");
                    JLog.d();
                    return false;
                } else {
                    JLog.d();
                    return true;
                }
            case 1:
                if (!InternetUtil.isNetWorkConnected(mContext)) {
                    ShowToast.getInstance(mContext).show("WIFI网络异常");
                    JLog.d();
                    return false;
                } else {
                    JLog.d();
                    return true;
                }
            default:
                if (!InternetUtil.isNetWorkConnected(mContext)) {
                    ShowToast.getInstance(mContext).show("网络异常");
                    JLog.d();
                    return false;
                } else {
                    JLog.d();
                    return true;
                }
        }
    }
}
