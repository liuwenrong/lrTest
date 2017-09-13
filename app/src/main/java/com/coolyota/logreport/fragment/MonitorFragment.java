package com.coolyota.logreport.fragment;

import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.coolyota.logreport.R;
import com.coolyota.logreport.base.BaseFragment;
import com.coolyota.logreport.constants.CYConstants;
import com.coolyota.logreport.tools.SystemProperties;
import com.dd.CircularProgressButton;
import com.kyleduo.switchbutton.SwitchButton;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.coolyota.logreport.tools.LogUtil.writeToFile;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/8/25
 */
public class MonitorFragment extends BaseFragment {

    public static final String FOLDER_NAME = "yota_log"; //日志存放SD卡的文件夹名称
    private static int mTabNameResId = R.string.monitor_tab_name;
    private static String FILE_FORMAT = ".txt"; //文件格式,后缀名
    public CircularProgressButton mCpBtnDumpsys;
    public CircularProgressButton mCpBtnProp;
    public CircularProgressButton mCpBtnMeminfo;
    public SwitchButton[] mSwitchBtns = new SwitchButton[CYConstants.Monitor_Toggles.length];
    /**
     * log文件夹 sd卡/yota_log
     */
    public String mAbsFolderName;
    SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");
    long mBtnLastClick = 0; // 上一次提交按钮点击的时间
    public View.OnClickListener onMeminfoClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (avoidClickRepeatedly()) {
                return;
            }

            CircularProgressButton cpBtn = (CircularProgressButton) v;

