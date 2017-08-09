package com.coolyota.logreport.tools;

import java.io.File;
import java.text.DecimalFormat;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/8/8
 */
public class FileUtil {

    public static final int LOG_FILE_MAX_SIZE = 500 * 1024 * 1024;           //内存中日志文件最大值，500M

    /**
     * 获取文件夹大小
     *
     * @param file File实例
     * @return long
     */
    public static long getFolderSize(File file) {

        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFolderSize(fileList[i]);

                } else {
                    size = size + fileList[i].length();

                }
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //return size/1048576;
        return size;
    }

    /**
     * 返回byte的数据大小对应的文本
     *
     * @param size
     * @return
     */
    public static String getDataSize(long size) {
        DecimalFormat decimalFormat = new DecimalFormat("####.00");
        if (size < 1024) {
            return size + "Bytes";
        } else if (size < 1024 * 1024) {
            float kbSize = size / 1024f;
            return decimalFormat.format(kbSize) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            float mbSize = size / 1024f / 1024f;
            return decimalFormat.format(mbSize) + "MB";
        } else if (size < 1024 * 1024 * 1024 * 1024) {
            float gbSize = size / 1024f / 1024f / 1024f;
            return decimalFormat.format(gbSize) + "GB";
        } else {
            return "size: error";
        }

    }
}
