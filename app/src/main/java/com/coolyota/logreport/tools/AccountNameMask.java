package com.coolyota.logreport.tools;

import java.util.regex.Pattern;

/**
 * 正则检查手机号或邮箱
 * Created by liuwenrong on 2017/6/2.
 */
public class AccountNameMask {
    private static final String STARS = "******";
    private static final String STARS_LESS = "***";

    /**
     * Regular expression for mobile number.For China
     */
//    private static final String REG_EXP_MOBILE = "^((\\+86)|(86))?1[34578]\\d{9}$";
//    private static final String REG_EXP_MOBILE = "^((\\+?86))?1[34578]\\d{9}$";
    private static final String REG_EXP_MOBILE = "^((\\+?\\d?\\d))?1[34578]\\d{9}$";
//  String m = "^((\\+{0,1}86){0,1})1[0-9]{10}"
    /**
     * Regular expression for email.
     */
    private static final String REG_EXP_EMAIL = "^(\\w)+(\\.\\w+)*@(\\w)+((\\.\\w{2,3}){1,3})$";

    public static String starMaskPhone(String phoneNumber) {
        if (phoneNumber.length() == 11) {
            return phoneNumber.substring(0, 3) + STARS + phoneNumber.substring(9, phoneNumber.length());
        } else if (phoneNumber.startsWith("86")) {
            return phoneNumber.substring(0, 5) + STARS + phoneNumber.substring(11, phoneNumber.length());
        } else if (phoneNumber.startsWith("+86")) {
            return phoneNumber.substring(0, 6) + STARS + phoneNumber.substring(12, phoneNumber.length());
        }
        return phoneNumber;
    }

    public static String starMaskEmail(String email) {
        int indexAt = email.indexOf("@");
        if (indexAt <= 3) {
            return email.substring(0, indexAt) + STARS_LESS + email.substring(indexAt, email.length());
        } else {
            return email.substring(0, 3) + STARS_LESS + email.substring(indexAt, email.length());
        }
    }

    public static String starMask(String accountName) {
        if (checkMobile(accountName)) {
            return starMaskPhone(accountName);
        } else if (checkEmail(accountName)) {
            return starMaskEmail(accountName);
        }
        return accountName;
    }

    /**
     * 验证手机号码（支持国际格式，+86135xxxx...（中国内地），+00852137xxxx...（中国香港））
     *
     * @param mobile 移动、联通、电信运营商的号码段
     *               移动的号段：134(0-8)、135、136、137、138、139、147（预计用于TD上网卡）、150、151、152、157（TD专用）、158、159、187（未启用）、188（TD专用）
     *               联通的号段：130、131、132、155、156（世界风专用）、185（未启用）、186（3g）
     *               电信的号段：133、153、180（未启用）、189
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkMobile(String mobile) {
        return null != mobile && Pattern.matches(REG_EXP_MOBILE, mobile);
    }

    /**
     * 验证Email
     *
     * @param email email地址，格式：zhang@gmail.com，zhang@xxx.com.cn，xxx代表邮件服务商
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkEmail(String email) {
        return null != email && Pattern.matches(REG_EXP_EMAIL, email);
    }
}
