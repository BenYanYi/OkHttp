package com.mylove.okhttp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.Map;

/**
 * @author BenYanYi
 * @date 2018/11/20 15:51
 * @email ben@yanyi.red
 * @overview
 */
public class NotificationUtil {
    private Context mContext;
    // NotificationManager ： 是状态栏通知的管理类，负责发通知、清楚通知等。
    private NotificationManager manager;
    // 定义Map来保存Notification对象
    private Map<Integer, Notification> map = null;
    private int icon;
    private Class<?> aClass;
    private boolean isCreateChannel = false;
    private static final String NOTIFICATION_CHANNEL_NAME = "Update";
    private NotificationManager notificationManager = null;
    private String tickerText;

    public NotificationUtil(Context context) {
        this.mContext = context;
        // NotificationManager 是一个系统Service，必须通过 getSystemService()方法来获取。
        manager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        map = new HashMap<>();
    }

    public NotificationUtil setIcon(int icon) {
        this.icon = icon;
        return this;
    }

    public NotificationUtil setClass(Class<?> aClass) {
        this.aClass = aClass;
        return this;
    }

    public NotificationUtil setTickerText(String tickerText) {
        this.tickerText = tickerText;
        return this;
    }

    public void showNotification(int notificationId) {
        // 判断对应id的Notification是否已经显示， 以免同一个Notification出现多次
        if (!map.containsKey(notificationId)) {
            // 创建通知对象
            Notification notification;
            Notification.Builder builder;
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                //Android O上对Notification进行了修改，如果设置的targetSDKVersion>=26建议使用此种方式创建通知栏
                if (null == notificationManager) {
                    notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
                }
                String channelId = mContext.getPackageName();
                if (!isCreateChannel) {
                    NotificationChannel notificationChannel = new NotificationChannel(channelId,
                            NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
                    notificationChannel.enableLights(true);//是否在桌面icon右上角展示小圆点
                    notificationChannel.setLightColor(Color.BLUE); //小圆点颜色
                    notificationChannel.setShowBadge(true); //是否在久按桌面图标时显示此渠道的通知
                    notificationManager.createNotificationChannel(notificationChannel);
                    isCreateChannel = true;
                }
                builder = new Notification.Builder(mContext.getApplicationContext(), channelId);
                notification = builder.build();
            } else {
                notification = new Notification();
            }
            // 设置通知栏滚动显示文字
            notification.tickerText = tickerText;
            // 设置显示时间
            notification.when = System.currentTimeMillis();
            // 设置通知显示的图标
            notification.icon = icon;
            // 设置通知的特性: 通知被点击后，自动消失
            notification.flags = Notification.FLAG_AUTO_CANCEL;
            // 设置点击通知栏操作
            if (aClass != null) {
                Intent in = new Intent(mContext, aClass);// 点击跳转到指定页面
                notification.contentIntent = PendingIntent.getActivity(mContext, 0, in,
                        0);
            }
            // 设置通知的显示视图
            notification.contentView = new RemoteViews(
                    mContext.getPackageName(),
                    R.layout.notification_contentview);
            // 发出通知
            manager.notify(notificationId, notification);
            map.put(notificationId, notification);// 存入Map中
        }
    }

    /**
     * 取消通知操作
     */
    public void cancel(int notificationId) {
        manager.cancel(notificationId);
        map.remove(notificationId);
    }

    public void updateProgress(int notificationId, int progress) {
        Notification notify = map.get(notificationId);
        if (null != notify) {
            // 修改进度条
            notify.contentView.setProgressBar(R.id.pBar, 100, progress, false);
            manager.notify(notificationId, notify);
        }
    }

    public void tickerText(int notificationId, String text) {
        Notification notify = map.get(notificationId);
        if (null != notify) {
            // 修改进度条
            notify.tickerText = text;
            manager.notify(notificationId, notify);
        }
    }

    public void updateProgress(int notificationId, int progress, String text) {
        Notification notify = map.get(notificationId);
        if (null != notify) {
            notify.tickerText = text;
            // 修改进度条
            notify.contentView.setProgressBar(R.id.pBar, 100, progress, false);
            manager.notify(notificationId, notify);
        }
    }

}
