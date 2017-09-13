package com.coolyota.logreport;

import android.app.Application;
import android.content.Context;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/21
 */
public class CYLogReporterApplication extends Application {

    private static CYLogReporterApplication sApplication;

    public static CYLogReporterApplication getInstance() {
        return sApplication;
    }

    public static Context getContext() {
        return sApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            return;
//        }
//        LeakCanary.install(this);
        sApplication = this;
        //目前不要监听Log大小弹通知,所以注释掉
//        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
//        TimeTickReceiver receiver = new TimeTickReceiver();
//        registerReceiver(receiver, filter);

    }
}
