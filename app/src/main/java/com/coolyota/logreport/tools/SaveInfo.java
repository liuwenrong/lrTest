/* *
   * Copyright (C) 2017 BaoliYota Tech. Co., Ltd, LLC - All Rights Reserved.
   *
   * Confidential and Proprietary.
   * Unauthorized copying of this file, via any medium is strictly prohibited.
   * */
package com.coolyota.logreport.tools;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import com.coolyota.logreport.constants.CYConstants;
import com.coolyota.logreport.tools.log.CYLog;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/22
 */
public class SaveInfo extends Thread {
    public static final String TAG = "SaveInfo";
    private static Handler handler;

    static {
        HandlerThread localHandlerThread = new HandlerThread("SaveInfo");
        localHandlerThread.start();
        handler = new Handler(localHandlerThread.getLooper());
    }

    public String msg;
    private String filetype;
    private String filePath;
    private Context context;

    public SaveInfo(String msg, String filetype, String cacheFilePath, Context context) {
        this.msg = msg;
        this.filePath = cacheFilePath;
        this.filetype = filetype;
        this.context = context;
    }

    public void run() {
        CYLog.d(TAG, SaveInfo.class, "Save cache file " + this.filePath);
        if (this.msg.length() != 0) {

            this.fileAppend(this.msg);

        }
    }


    /**
     * 文件追加的方式,写入arr中的每一个Obj
     *
     * @param msg
     */
    public void fileAppend(String msg) {
        FileWriter writer = null;
        ReentrantReadWriteLock rwl = CommonUtil.getRwl();

        while (!rwl.writeLock().tryLock()) {
            ;
        }
        rwl.writeLock().lock();

        try {
            writer = new FileWriter(this.filePath, true);

            CYLog.d(TAG, SaveInfo.class, "157==json data " + this.msg.toString());


            writer.write(msg + CYConstants.newLine); //JsonObj+换行

        } catch (IOException e1) {
            CYLog.e(TAG, e1);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                CYLog.e(TAG, e);
            }

            rwl.writeLock().unlock();
        }

    }
}
