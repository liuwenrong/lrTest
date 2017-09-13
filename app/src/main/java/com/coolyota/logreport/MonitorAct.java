package com.coolyota.logreport;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.coolyota.logreport.base.BaseActivity;

import java.io.IOException;

public class MonitorAct extends BaseActivity {

    private static final String EXCEPTION_TYPE_KEY = "exception.type";
    private static final String EXCEPTION_DATA_KEY = "exception.data";

    public String[] mMonitorData;
    int[] typeStrIds = {R.string.monitor_tomb_stone, R.string.monitor_anr_system, R.string.monitor_crash_app,
            R.string.monitor_framework_reboot, R.string.monitor_subsystem_reset, R.string.monitor_crash, R.string.monitor_anr_app};
    public int mMonitorType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.act_monitor);

        Intent intent = getIntent();
        mMonitorType = intent.getIntExtra(EXCEPTION_TYPE_KEY, -1);
        mMonitorData = intent.getStringArrayExtra(EXCEPTION_DATA_KEY);

        // 设置成-1,防止成任务进入APP 再次弹框 然而并没有用
//        intent.putExtra(EXCEPTION_TYPE_KEY, -1);

        if (mMonitorType != -1 && mMonitorType < typeStrIds.length) {
            showThemed(mMonitorType, mMonitorData);
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    public void showThemed(int type, String[] data) {

        Log.e("xx----54---------", "showThemed: data = " + data);

        if (data != null && data.length > 0) {
            String sData = "";
            for (int i = 0; i< data.length; i++) {
                sData += data[i] + "; ";
            }

            Log.e("xx----62---------", "showThemed: sData = " + sData);
        }

        if (type >= typeStrIds.length || type < 0) {
            return;
        }

        new MaterialDialog.Builder(this)
                .title("上传Log日志")
                .content(typeStrIds[type], false)
                .positiveText("确定")
                .negativeText("取消")
                .positiveColorRes(R.color.material_red_400)
                .negativeColorRes(R.color.material_red_400)
                .titleGravity(GravityEnum.CENTER)
                .titleColorRes(R.color.material_red_400)
                .contentColorRes(android.R.color.white)
                .backgroundColorRes(R.color.material_blue_grey_800)
                .dividerColorRes(R.color.colorAccent)
                .btnSelector(R.drawable.md_btn_selector_custom, DialogAction.POSITIVE)
                .positiveColor(Color.WHITE)
                .negativeColorAttr(android.R.attr.textColorSecondaryInverse)
                .onNegative(new MaterialDialog.SingleButtonCallback() {

                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        forceFinishApp();
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(getBaseContext(), MainActivity.class);
                        intent.putExtra(EXCEPTION_TYPE_KEY, mMonitorType);
                        intent.putExtra(EXCEPTION_DATA_KEY, mMonitorData);
                        startActivity(intent);
                        finish();
                    }
                })
                .canceledOnTouchOutside(false)
                .cancelable(false)
                .theme(Theme.DARK)
                .show();
    }


    private void forceFinishApp() {
        String packageName = "com.coolyota.logreport";
        try {
            final String command = "am force-stop " + packageName;
            Process process = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onBackPressed() {
        //屏蔽返回键
        super.onBackPressed();
    }
}
