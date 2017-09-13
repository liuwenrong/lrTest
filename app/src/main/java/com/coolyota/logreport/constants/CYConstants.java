package com.coolyota.logreport.constants;

import static com.coolyota.logreport.constants.CYConstants.MonitorToggle.ANR_APP;
import static com.coolyota.logreport.constants.CYConstants.MonitorToggle.ANR_SYSTEM;
import static com.coolyota.logreport.constants.CYConstants.MonitorToggle.CRASH;
import static com.coolyota.logreport.constants.CYConstants.MonitorToggle.CRASH_APP;
import static com.coolyota.logreport.constants.CYConstants.MonitorToggle.FRAMEWORK_REBOOT;
import static com.coolyota.logreport.constants.CYConstants.MonitorToggle.SUBSYSTEM_RESET;
import static com.coolyota.logreport.constants.CYConstants.MonitorToggle.TOMBSTONE;
import static com.coolyota.logreport.constants.CYConstants.dynamicToggle.INPUT;
import static com.coolyota.logreport.constants.CYConstants.dynamicToggle.POWER;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/7/14
 */
public class CYConstants {
    public static final String TYPE_LOG = "/log";
    public static final String PERSIST_SYS_MONITOR_TOGGLE = "persist.sys.yota.crash";   //每一位代表一个开关
    public static final String PERSIST_SYS_DYNAMIC_TOGGLE = "persist.sys.dynamic.toggle";
    public static final String PERSIST_SYS_SSR_ENABLE_RAMDUMPS = "persist.sys.ssr.enable_ramdumps"; //1表示打开,0
    public static final String PERSIST_SYS_SSR_RESTART_LEVEL = "persist.sys.ssr.restart_leve"; //ALL_DISABLE 表示打开 ,ALL_ENABLE 表示关闭
    public static final String PERSIST_SYS_DOWNLOAD_MODE = "persist.sys.download_mode"; //1表示打开,0
    public static final String PERSIST_SYS_LOG_LEVEL = "persist.sys.log.level";//2表示Verbose,3表示Debug,4表示Info,5表示Warn,6表示Error
    public static final int[] Dynamic_Toggles = {INPUT, POWER};
    public static String newLine = "\r\n";

    public class dynamicToggle {
        public static final int INPUT = 1 << 0;
        public static final int POWER = 1 << 1;
    }

    public static final int[] Monitor_Toggles = {TOMBSTONE, ANR_SYSTEM, CRASH_APP, FRAMEWORK_REBOOT, SUBSYSTEM_RESET, CRASH, ANR_APP};
    public class MonitorToggle {
        public static final int TOMBSTONE = 1;
        public static final int ANR_SYSTEM = 1 << 1;
        public static final int CRASH_APP = 1 << 2;
        public static final int FRAMEWORK_REBOOT = 1 << 3;
        public static final int SUBSYSTEM_RESET = 1 << 4;
        public static final int CRASH = 1 << 5;
        public static final int ANR_APP = 1 << 6;
    }

    /**
     * 异常类型,同时也是索引
     */
    public class MonitorType {
        public static final int TOMBSTONE = 0x0;
        public static final int ANR_SYSTEM = 0x1;
        public static final int CRASH_APP = 0x2;
        public static final int FRAMEWORK_REBOOT = 0x3;
        public static final int SUBSYSTEM_RESET = 0x4;
        public static final int CRASH = 0x5;
        public static final int ANR_APP = 0x6;
    }

}
