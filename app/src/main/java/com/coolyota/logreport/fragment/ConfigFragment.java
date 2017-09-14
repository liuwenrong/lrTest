package com.coolyota.logreport.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.coolyota.logreport.R;
import com.coolyota.logreport.base.BaseFragment;
import com.coolyota.logreport.constants.CYConstants;
import com.coolyota.logreport.tools.SystemProperties;
import com.kyleduo.switchbutton.SwitchButton;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/8/25
 */
public class ConfigFragment extends BaseFragment {

    public static final String PERSIST_SYS_YOTA_LOG = "persist.sys.yotalog.record"; // "true" "false"
    public static final String PERSIST_SYS_YOTALOG_MDTYPE = "persist.sys.yotalog.mdtype";
    public static final String PERSIST_SYS_YOTALOG_MDLOG = "persist.sys.yotalog.mdlog"; // "true" "false"
    public static final String Type_Modem = "Modem";
    public static final String Type_GPS = "gps";
    public static final String Type_WLAN = "wlan";
    public static final String Type_AUDIO = "Audio";
    public static final String Type_SENSOR = "sensor";
    private static int mTabNameResId = R.string.config_tab_name;
    public SwitchButton mSbModem;
    public SwitchButton mSbGps;
    public SwitchButton mSbWlan;
    public SwitchButton mSbAudio;
    public SwitchButton mSbSensor;
    public SwitchButton mSbAndroid;
    public SwitchButton mSbRadio;
    public SwitchButton mSbEvents;
    public SwitchButton mSbKernel;
    public CompoundButton.OnCheckedChangeListener mAndroidOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            SystemProperties.set(PERSIST_SYS_YOTA_LOG, isChecked ? "true" : "false");

            if (isChecked) { //联动打开子开关
                mSbEvents.setCheckedNoEvent(true);
                mSbKernel.setCheckedNoEvent(true);
                mSbRadio.setCheckedNoEvent(true);
            }

        }
    };
    public View mConfigScrollViewContainer;
    public View mConfigGridContainer;
    public View mAndroidContainer;
    public View mQxdmContainer;
    public View mConfigModem;
    public View mConfigDefault;
    public SwitchButton mSbRamDumps;
    public SwitchButton mSbRestartLevel;
    public SwitchButton mSbDownloadMode;
    public CompoundButton.OnCheckedChangeListener mDumpOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {

            if (buttonView == mSbRamDumps) {
                SystemProperties.set(CYConstants.PERSIST_SYS_SSR_ENABLE_RAMDUMPS, isChecked ? "1" : "0");
            } else if (buttonView == mSbRestartLevel) {
                SystemProperties.set(CYConstants.PERSIST_SYS_SSR_RESTART_LEVEL, isChecked ? "ALL_DISABLE" : "ALL_ENABLE");
            } else if (buttonView == mSbDownloadMode) {
                SystemProperties.set(CYConstants.PERSIST_SYS_DOWNLOAD_MODE, isChecked ? "1" : "0");
            }

            buttonView.setEnabled(false);
            buttonView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    buttonView.setEnabled(true);
                }
            }, 1000);


        }
    };
    String[] mTypes = {Type_Modem, Type_GPS, Type_WLAN, Type_AUDIO, Type_SENSOR};
    SwitchButton[] mQxdmBtns = new SwitchButton[5];
    public CompoundButton.OnCheckedChangeListener mQxdmOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {

            //设置 当选中时
            if (isChecked) {
                String curType = ""; //当前选中的类型
                for (int i = 0; i < mTypes.length; i++) {
                    SwitchButton btn = mQxdmBtns[i];
                    if (btn == buttonView) {
                        curType = mTypes[i];
                    } else {
                        //其他按钮全部设为false
                        btn.setCheckedNoEvent(false);

                    }

                }

                Log.i("x74---------------ax", "onCheckedChanged: curType = " + curType);


                SystemProperties.set(PERSIST_SYS_YOTALOG_MDTYPE, curType);

                if (SystemProperties.get(PERSIST_SYS_YOTALOG_MDLOG, "false").equals("true")) { //如果有之前 选中了, 先关闭 隔3s再打开,防止底层服务频繁打开关闭 导致卡死

                    SystemProperties.set(PERSIST_SYS_YOTALOG_MDLOG, "false");
                    //等3s 打开
                    buttonView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setAllQxdmBtnNoEnableDelay();
                            SystemProperties.set(PERSIST_SYS_YOTALOG_MDLOG, "true");
                        }
                    }, 3000);

                } else {
                    SystemProperties.set(PERSIST_SYS_YOTALOG_MDLOG, "true");
                }

            } else {

                SystemProperties.set(PERSIST_SYS_YOTALOG_MDLOG, "false");

            }

            setAllQxdmBtnNoEnableDelay();

        }
    };
    long mBtnLastClick = 0; // 上一次提交按钮点击的时间
    Menu mMenu;
    public View.OnClickListener onConfigGridClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            mConfigScrollViewContainer.setVisibility(View.VISIBLE);
