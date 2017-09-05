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
     * 获取文件或文件夹大小
     * @param file File实例
     * @return long
     */
    public static long getFileOrFolderSize(File file) {

        long size = 0;

        if (file.isFile()) {
            return file.length();
        }

        try {
            java.io.File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    size = size + getFileOrFolderSize(fileList[i]);

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

    static DecimalFormat decimalFormat = new DecimalFormat("####.00");
    /**
     * 返回byte的数据大小对应的文本
     * @param size
     * @return
     */
    public static String getDataSize(long size) {
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
            return "太大";
        }
    }

    public static String getDataSize(File file) {
        return getDataSize(getFileOrFolderSize(file));
    }

    public static File[] getFileList(File file) {

        if( !file.exists() || file.isFile()) { //不存在 或者是文件 退出
            return null;
        }

        return file.listFiles();
    }


    /**
     * 递归删除目录以及内部文件 或者文件
     * @param dir 要删除的目录
     */
    public static void deleteDirWithFile(File dir) {
        if (dir == null || !dir.exists())
            return;
        if ( !dir.isDirectory() ) {//是文件,删除后return
            dir.delete();
            return;
        }
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWithFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }


}
