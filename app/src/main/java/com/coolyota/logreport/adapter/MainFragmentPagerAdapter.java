package com.coolyota.logreport.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.coolyota.logreport.base.BaseFragment;

import java.util.ArrayList;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/8/25
 */
public class MainFragmentPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<BaseFragment> mFragments;
    private Context mCtx;
    public MainFragmentPagerAdapter(FragmentManager fm, Context ctx, ArrayList<BaseFragment> fragments) {
        super(fm);
        this.mCtx = ctx;
        this.mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

//    int[] mTabNameIds = {R.string.home_tab_name, R.string.config_tab_name, R.string.monitor_tab_name, R.string.manage_tab_name};
    @Override
    public CharSequence getPageTitle(int position) {
        // 返回tab名称 配合tabLayout使用
        return mFragments.get(position).getTabName();
//        return mCtx.getResources().getString(mTabNameIds[position]);
    }
}
