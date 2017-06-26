package com.coolyota.logreport.tools;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static android.content.Context.ALARM_SERVICE;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/6/2
 */
public class LogUtil {

    public static final String FOLDER_NAME = "yota_log"; //日志存放SD卡的文件夹名称
    private static final String TAG = "LogUtil";
    private static final int SDCARD_LOG_FILE_SAVE_DAYS = 7;                         //sd卡中日志文件的最多保存天数
    private static final int MEMORY_LOG_FILE_MAX_SIZE = 10 * 1024 * 1024;           //内存中日志文件最大值，10M
    private static final int SDCARD_TYPE = 0;          //当前的日志记录类型为存储在SD卡下面
    private static final int MEMORY_TYPE = 1;          //当前的日志记录类型为存储在内存中
    /**
     * 文件目录名称 /apps
     */
    public static String mAppsFolder = File.separator + "apps";
    /**
     * 文件夹名称 /kernel
     */
    public static String mKernelFolder = File.separator + "kernel";
    /**
     * 文件夹名称 /statusinfo
     */
    public static String mStatusInfoFolder = File.separator + "statusinfo";
    /**
     * 文件夹名称 /netlog
     */
    public static String mNetLogFolder = File.separator + "statusinfo" + File.separator + "netlog";
    /**
     * 文件夹名称 /dropbox
     */
    public static String mDropboxFolder = File.separator + "statusinfo" + File.separator + "dropbox";
    /**
     * 文件夹名称 /tombstones
     */
    public static String mTombstonesFolder = File.separator + "statusinfo" +  File.separator + "tombstones";
    /**
     * 文件夹名称 /anr
     */
    public static String mAnrFolder = File.separator + "statusinfo" +  File.separator + "anr";
    /**
     * 文件夹名称 /pstore
     */
    public static String mPstoreFolder = File.separator + "pstore";
    public static List<Process> mProcesses;
    static Context context;
    private static String LOG_PATH_MEMORY_DIR;     //日志文件在内存中的路径(日志文件在安装目录中的路径) 内部存储
    /**
     * 日志文件在sdcard中的路径 sdcard/yota_log
     */
    private static String LOG_PATH_SDCARD_DIR;
    @SuppressWarnings("unused")
    private static String LOG_SERVICE_LOG_PATH;    //本服务产生的日志，记录日志服务开启失败信息
    private static String logServiceLogName = "Log.txt";//本服务输出的日志文件名称
    private static OutputStreamWriter writer;
    public static SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
    private static int CURR_LOG_TYPE = SDCARD_TYPE;    //当前的日志记录类型
    private static String CURR_INSTALL_LOG_NAME;   //如果当前的日志写在内存中，记录当前的日志文件名称
    private static String FILE_FORMAT = ".txt"; //文件格式,后缀名
    /* 是否正在监测日志文件大小；
     * 如果当前日志记录在SDcard中则为false
     * 如果当前日志记录在内存中则为true*/
    private static boolean logSizeMoniting = false;
    private static String MONITOR_LOG_SIZE_ACTION = "MONITOR_LOG_SIZE";     //日志文件监测action
    private static String SWITCH_LOG_FILE_ACTION = "SWITCH_LOG_FILE_ACTION";    //切换日志文件action
    private static LogTaskReceiver logTaskReceiver;
    /**
     * 文件目录名称 /log_时间 -- > "" 空字符
     */
    private static String mLogPathByDate;
    /**
     * 绝对路径 mnt/../ @link(FOLDER_NAME)
     */
    public static String mLogAbsPathByDate;
    private static String[] mFolders = new String[]{/*mAppsFolder, mKernelFolder,*/ mStatusInfoFolder, /*mNetLogFolder,*/ mAnrFolder, mDropboxFolder, mTombstonesFolder, /*mPstoreFolder*/};

