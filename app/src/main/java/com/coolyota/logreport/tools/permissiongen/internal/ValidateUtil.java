package com.coolyota.logreport.tools.permissiongen.internal;

import android.content.pm.PackageManager;

/**
 * Created by namee on 2015. 11. 17..
 */
public class ValidateUtil {
    public static boolean verifyGrants(int... grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }
}
