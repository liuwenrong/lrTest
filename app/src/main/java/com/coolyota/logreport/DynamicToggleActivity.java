package com.coolyota.logreport;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;

import com.coolyota.logreport.base.BaseActivity;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/9/4
 */
public class DynamicToggleActivity extends BaseActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);

        //不会显示
//        setContentView(R.layout.act_dynamic_toggle);

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_dynamic_toggle);
    }
}
