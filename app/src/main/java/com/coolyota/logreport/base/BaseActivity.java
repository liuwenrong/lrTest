package com.coolyota.logreport.base;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import static com.coolyota.logreport.CYLogReporterApplication.getContext;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/9/1
 */
public class BaseActivity extends AppCompatActivity {

    private View mShelter;

    /**
     * 是否显示 覆盖层屏蔽点击
     *
     * @param host       activity
     * @param inProgress 是否正在 提交中 是的话:使屏幕不可点击
     */
    public void showOrHideCover(Activity host, boolean inProgress) {
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

    /**
     * 是否显示 覆盖层屏蔽点击
     *
     * @param inProgress 是否正在 提交中 是的话:使屏幕不可点击
     */
    public void showOrHideCover(boolean inProgress) {

        showOrHideCover(this, inProgress);

    }
}
