package com.coolyota.logreport;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.coolyota.logreport.adapter.MainFragmentPagerAdapter;
import com.coolyota.logreport.base.BaseActivity;
import com.coolyota.logreport.base.BaseFragment;
import com.coolyota.logreport.fragment.ConfigFragment;
import com.coolyota.logreport.fragment.HomeFragment;
import com.coolyota.logreport.fragment.ManageFragment;
import com.coolyota.logreport.fragment.MonitorFragment;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    private Toolbar mToolBar;
    public TabLayout mTabLayout;
    public ViewPager mViewPager;
    public FragmentPagerAdapter mFragmentPagerAdapter;
    public ArrayList<BaseFragment> mFragments;
    public HomeFragment mHomeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        ActionBar mActionBar = getSupportActionBar();
//        TextView tvTitle = (TextView) mActionBar.getCustomView().findViewById(R.id.tv);
//        tvTitle.setTextSize(10);

        initView();
    }

    private void initView() {

        mToolBar = (Toolbar) findViewById(R.id.tool_bar);
//        mToolBar.setTitle(R.string.app_name);
        mToolBar.setSubtitleTextColor(getResources().getColor(R.color.white, null));
        mToolBar.setSubtitle(R.string.home_tab_name);

        setSupportActionBar(mToolBar);

        mTabLayout = (TabLayout)findViewById(R.id.tab_layout);

        initViewPagerAndDatas();
        initTabLayout();
    }

    private void initViewPagerAndDatas() {
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mFragments = new ArrayList<>();
        mHomeFragment = new HomeFragment();
        mHomeFragment.setTabName(getString(R.string.home_tab_name));
        mFragments.add(mHomeFragment);
        mFragments.add(new ConfigFragment().setTabName(getString(R.string.config_tab_name)));
        mFragments.add(new MonitorFragment().setTabName(getString(R.string.monitor_tab_name)));
        mFragments.add(new ManageFragment().setTabName(getString(R.string.manage_tab_name)));

        mFragmentPagerAdapter = new MainFragmentPagerAdapter(getSupportFragmentManager(), this, mFragments);
        mViewPager.setAdapter(mFragmentPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //页面滚动事件

            }

            @Override
            public void onPageSelected(int position) {

                mToolBar.setSubtitle(mFragments.get(position).getTabName());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    int[] mTabNameIds = {R.string.home_tab_name, R.string.config_tab_name, R.string.monitor_tab_name, R.string.manage_tab_name};

    private void initTabLayout() {

        mTabLayout.setupWithViewPager(mViewPager, true); //一行代码关联ViewPager,实现联动
/*        for (int i=0; i<mFragments.size(); i++) {
            mTabLayout.getTabAt(i).setText(mTabNameIds[i]);
        }*/
/*        mTabLayout.getTabAt(0).setText(R.string.home_tab_name);
        mTabLayout.getTabAt(1).setText(R.string.config_tab_name);
        mTabLayout.getTabAt(2).setText(R.string.monitor_tab_name);
        mTabLayout.getTabAt(3).setText(R.string.manage_tab_name);*/
/*        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.home_tab_name));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.config_tab_name));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.monitor_tab_name));
        mTabLayout.addTab(mTabLayout.newTab().setText(R.string.manage_tab_name));*/
        mTabLayout.getTabAt(0).select(); //默认选中某项
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }


    @Override
    public void onBackPressed() {
        if (mHomeFragment != null && mHomeFragment.onBackPressed()) {
            //自己处理事件
        } else {
            super.onBackPressed();
        }
    }

    public void onClick(View view) {

        switch (view.getId()) {

        }

    }
}
