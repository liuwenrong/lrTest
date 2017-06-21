package com.coolyota.logreport.tools;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.coolyota.logreport.CYLogReporterApplication;

/**
 * des: 
 * 
 * @author  liuwenrong
 * @version 1.0,2017/6/21 
 */
public class TelephonyTools {

    private static volatile TelephonyTools sTelephonyTools;
    private static TelephonyManager sTelephonyManager;

    private TelephonyTools(){

    }

    /**
     * 懒汉式单例模式-线程安全
     * @return
     */
    public static TelephonyTools getInstance(){

        if (sTelephonyTools == null) { //双重校验锁,提高效率
            synchronized (TelephonyTools.class) {
                if (sTelephonyTools == null) {
                    sTelephonyTools = new TelephonyTools();
                    sTelephonyManager = (TelephonyManager) CYLogReporterApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
                }
            }
        }

        return sTelephonyTools;
    }

    /**
     * @return 唯一的设备ID：
     * GSM手机的 IMEI 和 CDMA手机的 MEID.
     */
    public String getImei(){
        return sTelephonyManager.getDeviceId();
    }

}
