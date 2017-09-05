package com.coolyota.logreport.task;

import android.os.AsyncTask;

import com.kyleduo.switchbutton.SwitchButton;

/**
 * des: 
 * 
 * @author  liuwenrong
 * @version 1.0,2017/8/29 
 */
public class PropSetTask extends AsyncTask<Boolean, Void, Void> {

    private final SwitchButton mSwitchButton;
    private final String mProp;
    private final boolean mPrompt;

    public PropSetTask(SwitchButton switchButton, String prop) {
        this(switchButton, prop, true);
    }

    public PropSetTask(SwitchButton switchButton, String prop, boolean prompt) {
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
        /*final Boolean onoff = params[0];
        String propValue;
        if (onoff) {
            propValue = sMapPropOn.get(mProp);
            propValue = null != propValue ? propValue : "1";
        } else {
            propValue = sMapPropOff.get(mProp);
            propValue = null != propValue ? propValue : "0";
        }
        SystemProperties.set(mProp, propValue);
        waitForFinish(mProp, onoff);*/
        return null;
    }

    private void waitForFinish(String prop, Boolean onoff) {
       /* final String svc = sMapLogStatus.get(prop);
        if (null != svc) {
            String status = onoff ? "running" : "stopped";
            int maxTime = 4;
            while (maxTime-- > 0 && !status.equals(SystemProperties.get(svc, null))) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }*/
//            logger.info("Svc[" + svc + "] status: " + SystemProperties.get(svc, null) + " after set prop: " + prop);
//        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        /*mTasks.remove(this);
        mSwitchButton.setEnabled(true);
        logger.debug("Set prop:" + mProp + " " + mSwitchButton.isChecked());*/
        if (mPrompt) {
//                Toast.makeText(LogSettingActivity.this, mSwitchButton.getText() + getString(R.string.prop_setting_success), Toast.LENGTH_SHORT).show();
        }
    }
}

