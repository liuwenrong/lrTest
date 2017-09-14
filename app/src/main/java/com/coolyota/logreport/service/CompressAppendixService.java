package com.coolyota.logreport.service;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.coolyota.logreport.R;
import com.coolyota.logreport.constants.ApiConstants;
import com.coolyota.logreport.fragment.ConfigFragment;
import com.coolyota.logreport.tools.FileUtil;
import com.coolyota.logreport.tools.LogUtil;
import com.coolyota.logreport.tools.NetUtil;
import com.coolyota.logreport.tools.SystemProperties;
import com.coolyota.logreport.tools.TelephonyTools;
import com.coolyota.logreport.tools.UploadFileUtil;
import com.coolyota.logreport.tools.log.CYLog;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public static final String KEY_MONITOR_TYPE = "key_monitorType";
    public static final String REBOOT_CHECKED_KEY = "reboot_checked";
    public static final String BUG_DETAILS = "bug_details";
    public static final String USER_CONTACT = "user_contact";
    public static final String PIC_IMAGE_LIST = "pic_image_list";
    private static final byte[] zipDataBuffer = new byte[1024 * 1024];
    private static final String START_ID = "start_id";
    private int mMonitorType = -1;

    HashMap<String, File> mDeleteFileOrFolder = new HashMap<>();

    private Context mContext;
    private Activity mActivity;
    /**
     * log文件夹 sd卡/yota_log
     */
    public String mAbsFolderName;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {//170906以后不使用startService启动服务
        final Bundle extras = new Bundle(intent.getExtras());
        extras.putInt(START_ID, startId);
        mContext = this;

        genZip(extras);

        return Service.START_NOT_STICKY;
//        return Service.START_REDELIVER_INTENT; //重启服务导致多次上传
    }

    /**
     * 采用bindService方式,不用StartService
     * @param intent
     */
    public void startCompressAndUpload(Intent intent, Activity activity) {

        final Bundle extras = new Bundle(intent.getExtras());
        mContext = this;
        mActivity = activity;
        genZip(extras);

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

                mMonitorType = extras.getInt(KEY_MONITOR_TYPE);

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
                                FileUtil.ensureTransferValidFileToGZip(zipOs, new File(picImagePath), null, null);
                                isZipEmpty = false;
                            }
                        }
                    }

                    LogUtil.getInstance().collectorStatusInfo();

                    //滚动日志与离线日志 系统的无法上传后删除, TODO 暂时注释
                   /* File dropbox = new File("/data/system/dropbox");
                    if (dropbox.exists() && dropbox.isDirectory()) {
                        ensureAllReadWrite(dropbox);
                        boolean result = ensureTransferValidFileToGZip(zipOs, dropbox, "statusinfo", null);
                        isZipEmpty = result ? false : isZipEmpty;
                    }
                    mDeleteFileOrFolder.put("dropbox", dropbox);*/

                    // anr日志
                    File anr = new File("/data/anr"); //traces.txt 文件
                    if (anr.exists() && anr.isDirectory()) {
                        FileUtil.ensureAllReadWrite(anr);
                        boolean result = FileUtil.ensureTransferValidFileToGZip(zipOs, anr, "statusinfo", null);
                        isZipEmpty = result ? false : isZipEmpty;
                    }
                    mDeleteFileOrFolder.put("anr", anr);

                    final File tombstones = new File("/data/tombstones");
                    if (tombstones.exists() && tombstones.isDirectory()) {
                        FileUtil.ensureAllReadWrite(tombstones);
                        boolean result = FileUtil.ensureTransferValidFileToGZip(zipOs, tombstones, "statusinfo", null);
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
//                        ensureAllReadWrite(statusinfo);
                        boolean result = FileUtil.ensureTransferValidFileToGZip(zipOs, statusinfo, null, null);
                        isZipEmpty = result ? false : isZipEmpty;
                    }
                    mDeleteFileOrFolder.put("statusinfo", statusinfo);

                    if ("false".equals(SystemProperties.get(ConfigFragment.PERSIST_SYS_YOTA_LOG, "false"))) {
                        // 没打开自己收集log
                        LogUtil.getInstance().collectLogcat();

                        try {
                            // 当前在子线程,等待3s收集logcat日志,然后在压缩
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        final File logFile = new File(mAbsFolderName + File.separator + "apps");
                        if (logFile.exists() && logFile.isDirectory()) {
//                        ensureAllReadWrite(logFile);
                            boolean result = FileUtil.ensureTransferValidFileToGZip(zipOs, logFile, null, null);
                            isZipEmpty = result ? false : isZipEmpty;
                        }
                        mDeleteFileOrFolder.put("apps", logFile);

                    } else {
                        // 如果打开了,则去拿底层收集的Log
                        isZipEmpty = genYotaLog(zipOs, isZipEmpty, extras);
                    }

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

            @Override
            protected void onPostExecute(final File zipAllFile) { //主要执行上传到服务器,当前处于UI线程
                if (null != zipAllFile && zipAllFile.exists()) {
                    if (0 == zipAllFile.length()) {
                        zipAllFile.delete();
                    } else {

                        if (zipAllFile.length() >= mSize10M * 10) {
                            if (mUploadListener != null) {
                                mUploadListener.onFail();
                                mUploadListener.sendMsg(ApiConstants.FILE_TOO_MAX_CODE, "文件大于100M,无法上传服务器");
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

                            //判断移动网,并弹框提示用户
//                            if (NetUtil.isNetworkTypeWifi(getContext())) {
                            if (NetUtil.isMobile(getContext())) {
                                showMobileNetUploadDialog(zipAllFile, extras);
                                return;

                            }

                            if (mUploadListener != null) {
                                mUploadListener.sendMsg(ApiConstants.OTHER_CODE, "压缩完成,存放至sdcard/yota_log/UploadFile,即将上传到服务器");
                            }
                        }

                        startUploadFile(zipAllFile, extras);

                    }
                } else {
                    Toast.makeText(CompressAppendixService.this, getString(R.string.compress_file_failed), Toast.LENGTH_SHORT).show();
                }
            }

        }.execute();

    }

    /**
     * 当处于数据网络时,弹框提示用户
     * @param zipAllFile
     * @param extras
     */
    private void showMobileNetUploadDialog(final File zipAllFile, final Bundle extras) {
        new MaterialDialog.Builder(getActivity())
                .title("上传Log日志")
                .content("当前使用的是数据网络,将消耗"+ FileUtil.getDataSize(zipAllFile.length()) + "流量,是否继续?")
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.material_red_400)
                .negativeColorRes(R.color.material_red_400)
                .titleGravity(GravityEnum.CENTER)
                .titleColorRes(R.color.material_red_400)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .dividerColorRes(R.color.colorAccent)
                .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.POSITIVE)
                .positiveColor(Color.WHITE)
                .negativeColorAttr(android.R.attr.textColorSecondaryInverse)
                .onNegative(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (mUploadListener != null) {
                            mUploadListener.sendMsg(ApiConstants.User_Cancel_Upload, "当前处于数据网络,用户取消上传");
                            zipAllFile.delete();
                        }

                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        if (mUploadListener != null) {
                            mUploadListener.sendMsg(ApiConstants.OTHER_CODE, "压缩完成,存放至sdcard/yota_log/UploadFile,即将上传到服务器");
                            startUploadFile(zipAllFile, extras);
                        }
                    }
                })
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .theme(Theme.DARK)
                .show();
    }

    private void startUploadFile(final File zipAllFile, Bundle extras) {
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

                        FileUtil.deleteUploadLogs(mDeleteFileOrFolder);
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
        if (mUploadListener != null) {
            //
            mUploadListener.sendMsg(ApiConstants.START_Upload, "开始上传");
        }
    }

    /**
     * 压缩yota_log中的log
     * @param zipOs
     * @param isZipEmpty
     * @param extras
     * @return
     * @throws IOException
     */
    private boolean genYotaLog(ZipOutputStream zipOs, boolean isZipEmpty, Bundle extras) throws IOException {
        // 获取底层最新的yot_log目录,如果第一个文件夹小于10M,压缩返回最新的和第二新的文件夹
        boolean isRebootChecked = extras.getBoolean(REBOOT_CHECKED_KEY);
        List<String> folderByDates = getYotaLogFoldersByTime();
        // 日期的文件夹大于两个, 且第一个文件夹小于10M 或者 或者勾选了 异常关机重启
        if(folderByDates.size() >= 2 && ( (getFolderSize(new File(mAbsFolderName + File.separator + folderByDates.get(0))) < mSize10M) || isRebootChecked) ){

            String folderByDate0 = folderByDates.get(0);
            final File yotalogDate0 = new File(mAbsFolderName + File.separator + folderByDate0);
            if (yotalogDate0.exists() && yotalogDate0.isDirectory()) {
//                            ensureAllReadWrite(yotalogDate0);
                boolean result = FileUtil.ensureTransferValidFileToGZip(zipOs, yotalogDate0, null, folderByDate0);
                isZipEmpty = result ? false : isZipEmpty;
            }
            mDeleteFileOrFolder.put("yota_log", yotalogDate0);

            // history 上一个记录的文件夹存放在history目录下
            String folderByDate1 = folderByDates.get(1);
            String appEntryParentName = "history" + File.separator + "apps"; //部分apps的log 取最新的几个文件
            String appFolderName = mAbsFolderName + File.separator + folderByDate1 + File.separator + "apps";
            FileUtil.transferFileToGZip(zipOs, appFolderName + File.separator + "android.txt", appEntryParentName);
            FileUtil.transferFileToGZip(zipOs, appFolderName + File.separator + "android_boot.txt", appEntryParentName);
            FileUtil.transferFileToGZip(zipOs, appFolderName + File.separator + "events.txt", appEntryParentName);
            FileUtil.transferFileToGZip(zipOs, appFolderName + File.separator + "events_boot.txt", appEntryParentName);
            FileUtil.transferFileToGZip(zipOs, appFolderName + File.separator + "radio.txt", appEntryParentName);
            FileUtil.transferFileToGZip(zipOs, appFolderName + File.separator + "radio_boot.txt", appEntryParentName);
            if (new File(appFolderName + File.separator + "android.txt").length() < 5 * 1024 * 1024){ //第一个文件小于10M

                FileUtil.transferFileToGZip(zipOs, appFolderName + File.separator + "android.txt.01", appEntryParentName);
            }

            String kernelFolderName = mAbsFolderName + File.separator + folderByDate1 + File.separator + "kernel"; //全部kernel
            File kernelDate0File = new File(kernelFolderName);
            if (kernelDate0File.exists() && kernelDate0File.isDirectory()) {
//                            ensureAllReadWrite(kernelDate0File);
                boolean result = FileUtil.ensureTransferValidFileToGZip(zipOs, kernelDate0File, "history", null);
                isZipEmpty = result ? false : isZipEmpty;
            }

            // TODO 文件过大,暂时不用
            /*String netlogFolderName = mAbsFolderName + File.separator + folderByDate1 + File.separator + "netlog";
            File netlogDate0File = new File(netlogFolderName);
            if (netlogDate0File.exists() && netlogDate0File.isDirectory()) {
                ensureAllReadWrite(netlogDate0File);
                boolean result = ensureTransferValidFileToGZip(zipOs, netlogDate0File, "history", null);
                isZipEmpty = result ? false : isZipEmpty;
            }*/

            mDeleteFileOrFolder.put("history", new File( mAbsFolderName + File.separator + folderByDate1));


        } else if (folderByDates.size() > 0){
            //去各目录取文件,有些文件取3个
            String folderByDate0 = folderByDates.get(0);
            String entryParentName = "apps";

            String appFolderName = mAbsFolderName + File.separator + folderByDate0 + File.separator + "apps";
            FileUtil.transferFileToGZip(zipOs, appFolderName + File.separator + "android.txt", entryParentName);
            FileUtil.transferFileToGZip(zipOs, appFolderName + File.separator + "android_boot.txt", entryParentName);
            FileUtil.transferFileToGZip(zipOs, appFolderName + File.separator + "events.txt", entryParentName);
            FileUtil.transferFileToGZip(zipOs, appFolderName + File.separator + "events_boot.txt", entryParentName);
            FileUtil.transferFileToGZip(zipOs, appFolderName + File.separator + "radio.txt", entryParentName);
            FileUtil.transferFileToGZip(zipOs, appFolderName + File.separator + "radio_boot.txt", entryParentName);
            if (new File(appFolderName + File.separator + "android.txt").length() < 10 * 1024 * 1024){ //第一个文件小于10M

                FileUtil.transferFileToGZip(zipOs, appFolderName + File.separator + "android.txt.01", entryParentName);
            }

            String kernelFolderName = mAbsFolderName + File.separator + folderByDate0 + File.separator + "kernel";
            File kernelDate0File = new File(kernelFolderName);
            if (kernelDate0File.exists() && kernelDate0File.isDirectory()) {
//                            ensureAllReadWrite(kernelDate0File);
                boolean result = FileUtil.ensureTransferValidFileToGZip(zipOs, kernelDate0File, null, null);
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
        return isZipEmpty;
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
    public Activity getActivity() {
        return mActivity;
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }
}
