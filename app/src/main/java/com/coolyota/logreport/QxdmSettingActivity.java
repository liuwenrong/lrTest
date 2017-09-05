package com.coolyota.logreport;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.os.AsyncTaskCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.coolyota.logreport.base.CloudBaseActivity;
import com.coolyota.logreport.tools.NotificationShow;
import com.coolyota.logreport.tools.SystemProperties;
import com.coolyota.logreport.tools.log.CYLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.support.v4.os.AsyncTaskCompat.executeParallel;

public class QxdmSettingActivity extends CloudBaseActivity {
    public static final String Type_Modem = "Modem";
    public static final String Type_WLAN = "wlan";
    public static final String Type_GPS = "gps";
    public static final String Type_AUDIO = "Audio";
    public static final String Type_SENSOR = "sensor";
    private static final String Type_DataCall = "DataCall";
    public static final String Type_IMSTEST = "IMStest";
    public static final String Type_IRAT = "IRAT";
    public static final String Type_PowerOn = "PowerOn";
    public static final String Type_RF = "RF";
    private static final String Type_UIM = "UIM";
    private static final String Type_VoiceCall = "VoiceCall";



    public static final String Type_Mdlog = "mdlog";

    public static final String PERSIST_SYS_YOTALOG_MDTYPE = "persist.sys.yotalog.mdtype";
    public static final String PERSIST_SYS_YOTALOG_MDLOG = "persist.sys.yotalog.mdlog";
    public static final Map<String, String> sMapPropOn = new HashMap<>();
    private static final Map<String, String> sMapTypesProps = new HashMap<>();
    private static final Map<String, String> sMapPropOff = new HashMap<>();

    static {
        sMapPropOn.put(Type_Modem, "Modem");
//        sMapPropOn.put(Type_DataCall, "DataCall");
//        sMapPropOn.put(Type_IMSTEST, "IMStest");
//        sMapPropOn.put(Type_IRAT, "IRAT");
//        sMapPropOn.put(Type_PowerOn, "PowerOn");
//        sMapPropOn.put(Type_RF, "RF");
//        sMapPropOn.put(Type_UIM, "UIM");
//        sMapPropOn.put(Type_VoiceCall, "VoiceCall");
        sMapPropOn.put(Type_WLAN, "wlan");
        sMapPropOn.put(Type_GPS, "gps");
        sMapPropOn.put(Type_AUDIO, Type_AUDIO);
        sMapPropOn.put(Type_SENSOR, Type_SENSOR);

        sMapPropOn.put(Type_Mdlog, "true");
    }

    static {
        sMapTypesProps.put(Type_Modem, PERSIST_SYS_YOTALOG_MDTYPE);
//        sMapTypesProps.put(Type_DataCall, PERSIST_SYS_YOTALOG_MDTYPE);
//        sMapTypesProps.put(Type_IMSTEST, PERSIST_SYS_YOTALOG_MDTYPE);
//        sMapTypesProps.put(Type_IRAT, PERSIST_SYS_YOTALOG_MDTYPE);
//        sMapTypesProps.put(Type_PowerOn, PERSIST_SYS_YOTALOG_MDTYPE);
//        sMapTypesProps.put(Type_RF, PERSIST_SYS_YOTALOG_MDTYPE);
//        sMapTypesProps.put(Type_UIM, PERSIST_SYS_YOTALOG_MDTYPE);
//        sMapTypesProps.put(Type_VoiceCall, PERSIST_SYS_YOTALOG_MDTYPE);
        sMapTypesProps.put(Type_WLAN, PERSIST_SYS_YOTALOG_MDTYPE);
        sMapTypesProps.put(Type_GPS, PERSIST_SYS_YOTALOG_MDTYPE);
        sMapTypesProps.put(Type_AUDIO, PERSIST_SYS_YOTALOG_MDTYPE);
        sMapTypesProps.put(Type_SENSOR, PERSIST_SYS_YOTALOG_MDTYPE);

        sMapTypesProps.put(Type_Mdlog, PERSIST_SYS_YOTALOG_MDLOG);
    }

    static {
        sMapPropOff.put(Type_Mdlog, "false");
    }

