package com.coolyota.logreport;

import android.app.Application;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/21
 */
public class CYLogReporterApplication extends Application {

    private static CYLogReporterApplication sApplication = new CYLogReporterApplication();

    private CYLogReporterApplication(){

    }

    public static CYLogReporterApplication getInstance(){
        return sApplication;
    }

}
