/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */

package com.coolyota.logreport.constants;

import com.coolyota.logreport.BuildConfig;

/**
 * @author liuwenrong@coolpad.com
 * @version 1.0, 2017/6/16
 */
public class ApiConstants {


    public static final boolean DebugEnabled = BuildConfig.LOG_DEBUG;
//    public static final String BASE_URL = "http://120.77.80.97";
    public static final String BASE_URL = BuildConfig.BASE_URL;
//    public static final String BASE_URL = "http://test.dcss.baoliyota.com";
//    public static final String BASE_URL = "http://pro.dcss.baoliyota.com";
//    private static final String BASE_URL = Constants.BASE_URL;

    /**
     * Broken pipe 有网络,但不能连接服务器
     * Exception.detailMessage
     */
    public static final String NET_BROKEN_PIPE = "Broken pipe";
    /**
     * 服务器返回的成功码
     */
    public static final int SUCCESS_CODE = 0;
    /**
     * 服务器返回的失败码
     */
    public static final int FAIL_CODE = 1;
    /**
     * 文件太大
     */
    public static final int FILE_TOO_MAX_CODE = 2;
    /**
    /**
     * 网络不可用
     */
    public static final int NET_UN_CODE = 3;
    /**
     * token 过期
     */
    public static final int TOKEN_CODE = 104;
    /**
     * 其他
     */
    public static final int OTHER_CODE = 105;
    /**
     * 压缩完成后开始上传
     */
    public static final int START_Upload = 110;
    /**
     * 压缩完成后用户取消上传
     */
    public static final int User_Cancel_Upload = 111;
    /**
     * 上传路径
     */
    public static final String PATH_UPLOAD = BASE_URL + "/dcss-collector/log/upload";

    /**
     * 登录token
     */
    public static final String PARAM_TOKEN = "token";

    /**
     * 产品型号在服务器端对应的key
     */
    public static final String PARAM_KEY = "key";

    /**
     * 产品型号在服务器端对应的key = 7576E9DD910227F0D1B297FC05D90BE7(注：固定的，其实是’ YOTA3’的MD5编码的大写)
     * 离线日志使用5001
     */
    public static final String VALUE_KEY = "5001";

    /**
     * 手机的唯一表示IMEI
     */
    public static final String PARAM_IMEI = "imei";

    /**
     * 时间戳参数
     */
    public static final String PARAM_TIME_STAMP = "timestamp";

    /**
     * logType 日志类型
     */
    public static final String PARAM_LOG_TYPE = "logType";

    /**
     * 日志分类:离线日志 20014
     */
    public static final int LOG_TYPE_LOG =  20014;

    /**
     * account 账号 (非必须)
     */
    public static final String PARAM_ACCOUNT = "account";

    /**
     * phone 手机号 (非必须)
     */
    public static final String PARAM_PHONE = "phone";
    /**
     * phone 手机号 (非必须)
     */
    public static final String PARAM_FILE = "file";

    /**
     * proType产品型号 (Y3)
     */
    public static final String PARAM_PRO_TYPE = "proType";

    /**
     * sysVersion 系统版本
     */
    public static final String PARAM_SYS_VERSION = "sysVersion";

    /**
     * upDesc上报描述 (非必须)
     */
    public static final String PARAM_UP_DESC = "upDesc";

    /**
     * upType上报分类 (必须)
     */
    public static final String PARAM_UP_TYPE = "upType";

    public static class UpType{

        public static final int TYPE_SUGGESTIONS = 20001;
        public static final int TYPE_TALK_SMS = 20002;
        public static final int TYPE_CAMERA_GALLERY = 20003;
        public static final int TYPE_WIFI_NET = 20004;
        public static final int TYPE_SYSTEM_PLAYER = 20005;
        public static final int TYPE_SYSTEM_UPGRADE = 20006;
        public static final int TYPE_OTHER_APP = 20007;
        public static int[] upTypes = {TYPE_SUGGESTIONS, TYPE_TALK_SMS, TYPE_CAMERA_GALLERY, TYPE_WIFI_NET, TYPE_SYSTEM_PLAYER, TYPE_SYSTEM_UPGRADE, TYPE_OTHER_APP};
    }

    /**
     * post请求的头信息
     */
    public static final String HEADER_CONTENT_TYPE = "Content-Type";

    /**
     * post请求的头信息-要求body为json格式
     */
    public static final String HEADER_CONTENT_TYPE_JSON = "application/json";

    /**
     * 返回值成功
     */
    public static final int CODE_SUCCESS = 0;

}

