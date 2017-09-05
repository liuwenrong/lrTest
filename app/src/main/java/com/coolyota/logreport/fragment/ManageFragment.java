package com.coolyota.logreport.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.coolyota.logreport.QxdmSettingActivity;
import com.coolyota.logreport.R;
import com.coolyota.logreport.adapter.ManageRecyclerViewAdapter;
import com.coolyota.logreport.base.BaseFragment;
import com.coolyota.logreport.model.FileModel;
import com.coolyota.logreport.tools.FileUtil;
import com.coolyota.logreport.tools.SystemProperties;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/8/25
 */
public class ManageFragment extends BaseFragment {

    public static final String TAG = "ManageFragment";
    public static final String FOLDER_NAME = "yota_log"; //日志存放SD卡的文件夹名称
    private static int mTabNameResId = R.string.manage_tab_name;
    public RecyclerView mRlvManage;
    public ManageRecyclerViewAdapter mAdapter;
    public List<FileModel> mDatas;
    public File[] mFileList = new File[1];
    /**
     * log文件夹 sd卡/yota_log
     */
    public String mAbsFolderName;
    public SwipeRefreshLayout mSwipeRefreshLayout;
    SimpleDateFormat mSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    boolean mIsRefreshData = false;

    @Override
    public int getTabNameResId() {
        return mTabNameResId;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_manage, container, false);
        setHasOptionsMenu(true);    //保证能在Fragment里调用onCreateOptionsMenu()方法

        initView(view);

        initData();