            if (cpBtn.getProgress() == 0) {

//                showOrHideProgressAndCover(false, 0);
                cpBtn.setProgress(1);
//                collectMeminfo();
                collectInfo("meminfo_", "dumpsys meminfo -a", cpBtn);
            } else {
                cpBtn.setProgress(0);
            }
        }
    };

        CompoundButton.OnCheckedChangeListener mOnMonitorCheckedChangerListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            for (int i=0; i<mSwitchBtns.length; i++) {

                SwitchButton switchBtn = mSwitchBtns[i];

                if (buttonView == switchBtn) {
                    SystemProperties.set(CYConstants.PERSIST_SYS_MONITOR_TOGGLE, isChecked ?
                            "" + (mMonitorToggleValues |= CYConstants.Monitor_Toggles[i]) :   //加上
                            "" + (mMonitorToggleValues &= ~CYConstants.Monitor_Toggles[i])); //取消
//                    sendToggleBoardCast(mMonitorToggleValues, i, isChecked);
                    break; //只要找到一个btn即可退出循环
                }

            }
        }
    };


    public View.OnClickListener onPropClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (avoidClickRepeatedly()) {
                return;
            }


            CircularProgressButton cpBtn = (CircularProgressButton) v;

            if (cpBtn.getProgress() == 0) {

//                showOrHideProgressAndCover(false, 0);
                cpBtn.setProgress(1);
//                collectProp();
                collectInfo("property_", "getprop", cpBtn);

            } else {
                cpBtn.setProgress(0);
            }
        }
    };
    public View.OnClickListener onDumpsysClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (avoidClickRepeatedly()) {
                return;
            }
            CircularProgressButton cpBtn = (CircularProgressButton) v;

            if (cpBtn.getProgress() == 0) {

//                showOrHideProgressAndCover(false, 0);
                cpBtn.setProgress(1);

                switch (v.getId()) {
                    case R.id.cp_btn_dumpsys:

                        collectInfo("dumpsys_", "dumpsys", cpBtn);
                        break;
                    case R.id.cp_btn_cpu:
                        collectInfo("cpu_info_", "dumpsys cpuinfo", cpBtn);
                        break;
                    case R.id.cp_btn_sf:

                        collectInfo("SurfaceFlinger_", "dumpsys SurfaceFlinger", cpBtn);
                        break;
                }


            } else {
                cpBtn.setProgress(0);
            }

        }
    };
    public CircularProgressButton mSFBtn;
    public CircularProgressButton mCpuInfoBtn;
    public int mMonitorToggleValues;

    private boolean avoidClickRepeatedly() {
        if (System.currentTimeMillis() - mBtnLastClick < 1000) { // 1 s内不可重复点击
            return true;
        }
        mBtnLastClick = System.currentTimeMillis();
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_monitor, container, false);

        initView(view);
        initData();

        return view;
    }

    private void initView(View view) {
        mCpBtnMeminfo = (CircularProgressButton) view.findViewById(R.id.cp_btn_meminfo);
        mCpBtnProp = (CircularProgressButton) view.findViewById(R.id.cp_btn_prop);
        mCpBtnDumpsys = (CircularProgressButton) view.findViewById(R.id.cp_btn_dumpsys);
        mCpuInfoBtn = (CircularProgressButton) view.findViewById(R.id.cp_btn_cpu);
        mSFBtn = (CircularProgressButton) view.findViewById(R.id.cp_btn_sf);

        mCpBtnDumpsys.setIndeterminateProgressMode(true);
        mCpBtnMeminfo.setIndeterminateProgressMode(true);
        mCpBtnProp.setIndeterminateProgressMode(true);
        mCpuInfoBtn.setIndeterminateProgressMode(true);
        mSFBtn.setIndeterminateProgressMode(true);

        mCpBtnMeminfo.setOnClickListener(onMeminfoClickListener);
        mCpBtnProp.setOnClickListener(onPropClickListener);
        mCpBtnDumpsys.setOnClickListener(onDumpsysClickListener);
        mCpuInfoBtn.setOnClickListener(onDumpsysClickListener);
        mSFBtn.setOnClickListener(onDumpsysClickListener);

        SwitchButton mTombStoneSb = (SwitchButton) view.findViewById(R.id.sb_md_tomb_stone);
        mSwitchBtns[0] = mTombStoneSb;
        SwitchButton mAnrSystemSb = (SwitchButton) view.findViewById(R.id.sb_md_anr_system);
        mSwitchBtns[1] = mAnrSystemSb;
        SwitchButton mCrashApp = (SwitchButton) view.findViewById(R.id.sb_crash_app);
        mSwitchBtns[2] = mCrashApp;
        SwitchButton mFrameworkReboot = (SwitchButton) view.findViewById(R.id.sb_md_reboot);
        mSwitchBtns[3]  = mFrameworkReboot;
        SwitchButton mSubSystemReset = (SwitchButton) view.findViewById(R.id.sb_md_reset);
        mSwitchBtns[4] = mSubSystemReset;
        SwitchButton mCrashSystem = (SwitchButton) view.findViewById(R.id.sb_crash_system);
        mSwitchBtns[5] = mCrashSystem;
        SwitchButton mAnrApp = (SwitchButton) view.findViewById(R.id.sb_anr_app);
        mSwitchBtns[6] = mAnrApp;

    }

    private void initData() {
        mAbsFolderName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FOLDER_NAME;

        //设置Monitor属性值
        for (int i=0; i<mSwitchBtns.length; i++) {
            mMonitorToggleValues = SystemProperties.getInt(CYConstants.PERSIST_SYS_MONITOR_TOGGLE, 0);
            if ((mMonitorToggleValues & CYConstants.Monitor_Toggles[i]) != 0) { //=0不包含当前的值,!=0表示打开了当前开关
                mSwitchBtns[i].setCheckedNoEvent(true);
            }
            mSwitchBtns[i].setOnCheckedChangeListener(mOnMonitorCheckedChangerListener);
        }

    }


    /**
     * @param type 类型,如meminfo_, property_, dumpsys_,
     * @param command 命令,如dumpsys meminfo -a, getprop, dumpsys,
     * @param cpBtn
     */
    public void collectInfo(final String type, final String command, final CircularProgressButton cpBtn) {
        getBaseActivity().showOrHideCover(true);

        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    String fileName = mAbsFolderName + File.separator + type + mSdf.format(new Date())  + FILE_FORMAT;


                    Process meminfoDumpPro = Runtime.getRuntime().exec(command);
                    writeToFile(new File(fileName), meminfoDumpPro.getInputStream());

                    setCpBtnProgress(fileName, cpBtn, 100);

                } catch (IOException e) {
                    e.printStackTrace();
                    setCpBtnProgress(null, cpBtn, -1);
                } finally {
                }

            }
        }.start();


    }

    public void collectMeminfo() {

        //耗时操作 可以加弹框
        getBaseActivity().showOrHideCover(true);

        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    String meminfoFileName = mAbsFolderName + File.separator + "meminfo_" + mSdf.format(new Date())  + FILE_FORMAT;
//                    String meminfoCommand = "cat proc/meminfo";
                    String meminfoCommandDump = "dumpsys meminfo -a";

//                    Process meminfoPro = Runtime.getRuntime().exec(meminfoCommand);  //会卡住进程,最好放最后或者子线程
                    Process meminfoDumpPro = Runtime.getRuntime().exec(meminfoCommandDump);
//                    writeToFile(new File(meminfoFileName), meminfoPro.getInputStream());
                    writeToFile(new File(meminfoFileName), meminfoDumpPro.getInputStream());

                    setCpBtnProgress(meminfoFileName, mCpBtnMeminfo, 100);

                } catch (IOException e) {
                    e.printStackTrace();
                    setCpBtnProgress(null, mCpBtnMeminfo, -1);
                } finally {
                }

            }
        }.start();
    }

    public void collectProp() {
        getBaseActivity().showOrHideCover(true);
        new Thread() {
            @Override
            public void run() {
                super.run();

                try {
                    String propertyFileName = mAbsFolderName + File.separator + "property_" + mSdf.format(new Date()) + FILE_FORMAT;
                    String propertyCommand = "getprop";

                    Process propertyPro = Runtime.getRuntime().exec(propertyCommand);
                    writeToFile(new File(propertyFileName), propertyPro.getInputStream());

                    setCpBtnProgress(propertyFileName, mCpBtnProp, 100);

                } catch (IOException e) {
                    e.printStackTrace();
                    setCpBtnProgress(null, mCpBtnProp, -1);
                } finally {
                }

            }
        }.start();
    }

    private void setCpBtnProgress(final String fileName, final CircularProgressButton cpBtn, final int progress) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                getBaseActivity().showOrHideCover(false);

                if (fileName == null) {
                    Toast.makeText(getContext(), "运行命令失败", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(getContext(), "文件存放至"+fileName, Toast.LENGTH_LONG).show();
                cpBtn.setProgress(progress);
            }
        });
    }

    @Override
    public int getTabNameResId() {
        return mTabNameResId;
    }

}
