package com.coolyota.logreport.tools;

import android.text.TextUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/8/8
 */
public class FileUtil {

    private static final byte[] zipDataBuffer = new byte[1024 * 1024];  //压缩zip文件的buffer大小
    public static final int LOG_FILE_MAX_SIZE = 500 * 1024 * 1024;           //内存中日志文件最大值，500M
    static DecimalFormat decimalFormat = new DecimalFormat("####.00");

    /**
     * 获取文件或文件夹大小
     *
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

    /**
     * 返回byte的数据大小对应的文本
     *
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

        if (!file.exists() || file.isFile()) { //不存在 或者是文件 退出
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
        if (!dir.isDirectory()) {//是文件,删除后return
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

    /**
     * 递归删除目录(必须是目录)
     * @param dir
     * @param deleteDir 是否删除本身
     */
    public static void deleteLogFileDir(File dir, boolean deleteDir) {
                if (!dir.exists()) return;
                File[] list = dir.listFiles();
                if (null == list) return;
                for (File file : list) {
                    if (file.isFile()) {
                        file.delete();
                    } else if (file.isDirectory()) {
                        deleteLogFileDir(file, true);
                    }
                }
                if (deleteDir) dir.delete();
            }

    /**
     * 删除已经上传的log
     */
    public static void deleteUploadLogs(HashMap<String, File> mDeleteFileOrFolder) {
        Set<String> keys = mDeleteFileOrFolder.keySet();
        for (String key: keys){

            File file = mDeleteFileOrFolder.get(key);
            if (file.exists()){
                if (file.isFile()){
                    file.delete();
                }else if(file.isDirectory()) {
                    FileUtil.deleteLogFileDir(file, true);
                }
            }

        }
    }
    /**
     * 确保文件目录下都可读写
     * @param logDir
     */
    public static void ensureAllReadWrite(File logDir) {
        try {
            Process process = Runtime.getRuntime().exec("chmod a+rw -R " + logDir.getAbsolutePath());
            Thread.currentThread().sleep(500);
            process.destroy();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static boolean ensureTransferValidFileToGZip(ZipOutputStream zipOs, File logFile, String entryParentName, String folderNameByDate) throws IOException {
        boolean hasLogFile = false;
        if (null != logFile && logFile.exists()) {
            if (logFile.isFile() && 0 < logFile.length()) {
                transferFileToGZip(zipOs, logFile.getAbsolutePath(), entryParentName);
                hasLogFile = true;
            } else if (logFile.isDirectory()) {
                File[] logFiles = logFile.listFiles();
                if (null != logFiles && 0 < logFiles.length) {
                    for (File targetFile : logFiles) {
                        boolean result;
                        if (logFile.getName().equals(folderNameByDate)) { //如果文件夹名字 是日期 就不当成entryParentName
                            result = ensureTransferValidFileToGZip(zipOs, targetFile, null, null);
                        } else {
                            result = ensureTransferValidFileToGZip(zipOs, targetFile,
                                    (null == entryParentName ? "" : (entryParentName + File.separator)) + logFile.getName(), null);
                        }
                        hasLogFile = result ? result : hasLogFile;
                    }
                }
            }
        }
        return hasLogFile;
    }

    /**
     * 将文件转换成zip
     * @param zipOs
     * @param reportFilePath 文件的绝对路径
     * @param entryParentName 文件在zip中的父路径名
     * @throws IOException
     */
    public static void transferFileToGZip(ZipOutputStream zipOs, String reportFilePath, String entryParentName) throws IOException {
        InputStream is = null;
        int length;
        try {
            ZipEntry entry;
            if (TextUtils.isEmpty(entryParentName)) {
                entry = new ZipEntry(new File(reportFilePath).getName());
            } else {
                entry = new ZipEntry(entryParentName + File.separator + new File(reportFilePath).getName());
            }
            zipOs.putNextEntry(entry);
            is = new BufferedInputStream(new FileInputStream(reportFilePath));
            while (-1 != (length = is.read(zipDataBuffer))) {
                zipOs.write(zipDataBuffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            zipOs.flush();
//                    zipOs.closeEntry();
        }
    }

}
