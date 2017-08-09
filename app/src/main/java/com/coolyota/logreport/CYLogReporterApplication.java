package com.coolyota.logreport;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.coolyota.logreport.receiver.TimeTickReceiver;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/21
 */
public class CYLogReporterApplication extends Application {

    private static CYLogReporterApplication sApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
        TimeTickReceiver receiver = new TimeTickReceiver();
        registerReceiver(receiver, filter);
    }

    public static CYLogReporterApplication getInstance(){
        return sApplication;
    }

    public static Context getContext(){
        return sApplication;
    }
}
