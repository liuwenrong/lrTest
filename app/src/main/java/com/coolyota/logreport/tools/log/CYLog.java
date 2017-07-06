package com.coolyota.logreport.tools.log;

import android.text.TextUtils;
import android.util.Log;

import com.coolyota.logreport.BuildConfig;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/2
 */
public class CYLog {
    public static final boolean DEBUG = BuildConfig.LOG_DEBUG;
    private final String module;
    private final String tag;

    public CYLog(String module, String tag) {
        this.module = !TextUtils.isEmpty(module) ? module : "CoolYota";
        this.tag = !TextUtils.isEmpty(tag) ? tag : "CoolYota";
    }

    public CYLog(String tag) {
        this("CoolYota", tag);
    }

    public void e(String msg, Throwable tr) {
        Log.e(tag, msg, tr);
    }

    public void w(String msg) {
        Log.w(tag, msg);
    }

    public void info(String msg) {
        Log.i(tag, msg);
    }

    public void debug(String msg) {
        if (DEBUG || Log.isLoggable(module, Log.DEBUG)) Log.d(tag, msg);
    }

    public void verbose(String msg) {
        if (DEBUG || Log.isLoggable(module, Log.VERBOSE)) Log.v(tag, msg);
    }
}
