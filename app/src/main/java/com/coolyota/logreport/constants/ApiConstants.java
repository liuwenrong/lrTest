/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */

package com.coolyota.logreport.constants;

/**
 * @author liuwenrong@coolpad.com
 * @version 1.0, 2017/6/16
 */
public class ApiConstants {


//    public static final String BASE_URL = "http://120.77.80.97";
    public static final String BASE_URL = "http://test.api.baoliyota.com";
//    private static final String BASE_URL = Constants.BASE_URL;

    // /collector/log/upload?token=xxxxxxxxxxxxxxxx

    /**
     * 上传路径
     */
    public static final String PATH_UPLOAD = BASE_URL + "/collector/log/upload";

    /**
     * 登录token
     */
    public static final String PARAM_TOKEN = "token";

    /**
     * 产品型号在服务器端对应的key
     */
    public static final String PARAM_KEY = "key";

    /**
     * 时间戳参数
     */
    public static final String PARAM_TIME_STAMP = "timestamp";


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

