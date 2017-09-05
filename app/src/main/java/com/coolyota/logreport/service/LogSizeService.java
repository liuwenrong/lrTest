package com.coolyota.logreport.service;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.coolyota.logreport.BuildConfig;
import com.coolyota.logreport.tools.ActivityUtils;
import com.coolyota.logreport.tools.FileUtil;
import com.coolyota.logreport.tools.NotificationShow;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import static com.coolyota.logreport.tools.LogUtil.FOLDER_NAME;

/**
 * 监听日志大小的服务,当大于500M,通知用户清除日志
 */
public class LogSizeService extends Service {

    public int timePeriod = 600 * 1000; //600s计算一次

    public LogSizeService() {
    }

    private final Timer timer = new Timer();
    private TimerTask task;
    /**
     * 日志文件在sdcard中的路径 sdcard/yota_log
     */
    private static String LOG_PATH_SDCARD_DIR = null;

    private String TAG = "LogSizeService";
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            // 要做的事情

            if (LOG_PATH_SDCARD_DIR == null) {
                LOG_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FOLDER_NAME;
            }
//            Log.e(TAG, "43-----------handleMessage: begin");
            long folderSize = FileUtil.getFileOrFolderSize(new File(LOG_PATH_SDCARD_DIR));
//            Log.e(TAG, "44----handleMessage: folderSize = " + folderSize );

            if (folderSize > FileUtil.LOG_FILE_MAX_SIZE) {

                String packageName = ActivityUtils.getTopPackageName(getApplicationContext());
//                Log.i(TAG, "handleMessage: ------55------packageName = " + packageName);
                if (!packageName.equals(BuildConfig.APPLICATION_ID)) {
                    NotificationShow.startLogDeleteNotice(LogSizeService.this, folderSize);
                }
            }

            super.handleMessage(msg);
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        task = new TimerTask() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task, 2000, timePeriod);

        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        timer.cancel();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
