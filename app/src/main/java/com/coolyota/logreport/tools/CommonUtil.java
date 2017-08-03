package com.coolyota.logreport.tools;

import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/7/14
 */
public class CommonUtil {
    private static ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

    CommonUtil() {
    }

    public static ReentrantReadWriteLock getRwl() {
        return rwl;
    }

}
