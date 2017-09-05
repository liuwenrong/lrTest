package com.coolyota.logreport.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.coolyota.logreport.R;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/8/25
 */
public class NoScrollViewPager extends ViewPager {
    public NoScrollViewPager(Context context) {
        super(context);
    }

    public NoScrollViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray mTypeArray = context.obtainStyledAttributes(attrs, R.styleable.NoScrollViewPager);
        mIsScroll = mTypeArray.getBoolean(R.styleable.NoScrollViewPager_isScroll, false);
    }

    private boolean mIsScroll;

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        if (mIsScroll) {
            return super.onInterceptTouchEvent(ev);
        } else {
            return false; // 不拦截事件
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mIsScroll) {
            return super.onTouchEvent(ev);
        } else {
            return true; //消费事件
        }
    }
}
