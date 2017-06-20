package com.coolyota.logreport;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.os.AsyncTaskCompat;
import android.text.TextUtils;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.coolyota.logreport.base.CloudBaseActivity;
import com.coolyota.logreport.tools.NotificationShow;
import com.coolyota.logreport.tools.SystemProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.support.v4.os.AsyncTaskCompat.executeParallel;

public class QxdmSettingActivity extends CloudBaseActivity {
    public static final String Type_Generic = "Generic";
    private static final String Type_DataCall = "DataCall";
    public static final String Type_IMSTEST = "IMStest";
    public static final String Type_IRAT = "IRAT";
    public static final String Type_PowerOn = "PowerOn";
    public static final String Type_RF = "RF";
    private static final String Type_UIM = "UIM";
    private static final String Type_VoiceCall = "VoiceCall";
    public static final String Type_WLAN = "wlan";
    public static final String Type_GPS = "gps";

    public static final String Type_Mdlog = "mdlog";

    public static final String PERSIST_SYS_YOTALOG_MDTYPE = "persist.sys.yotalog.mdtype";
    private static final String PERSIST_SYS_YOTALOG_MDLOG = "persist.sys.yotalog.mdlog";
    public static final Map<String, String> sMapPropOn = new HashMap<>();
    private static final Map<String, String> sMapTypesProps = new HashMap<>();
    private static final Map<String, String> sMapPropOff = new HashMap<>();

    static {
        sMapPropOn.put(Type_Generic, "Generic");
        sMapPropOn.put(Type_DataCall, "DataCall");
        sMapPropOn.put(Type_IMSTEST, "IMStest");
        sMapPropOn.put(Type_IRAT, "IRAT");
        sMapPropOn.put(Type_PowerOn, "PowerOn");
        sMapPropOn.put(Type_RF, "RF");
        sMapPropOn.put(Type_UIM, "UIM");
        sMapPropOn.put(Type_VoiceCall, "VoiceCall");
        sMapPropOn.put(Type_WLAN, "wlan");
        sMapPropOn.put(Type_GPS, "gps");

        sMapPropOn.put(Type_Mdlog, "true");
    }

    static {
        sMapTypesProps.put(Type_Generic, PERSIST_SYS_YOTALOG_MDTYPE);
        sMapTypesProps.put(Type_DataCall, PERSIST_SYS_YOTALOG_MDTYPE);
        sMapTypesProps.put(Type_IMSTEST, PERSIST_SYS_YOTALOG_MDTYPE);
        sMapTypesProps.put(Type_IRAT, PERSIST_SYS_YOTALOG_MDTYPE);
        sMapTypesProps.put(Type_PowerOn, PERSIST_SYS_YOTALOG_MDTYPE);
        sMapTypesProps.put(Type_RF, PERSIST_SYS_YOTALOG_MDTYPE);
        sMapTypesProps.put(Type_UIM, PERSIST_SYS_YOTALOG_MDTYPE);
        sMapTypesProps.put(Type_VoiceCall, PERSIST_SYS_YOTALOG_MDTYPE);
        sMapTypesProps.put(Type_WLAN, PERSIST_SYS_YOTALOG_MDTYPE);
        sMapTypesProps.put(Type_GPS, PERSIST_SYS_YOTALOG_MDTYPE);

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

        Switch mGenericSwitch = (Switch) findViewById(R.id.switch_generic);
        mMutualProp.put(Type_Generic, mGenericSwitch); //put完之后,只能开一个,类似单选
        mTasks.add(executeParallel(new InitSwitchTask(mGenericSwitch, Type_Generic, PERSIST_SYS_YOTALOG_MDTYPE)));

        mDataCallSwitch = (Switch) findViewById(R.id.switch_data_call);
        mMutualProp.put(Type_DataCall, mDataCallSwitch);
        mTasks.add(executeParallel(new InitSwitchTask(mDataCallSwitch, Type_DataCall, PERSIST_SYS_YOTALOG_MDTYPE)));
//        mDataCallSwitch.setOnCheckedChangeListener(mSaveSDOnClickListener);

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
        mTasks.add(executeParallel(new InitSwitchTask(mVoiceCallSwitch, Type_VoiceCall, PERSIST_SYS_YOTALOG_MDTYPE)));

        Switch mWlanLogSwitch = (Switch) findViewById(R.id.switch_log_wlan);
        mMutualProp.put(Type_WLAN, mWlanLogSwitch);
        mTasks.add(executeParallel(new InitSwitchTask(mWlanLogSwitch, Type_WLAN, PERSIST_SYS_YOTALOG_MDTYPE)));

        Switch mGpsLogSwitch = (Switch) findViewById(R.id.switch_log_gps);
        mMutualProp.put(Type_GPS, mGpsLogSwitch);
        mTasks.add(executeParallel(new InitSwitchTask(mGpsLogSwitch, Type_GPS, PERSIST_SYS_YOTALOG_MDTYPE)));


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
            final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (mMutualProp.containsKey(mType)) { //10种类型
                        if ( mMdLogSwitch.isChecked() ){

                            mMdLogSwitch.setChecked(false); //只要切换就把总开关关掉
                            mTasks.add(new PropSetTask(mMdLogSwitch, Type_Mdlog, "false").execute(false));

                        }

                        if (isChecked) {
                            mSelectedType = mType;
                            final Iterator<String> iterator = mMutualProp.keySet().iterator();
                            while (iterator.hasNext()) {
                                final String type = iterator.next();
                                final Switch aSwitch = mMutualProp.get(type);
                                if (!type.equals(mType) && isChecked == aSwitch.isChecked()) {
                                    aSwitch.setOnCheckedChangeListener(null);
                                    aSwitch.setChecked(!isChecked);
                                    aSwitch.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) aSwitch.getTag());
                                    new PropSetTask(aSwitch, type, PERSIST_SYS_YOTALOG_MDTYPE, false).execute(!isChecked);
                                }
                            }
                        } else {
                            mSelectedType = "";
                        }

                        mTasks.add(new PropSetTask(mSwitchButton, mType, mProp).execute(isChecked));


                    } else { //QXDM总开关

                        if (isChecked && TextUtils.isEmpty(mSelectedType)) {
                            Toast.makeText(getApplicationContext(), "请选择以上一种类型,才能打开QXDM开关", Toast.LENGTH_LONG).show();
                            mMdLogSwitch.setChecked(false);
                        } else {

                            mTasks.add(new PropSetTask(mSwitchButton, mType, mProp).execute(isChecked));
                        }

                    }

                }
            };
            mSwitchButton.setOnCheckedChangeListener(listener);
            mSwitchButton.setTag(listener);
        }
    }

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
            logger.debug("Get prop:" + mProp + " " + checked);
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
            /*final String svc = sMapLogStatus.get(prop);
            if (null != svc) {
                String status = onoff ? "running" : "stopped";
                int maxTime = 4;
                while (maxTime-- > 0 && !status.equals(SystemProperties.get(svc, null))) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                logger.info("Svc[" + svc + "] status: " + SystemProperties.get(svc, null) + " after set prop: " + prop);
            }*/
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mTasks.remove(this);
            mSwitchButton.setEnabled(true);
            logger.debug("Set prop:" + mProp + " " + mSwitchButton.isChecked());
            if (mPrompt && mType.equals(Type_Mdlog) && mSwitchButton.isChecked()) {
                Toast.makeText(QxdmSettingActivity.this, "QXDM日志存至sdcard/yota_log/diag_logs目录下", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
