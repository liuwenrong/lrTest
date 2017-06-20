package com.coolyota.logreport;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.coolyota.logreport.base.CloudBaseActivity;
import com.coolyota.logreport.tools.SystemProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.support.v4.os.AsyncTaskCompat.executeParallel;

public class ServerSettingActivity extends CloudBaseActivity {
    private static final String PERSIST_SYS_TEST_SERVER = "persist.sys.test.server";
    public static final Map<String, String> sMapPropOn = new HashMap<>();
    private static final Map<String, String> sMapLogStatus = new HashMap<>();
    private static final Map<String, String> sMapPropOff = new HashMap<>();

    static {
        sMapPropOn.put(PERSIST_SYS_TEST_SERVER, "true");
    }

    static {
    }

    static {
        sMapPropOff.put(PERSIST_SYS_TEST_SERVER, "false");
    }

    private final List<AsyncTask<?, ?, ?>> mTasks = new ArrayList<>();
    private Switch mTestServerSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server_setting);
        getSupportActionBar().hide();

        mTestServerSwitch = (Switch) findViewById(R.id.switch_test_server);
        mTasks.add(executeParallel(new InitSwitchTask(mTestServerSwitch, PERSIST_SYS_TEST_SERVER)));

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }


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
        public InitSwitchTask(Switch switchButton, String prop) {
            super(switchButton, prop);
        }

        @Override
        protected void onPostExecute(Boolean checked) {
            super.onPostExecute(checked);
            final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    mTasks.add(new PropSetTask(mSwitchButton, mProp).execute(isChecked));
                }
            };
            mSwitchButton.setOnCheckedChangeListener(listener);
            mSwitchButton.setTag(listener);
        }
    }

    private class PropGetTask extends AsyncTask<Void, Void, Boolean> {

        protected final Switch mSwitchButton;
        protected final String mProp;

        public PropGetTask(Switch switchButton, String prop) {
            this.mSwitchButton = switchButton;
            this.mProp = prop;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String propValue = sMapPropOn.get(mProp);
            propValue = null != propValue ? propValue : "1";
            return propValue.equals(SystemProperties.get(mProp, "0"));
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
        private final String mProp;
        private final boolean mPrompt;

        public PropSetTask(Switch switchButton, String prop) {
            this(switchButton, prop, true);
        }

        public PropSetTask(Switch switchButton, String prop, boolean prompt) {
            this.mSwitchButton = switchButton;
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
                propValue = sMapPropOn.get(mProp);
                propValue = null != propValue ? propValue : "1";
            } else {
                propValue = sMapPropOff.get(mProp);
                propValue = null != propValue ? propValue : "0";
            }
            SystemProperties.set(mProp, propValue);
            waitForFinish(mProp, onoff);
            return null;
        }

        private void waitForFinish(String prop, Boolean onoff) {
            final String svc = sMapLogStatus.get(prop);
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
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mTasks.remove(this);
            mSwitchButton.setEnabled(true);
            logger.debug("Set prop:" + mProp + " " + mSwitchButton.isChecked());
            if (mPrompt) {
            }
        }
    }

    public static Intent getLaunchIntent(Context context) {
//        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage("com.coolyota.logreport");
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        String className = ServerSettingActivity.class.getName();
        Log.i("xx---", "getLaunchIntent: className = " + className);
        ComponentName compMain = new ComponentName(context.getPackageName(), className);
        intent.setComponent(compMain);
        // 如果打开了首页,用暗码打开该页面会跳转首页
        return intent;
    }
}
