/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */

package com.coolyota.logreport.tools;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.coolyota.logreport.constants.ApiConstants;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * url的拼接工具
 *
 * @author liuwenrong@coolpad.com
 * @version 1.0, 2017/6/16
 */
public class UrlBuilder {

    /**
     * 为目标url添加通用get参数
     *
     * @param urlString 目标url
     * @param token     token不需要可以是null
     * @return 装饰过公共get参数以后的url
     */
    public static String decorateCommonParams(String urlString, @Nullable String token, Map<String, Object> params) {
        Map<String, String> map = new HashMap<>();

        String imei = TelephonyTools.getInstance(null).getImei();
        String timeStamp = String.valueOf(System.currentTimeMillis());
        map.put(ApiConstants.PARAM_KEY, ApiConstants.VALUE_KEY);
        map.put(ApiConstants.PARAM_IMEI, imei);
        map.put(ApiConstants.PARAM_TIME_STAMP, timeStamp);

        params.put(ApiConstants.PARAM_KEY, ApiConstants.VALUE_KEY);
        params.put(ApiConstants.PARAM_IMEI, imei);
        params.put(ApiConstants.PARAM_TIME_STAMP, timeStamp);

//        if (!TextUtils.isEmpty(token)) {
//            map.put(ApiConstants.PARAM_TOKEN, token);
//        }

        return urlString + "?token=" + UrlBuilder.buildSign(map, token);

       /* StringBuilder sb = new StringBuilder();
        sb.append(urlString).append("?");

        for (Map.Entry<String, String> entry : mapToken.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        return sb.substring(0, sb.length() - 1);*/
    }

    /**
     * 对所有非空的参数拼接后Md5加密
     *
     * @param params 待拼接的参数
     * @param salt   盐值,不需要可以传空
     * @return 拼接并用md5加密以后的值
     */
    public static String buildSign(final Map<String, String> params, @Nullable String salt) {
        if (params == null) {
            return "";
        }
        Map<String, String> map = new HashMap<>();
        map.putAll(params);

        for (Map.Entry<String, String> entry : params.entrySet()) {// 去除空值参数，不加入签名
            if (TextUtils.isEmpty(entry.getValue())) {
                map.remove(entry.getKey());
            }
        }
//        map = sortMapByKey(map);// 排序
        StringBuilder signUrl = new StringBuilder();
        signUrl.append(map.get(ApiConstants.PARAM_KEY)).append(map.get(ApiConstants.PARAM_IMEI)).append(map.get(ApiConstants.PARAM_TIME_STAMP));
//        for (Map.Entry<String, String> entry : map.entrySet()) {
//            signUrl.append("&").append(entry.getKey()).append("=").append(entry.getValue());
//        }
        return MD5Utils.MD5(signUrl.toString()/*, salt*/);
    }

    /**
     * 使用 Map按key进行排序
     */
    private static Map<String, String> sortMapByKey(Map<String, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<String, String> sortMap = new TreeMap<>(new MapKeyComparatorUtil());
        sortMap.putAll(map);
        return sortMap;
    }

    // 比较器类
    private static class MapKeyComparatorUtil implements Comparator<String> {
        public int compare(String str1, String str2) {
            return str1.compareTo(str2);
        }
    }

}

