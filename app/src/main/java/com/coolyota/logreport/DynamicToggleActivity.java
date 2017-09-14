package com.coolyota.logreport;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.coolyota.logreport.base.BaseActivity;
import com.coolyota.logreport.constants.CYConstants;
import com.coolyota.logreport.tools.SystemProperties;
import com.kyleduo.switchbutton.SwitchButton;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/9/4
 */
public class DynamicToggleActivity extends BaseActivity {

    public static final String ACTION_DYNAMIC_TOGGLE = "com.coolyota.logreport.dynamic.toggle";
    public static final String KEY_DYNAMIC_TOGGLE = "dynamicToggle";
    public static final String KEY_TYPE = "key_type";//值0时则为INPUT,1表示Power
    public static final String KEY_IS_OPEN = "key_is_open"; //true 表示打开,false表示关闭
    private static final String KEY_LOG_LEVEL = "key_log_level";
    private static final int LOG_LEVEL = 111; //configure dynamic log level

    public SwitchButton mSbPower;
    public SwitchButton mSbInput;
    public SwitchButton[] mSwitchButtons = new SwitchButton[CYConstants.Dynamic_Toggles.length];
    public int mDynamicToggle;
    public TextView mLogLevelTv;
    public int mLogLevel;
    public String[] mStrLogLevels;
    CompoundButton.OnCheckedChangeListener mOnCheckedChangerListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            for (int i = 0; i < mSwitchButtons.length; i++) {

                SwitchButton switchBtn = mSwitchButtons[i];

                if (buttonView == switchBtn) {
                    SystemProperties.set(CYConstants.PERSIST_SYS_DYNAMIC_TOGGLE, isChecked ?
                            "" + (mDynamicToggle |= CYConstants.Dynamic_Toggles[i]) :   //加上Input
                            "" + (mDynamicToggle &= ~CYConstants.Dynamic_Toggles[i])); //取消Input
                    sendToggleBoardCast(mDynamicToggle, i, isChecked, Log.VERBOSE);
                    break; //只要找到一个btn即可退出循环
                }

            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dynamic_toggle);

        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setHomeButtonEnabled(true);
        mActionBar.setDisplayHomeAsUpEnabled(true);
        mActionBar.setTitle("动态开关");

        initView();

        initData();

    }

    private void initView() {
        mSbInput = (SwitchButton) findViewById(R.id.sb_input);
        mSbPower = (SwitchButton) findViewById(R.id.sb_power);
        mSwitchButtons[0] = mSbInput;
        mSwitchButtons[1] = mSbPower;

        mSbInput.setOnCheckedChangeListener(mOnCheckedChangerListener);
        mSbPower.setOnCheckedChangeListener(mOnCheckedChangerListener);

        mLogLevelTv = (TextView) findViewById(R.id.log_level);
        mLogLevelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new MaterialDialog.Builder(getContext())
//                        .title("Log Toggle Level")
                        .title("")
                        .titleGravity(GravityEnum.CENTER)
                        .items(R.array.log_levels)
//                        .itemsDisabledIndices(1, 3) //设置一些按钮不可点击
                        .itemsCallbackSingleChoice(mLogLevel - Log.VERBOSE,
                                new MaterialDialog.ListCallbackSingleChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                                        Toast.makeText(getContext(), which + ": " + text, Toast.LENGTH_SHORT).show();

                                        // 设置属性,并显示TextView
                                        SystemProperties.set(CYConstants.PERSIST_SYS_LOG_LEVEL, which + Log.VERBOSE + "");
                                        mLogLevelTv.setText("" + mStrLogLevels[which]);
                                        mLogLevel = which + Log.VERBOSE;
                                        sendToggleBoardCast(LOG_LEVEL, LOG_LEVEL, true, which + Log.VERBOSE);

                                        return true; // allow selection
                                    }
                                })
                        .positiveText("确定")
                        .show();
            }
        });

    }

    private void initData() {

        mDynamicToggle = SystemProperties.getInt(CYConstants.PERSIST_SYS_DYNAMIC_TOGGLE, 0);
        if ((mDynamicToggle & CYConstants.dynamicToggle.INPUT) != 0) { //=0不包含Input,!=0表示打开了INPUT开关
            mSbInput.setCheckedNoEvent(true);
        }
        if ((mDynamicToggle & CYConstants.dynamicToggle.POWER) != 0) { // !=0即包含Power
            mSbPower.setCheckedNoEvent(true);
        }

        mStrLogLevels = getResources().getStringArray(R.array.log_levels);
        //设置 LogLevel

        mLogLevel = SystemProperties.getInt(CYConstants.PERSIST_SYS_LOG_LEVEL, Log.VERBOSE);
        mLogLevelTv.setText("" + mStrLogLevels[mLogLevel - Log.VERBOSE]);

    }

    private void sendToggleBoardCast(int dynamicToggleValue, int type, boolean isOpen, int logLevel) {
        Intent intent = new Intent();
        intent.setAction(ACTION_DYNAMIC_TOGGLE);
        intent.putExtra(KEY_DYNAMIC_TOGGLE, dynamicToggleValue);
        intent.putExtra(KEY_TYPE, type);
        intent.putExtra(KEY_IS_OPEN, isOpen);
        intent.putExtra(KEY_LOG_LEVEL, logLevel);
        getContext().sendBroadcast(intent);
    }

    private Context getContext() {
        return this;
    }

}
