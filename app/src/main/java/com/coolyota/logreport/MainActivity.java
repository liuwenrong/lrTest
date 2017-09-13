package com.coolyota.logreport;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.coolyota.logreport.adapter.MainFragmentPagerAdapter;
import com.coolyota.logreport.base.BaseActivity;
import com.coolyota.logreport.base.BaseFragment;
import com.coolyota.logreport.fragment.ConfigFragment;
import com.coolyota.logreport.fragment.HomeFragment;
import com.coolyota.logreport.fragment.ManageFragment;
import com.coolyota.logreport.fragment.MonitorFragment;
import com.coolyota.logreport.tools.NotificationShow;
import com.coolyota.logreport.tools.SystemProperties;

import java.io.IOException;
import java.util.ArrayList;

import static com.coolyota.logreport.fragment.ConfigFragment.PERSIST_SYS_YOTA_LOG;

public class MainActivity extends BaseActivity {

    private static final String EXCEPTION_TYPE_KEY = "exception.type";
    private static final String EXCEPTION_DATA_KEY = "exception.data";
    public TabLayout mTabLayout;
    public ViewPager mViewPager;
    public FragmentPagerAdapter mFragmentPagerAdapter;
    public ArrayList<BaseFragment> mFragments;
    public HomeFragment mHomeFragment;
    public String[] mMonitorData;
    int[] mTabNameIds = {R.string.home_tab_name, R.string.config_tab_name, R.string.monitor_tab_name, R.string.manage_tab_name};
    int[] typeStrIds = {R.string.monitor_tomb_stone, R.string.monitor_anr_system, R.string.monitor_crash_app,
            R.string.monitor_framework_reboot, R.string.monitor_subsystem_reset, R.string.monitor_crash, R.string.monitor_anr_app};
    private Toolbar mToolBar;
    private AsyncTask<?, ?, ?> mShowNotifyTask;
    public int mMonitorType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        int flags = intent.getFlags();
        mMonitorType = intent.getIntExtra(EXCEPTION_TYPE_KEY, -1);
        mMonitorData = intent.getStringArrayExtra(EXCEPTION_DATA_KEY);

        // 设置成-1,防止成任务进入APP 再次弹框 然而并没有用
//        intent.putExtra(EXCEPTION_TYPE_KEY, -1);

        initView();

        if (mMonitorType != -1) {
            mHomeFragment.setMonitorType(mMonitorType);
            if ( (flags & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS) == 0) { //不包含这个Flag,即显示在最近任务

                intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

            }
//            showThemed(mMonitorType, mMonitorData);
        } else {

        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (mTabLayout != null & mMonitorType != -1)
            mTabLayout.getTabAt(0).select(); //默认选中某项
    }

    private void initView() {

        mToolBar = (Toolbar) findViewById(R.id.tool_bar);
//        mToolBar.setTitle(R.string.app_name);
        mToolBar.setSubtitleTextColor(getResources().getColor(R.color.white, null));
        mToolBar.setSubtitle(R.string.home_tab_name);

        setSupportActionBar(mToolBar);

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

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

    private void initTabLayout() {

        mTabLayout.setupWithViewPager(mViewPager, true); //一行代码关联ViewPager,实现联动
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

    public void showThemed(int type, String[] data) {

        Log.e("xx----152---------", "showThemed: data = " + data);
        if (type > typeStrIds.length || type < 0) {
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
                .theme(Theme.DARK)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (mHomeFragment != null && mHomeFragment.onBackPressed()) {
            //自己处理事件
        } else {
            goHome();
//            super.onBackPressed();
        }
    }

    public void onClick(View view) {

        switch (view.getId()) {

        }

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
    protected void onStart() {
        super.onStart();
        hideNofityIfNeed();
    }

    protected void onStop() {
        showNotifyIfNeed();
        super.onStop();
    }

    private void showNotifyIfNeed() {
        mShowNotifyTask = AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return "true".equals(SystemProperties.get(PERSIST_SYS_YOTA_LOG, "false"));
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (null != result && result) {
                    NotificationShow.startLogRecording(getBaseContext());
                }
                mShowNotifyTask = null;
            }
        });
    }

    private void hideNofityIfNeed() {
        if (null != mShowNotifyTask) mShowNotifyTask.cancel(false);
        NotificationShow.cancelLogRecording(getBaseContext());
//        NotificationShow.cancelLogDeleteNotice(getBaseContext());
    }


}
