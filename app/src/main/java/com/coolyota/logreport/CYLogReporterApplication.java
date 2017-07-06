package com.coolyota.logreport;

import android.app.Application;
import android.util.Log;

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
        Log.e("CYLogApp", "onCreate");
        sApplication = this;
    }

    public static CYLogReporterApplication getInstance(){
        return sApplication;
    }

}