        return view;
    }

    private void initView(View view) {

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setEnabled(false);
        mRlvManage = (RecyclerView) view.findViewById(R.id.rlv_manage);

    }

    private void initData() {

        mAbsFolderName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + FOLDER_NAME;
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRlvManage.setLayoutManager(layoutManager);
        mRlvManage.setHasFixedSize(true);   //item固定高度,提高性能
        mFileList = FileUtil.getFileList(new File(mAbsFolderName));

        mDatas = getDatas(mFileList);


        if (mAdapter == null) {
            mAdapter = new ManageRecyclerViewAdapter(getActivity(), mDatas);
//            Log.i(TAG, "initData: x-----84---xin新建Adapter");
//        mAdapter.notifyDataSetChanged();
        }
        mRlvManage.setAdapter(mAdapter);
    }

    private List<FileModel> getDatas(File[] fileList) {

        if (mDatas != null) {

            if (mIsRefreshData) { //刷新数据

            }

            return mDatas;
        }

        List<FileModel> datas = new ArrayList<>();

        if (fileList == null) {
            return datas;
        }

        for (int i = 0; i < fileList.length; i++) {
            File file = fileList[i];
            FileModel fileModel = new FileModel();
            fileModel.setFile(file);
            fileModel.setFileName("sdcard/yota_log/" + file.getName());
            fileModel.setFileSize(FileUtil.getDataSize(file));
            fileModel.setFold(file.isDirectory());
            fileModel.setFileTime(mSdf.format(new Date(file.lastModified())));

            datas.add(fileModel);
        }


        return datas;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
        inflater.inflate(R.menu.manage_menu_items, menu);
        // 设置MenuItem.SHOW_AS_ACTION_IF_ROOM使得在不溢出的情况让选项添加到actionbar上
//        menu.add(0, 0, 0, "删除").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
//        menu.add(0, 1, 1, "全选").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.action_delete:

                if (mDatas != null) {
                    //删除选中的
                    int count = 0; //计数
                    FileModel lastFolder = null; // 最后创建的文件夹被选中 就需要关闭开关,并重新打开

                    for (FileModel fileModel : mDatas) {

                        if (fileModel.getFile().isDirectory()) {
                            if (lastFolder == null) {
                                lastFolder = fileModel;
                            } else {
                                if (fileModel.getFile().lastModified() > lastFolder.getFile().lastModified()) {
                                    // 当前文件夹 最新修改时间 大于 记录的 则替换 lastFolder
                                    lastFolder = fileModel;
                                }
                            }
                        }

                        if (fileModel.isSelected()) {
                            count++;
                        }
                    }

                    if (count == 0) {
                        Toast.makeText(getContext(), "请先选中要删除的文件", Toast.LENGTH_SHORT).show();

                    } else {
                        //全选弹框提示 并关闭
                        final boolean lastFolderIsSelected = lastFolder.isSelected();//需要关闭常规log,再打开

                        clearLog(count == mDatas.size(), lastFolderIsSelected);


                    }

                }

                break;

            case R.id.action_select:

                if (mDatas == null) {
                    return super.onOptionsItemSelected(item);
                } else {
                    // 全选所有CheckBox
                    for (FileModel fileModel : mDatas) {
                        fileModel.setSelected(true);
                    }
                    mAdapter.notifyDataSetChanged();

                }

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * @param isClearAll        是否清除全部
     * @param isLastFoldChecked 最后一个是否选中,选中则需要重启开关
     */
    private void clearLog(final boolean isClearAll, final boolean isLastFoldChecked) {
        new AlertDialog.Builder(getContext()).setMessage(R.string.confirm_clean_log)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

//                        showOrHideCover((Activity) getContext(), true);
                        if (getBaseActivity() == null) {
                            return;
                        }
                        getBaseActivity().showOrHideCover(true);//不可点击防止误操作
                        Toast.makeText(getContext(), "正在清除日志,请稍等...", Toast.LENGTH_SHORT).show();
                        if (isClearAll || isLastFoldChecked) {
                            //先关闭开关,防止写入和删除冲突
                            SystemProperties.set(QxdmSettingActivity.PERSIST_SYS_YOTALOG_MDTYPE, "0");
                            SystemProperties.set(QxdmSettingActivity.PERSIST_SYS_YOTALOG_MDLOG, "false");
                            SystemProperties.set(ConfigFragment.PERSIST_SYS_YOTA_LOG, "false");
                            if (isClearAll) {
                                // 清除全部
                                mDatas.clear();
                                FileUtil.deleteDirWithFile(new File(mAbsFolderName));//清空sdcard/yota_log
                                mAdapter.notifyDataSetChanged();
                                SystemProperties.set(ConfigFragment.PERSIST_SYS_YOTA_LOG, "true");
                                getBaseActivity().showOrHideCover(false);//恢复点击
                            } else {
                                clearSelectedLog();
                            }
                        } else {
                            clearSelectedLog();
                        }

                    }

                    private void clearSelectedLog() {
                        new Thread() {
                            @Override
                            public void run() {
                                super.run();

                                final List<FileModel> removeDatas = new ArrayList<>();
                                if (mDatas != null) {
                                    for (FileModel fileModel : mDatas) {
                                        if (fileModel.isSelected()) {
                                            removeDatas.add(fileModel);
                                            FileUtil.deleteDirWithFile(fileModel.getFile());
                                        }
                                    }
                                }

                                ((Activity) getContext()).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
//                                        NotificationShow.cancelLogDeleteNotice(LogSettingActivity.this);
//                                        showAndSaveMsg(ApiConstants.OTHER_CODE, "日志已清除,请确保您需要的log开关已打开");
//                                            Toast.makeText(getContext(), "日志已清除,如有需要,请重新打开开关记录log", Toast.LENGTH_LONG).show();
//                                        mYotaLogSwitch.setChecked(true);
//                                        showOrHideCover((Activity) getContext(), false);
                                        if (getBaseActivity() == null) {
                                            return;
                                        }

                                        mDatas.removeAll(removeDatas);
                                        mAdapter.notifyDataSetChanged();
                                        getBaseActivity().showOrHideCover(false);//恢复点击
                                    }
                                });

                            }
                        }.start();
                    }

                }).show();
    }
}
