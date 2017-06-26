package com.coolyota.logreport.tools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.coolyota.logreport.LogSettingActivity;
import com.coolyota.logreport.R;

/**
 * 显示通知栏
 * Created by liuwenrong on 2016/6/20.
 */
public class NotificationShow {

    private static final int ID_LOG_RECORD = 111;
    public static final int ID_SCREEN_RECORD = 112;

    public static void startLogRecording(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_log_ongoing).setContentTitle(context.getString(R.string.offline_log))
                .setContentText(context.getString(R.string.log_recording))
                .setTicker(context.getString(R.string.log_recording))
               // .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOngoing(true)
                .setShowWhen(true)
                .setAutoCancel(false);
        builder.setContentIntent(PendingIntent.getActivity(context, ID_SCREEN_RECORD, new Intent(context, LogSettingActivity.class), PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_UPDATE_CURRENT));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID_LOG_RECORD, builder.build());
    }

    public static void cancelLogRecording(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(ID_LOG_RECORD);
    }

    public static Notification startScreenRecording(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_screen_video_record).setContentTitle(context.getString(R.string.screen_recording))
                .setContentText(context.getString(R.string.screen_video_recording))
                .setTicker(context.getString(R.string.screen_video_recording))
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOngoing(true)
                .setShowWhen(true)
                .setAutoCancel(false);
//        builder.setContentIntent(PendingIntent.getActivity(context, ID_SCREEN_RECORD, new Intent(context, ScreenRecordActivity.class), PendingIntent.FLAG_UPDATE_CURRENT));
        /*NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        final Notification notification = builder.build();
        notificationManager.notify(ID_SCREEN_RECORD, notification);*/
        return builder.build();
    }

    public static void cancelScreenRecording(Context context){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(ID_SCREEN_RECORD);
    }
}
