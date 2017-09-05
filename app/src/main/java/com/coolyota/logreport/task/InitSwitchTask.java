package com.coolyota.logreport.task;


import android.os.AsyncTask;
import android.widget.CompoundButton;

import com.kyleduo.switchbutton.SwitchButton;

import java.util.List;
import java.util.Map;

/**
 * des: 获取系统属性的异步任务
 *
 * @author liuwenrong
 * @version 1.0, 2017/8/29
 */
public class InitSwitchTask extends PropGetTask {


    private final List<AsyncTask> mTasks;

    public InitSwitchTask(SwitchButton switchButton, String prop, List<AsyncTask> mTasks, Map<String, String> sMapPropOn) {
        super(switchButton, prop, mTasks, sMapPropOn);
        this.mTasks = mTasks;
    }

    @Override
    protected void onPostExecute(Boolean checked) {
        super.onPostExecute(checked);
        final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

//                if (isChecked && mMutualProp.containsKey(mProp)) {
//                    final Iterator<String> iterator = mMutualProp.keySet().iterator();
//                    while (iterator.hasNext()) {
//                        final String prop = iterator.next();
//                        final Switch aSwitch = mMutualProp.get(prop);
//                        if (!prop.equals(mProp) && isChecked == aSwitch.isChecked()) {
//                            aSwitch.setOnCheckedChangeListener(null);
//                            aSwitch.setChecked(!isChecked);
//                            aSwitch.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) aSwitch.getTag());
//                            new PropSetTask(aSwitch, prop, false).execute(!isChecked);
//                        }
//                    }
//                }
                mTasks.add(new PropSetTask(mSwitchButton, mProp).execute(isChecked));
            }
        };
        mSwitchButton.setOnCheckedChangeListener(listener);
        mSwitchButton.setTag(listener);
    }
}