//            mQxdmContainer.setVisibility(View.VISIBLE);
//            mAndroidContainer.setVisibility(View.VISIBLE);
            mConfigGridContainer.setVisibility(View.GONE);
            mMenu.getItem(0).setVisible(false);
            mMenu.getItem(1).setVisible(true);
            switch (v.getId()) {
                case R.id.config_default:

                    // 如果 mdlog=true 设置  mdlog = false
//                    if ("true".equals(SystemProperties.get(PERSIST_SYS_YOTALOG_MDLOG, "false"))) {
//                        SystemProperties.set(PERSIST_SYS_YOTALOG_MDLOG, "false");
//                    }
                    //把所有md按钮设为false
                    for (int i = 0; i < mQxdmBtns.length; i++) {
                        if (mQxdmBtns[i].isChecked()) {
                            mQxdmBtns[i].setChecked(false);
                        }
                    }

                    //打开Android开关
                    if ("false".equals(SystemProperties.get(PERSIST_SYS_YOTA_LOG, "false"))) {
                        SystemProperties.set(PERSIST_SYS_YOTA_LOG, "true");
                    }
                    mSbAndroid.setCheckedNoEvent(true);

                    break;

                case R.id.config_Modem:

                    mSbModem.setChecked(true);

                    mSbAndroid.setChecked(false);

                    break;
            }


        }
    };

    /**
     * 使所有按钮延时3s不可点击
     */
    private void setAllQxdmBtnNoEnableDelay() {
        for (int i = 0; i < mQxdmBtns.length; i++) {
            final SwitchButton buttonView = mQxdmBtns[i];
            buttonView.setEnabled(false);
            buttonView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    buttonView.setEnabled(true);
                }
            }, 3000);

        }
    }

    @Override
    public int getTabNameResId() {
        return mTabNameResId;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_config, container, false);
        setHasOptionsMenu(true);    //保证能在Fragment里调用onCreateOptionsMenu()方法

        initView(view);
        initData();

        return view;
    }

    private void initView(View view) {

        mQxdmContainer = (View) view.findViewById(R.id.qxdm_container);
        mAndroidContainer = (View) view.findViewById(R.id.android_container);
        mConfigScrollViewContainer = view.findViewById(R.id.tab_config_scrollview_container);
        mConfigGridContainer = (View) view.findViewById(R.id.config_grid_container);

        mConfigDefault = (View) view.findViewById(R.id.config_default);
        mConfigModem = (View) view.findViewById(R.id.config_Modem);

        mSbModem = (SwitchButton) view.findViewById(R.id.sb_md_modem);
        mSbGps = (SwitchButton) view.findViewById(R.id.sb_md_gps);
        mSbWlan = (SwitchButton) view.findViewById(R.id.sb_md_wlan);
        mSbAudio = (SwitchButton) view.findViewById(R.id.sb_md_audio);
        mSbSensor = (SwitchButton) view.findViewById(R.id.sb_md_sensor);

        mQxdmBtns[0] = mSbModem;
        mQxdmBtns[1] = mSbGps;
        mQxdmBtns[2] = mSbWlan;
        mQxdmBtns[3] = mSbAudio;
        mQxdmBtns[4] = mSbSensor;

        mSbRamDumps = (SwitchButton) view.findViewById(R.id.sb_ramdumps);
        mSbRestartLevel = (SwitchButton) view.findViewById(R.id.sb_restart_level);
        mSbDownloadMode = (SwitchButton) view.findViewById(R.id.sb_download_mode);

        mSbAndroid = (SwitchButton) view.findViewById(R.id.sb_md_android);
        mSbRadio = (SwitchButton) view.findViewById(R.id.sb_md_radio);
        mSbEvents = (SwitchButton) view.findViewById(R.id.sb_md_events);
        mSbKernel = (SwitchButton) view.findViewById(R.id.sb_md_kernel);
    }

    private void initData() {

        mConfigDefault.setOnClickListener(onConfigGridClickListener);
        mConfigModem.setOnClickListener(onConfigGridClickListener);
//        mConfigDefault.setOnC
        boolean yotaLogOn = "true".equals(SystemProperties.get(PERSIST_SYS_YOTA_LOG, "false"));
        mSbAndroid.setCheckedNoEvent(yotaLogOn);
        if (yotaLogOn) {
            mSbRadio.setCheckedNoEvent(true);
            mSbEvents.setCheckedNoEvent(true);
            mSbKernel.setCheckedNoEvent(true);
        }

        mSbRamDumps.setCheckedNoEvent(1 == SystemProperties.getInt(CYConstants.PERSIST_SYS_SSR_ENABLE_RAMDUMPS, 0));
        mSbRestartLevel.setCheckedNoEvent("ALL_DISABLE".equals(SystemProperties.get(CYConstants.PERSIST_SYS_SSR_RESTART_LEVEL, "")));
        mSbDownloadMode.setCheckedNoEvent(1 == SystemProperties.getInt(CYConstants.PERSIST_SYS_DOWNLOAD_MODE, 0));

        mSbRamDumps.setOnCheckedChangeListener(mDumpOnCheckedChangeListener);
        mSbRestartLevel.setOnCheckedChangeListener(mDumpOnCheckedChangeListener);
        mSbDownloadMode.setOnCheckedChangeListener(mDumpOnCheckedChangeListener);

        String type = SystemProperties.get(PERSIST_SYS_YOTALOG_MDTYPE, "0");
        for (int i = 0; i < mTypes.length; i++) {
            if ("true".equals(SystemProperties.get(PERSIST_SYS_YOTALOG_MDLOG, "false"))) {
                if (type.equals(mTypes[i])) {
                    mQxdmBtns[i].setCheckedNoEvent(true);
                } else {
                    mQxdmBtns[i].setCheckedNoEvent(false);
                }
            }

            mQxdmBtns[i].setOnCheckedChangeListener(mQxdmOnCheckedChangeListener);
            mQxdmBtns[i].setTag(mQxdmOnCheckedChangeListener);
        }

        mSbAndroid.setOnCheckedChangeListener(mAndroidOnCheckedChangeListener);
        mSbAndroid.setTag(mAndroidOnCheckedChangeListener);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.config_menu_items, menu);

        mMenu = menu;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_config_list: //goto 列表

//                mQxdmContainer.setVisibility(View.VISIBLE);
//                mAndroidContainer.setVisibility(View.VISIBLE);
                mConfigScrollViewContainer.setVisibility(View.VISIBLE);
                mConfigGridContainer.setVisibility(View.GONE);
                item.setVisible(false);
                mMenu.getItem(1).setVisible(true);

                break;
            case R.id.action_config_grid:

//                mQxdmContainer.setVisibility(View.GONE);
//                mAndroidContainer.setVisibility(View.GONE);
                mConfigScrollViewContainer.setVisibility(View.GONE);
                mConfigGridContainer.setVisibility(View.VISIBLE);
                item.setVisible(false);
                mMenu.getItem(0).setVisible(true);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
