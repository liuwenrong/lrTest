package com.coolyota.logreport.ui;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/9/4
 */
public class CheckLinearLayout extends LinearLayout implements Checkable{
    public CheckLinearLayout(Context context) {
        super(context);
    }

    public CheckLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CheckLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setChecked(boolean checked) {

    }

    @Override
    public boolean isChecked() {
        return false;
    }

    @Override
    public void toggle() {

    }
}
