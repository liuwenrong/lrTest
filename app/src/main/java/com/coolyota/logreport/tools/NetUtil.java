/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */
package com.coolyota.logreport.tools;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import com.coolyota.logreport.tools.log.CYLog;

import java.security.MessageDigest;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * des: 
 * 
 * @author  liuwenrong
 * @version 1.0,2017/6/22
 */
public    class NetUtil {
    public static final String TAG = "NetUtil";
    private static String USER_ID = "";
    private static String curVersion = "";
    private static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    NetUtil() {
    }

    public static ReentrantReadWriteLock getRwl() {
        return rwl;
    }







    public static String getNetworkType(Context context) {
        if(context == null) {
            return "";
        } else {
            TelephonyManager manager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
            int type = manager.getNetworkType();
            String typeString = "UNKNOWN";
            if(type == 4) {
                typeString = "CDMA";
            }

            if(type == 2) {
                typeString = "EDGE";
            }

            if(type == 5) {
                typeString = "EVDO_0";
            }

            if(type == 6) {
                typeString = "EVDO_A";
            }

            if(type == 1) {
                typeString = "GPRS";
            }

            if(type == 8) {
                typeString = "HSDPA";
            }

            if(type == 10) {
                typeString = "HSPA";
            }

            if(type == 9) {
                typeString = "HSUPA";
            }

            if(type == 3) {
                typeString = "UMTS";
            }

            if(type == 0) {
                typeString = "UNKNOWN";
            }

            if(type == 7) {
                typeString = "1xRTT";
            }

            if(type == 11) {
                typeString = "iDen";
            }

            if(type == 12) {
                typeString = "EVDO_B";
            }

            if(type == 13) {
                typeString = "LTE";
            }

            if(type == 14) {
                typeString = "eHRPD";
            }

            if(type == 15) {
                typeString = "HSPA+";
            }

            return typeString;
        }
    }

    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            CYLog.e(TAG, NetUtil.class, "context is null");
            return false;
        } else if (checkPermissions(context, "android.permission.INTERNET")) {
            ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cManager == null) {
                return false;
            } else {
                NetworkInfo info = cManager.getActiveNetworkInfo();
                if (info != null && info.isAvailable()) {
                    CYLog.i(TAG, NetUtil.class, "Network is available.");
                    return true;
                } else {
                    CYLog.i(TAG, NetUtil.class, "Network is not available.");
                    return false;
                }
            }
        } else {
            CYLog.e(TAG, NetUtil.class, "android.permission.INTERNET permission should be added into AndroidManifest.xml.");
            return false;
        }
    }

    public static boolean isNetworkTypeWifi(Context context) {
        if(context == null) {
            return false;
        } else if(checkPermissions(context, "android.permission.INTERNET")) {
            ConnectivityManager cManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if(cManager == null) {
                return false;
            } else {
                NetworkInfo info = cManager.getActiveNetworkInfo();
                if(info != null && info.isAvailable() && info.getType() == 1) {
                    return true;
                } else {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public static boolean checkPermissions(Context context, String permission) {
        if(context != null && !permission.equals("") && !permission.equals("")) {
            PackageManager pm = context.getPackageManager();
            return pm.checkPermission(permission, context.getPackageName()) == 0;
        } else {
            return false;
        }
    }

    public static String md5(String str) {
        try {
            MessageDigest e = MessageDigest.getInstance("MD5");
            e.update(str.getBytes());
            byte[] arrayOfByte = e.digest();
            StringBuffer localStringBuffer = new StringBuffer();
            byte[] var7 = arrayOfByte;
            int var6 = arrayOfByte.length;

            for(int var5 = 0; var5 < var6; ++var5) {
                byte anArrayOfByte = var7[var5];
                int j = 255 & anArrayOfByte;
                if(j < 16) {
                    localStringBuffer.append("0");
                }

                localStringBuffer.append(Integer.toHexString(j));
            }

            return localStringBuffer.toString();
        } catch (Exception var9) {
            return "";
        }
    }


}