    public static void init(Context context) {
        LOG_PATH_MEMORY_DIR = context.getFilesDir().getAbsolutePath() + File.separator + "log";
        LOG_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FOLDER_NAME;

        getLogAbsPathByDate();

        Log.i(TAG, "LogService onCreate");
        setContext(context);
        register();
        deploySwitchLogFileTask();
    }

    public static void startLog() {
        new LogCollectorThread().start();
    }

    /**
     * 开始收集日志信息
     */
    public static void createLogCollector() {

        mProcesses = new ArrayList<>();

        FileOutputStream out = null;
        try {

            copyTombstonesToSdcard();
            copyPstoreToSdcard();
            copyDropboxToSdcard();

            collectorStatusInfo();

        } catch (Exception e) {
            Log.e(TAG, "CollectorThread == >" + e.getMessage(), e);
            recordLogServiceLog("CollectorThread == >" + e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void collectorStatusInfo() {

        new Thread() {

            @Override
            public void run() {
                super.run();

                try {
                    String pkgListFileName = mLogAbsPathByDate + mStatusInfoFolder + File.separator + "packageslist" + FILE_FORMAT;
                    String pkgListCommand = "pm list packages";
                    String meminfoFileName = mLogAbsPathByDate + mStatusInfoFolder + File.separator + "meminfo1" + FILE_FORMAT;
                    String meminfoCommand = "dumpsys meminfo -a";
                    String propertyFileName = mLogAbsPathByDate + mStatusInfoFolder + File.separator + "property" + FILE_FORMAT;
                    String propertyCommand = "getprop";
                    Process pkgListPro = Runtime.getRuntime().exec(pkgListCommand);
                    writeToFile(new File(pkgListFileName), pkgListPro.getInputStream());

                    Process propertyPro = Runtime.getRuntime().exec(propertyCommand);
                    writeToFile(new File(propertyFileName), propertyPro.getInputStream());

                    Process meminfoPro = Runtime.getRuntime().exec(meminfoCommand);  //会卡住进程,最好放最后或者子线程
                    writeToFile(new File(meminfoFileName), meminfoPro.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                }

            }
        }.start();
    }

    /**
     * 创建日志目录
     */
    private static void createLogDir() {
        mLogPathByDate = "" /*+ File.separator + "log_" + myLogSdf.format(new Date())*/;
        boolean mkOk;

        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {

            for (int i = 0; i < mFolders.length; i++) {
                String absPath = LOG_PATH_SDCARD_DIR + mLogPathByDate + mFolders[i];
                File file = new File(absPath);
                if (!file.isDirectory()) {
                    mkOk = file.mkdirs();
                    if (!mkOk) {
                        recordLogServiceLog(absPath + ", dir is not created succ");
                    }
                }
            }
        }
    }

    public static void stopLog() {

        Log.d(TAG, "stopLog: ");
        recordLogServiceLog("LogService onStop");
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        new Thread() {
            @Override
            public void run() {
                super.run();

                clearLogCache(); //耗时操作,子线程
            }
        }.start();
        Toast.makeText(getContext(), "日志存放至sdcard/" + FOLDER_NAME + "/log/" + mLogPathByDate, Toast.LENGTH_LONG).show();

        for (Process process : mProcesses) {

            if (process != null) {
                process.destroy();
            }

        }

    }

    /**
     * 创建目录并根据当前的存储位置得到日志的绝对存储路径
     *
     * @return sdcard/0CYLogReport/log_时间
     */
    public static String getLogAbsPathByDate() {
        createLogDir();
        CURR_INSTALL_LOG_NAME = null;
        Log.d(TAG, "Log stored in SDcard, the path is:" + LOG_PATH_SDCARD_DIR + mLogPathByDate);
        mLogAbsPathByDate = LOG_PATH_SDCARD_DIR + mLogPathByDate;
        return mLogAbsPathByDate;
    }

    private static void register() {

        IntentFilter logTaskFilter = new IntentFilter();
        logTaskFilter.addAction(MONITOR_LOG_SIZE_ACTION);
        logTaskFilter.addAction(SWITCH_LOG_FILE_ACTION);
    }

    /**
     * 检查日志文件大小是否超过了规定大小
     * 如果超过了重新开启一个日志收集进程
     */
    private static void checkLogSize() {
        if (CURR_INSTALL_LOG_NAME != null && !"".equals(CURR_INSTALL_LOG_NAME)) {
            String path = LOG_PATH_MEMORY_DIR + File.separator + CURR_INSTALL_LOG_NAME;
            File file = new File(path);
            if (!file.exists()) {
                return;
            }
            Log.d(TAG, "checkLog() ==> The size of the log is too big?");
            if (file.length() >= MEMORY_LOG_FILE_MAX_SIZE) {
                Log.d(TAG, "The log's size is too big!");
                new LogCollectorThread().start();
            }
        }
    }

    /**
     * 关闭由本程序开启的logcat进程：
     * 根据用户名称杀死进程(如果是本程序进程开启的Logcat收集进程那么两者的USER一致)
     * 如果不关闭会有多个进程读取logcat日志缓存信息写入日志文件
     *
     * @param allProcList
     * @return
     */
    private static void killLogcatProc(List<ProcessInfo> allProcList) {
        if (mProcesses != null) {
            for (Process process : mProcesses) {
                if (process != null) {
                    process.destroy();
                }
            }
        }
        String packName = getContext().getPackageName();
        String myUser = getAppUser(packName, allProcList);
        for (ProcessInfo processInfo : allProcList) {
            if (processInfo.name.toLowerCase().equals("logcat")
                    && processInfo.user.equals(myUser)) {
                android.os.Process.killProcess(Integer
                        .parseInt(processInfo.pid));
            }
        }
    }

    /**
     * 处理日志文件
     * 1.如果日志文件存储位置切换到内存中，删除除了正在写的日志文件
     * 并且部署日志大小监控任务，控制日志大小不超过规定值
     * 2.如果日志文件存储位置切换到SDCard中，删除7天之前的日志，移
     * 动所有存储在内存中的日志到SDCard中，并将之前部署的日志大小
     * 监控取消
     */
    public static void handleLog() {
        if (CURR_LOG_TYPE == MEMORY_TYPE) {
            deployLogSizeMonitorTask();
//            deleteMemoryExpiredLog();
        } else {
            deleteSDcardExpiredLog();
        }
    }

    /**
     * 删除内存下过期的日志
     */
    private static void deleteSDcardExpiredLog() {
    }

    /**
     * 判断sdcard上的日志文件是否可以删除
     *
     * @param createDateStr
     * @return
     */
    public static boolean canDeleteSDLog(String createDateStr) {
        boolean canDel = false;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -1 * SDCARD_LOG_FILE_SAVE_DAYS);//删除7天之前日志
        Date expiredDate = calendar.getTime();
        try {
            Date createDate = myLogSdf.parse(createDateStr);
            canDel = createDate.before(expiredDate);
        } catch (ParseException e) {
            Log.e(TAG, e.getMessage(), e);
            canDel = false;
        }
        return canDel;
    }

    /**
     * 去除文件的扩展类型（.log or .txt）
     *
     * @param fileName
     * @return
     */
    private static String getFileNameWithoutExtension(String fileName) {
        return fileName.substring(0, fileName.indexOf("."));
    }

    /**
     * 部署日志大小监控任务
     */
    private static void deployLogSizeMonitorTask() {
        if (logSizeMoniting) {    //如果当前正在监控着，则不需要继续部署
            return;
        }
        logSizeMoniting = true;
        Log.d(TAG, "deployLogSizeMonitorTask() succ !");
    }

    /**
     * 获取本程序的用户名称
     *
     * @param packName
     * @param allProcList
     * @return
     */
    private static String getAppUser(String packName, List<ProcessInfo> allProcList) {
        for (ProcessInfo processInfo : allProcList) {
            if (processInfo.name.equals(packName)) {
                return processInfo.user;
            }
        }
        return null;
    }

    /**
     * 运行PS命令得到进程信息
     *
     * @return USER PID PPID VSIZE RSS WCHAN PC NAME
     * root 1 0 416 300 c00d4b28 0000cd5c S /init
     */
    private static List<String> getAllProcess() {
        List<String> orgProcList = new ArrayList<String>();
        Process proc = null;
        try {
            proc = Runtime.getRuntime().exec("ps");
            StreamConsumer errorConsumer = new StreamConsumer(proc
                    .getErrorStream());

            StreamConsumer outputConsumer = new StreamConsumer(proc
                    .getInputStream(), orgProcList);

            errorConsumer.start();
            outputConsumer.start();
            if (proc.waitFor() != 0) {
                Log.e(TAG, "getAllProcess proc.waitFor() != 0");
                recordLogServiceLog("getAllProcess proc.waitFor() != 0");
            }
        } catch (Exception e) {
            Log.e(TAG, "getAllProcess failed", e);
            recordLogServiceLog("getAllProcess failed");
        } finally {
            try {
                proc.destroy();
            } catch (Exception e) {
                Log.e(TAG, "getAllProcess failed", e);
                recordLogServiceLog("getAllProcess failed");
            }
        }
        return orgProcList;
    }

    /**
     * 根据ps命令得到的内容获取PID，User，name等信息
     *
     * @param orgProcessList
     * @return
     */
    private static List<ProcessInfo> getProcessInfoList(List<String> orgProcessList) {
        List<ProcessInfo> procInfoList = new ArrayList<ProcessInfo>();
        for (int i = 1; i < orgProcessList.size(); i++) {
            String processInfo = orgProcessList.get(i);
            String[] proStr = processInfo.split(" ");
            // USER PID PPID VSIZE RSS WCHAN PC NAME
            // root 1 0 416 300 c00d4b28 0000cd5c S /init
            List<String> orgInfo = new ArrayList<String>();
            for (String str : proStr) {
                if (!"".equals(str)) {
                    orgInfo.add(str);
                }
            }
            if (orgInfo.size() == 9) {
                ProcessInfo pInfo = new ProcessInfo();
                pInfo.user = orgInfo.get(0);
                pInfo.pid = orgInfo.get(1);
                pInfo.ppid = orgInfo.get(2);
                pInfo.name = orgInfo.get(8);
                procInfoList.add(pInfo);
            }
        }
        return procInfoList;
    }

    /**
     * 清除sdcard的yota_log
     */
    public static void cleanSdcardLog() {
        LOG_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FOLDER_NAME;
        File dir = new File(LOG_PATH_SDCARD_DIR);
        deleteDirWithFile(dir);
    }
    public static void deleteDirWithFile(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return;
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirWithFile(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }
    /**
     * 每次记录日志之前先清除日志的缓存, 不然会在两个日志文件中记录重复的日志
     * 可以在关闭 获取日志时清除,这样下次就不会重复了
     */
    private static void clearLogCache() {
        Process proc = null;
        List<String> commandList = new ArrayList<String>();
        commandList.add("logcat");
        commandList.add("-c");
        try {
            proc = Runtime.getRuntime().exec(
                    commandList.toArray(new String[commandList.size()]));
            StreamConsumer errorGobbler = new StreamConsumer(proc
                    .getErrorStream());

            StreamConsumer outputGobbler = new StreamConsumer(proc
                    .getInputStream());

            errorGobbler.start();
            outputGobbler.start();
            if (proc.waitFor() != 0) {
                Log.e(TAG, " clearLogCache proc.waitFor() != 0");
                recordLogServiceLog("clearLogCache clearLogCache proc.waitFor() != 0");
            }
        } catch (Exception e) {
            Log.e(TAG, "clearLogCache failed", e);
            recordLogServiceLog("clearLogCache failed");
        } finally {
            try {
                proc.destroy();
            } catch (Exception e) {
                Log.e(TAG, "clearLogCache failed", e);
                recordLogServiceLog("clearLogCache failed");
            }
        }
    }

    static Context getContext() {
        return context;
    }

    static void setContext(Context ctx) {

        context = ctx;

    }

    /**
     * 将日志文件转移到SD卡下面
     */
    public static void moveLogfile() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            return;
        }
        File file = new File(LOG_PATH_SDCARD_DIR);
        if (!file.isDirectory()) {
            boolean mkOk = file.mkdirs();
            if (!mkOk) {
                return;
            }
        }

        file = new File(LOG_PATH_MEMORY_DIR);
        if (file.isDirectory()) {
            File[] allFiles = file.listFiles();
            for (File logFile : allFiles) {
                String fileName = logFile.getName();
                if (logServiceLogName.equals(fileName)) {
                    continue;
                }
                boolean isSucc = copy(logFile, new File(LOG_PATH_SDCARD_DIR
                        + File.separator + fileName));
                if (isSucc) {
                    logFile.delete();
                }
            }
        }
    }

    /**
     * 拷贝文件
     *
     * @param source 源文件
     * @param target 目标文件
     * @return
     */
    private static boolean copy(File source, File target) {
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            if (!target.exists()) {
                boolean createSucc = target.createNewFile();
                if (!createSucc) {
                    return false;
                }
            }
            in = new FileInputStream(source);
            out = writeToFile(target, in);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage(), e);
            recordLogServiceLog("copy file fail");
            return false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage(), e);
                recordLogServiceLog("copy file fail");
                return false;
            }
        }

    }

    @NonNull
    private static FileOutputStream writeToFile(File target, InputStream in) throws IOException {
        FileOutputStream out = new FileOutputStream(target);
        byte[] buffer = new byte[8 * 1024];
        int count;
        String inStr = in.toString();
        Log.e(TAG, "writeToFile: inStr = " + new String(inStr).toString());
        while ((count = in.read(buffer)) != -1) {
            out.write(buffer, 0, count);
        }
        out.flush();
        return out;
    }

    /**
     * 记录日志服务的基本信息 防止日志服务有错，在LogCat日志中无法查找
     * 此日志名称为Log.log 点开关,立刻关闭会crash
     *
     * @param msg
     */
    private static void recordLogServiceLog(String msg) {
    }

    /**
     * 部署日志切换任务，每天凌晨切换日志文件
     */
    private static void deploySwitchLogFileTask() {
        Intent intent = new Intent(SWITCH_LOG_FILE_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(getContext(), 0, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // 部署任务
        AlarmManager am = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, sender);
        recordLogServiceLog("deployNextTask succ,next task time is:" + myLogSdf.format(calendar.getTime()));
    }

    /**
     * 确保所有文件可读写
     *
     * @param logDir
     */
    private static void ensureAllReadWrite(File logDir) {
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

    private static void copyDropboxToSdcard() throws IOException {

        File dropbox = new File("/data/system/dropbox");
        if (dropbox.exists() && dropbox.isDirectory()) {
            ensureAllReadWrite(dropbox);
            copyFolderOrFile(dropbox, mLogAbsPathByDate + mDropboxFolder, false);
        }

    }

    private static void copyAnrToSdcard() throws IOException {

        File tombstones = new File("/data/anr");
        if (tombstones.exists() && tombstones.isDirectory()) {
            ensureAllReadWrite(tombstones);
            copyFolderOrFile(tombstones, mLogAbsPathByDate + mAnrFolder, false);
        }

    }

    private static void copyTombstonesToSdcard() throws IOException {

        File tombstones = new File("/data/tombstones");
        if (tombstones.exists() && tombstones.isDirectory()) {
            ensureAllReadWrite(tombstones);
            copyFolderOrFile(tombstones, mLogAbsPathByDate + mTombstonesFolder, true);//没有文件扩展名,所以文件加上.txt
        }

    }

    private static void copyNetLogToSdcard() throws IOException {

        File netlog = new File("/data/zslogs/tcpdump");
        if (netlog.exists() && netlog.isDirectory()) {
            ensureAllReadWrite(netlog);
            copyFolderOrFile(netlog, mLogAbsPathByDate + mNetLogFolder, true); //格式是.cap0,还有乱码,改成txt在手机上也查看不了
        }

    }

    private static void copyPstoreToSdcard() throws IOException {

        File pstore = new File("sys/fs/pstore");
        if (pstore.exists() && pstore.isDirectory()) {
            ensureAllReadWrite(pstore);
            copyFolderOrFile(pstore, mLogAbsPathByDate + mPstoreFolder, true); //一个文件没有格式,一个dmesg的,改成txt在手机上也查看不了
        }

    }
    /**
     * 将文件夹中文件全部拷贝到另一个文件夹中
     *
     * @param logFile
     * @param targetFolderName
     * @param hasFileFormat    是否需要加文件扩展名 .txt
     * @return
     * @throws IOException
     */
    private static boolean copyFolderOrFile(File logFile, String targetFolderName, boolean hasFileFormat) throws IOException {
        boolean hasLogFile = false;
        if (null != logFile && logFile.exists()) {
            if (logFile.isFile() && 0 < logFile.length()) {
                copy(new File(logFile.getAbsolutePath()), new File(targetFolderName + File.separator + logFile.getName() + (hasFileFormat ? FILE_FORMAT : "")));
                hasLogFile = true;
            } else if (logFile.isDirectory()) {
                File[] logFiles = logFile.listFiles();
                if (null != logFiles && 0 < logFiles.length) {
                    for (File sourceFile : logFiles) {
                        boolean result = copyFolderOrFile(sourceFile, targetFolderName, hasFileFormat);
                        hasLogFile = result ? result : hasLogFile;
                    }
                }
            }
        }
        return hasLogFile;
    }

    /**
     * 日志任务接收
     * 切换日志，监控日志大小
     *
     * @author Administrator
     */
    static class LogTaskReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (SWITCH_LOG_FILE_ACTION.equals(action)) {
                new LogCollectorThread().start();
            } else if (MONITOR_LOG_SIZE_ACTION.equals(action)) {
                checkLogSize();
            }
        }
    }

    /**
     * 日志收集
     * 1.清除日志缓存
     * 2.杀死应用程序已开启的Logcat进程防止多个进程写入一个日志文件
     * 3.开启日志收集进程
     * 4.处理日志文件
     * 移动 OR 删除
     */
    static class LogCollectorThread extends Thread {

        public LogCollectorThread() {
            super("LogCollectorThread");
            Log.d(TAG, "LogCollectorThread is create");
        }

        @Override
        public void run() {
            try {

                List<String> orgProcessList = getAllProcess();
                List<ProcessInfo> processInfoList = getProcessInfoList(orgProcessList);
                killLogcatProc(processInfoList);

                createLogCollector();

                Thread.sleep(1000);//休眠，创建文件，然后处理文件，不然该文件还没创建，会影响文件删除

                handleLog();

            } catch (Exception e) {
                e.printStackTrace();
                recordLogServiceLog(Log.getStackTraceString(e));
            }
        }
    }

    static class ProcessInfo {
        public String user;
        public String pid;
        public String ppid;
        public String name;

        @Override
        public String toString() {
            String str = "user=" + user + " pid=" + pid + " ppid=" + ppid
                    + " name=" + name;
            return str;
        }
    }

    static class StreamConsumer extends Thread {
        InputStream is;
        List<String> list;

        StreamConsumer(InputStream is) {
            this.is = is;
        }

        StreamConsumer(InputStream is, List<String> list) {
            this.is = is;
            this.list = list;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);

                String line = null;
                while ((line = br.readLine()) != null  /*&& line.contains(getContext().getPackageName())*/) {
                    if (list != null) {
                        list.add(line);
                    }
                }
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