    private final Map<String, Switch> mMutualProp = new HashMap<>();
    private final List<AsyncTask<?, ?, ?>> mTasks = new ArrayList<>();
    private Switch mRFSwitch;
    private AsyncTask<?, ?, ?> mShowNotifyTask;
    private Switch mVoiceCallSwitch;
    private Switch mMdLogSwitch;
    private Switch mDataCallSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qxdm_setting);
        getSupportActionBar().hide();

        Switch mGenericSwitch = (Switch) findViewById(R.id.switch_modem);
        mMutualProp.put(Type_Modem, mGenericSwitch); //put完之后,只能开一个,类似单选
        mTasks.add(executeParallel(new InitSwitchTask(mGenericSwitch, Type_Modem, PERSIST_SYS_YOTALOG_MDTYPE)));

        /*mDataCallSwitch = (Switch) findViewById(R.id.switch_data_call);
        mMutualProp.put(Type_DataCall, mDataCallSwitch);
        mTasks.add(executeParallel(new InitSwitchTask(mDataCallSwitch, Type_DataCall, PERSIST_SYS_YOTALOG_MDTYPE)));

        Switch mIMStestSwitch = (Switch) findViewById(R.id.switch_imstest);
        mMutualProp.put(Type_IMSTEST, mIMStestSwitch);
        mTasks.add(executeParallel(new InitSwitchTask(mIMStestSwitch, Type_IMSTEST, PERSIST_SYS_YOTALOG_MDTYPE)));

        Switch mIRATSwitch = (Switch) findViewById(R.id.switch_irat);
        mMutualProp.put(Type_IRAT, mIRATSwitch);
        mTasks.add(executeParallel(new InitSwitchTask(mIRATSwitch, Type_IRAT, PERSIST_SYS_YOTALOG_MDTYPE)));

        Switch mPowerOnSwitch = (Switch) findViewById(R.id.switch_poweron);
        mMutualProp.put(Type_PowerOn, mPowerOnSwitch);
        mTasks.add(executeParallel(new InitSwitchTask(mPowerOnSwitch, Type_PowerOn, PERSIST_SYS_YOTALOG_MDTYPE)));

        mRFSwitch = (Switch) findViewById(R.id.switch_rf);
        mMutualProp.put(Type_RF, mRFSwitch);
        mTasks.add(executeParallel(new InitSwitchTask(mRFSwitch, Type_RF, PERSIST_SYS_YOTALOG_MDTYPE)));

        Switch mUIMSwitch = (Switch) findViewById(R.id.switch_uim);
        mMutualProp.put(Type_UIM, mUIMSwitch);
        mTasks.add(executeParallel(new InitSwitchTask(mUIMSwitch, Type_UIM, PERSIST_SYS_YOTALOG_MDTYPE)));

        mVoiceCallSwitch = (Switch)findViewById(R.id.switch_voice_call);
        mMutualProp.put(Type_VoiceCall, mVoiceCallSwitch);
        mTasks.add(executeParallel(new InitSwitchTask(mVoiceCallSwitch, Type_VoiceCall, PERSIST_SYS_YOTALOG_MDTYPE)));*/

        Switch mWlanLogSwitch = (Switch) findViewById(R.id.switch_log_wlan);
        mMutualProp.put(Type_WLAN, mWlanLogSwitch);
        mTasks.add(executeParallel(new InitSwitchTask(mWlanLogSwitch, Type_WLAN, PERSIST_SYS_YOTALOG_MDTYPE)));

        Switch mGpsLogSwitch = (Switch) findViewById(R.id.switch_log_gps);
        mMutualProp.put(Type_GPS, mGpsLogSwitch);
        mTasks.add(executeParallel(new InitSwitchTask(mGpsLogSwitch, Type_GPS, PERSIST_SYS_YOTALOG_MDTYPE)));

        Switch mAudioSwitch = (Switch) findViewById(R.id.switch_audio);
        mMutualProp.put(Type_AUDIO, mAudioSwitch);
        mTasks.add(executeParallel(new InitSwitchTask(mAudioSwitch, Type_AUDIO, PERSIST_SYS_YOTALOG_MDTYPE)));

        Switch mSensorSwitch = (Switch) findViewById(R.id.switch_sensor);
        mMutualProp.put(Type_SENSOR, mSensorSwitch);
        mTasks.add(executeParallel(new InitSwitchTask(mSensorSwitch, Type_SENSOR, PERSIST_SYS_YOTALOG_MDTYPE)));


        mMdLogSwitch = (Switch)findViewById(R.id.switch_mdlog);
        mTasks.add(executeParallel(new InitSwitchTask(mMdLogSwitch, Type_Mdlog, PERSIST_SYS_YOTALOG_MDLOG)));

    }

    @Override
    protected void onStart() {
        super.onStart();
        hideNotifyIfNeed();
    }

    @Override
    protected void onStop() {
        showNotifyIfNeed();
        super.onStop();
    }

    private void showNotifyIfNeed() {
        mShowNotifyTask = AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return sMapPropOn.get(Type_Mdlog).equals(SystemProperties.get(PERSIST_SYS_YOTALOG_MDLOG, "true"));
            }

            @Override
            protected void onPostExecute(Boolean result) {
                logger.debug("Show notify need: " + result);
                if (null != result && result) {
                    NotificationShow.startLogRecording(QxdmSettingActivity.this);
                }
                mShowNotifyTask = null;
            }
        });
    }

    private void hideNotifyIfNeed() {
        if (null != mShowNotifyTask) mShowNotifyTask.cancel(false);
        NotificationShow.cancelLogRecording(QxdmSettingActivity.this);
    }

    String mSelectedType = "";

    @Override
    protected void onDestroy() {
        cancelAllTasks();
        super.onDestroy();
    }

    private void cancelAllTasks() {
        for (AsyncTask<?, ?, ?> task : mTasks) {
            task.cancel(false);
        }
        mTasks.clear();
    }

    private class InitSwitchTask extends PropGetTask {
        public InitSwitchTask(Switch switchButton, String type, String prop) {
            super(switchButton, type, prop);
        }

        @Override
        protected void onPostExecute(Boolean checked) {
            super.onPostExecute(checked);
            CYLog.d("211----", QxdmSettingActivity.class, "type = " + mType);

            final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {

                    CYLog.d("225----", QxdmSettingActivity.class, "type = " + mType);
                    if (mMutualProp.containsKey(mType)) { //10种类型

                        if (isChecked) { //点击 选中
                            mSelectedType = mType;
                            final Iterator<String> iterator = mMutualProp.keySet().iterator();
                            while (iterator.hasNext()) { //遍历,
                                final String type = iterator.next();
                                final Switch aSwitch = mMutualProp.get(type);


                                if (!type.equals(mType) && isChecked == aSwitch.isChecked()) { //关闭其他的已打开的开关
                                    aSwitch.setOnCheckedChangeListener(null);
                                    aSwitch.setChecked(!isChecked);
                                    aSwitch.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) aSwitch.getTag());
                                    isCloseOther = true;

                                }
                            }
                            if ( mMdLogSwitch.isChecked() ){

                                mMdLogSwitch.setChecked(false); //只要切换就把总开关关掉
                                if (!isCloseOther) {
                                    sleepNoClick();
                                }

                            }
                            if (isCloseOther) {
                                sleep6NoClick(isChecked);
                            } else {

                                mTasks.add(new PropSetTask(mSwitchButton, mType, mProp, false).execute(isChecked));
                                mMdLogSwitch.setChecked(true);
                                if (!isCloseOther) {
                                    sleepNoClick();
                                }
                            }

                        } else { //点击关闭

                            if ( mMdLogSwitch.isChecked() ){

                                mMdLogSwitch.setChecked(false); //只要切换就把总开关关掉
                                if (!isCloseOther) {
                                    sleepNoClick();
                                }

                            }

                            mTasks.add(new PropSetTask(mSwitchButton, mType, mProp, false).execute(isChecked));
                            mSelectedType = "";
                        }


                    } else { //QXDM总开关

                        if (isChecked && TextUtils.isEmpty(mSelectedType)) {
                            Toast.makeText(getApplicationContext(), "请选择以上一种类型,才能打开QXDM开关", Toast.LENGTH_LONG).show();
                            mMdLogSwitch.setChecked(false);
                            sleepNoClick();
                        } else {

                            mTasks.add(new PropSetTask(mSwitchButton, mType, mProp, isChecked).execute(isChecked));
                        }

                    }

                }
            };
            mSwitchButton.setOnCheckedChangeListener(listener);
            mSwitchButton.setTag(listener);
        }

        private void sleep6NoClick(final boolean isChecked) {
            showOrHideCover((Activity)getContext(), true);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(3000);//关闭后延时3秒在打开
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    QxdmSettingActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTasks.add(new PropSetTask(mSwitchButton, mType, mProp, false).execute(isChecked));
                            mMdLogSwitch.setChecked(true);
//                            showOrHideCover((Activity)getContext(), true);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Thread.sleep(3000);//打开后后延时3秒在设置成可点击
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    QxdmSettingActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            showOrHideCover((Activity)getContext(), false);
                                            isCloseOther = false;
                                        }
                                    });
                                }
                            }).start();
                        }
                    });
                }
            }).start();
        }
    }

    private void sleepNoClick() { //休眠3s且不可点击
        showOrHideCover((Activity)getContext(), true);// 3s不可点击
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);//关闭后延时3秒在打开
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                QxdmSettingActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showOrHideCover((Activity) getContext(), false);
                    }
                });
            }
        }).start();
    }

    boolean isCloseOther = false; //是否有关闭其他的开关,有的话加1s延时在打开其他开关,防止关闭出错

    private class PropGetTask extends AsyncTask<Void, Void, Boolean> {

        protected final Switch mSwitchButton;
        protected final String mType;
        protected final String mProp;

        public PropGetTask(Switch switchButton, String type, String prop) {
            this.mSwitchButton = switchButton;
            this.mType = type;
            this.mProp = prop;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String propValue = sMapPropOn.get(mType);
            propValue = null != propValue ? propValue : "1";
            mSelectedType = SystemProperties.get(PERSIST_SYS_YOTALOG_MDTYPE, "");
            return propValue.equals(SystemProperties.get(sMapTypesProps.get(mType), "0"));
        }

        @Override
        protected void onPostExecute(Boolean checked) {
            mTasks.remove(this);
            mSwitchButton.setChecked(checked);
            logger.debug("Get prop:" + mType + " " + checked);
        }
    }

    private class PropSetTask extends AsyncTask<Boolean, Void, Void> {

        private final Switch mSwitchButton;
        private final String mType;
        private final String mProp;
        private final boolean mPrompt;

        public PropSetTask(Switch switchButton, String type, String prop) {
            this(switchButton, type, prop, true);
        }

        public PropSetTask(Switch switchButton, String type, String prop, boolean prompt) {
            this.mSwitchButton = switchButton;
            this.mType = type;
            this.mProp = prop;
            this.mPrompt = prompt;
        }

        @Override
        protected void onPreExecute() {
            mSwitchButton.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Boolean... params) {
            final Boolean onoff = params[0];
            String propValue;
            if (onoff){
                propValue = sMapPropOn.get(mType);
                propValue = null != propValue ? propValue : "1";
            } else {
                propValue = sMapPropOff.get(mType);
                propValue = null != propValue ? propValue : "0";
            }
            SystemProperties.set(mProp, propValue);
            waitForFinish(mProp, onoff);
            return null;
        }

        private void waitForFinish(String prop, Boolean onoff) {
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mTasks.remove(this);
            mSwitchButton.setEnabled(true);
            logger.debug("389---Set prop:" + mProp + ",mType = " + mType);
            if (mPrompt && mType.equals(Type_Mdlog)) {

                if ( mSwitchButton.isChecked()) {
                    Toast.makeText(QxdmSettingActivity.this, "QXDM日志存至sdcard/yota_log/diag_logs目录下", Toast.LENGTH_SHORT).show();
                }
            }



        }
    }

    private View mShelter;

    /**
     * 设置屏幕不可点击
     * @param host
     * @param inProgress
     */
    private void showOrHideCover(Activity host, boolean inProgress) {
        CYLog.d("427---", QxdmSettingActivity.class, "inProgress = " + inProgress);
        if (null == mShelter) {
            mShelter = new View(getContext());
            mShelter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                }
            });
        }
        ViewParent viewParent = mShelter.getParent();
        if (null != viewParent) ((ViewGroup) viewParent).removeView(mShelter);
        if (inProgress) {
            ViewGroup parent = (ViewGroup) host.findViewById(android.R.id.content);
            parent.addView(mShelter);
        }
    }
    public Context getContext() {
        return this;
    }
}
