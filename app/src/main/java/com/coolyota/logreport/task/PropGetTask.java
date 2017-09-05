package com.coolyota.logreport.task;


import android.os.AsyncTask;
import android.util.Log;

import com.coolyota.logreport.tools.SystemProperties;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.List;
import java.util.Map;

/**
 * des: 获取系统属性的异步任务
 *
 * @author liuwenrong
 * @version 1.0, 2017/8/29
 */
public class PropGetTask extends AsyncTask<Void, Void, Boolean> {

    protected final SwitchButton mSwitchButton;
    protected final String mProp;
    private final List<AsyncTask> mTasks;
    private final Map<String, String> sMapPropOn;

    public PropGetTask(SwitchButton mSwitchButton, String mProp, List<AsyncTask> mTasks, Map<String, String> sMapPropOn) {
        this.mSwitchButton = mSwitchButton;
        this.mProp = mProp;
        this.mTasks = mTasks;
        this.sMapPropOn = sMapPropOn;
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
        Log.i("38--------", "Get prop:" + mProp + " " + checked);
    }

}
