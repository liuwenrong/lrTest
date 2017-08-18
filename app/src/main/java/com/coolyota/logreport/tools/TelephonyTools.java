package com.coolyota.logreport.tools;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.coolyota.logreport.CYLogReporterApplication;
import com.coolyota.logreport.tools.log.CYLog;

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
    public static TelephonyTools getInstance(Context ctx){

        if (sTelephonyTools == null) { //双重校验锁,提高效率
            synchronized (TelephonyTools.class) {
                if (sTelephonyTools == null) {
                    sTelephonyTools = new TelephonyTools();
                    if (ctx != null) {

                        sTelephonyManager = (TelephonyManager) ctx.getSystemService(Context.TELEPHONY_SERVICE);
                    } else {

                        sTelephonyManager = (TelephonyManager) CYLogReporterApplication.getInstance().getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
                    }
                }
            }
        }

        return sTelephonyTools;
    }

    /**
     * @return 获取1卡的号码
     */
    public String getLine1Number() {
        return sTelephonyManager.getLine1Number();//获取本机号码
    }

    /**
     * @return 唯一的设备ID： 得到的是null
     * GSM手机的 IMEI 和 CDMA手机的 MEID.
     */
    public String getImei(){
        String imei = sTelephonyManager.getDeviceId();
        CYLog.i("TelephonyTools", TelephonyTools.class, "getImei: imei = " + imei);
        if (TextUtils.isEmpty(imei)){
            imei = getIMEI();
        }
        return imei;
    }
    /**
     * 获取设备imei号
     *
     * @return imei号
     */
    public static String getIMEI() {
        String imei = Settings.System.getString(CYLogReporterApplication.getInstance().getContentResolver(), Settings.System.ANDROID_ID);
//        String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
//        JvLog.d("JvLApplication imei ", deviceId + " TelephonyManager");
        return imei;
    }

    /**
     * @return 产品型号 (Y3) ro.product.brand ro.product.model
     */
    public static String getProType(){
        return Build.BRAND + " " +Build.MODEL;
    }

    /**
     * @return 系统版本 Y3XSCN061000DPX1707031 Y3-userdebug 7.1.1 230 test-keys
     */
    public static String getSysVersion() {
        return SystemProperties.get("persist.sys.zs.last_build", "Y3XSCN061000DPX1707031 Y3-userdebug 7.1.1 230 test-keys");
    }

}
