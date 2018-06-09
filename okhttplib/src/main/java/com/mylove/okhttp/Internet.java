package com.mylove.okhttp;

import android.content.Context;


/**
 * @author myLove
 */

class Internet {
    /**
     * 判断网络
     */
    static InternetBean ifInternet(Context mContext) {
        InternetBean msg = new InternetBean();
        switch (InternetUtil.getConnectedType(mContext)) {
            case -1:
                msg.setStatus(false);
                msg.setMsg("网络异常");
                return msg;
            case 0:
                if (InternetUtil.isNetWorkConnected(mContext)) {
                    msg.setStatus(false);
                    msg.setMsg("网络异常");
                    return msg;
                } else {
                    msg.setStatus(true);
                    return msg;
                }
            case 1:
                if (InternetUtil.isNetWorkConnected(mContext)) {
                    msg.setStatus(false);
                    msg.setMsg("WIFI网络异常");
                    return msg;
                } else {
                    msg.setStatus(true);
                    return msg;
                }
            default:
                if (InternetUtil.isNetWorkConnected(mContext)) {
                    msg.setStatus(false);
                    msg.setMsg("网络异常");
                    return msg;
                } else {
                    msg.setStatus(true);
                    return msg;
                }
        }
    }
}
