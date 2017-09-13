package com.coolyota.logreport.tools;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.coolyota.logreport.MainActivity;
import com.coolyota.logreport.R;

/**
 * 显示通知栏
 * Created by liuwenrong on 2016/6/20.
 */
public class NotificationShow {

    private static final int ID_LOG_RECORD = 111;
    public static final int ID_LOG_DELETE = 113;
    public static final int ID_SCREEN_RECORD = 112;
    public static final String TYPE = "type";

    public static void startLogRecording(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_log_ongoing).setContentTitle(context.getString(R.string.offline_log))
                .setContentText(context.getString(R.string.log_recording))
                .setTicker(context.getString(R.string.log_recording))
               // .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setOngoing(true)
                .setShowWhen(true)
                .setAutoCancel(false);
        builder.setContentIntent(PendingIntent.getActivity(context, ID_SCREEN_RECORD, new Intent(context, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_UPDATE_CURRENT));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID_LOG_RECORD, builder.build());
    }

    public static void cancelLogRecording(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(ID_LOG_RECORD);
    }
    public static void startLogDeleteNotice(Context context, long size) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_file_delete).setContentTitle(context.getString(R.string.offline_log))
                .setContentText("Log大小" + FileUtil.getDataSize(size) + "可点击清理")
                .setTicker(context.getString(R.string.log_recording))
               // .setDefaults(NotificationCompat.DEFAULT_ALL)
//                .setOngoing(true)
                .setShowWhen(true)
                .setAutoCancel(true);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(TYPE, ID_LOG_DELETE);
        builder.setContentIntent(PendingIntent.getActivity(context, ID_LOG_DELETE, intent, PendingIntent.FLAG_UPDATE_CURRENT |
                PendingIntent.FLAG_UPDATE_CURRENT));
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(ID_LOG_DELETE, builder.build());
    }

    public static void cancelLogDeleteNotice(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(ID_LOG_DELETE);
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
