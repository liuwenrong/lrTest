package com.coolyota.logreport.tools;


import com.coolyota.logreport.tools.log.ReflectionCall;

/**
 * Created by liuwenrong on 2016/05/31.
 */

public class SystemProperties {
    public static String get(String key, String def){
        try {
            return ReflectionCall.invoke(ReflectionCall.getMethod(Class.forName("android.os.SystemProperties"), "get",
                    String.class, String.class), null, key, def);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return def;
    }

    public static void set(String key, String val) {
        try {
            ReflectionCall.invoke(ReflectionCall.getMethod(Class.forName("android.os.SystemProperties"),"set",
                    String.class, String.class), null, key, val);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
