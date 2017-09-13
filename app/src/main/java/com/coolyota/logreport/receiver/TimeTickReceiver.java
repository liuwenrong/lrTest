package com.coolyota.logreport.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * des: 在应用启动时调用,开启记录log大小并通知
 * @author liuwenrong
 * @version 1.0, 2017/8/8
 */
public class TimeTickReceiver extends BroadcastReceiver {
    private String TAG = "TimeTickReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
//
//        if (intent.getAction().equals(Intent.ACTION_TIME_TICK)) {
//
//            //检查Service状态
//            boolean isServiceRunning = false;
//
//            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
//                String className = service.service.getClassName();
////                Log.e(TAG, "32----------onReceive: className = " + className);
//                if ("com.coolyota.logreport.service.LogSizeService".equals(className)) {
//                    isServiceRunning = true;
//                }
//            }
////            Log.e(TAG, "40------onReceive: isServiceRunning = " + isServiceRunning);
//            if (!isServiceRunning) {
//                Intent i = new Intent(context, LogSizeService.class);
//                context.startService(i);
//                /*String LOG_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FOLDER_NAME;
//                Log.e(TAG, "43-----------handleMessage: begin");
//                long folderSize = FileUtil.getFolderSize(new File(LOG_PATH_SDCARD_DIR));
//                Log.e(TAG, "44----handleMessage: folderSize = " + folderSize );
//                NotificationShow.startLogDeleteNotice(CYLogReporterApplication.getContext(), folderSize);*/
//            }
//        }
    }
}
