package com.coolyota.logreport.tools.log;

import android.text.TextUtils;
import android.util.Log;

import com.coolyota.logreport.BuildConfig;
import com.coolyota.logreport.constants.ApiConstants;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/2
 */
public class CYLog {
    CYLog() {
    }

    public static void v(String tag, Class<?> classobj, String msg) {
        if (ApiConstants.DebugEnabled) {
                Log.v(tag, classobj.getCanonicalName() + ": " + msg);
        }
    }

    public static void d(String tag, Class<?> classobj, String msg) {
        if (ApiConstants.DebugEnabled) {
                Log.d(tag, classobj.getCanonicalName() + ": " + msg);
        }
    }

    public static void i(String tag, Class<?> classobj, String msg) {
        if (ApiConstants.DebugEnabled) {
                Log.i(tag, classobj.getCanonicalName() + ": " + msg);
        }
    }

    public static void w(String tag, Class<?> classobj, String msg) {
        if (ApiConstants.DebugEnabled) {
                Log.w(tag, classobj.getCanonicalName() + ": " + msg);
        }
    }

    public static void e(String tag, Class<?> classobj, String msg) {
        if (ApiConstants.DebugEnabled) {
            Log.e(tag, classobj.getCanonicalName() + ": " + msg);
        }
    }
    public static final boolean DEBUG = BuildConfig.LOG_DEBUG;
    private static String module;
    private static String tag;

    public CYLog(String module, String tag) {
        this.module = !TextUtils.isEmpty(module) ? module : "CoolYota";
        this.tag = !TextUtils.isEmpty(tag) ? tag : "CoolYota";
    }

    public CYLog(String tag) {
        this("CoolYota", tag);
    }

    public static void e(String msg, Throwable tr) {
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
