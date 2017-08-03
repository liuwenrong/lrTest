package com.coolyota.logreport.tools;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.coolyota.logreport.R;
import com.coolyota.logreport.constants.ApiConstants;
import com.coolyota.logreport.tools.log.CYLog;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.coolyota.logreport.tools.LogUtil.FOLDER_NAME;


/**
 * des: 压缩附件的服务
 * 
 * @author  liuwenrong
 * @version 1.0,2017/6/15 
 */
public class CompressAppendixService extends Service {

    public CYLog mCYLog = new CYLog("CompressAppendixService");
    public static final String UP_TYPE = "upType";
    public static final String BUG_DETAILS = "bug_details";
    public static final String USER_CONTACT = "user_contact";
    public static final String PIC_IMAGE_LIST = "pic_image_list";
    private static final byte[] zipDataBuffer = new byte[1024 * 1024];
    private static final String START_ID = "start_id";

    HashMap<String, File> mDeleteFileOrFolder = new HashMap<>();

    private Context mContext;
    /**
     * log文件夹 sd卡/yota_log
     */
    public String mAbsFolderName;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final Bundle extras = new Bundle(intent.getExtras());
        extras.putInt(START_ID, startId);
        mContext = this;

        genZip(extras);

        return Service.START_REDELIVER_INTENT;
    }

    /**
     * 生成.tar
     * @param extras
     */
    public void genZip(final Bundle extras/*, final File[] typeLogs*/) {

        new AsyncTask<Void, Void, File>() {
            public String imei;

            @Override
            protected void onPreExecute() {
//                this.imei = getImeiForIndex(0);
            }

            @Override
            protected File doInBackground(Void... params) {

                if (!NetUtil.isNetworkAvailable(getContext())) {
                    return null;
                }

                File zipAllFile = new File(Environment.getExternalStorageDirectory(), FOLDER_NAME + File.separator + "UploadFile" + File
                        .separator + SystemProperties.get("ro.build.id", "") + "_" + getReportFileTimestamp() /* + "_" + imei */+ ".zip");

                zipAllFile.getParentFile().mkdirs();
                BufferedOutputStream outputStream = null;
                ZipOutputStream zipOs = null;
                boolean isZipEmpty = true;
                try {
                    zipAllFile.createNewFile();
                    outputStream = new BufferedOutputStream(new FileOutputStream(zipAllFile));
                    zipOs = new ZipOutputStream(outputStream);

                    //截图
                    String[] picImageList = extras.getStringArray(PIC_IMAGE_LIST);
                    if (null != picImageList) {
                        for (String picImagePath : picImageList) {
                            if (null != picImagePath) {
                                ensureTransferValidFileToGZip(zipOs, new File(picImagePath), null, null);
                                isZipEmpty = false;
                            }
                        }
                    }

                    LogUtil.init(getContext());
                    LogUtil.collectorStatusInfo();

                    //滚动日志与离线日志 系统的无法上传后删除, TODO 暂时注释
                   /* File dropbox = new File("/data/system/dropbox");
                    if (dropbox.exists() && dropbox.isDirectory()) {
                        ensureAllReadWrite(dropbox);
                        boolean result = ensureTransferValidFileToGZip(zipOs, dropbox, "statusinfo", null);
                        isZipEmpty = result ? false : isZipEmpty;
                    }
                    mDeleteFileOrFolder.put("dropbox", dropbox);*/

                    // anr日志
                    File anr = new File("/data/anr");
                    if (anr.exists() && anr.isDirectory()) {
                        ensureAllReadWrite(anr);
                        boolean result = ensureTransferValidFileToGZip(zipOs, anr, "statusinfo", null);
                        isZipEmpty = result ? false : isZipEmpty;
                    }
                    mDeleteFileOrFolder.put("anr", anr);

                    final File tombstones = new File("/data/tombstones");
                    if (tombstones.exists() && tombstones.isDirectory()) {
                        ensureAllReadWrite(tombstones);
                        boolean result = ensureTransferValidFileToGZip(zipOs, tombstones, "statusinfo", null);
                        isZipEmpty = result ? false : isZipEmpty;
                    }
                    // 没有删除权限
//                    mDeleteFileOrFolder.put("tomb", tombstones);

                    mAbsFolderName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FOLDER_NAME;
                    //Qxdm日志 diag_logs 太大对服务器压力大 TODO 暂时注释
                    /*final File diagLogs = new File(mAbsFolderName + File.separator + "diag_logs");
                    if (diagLogs.exists() && diagLogs.isDirectory()) {
                        ensureAllReadWrite(diagLogs);
                        boolean result = ensureTransferValidFileToGZip(zipOs, diagLogs, null, null);
                        isZipEmpty = result ? false : isZipEmpty;
                    }
                    mDeleteFileOrFolder.put("diag_logs", diagLogs);*/

                    final File statusinfo = new File(mAbsFolderName + File.separator + "statusinfo");
                    if (statusinfo.exists() && statusinfo.isDirectory()) {
                        ensureAllReadWrite(statusinfo);
                        boolean result = ensureTransferValidFileToGZip(zipOs, statusinfo, null, null);
                        isZipEmpty = result ? false : isZipEmpty;
                    }
                    mDeleteFileOrFolder.put("statusinfo", statusinfo);

                    // 获取底层最新的yot_log目录,如果第一个文件夹小于10M,压缩返回最新的和第二新的文件夹
                    List<String> folderByDates = getYotaLogFoldersByTime();
                    /*if(folderByDates.size() >= 2 && getFolderSize(new File(mAbsFolderName + File.separator + folderByDates.get(0))) < mSize10M){

                        String folderByDate0 = folderByDates.get(0);
                        final File yotalogDate0 = new File(mAbsFolderName + File.separator + folderByDate0);
                        if (yotalogDate0.exists() && yotalogDate0.isDirectory()) {
                            ensureAllReadWrite(yotalogDate0);
                            boolean result = ensureTransferValidFileToGZip(zipOs, yotalogDate0, null, folderByDate0);
                            isZipEmpty = result ? false : isZipEmpty;
                        }
                        mDeleteFileOrFolder.put("yota_log", yotalogDate0);

                        // history 上一个记录的文件夹存放在history目录下
                        String folderByDate1 = folderByDates.get(1);
                        String appEntryParentName = "history" + File.separator + "apps";
                        String appFolderName = mAbsFolderName + File.separator + folderByDate1 + File.separator + "apps";
                        transferFileToGZip(zipOs, appFolderName + File.separator + "android.txt", appEntryParentName);
                        transferFileToGZip(zipOs, appFolderName + File.separator + "android_boot.txt", appEntryParentName);
                        transferFileToGZip(zipOs, appFolderName + File.separator + "events.txt", appEntryParentName);
                        transferFileToGZip(zipOs, appFolderName + File.separator + "events_boot.txt", appEntryParentName);
//                        transferFileToGZip(zipOs, appFolderName + File.separator + "events.txt.1", appEntryParentName);
                        transferFileToGZip(zipOs, appFolderName + File.separator + "radio.txt", appEntryParentName);
                        transferFileToGZip(zipOs, appFolderName + File.separator + "radio_boot.txt", appEntryParentName);
//                        transferFileToGZip(zipOs, appFolderName + File.separator + "radio.txt.01", appEntryParentName);
                        if (new File(appFolderName + File.separator + "android.txt").length() < 5 * 1024 * 1024){ //第一个文件小于10M

                            transferFileToGZip(zipOs, appFolderName + File.separator + "android.txt.01", appEntryParentName);
                        }
//                        transferFileToGZip(zipOs, appFolderName + File.separator + "android.txt.02", appEntryParentName);


                        String kernelFolderName = mAbsFolderName + File.separator + folderByDate1 + File.separator + "kernel";
                        File kernelDate0File = new File(kernelFolderName);
                        if (kernelDate0File.exists() && kernelDate0File.isDirectory()) {
                            ensureAllReadWrite(kernelDate0File);
                            boolean result = ensureTransferValidFileToGZip(zipOs, kernelDate0File, "history", null);
                            isZipEmpty = result ? false : isZipEmpty;
                        }

                        String netlogFolderName = mAbsFolderName + File.separator + folderByDate1 + File.separator + "netlog";
                        File netlogDate0File = new File(netlogFolderName);
                        if (netlogDate0File.exists() && netlogDate0File.isDirectory()) {
                            ensureAllReadWrite(netlogDate0File);
                            boolean result = ensureTransferValidFileToGZip(zipOs, netlogDate0File, "history", null);
                            isZipEmpty = result ? false : isZipEmpty;
                        }

                        mDeleteFileOrFolder.put("history", new File( mAbsFolderName + File.separator + folderByDate1));


                    } else */if (folderByDates.size() > 0){
                        //去各目录取文件,有些文件取3个
                        String folderByDate0 = folderByDates.get(0);
                        String entryParentName = "apps";

                        String appFolderName = mAbsFolderName + File.separator + folderByDate0 + File.separator + "apps";
                        transferFileToGZip(zipOs, appFolderName + File.separator + "android.txt", entryParentName);
                        transferFileToGZip(zipOs, appFolderName + File.separator + "android_boot.txt", entryParentName);
                        transferFileToGZip(zipOs, appFolderName + File.separator + "events.txt", entryParentName);
                        transferFileToGZip(zipOs, appFolderName + File.separator + "events_boot.txt", entryParentName);
//                        transferFileToGZip(zipOs, appFolderName + File.separator + "events.txt.1", entryParentName);
                        transferFileToGZip(zipOs, appFolderName + File.separator + "radio.txt", entryParentName);
                        transferFileToGZip(zipOs, appFolderName + File.separator + "radio_boot.txt", entryParentName);
//                        transferFileToGZip(zipOs, appFolderName + File.separator + "radio.txt.01", entryParentName);
                        if (new File(appFolderName + File.separator + "android.txt").length() < 10 * 1024 * 1024){ //第一个文件小于10M

                            transferFileToGZip(zipOs, appFolderName + File.separator + "android.txt.01", entryParentName);
                        }
//                        transferFileToGZip(zipOs, appFolderName + File.separator + "android.txt.02", entryParentName);

                        String kernelFolderName = mAbsFolderName + File.separator + folderByDate0 + File.separator + "kernel";
                        File kernelDate0File = new File(kernelFolderName);
                        if (kernelDate0File.exists() && kernelDate0File.isDirectory()) {
                            ensureAllReadWrite(kernelDate0File);
                            boolean result = ensureTransferValidFileToGZip(zipOs, kernelDate0File, null, null);
                            isZipEmpty = result ? false : isZipEmpty;
                        }

                        /*String netlogFolderName = mAbsFolderName + File.separator + folderByDate0 + File.separator + "netlog";
                        File netlogDate0File = new File(netlogFolderName);// TODO 文件过大,暂时不用
                        if (netlogDate0File.exists() && netlogDate0File.isDirectory()) {
                            ensureAllReadWrite(netlogDate0File);
                            boolean result = ensureTransferValidFileToGZip(zipOs, netlogDate0File, null, null);
                            isZipEmpty = result ? false : isZipEmpty;
                        }*/

                        mDeleteFileOrFolder.put("yota_log", new File(mAbsFolderName + File.separator + folderByDate0));

                    }

//                    cleanAllLogFiles();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (null != zipOs && !isZipEmpty) {
                        try {
                            zipOs.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else if (null != outputStream) {
                        try {
                            outputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mDeleteFileOrFolder.put("zipAllFile", zipAllFile);
                return zipAllFile;
            }

            private void ensureAllReadWrite(File logDir) {
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

            private void deleteLogFileDir(File dir, boolean deleteDir) {
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
            private void deleteUploadLogs() {
                mCYLog.debug("删除已上传的文件");
                Set<String> keys = mDeleteFileOrFolder.keySet();
                for (String key: keys){

                    File file = mDeleteFileOrFolder.get(key);
                    if (file.exists()){
                        if (file.isFile()){
                            file.delete();
                        }else if(file.isDirectory()) {
                            deleteLogFileDir(file, true);
                            /*if ("history".equals(key)) {
                                deleteLogFileDir(file, true);
                            }else {
                                deleteLogFileDir(file, false);
                            }*/
                        }
                    }

                }
            }


            private void cleanAllLogFiles() {
                deleteLogFileDir(new File("/data/system/dropbox"), false);
                deleteLogFileDir(new File("/data/anr"), false);
                deleteLogFileDir(new File("/data/tombstones"), false);
                deleteLogFileDir(new File("/data/zslogs/logcat"), false);
                deleteLogFileDir(new File("/data/zslogs/battery"), false);
                deleteLogFileDir(new File("/data/zslogs/modem"), false);

                deleteLogFileDir(new File("/data/zslogs/wlan"), false);
                deleteLogFileDir(new File("/data/zslogs/gps"), false);
                deleteLogFileDir(new File("/data/zslogs/tcpdump"), false);
            }

            private void keepLastFiles(File zslogDir, int keepNum) {
                if (null != zslogDir && zslogDir.exists() && zslogDir.isDirectory()) {
                    final File[] subDir = zslogDir.listFiles();
                    if (null != subDir && subDir.length == 1 && subDir[0].isDirectory() && subDir[0].getName().equals(zslogDir.getName())) {
                        keepLastFiles(subDir[0], keepNum);
                    } else if (null != subDir && subDir.length > 0) {
                        List<File> subLogFiles = Arrays.asList(subDir);
                        Collections.sort(subLogFiles, new Comparator<File>() {
                            @Override
                            public int compare(File o1, File o2) {
                                return (int) (o2.lastModified() - o1.lastModified());
                            }
                        });
                        int index = 0;
                        for (; index < subLogFiles.size(); index++) {
                            if (index >= keepNum) break;
                        }
                        for (; index < subLogFiles.size(); index++) {
                            String fileName = subLogFiles.get(index).getName();
                            if (fileName.endsWith(".qdb") || fileName.endsWith(".xml"))
                                continue;
                            subLogFiles.get(index).delete();
                        }
                    }
                }
            }

            private boolean ensureTransferValidFileToGZip(ZipOutputStream zipOs, File logFile, String entryParentName, String folderNameByDate) throws IOException {
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

            private void transferFileToGZip(ZipOutputStream zipOs, String reportFilePath, String entryParentName) throws IOException {
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

            @Override
            protected void onPostExecute(final File zipAllFile) {
                if (null != zipAllFile && zipAllFile.exists()) {
                    if (0 == zipAllFile.length()) {
                        zipAllFile.delete();
                    } else {

                        if (zipAllFile.length() >= mSize10M * 5) {
                            if (mUploadListener != null) {
                                mUploadListener.onFail();
                                mUploadListener.sendMsg(ApiConstants.FILE_TOO_MAX_CODE, "文件大于50M,无法上传服务器");
                                zipAllFile.delete();
                            }
                            return;
                        }
//                        final Uri zipFileUri = Uri.fromFile(zipAllFile);
//                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).setData(zipFileUri));

                        if (!NetUtil.isNetworkAvailable(getContext())){
                            if (mUploadListener != null) {
                                mUploadListener.onFail();
                                mUploadListener.sendMsg(ApiConstants.NET_UN_CODE, "压缩完成,当前网络不可用,请确保能打开网页再试");
                                zipAllFile.delete();
                            }
                            return;
                        } else {
                            if (mUploadListener != null) {
                                mUploadListener.sendMsg(ApiConstants.OTHER_CODE, "压缩完成,存放至sdcard/yota_log/UploadFile,即将上传到服务器");
                            }
                        }

                        final HashMap<String, Object> params = new HashMap<>();
                        params.put(ApiConstants.PARAM_LOG_TYPE, ApiConstants.LOG_TYPE_LOG);
                        params.put(ApiConstants.PARAM_PRO_TYPE, TelephonyTools.getProType());
                        params.put(ApiConstants.PARAM_SYS_VERSION, TelephonyTools.getSysVersion());
                        params.put(ApiConstants.PARAM_UP_TYPE, extras.getInt(UP_TYPE));
                        params.put(ApiConstants.PARAM_UP_DESC, extras.getString(BUG_DETAILS));
                        params.put(ApiConstants.PARAM_PHONE, extras.getString(USER_CONTACT));

                        params.put(ApiConstants.PARAM_FILE, zipAllFile);

//                        UploadFileUtil.uploadFile(ApiConstants.PATH_UPLOAD, params, zipAllFile);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                UploadFileUtil.upLoadFile(ApiConstants.PATH_UPLOAD, params, new UploadFileUtil.ReqProgressCallBack<String>(){

                                    @Override
                                    public void onSuccessInUiThread() {

                                        deleteUploadLogs();
                                    }

                                    @Override
                                    public void onFail() {
                                        zipAllFile.delete();
                                        if (mUploadListener != null) {
                                            mUploadListener.onFail();
                                        }
                                    }

                                    @Override
                                    public void sendMsg(int code, String msg) {
                                        if (mUploadListener != null) {
                                            mUploadListener.sendMsg(code, msg);
                                        }
                                    }


                                    @Override
                                    public void onProgressInUIThread(long total, long current, float progress, long networkSpeed) {
                                        if (mUploadListener != null) {
                                            mUploadListener.updateBar(total, current, progress, networkSpeed);
                                        }
                                    }
                                });
                            }
                        }
                        ).start();

                    }
                } else {
                    Toast.makeText(CompressAppendixService.this, getString(R.string.compress_file_failed), Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();

    }

    /**
     *
     * @author liuwenrong
     * 使用类部类，返回当前service的实例，用于activity，调用service的各种方法
     *
     */
    public class MyBinder extends Binder
    {
        public CompressAppendixService getMyService()
        {
            return CompressAppendixService.this;
        }
    }

    UploadListener mUploadListener;

    public void setUploadListener(UploadListener listener){
        mUploadListener = listener;
    }

    public interface UploadListener {
        void updateBar(long totalSize, long currentSize, float progress, long networkSpeed);
        void onFail();
        void sendMsg(int code, String msg);
    }

    public List<String> getYotaLogFoldersByTime(){

        ArrayList<String> yotaLogFolders = new ArrayList<String>();
        Map<Date, File> map = new HashMap<>();
        List<Date> dates = new ArrayList<>();
        File absFolderNameFile = new File(mAbsFolderName);
        if (absFolderNameFile.exists() && absFolderNameFile.isDirectory()) {
            if (null != absFolderNameFile && absFolderNameFile.exists()) {
                if (absFolderNameFile.isFile() && 0 < absFolderNameFile.length()) {
                    return yotaLogFolders;
                } else if (absFolderNameFile.isDirectory()) {
                    File[] logFiles = absFolderNameFile.listFiles();
                    if (null != logFiles && 0 < logFiles.length) {
                        for (File targetFile : logFiles) {
                            try {
                                Date date = LogUtil.myLogSdf.parse(targetFile.getName());
                                map.put(date, targetFile);
                                int index = -1;
                                for (int i = 0; i<dates.size(); i++) {

                                    if (date.after(dates.get(i))) {//如果map中新增的date时间大于之前的,记录坐标
                                        index = i;
                                        break;
                                    }
                                }
                                int size = dates.size();
                                if (index == -1) { //dates数据为0,或者新增的date应该是小的,这时候应该把date加的list的最后一个
                                    dates.add(size, date);
                                } else {
                                    dates.add(index, date);
                                }

                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }

        //从map中提取key按Date先后排序

        for (int j=0; j<dates.size(); j++) {
            File file = map.get(dates.get(j));
            yotaLogFolders.add(yotaLogFolders.size(), file.getName());
        }


        return yotaLogFolders;
    }

    private long mSize10M = 10 * 1024 * 1024; //100M 改成 10M,考虑到服务器的承载量有限
    /**
     * 获取文件夹大小
     * @param file File实例
     * @return long
     */
    public static long getFolderSize(File file){

        long size = 0;
        try {
            java.io.File[] fileList = file.listFiles();
            for (int i = 0; i < fileList.length; i++)
            {
                if (fileList[i].isDirectory())
                {
                    size = size + getFolderSize(fileList[i]);

                }else{
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

    private String getReportFileTimestamp() {
        return (String) DateFormat.format("yyyy_MM_dd_HH_mm_ss", System.currentTimeMillis());
    }

    /*private String getImeiForIndex(int index) {
        return TelephonyManager.getImei(getContext(), index);
    }*/

    public Context getContext(){
        return mContext;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }
}
