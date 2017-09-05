package com.coolyota.logreport.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.coolyota.logreport.DynamicToggleActivity;
import com.coolyota.logreport.R;
import com.coolyota.logreport.base.BaseFragment;
import com.coolyota.logreport.constants.ApiConstants;
import com.coolyota.logreport.constants.CYConstants;
import com.coolyota.logreport.tools.AccountNameMask;
import com.coolyota.logreport.tools.CompressAppendixService;
import com.coolyota.logreport.tools.ImageDecoder;
import com.coolyota.logreport.tools.LogUtil;
import com.coolyota.logreport.tools.NetUtil;
import com.coolyota.logreport.tools.SystemProperties;
import com.coolyota.logreport.tools.TelephonyTools;
import com.coolyota.logreport.tools.permissiongen.PermissionFail;
import com.coolyota.logreport.tools.permissiongen.PermissionGen;
import com.coolyota.logreport.tools.permissiongen.PermissionSuccess;
import com.coolyota.logreport.ui.CustomDialog;
import com.coolyota.logreport.ui.CyNumberProgressBar;
import com.dd.CircularProgressButton;
import com.kyleduo.switchbutton.SwitchButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/8/25
 */
public class HomeFragment extends BaseFragment {

    private static final int REQUEST_CODE_PERMISSION_ACCESS_FILE = 201;
    private static final int REQUEST_CODE_PERMISSION_READ_PHONE_STATE = 202;
    private static final int REQUEST_CODE_ACTIVITY_PICK_PHOTO = 103;
    public static final String TAG = "HomeFragment";
    private static final int REQUEST_CODE_PERMISSION_ACCESS_PHONE = 203;
    private static String mMsg = "";
    SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm:ss");
    TextView tvDownloadSize;
    TextView tvProgress;
    TextView tvNetSpeed;
    CyNumberProgressBar pbProgress;
    CompressAppendixService myService;
    boolean mIsStartUpload = false;
    CustomDialog dialog;
    CustomDialog.Builder builder;
    public LinearLayout mProgressContainer;
    public UploadServiceConn mUploadServiceConn;
    public TextView mTvMsg;
    CheckBox mRebootCheck;
    SwitchButton mSbReboot;
    long mBtnLastClick = 0; // 上一次提交按钮点击的时间
    private EditText mEditBugDetails;
    private ViewGroup mPicImageParent;
    private ViewGroup mDeletePicParent;
    private EditText mEditContacts;
    private CircularProgressButton mBtnSubmit;
    private int mCurrentAddPic;
    private List<String> mPicImageList = new ArrayList<>();

    private static int mTabNameResId = R.string.home_tab_name;

    View.OnClickListener mSubmitOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {

            if (System.currentTimeMillis() - mBtnLastClick < 1000) { // 1 s内不可重复点击
                return;
            }
            mBtnLastClick = System.currentTimeMillis();

            if (mBtnSubmit.getProgress() == 0) {

                showOrHideProgressAndCover(false, 1); //设为0时不会转
                submitBugReport();

            } else {
                mBtnSubmit.setProgress(0);
            }

        }
    };
    private View mShelter;
    public Intent mServiceIntent;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        setHasOptionsMenu(true);    //保证能在Fragment里调用onCreateOptionsMenu()方法

        initView(view);

        return view;
    }

    private void initView(View view) {

        mScrollView = (ScrollView) view.findViewById(R.id.scroll_view);
//        mRebootCheck = (CheckBox) view.findViewById(R.id.check_reboot);
        mSbReboot = (SwitchButton) view.findViewById(R.id.sb_md_reboot);
        mEditBugDetails = (EditText) view.findViewById(R.id.edit_bug_details);
        mPicImageParent = (ViewGroup) view.findViewById(R.id.parent_add_pic);
        initPicImageParent();
        mDeletePicParent = (ViewGroup) view.findViewById(R.id.parent_delete_pic);
        mEditContacts = (EditText) view.findViewById(R.id.edit_phone_number);

        mBtnSubmit = (CircularProgressButton) view.findViewById(R.id.button_submit);
        mBtnSubmit.setOnClickListener(mSubmitOnClickListener);

        mProgressContainer = (LinearLayout) view.findViewById(R.id.progress_container);
        tvDownloadSize = (TextView) view.findViewById(R.id.downloadSize);
        tvProgress = (TextView) view.findViewById(R.id.tvProgress);
        tvNetSpeed = (TextView) view.findViewById(R.id.netSpeed);
        pbProgress = (CyNumberProgressBar) view.findViewById(R.id.pbProgress);
        mTvMsg = (TextView) view.findViewById(R.id.tv_msg);

        initData();

    }

    private void initData() {

        PermissionGen.needPermission(getFragment(), REQUEST_CODE_PERMISSION_ACCESS_PHONE, Manifest.permission.READ_PHONE_STATE);

    }

    @PermissionFail(requestCode = REQUEST_CODE_PERMISSION_ACCESS_PHONE)
    private void failReadPhone() {
        Log.i(TAG, "failReadPhone: --------350------");
    }

    @PermissionSuccess(requestCode = REQUEST_CODE_PERMISSION_ACCESS_PHONE)
    private void setTextToEditContact() {
        String phone1Name = TelephonyTools.getInstance(getContext()).getLine1Number();
        if (TextUtils.isEmpty(phone1Name)) {
            mEditContacts.setText("");
        } else {
            mEditContacts.setText(phone1Name);
        }
    }

    private void submitBugReport() {
        if (checkBugDesc(false) && checkUserContacts()) {
            doRealSubmitBugReport();
        } else {
            showOrHideProgressAndCover(false, -1);
        }
    }

    private void doRealSubmitBugReport() {
        if (NetUtil.isNetworkAvailable(getContext())) {
            if (TextUtils.isEmpty(mEditContacts.getText().toString())) { //号码为空时去获取用户手机号
                PermissionGen.needPermission(getFragment(), REQUEST_CODE_PERMISSION_ACCESS_PHONE, Manifest.permission.READ_PHONE_STATE);
            }
            fireServiceForSubmit();
//                    Toast.makeText(LogSettingActivity.this, R.string.report_save_ok, Toast.LENGTH_LONG).show();
//                    finish();
        } else if (!getActivity().isDestroyed()) {
            Toast.makeText(getContext(), "当前网络不可用,请连上网络后再试.", Toast.LENGTH_SHORT).show();
//            mBtnSubmit.setInProgress((Activity) getContext(), false);
            showOrHideProgressAndCover(false, -1);
        }
    }


    /**
     * 压缩 并 提交到服务器
     */
    private void fireServiceForSubmit() {

        if (!NetUtil.isNetworkAvailable(getContext())) {
            Toast.makeText(getContext(), "当前网络不可用,请连上网络后再试.", Toast.LENGTH_LONG).show();
            mBtnSubmit.setVisibility(View.VISIBLE);
            mProgressContainer.setVisibility(View.GONE);
//            mBtnSubmit.setInProgress((Activity) getContext(), false);
//            mBtnSubmit.setProgress(-1);
            showOrHideProgressAndCover(false, -1);
            mIsStartUpload = false;
            return;
        }

        if ( mServiceIntent != null) {
            unBindAndStopService();
        }
        mServiceIntent = new Intent(getContext(), CompressAppendixService.class);

//        mServiceIntent.putExtra(CompressAppendixService.REBOOT_CHECKED_KEY, mRebootCheck.isChecked());
        mServiceIntent.putExtra(CompressAppendixService.REBOOT_CHECKED_KEY, mSbReboot.isChecked());
        mServiceIntent.putExtra(CompressAppendixService.BUG_DETAILS, mEditBugDetails.getText().toString());
        mServiceIntent.putExtra(CompressAppendixService.USER_CONTACT, mEditContacts.getText().toString());
        mServiceIntent.putExtra(CompressAppendixService.PIC_IMAGE_LIST, mPicImageList.toArray(new String[mPicImageList.size()]));

        mUploadServiceConn = new UploadServiceConn();
        getContext().bindService(mServiceIntent, mUploadServiceConn, BIND_AUTO_CREATE);
        getContext().startService(mServiceIntent);
        mProgressContainer.setVisibility(View.VISIBLE);

    }

    @Override
    public int getTabNameResId() {
        return mTabNameResId;
    }

    class UploadServiceConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = ((CompressAppendixService.MyBinder) service).getMyService(); //获取MyService对象

            /**
             * 使用一个接口来实现回调，这样比上面方法更加灵活，推荐
             */
            myService.setUploadListener(new CompressAppendixService.UploadListener() {
                @Override
                public void updateBar(long totalSize, long currentSize, float progress, long networkSpeed) {

                    if (!mIsStartUpload) {
                        mProgressContainer.setVisibility(View.VISIBLE);
                        mIsStartUpload = true;
                        scrollToBottom();
                        showOrHideProgressAndCover(true, 1);
                    } else {

                        if (totalSize == currentSize) { //上传完成

                        }

                    }
                    String downloadLength = Formatter.formatFileSize(getContext(), currentSize);
                    String totalLength = Formatter.formatFileSize(getContext(), totalSize);
                    tvDownloadSize.setText(downloadLength + "/" + totalLength);
                    String netSpeed = Formatter.formatFileSize(getContext(), networkSpeed);
                    tvNetSpeed.setText(netSpeed + "/S");
                    tvProgress.setText((Math.round(progress * 10000) * 1.0f / 100) + "%");
                    pbProgress.setMax(100);
                    pbProgress.setProgress((int) (progress * 100));
                    mBtnSubmit.setProgress((int) (progress * 100));
                    getDialog("上传到服务器成功");

                }

                @Override
                public void onFail() {
                    //上传失败
                    getDialog("上传失败").show();
//                    mBtnSubmit.setVisibility(View.VISIBLE);
                    mProgressContainer.setVisibility(View.GONE);
//                    mBtnSubmit.setInProgress((Activity) getContext(), false);
//                    mBtnSubmit.setProgress(-1);
                    showOrHideProgressAndCover(true, -1);
                    mIsStartUpload = false;
                }

                @Override
                public void sendMsg(int code, String msg) {
                    if (code == ApiConstants.SUCCESS_CODE) {
                        mProgressContainer.setVisibility(View.GONE);
//                        mBtnSubmit.setInProgress((Activity) getContext(), false);
                        showOrHideProgressAndCover(true, 100);
                        mIsStartUpload = false;
//                        mYotaLogSwitch.setChecked(false);

                        //如果 log开关已经打开,那么先关闭,在重新打开,防止文件删除后 log不记录的情况
                        if ("true".equals(SystemProperties.get(ConfigFragment.PERSIST_SYS_YOTA_LOG, "false"))) {
                            SystemProperties.set(ConfigFragment.PERSIST_SYS_YOTA_LOG, "false");
                            SystemProperties.set(ConfigFragment.PERSIST_SYS_YOTA_LOG, "true");
                        }

                    }
                    showAndSaveMsg(code, msg);
                }
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private void unBindAndStopService() {
//        if (mServiceIntent != null)
        getContext().stopService(mServiceIntent);
        getContext().unbindService(mUploadServiceConn);
    }


    public boolean onBackPressed() {
        if (mEditBugDetails == null) {
            return false;
        }

        if ( !checkBugDesc(true) && !checkAttachmentExist()) {
//            super.onBackPressed();
            return false;
        } else {
            new AlertDialog.Builder(getContext()).setMessage(R.string.abandon_current_edit_content)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            getActivity().finish();

                        }
                    }).show();
            return true;
        }
    }

    private boolean checkUserContacts() {
        final String contact = mEditContacts.getText().toString();
        final boolean result = TextUtils.isEmpty(contact) || AccountNameMask.checkMobile(contact) || AccountNameMask.checkEmail(contact);
        if (!result) {
            Toast.makeText(getContext(), R.string.please_input_valid_contacts, Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    private boolean checkBugDesc(boolean backKeyPressed) {

        boolean result = !TextUtils.isEmpty(mEditBugDetails.getText().toString());
        if (!result && !backKeyPressed) {
//            getSuccessDialog(getResources().getText(R.string.please_input_bug_desc).toString() + "test test test test 点击确定后,即将退出应用!").show();
//            getDialog(getContext().getString(R.string.please_input_bug_desc)).show();
            Toast.makeText(getContext(), R.string.please_input_bug_desc, Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    private boolean checkAttachmentExist() {
        return /*!TextUtils.isEmpty(mEditBugTitle.getText().toString()) || */!checkPicListEmpty();
    }

    private boolean checkPicListEmpty() {
        int index;
        for (index = 0; index < mPicImageList.size(); index++) {
            if (mPicImageList.get(index) != null) {
                return false;
            }
        }
        return index >= mPicImageList.size() || mPicImageList.isEmpty();
    }


    private void initPicImageParent() {
        for (int index = 0; index < mPicImageParent.getChildCount(); index++) {
            final int finalIndex = index;
            mPicImageParent.getChildAt(index).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentAddPic = finalIndex;
                    Log.i(TAG, "onClick: -----338------");
                    PermissionGen.needPermission(getFragment(), REQUEST_CODE_PERMISSION_ACCESS_FILE, Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            });
            mPicImageList.add(null);
        }
    }

    @PermissionFail(requestCode = REQUEST_CODE_PERMISSION_ACCESS_FILE)
    private void failToAccessFile() {
//        Log.i(TAG, "failToAccessFile: --------350------");
    }

    @PermissionSuccess(requestCode = REQUEST_CODE_PERMISSION_ACCESS_FILE)
    private void addTestPhoto() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_ACTIVITY_PICK_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE_ACTIVITY_PICK_PHOTO == requestCode) {
            if (Activity.RESULT_OK == resultCode && null != data) {
                final String path = getPath(data.getData());
                if (null != path && mPicImageList.contains(path)) {
                    Toast.makeText(getContext(), R.string.please_not_reset_pic, Toast.LENGTH_SHORT).show();
                } else {
                    AsyncTaskCompat.executeParallel(new DecodeImageTask(new File(path)));
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String getPath(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(getContext(), uri)) {
            if (isMediaDocument(uri)) {
                String docId = DocumentsContract.getDocumentId(uri);
                String[] split = docId.split(":");
                String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                String selection = "_id=?";
                String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(contentUri, selection, selectionArgs);
            }
        }
        return getDataColumn(uri, null, null);
    }

    private String getDataColumn(Uri uri, String selection, String[] selectionArgs) {
        String column = "_data";
        String[] projection = {column};
        Cursor cursor = getContext().getContentResolver().query(uri, projection, selection, selectionArgs, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    private class DecodeImageTask extends AsyncTask<Void, Void, Bitmap> {
        private final File mImagePicFile;
        private final int mOpImageIndex;
        private int mMaxWidth;
        private int mMaxHeight;
        private ImageView mImageView;
        private Bitmap mImageBitmap;

        public DecodeImageTask(File imagePicFile) {
            mImagePicFile = imagePicFile;
            mOpImageIndex = mCurrentAddPic;
        }

        @Override
        protected void onPreExecute() {
            mImageView = (ImageView) mPicImageParent.getChildAt(mOpImageIndex);
            mMaxWidth = mImageView.getWidth();
            mMaxHeight = mImageView.getHeight();
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            return ImageDecoder.decode(mImagePicFile, mMaxWidth, mMaxHeight, ImageView.ScaleType.CENTER_CROP);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (null != bitmap) {
                mImageBitmap = bitmap;
                mImageView.setImageBitmap(bitmap);
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri photoUri = FileProvider.getUriForFile(getContext(), "com.coolyota.logreport.fileprovider", mImagePicFile);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        intent.setDataAndType(photoUri, "image/*");
                        startActivity(intent);
                    }
                });
                final View deletePic = mDeletePicParent.getChildAt(mOpImageIndex);
                deletePic.setVisibility(View.VISIBLE);
                deletePic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mPicImageList.set(mOpImageIndex, null);
                        deletePic.setVisibility(View.INVISIBLE);
                        deletePic.setOnClickListener(null);
                        mImageView.setImageResource(R.drawable.ic_add_image_selector);
                        mImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentAddPic = mOpImageIndex;
                                PermissionGen.needPermission(getFragment(), REQUEST_CODE_PERMISSION_ACCESS_FILE, Manifest.permission.READ_EXTERNAL_STORAGE);
                            }
                        });
                        if (!mImageBitmap.isRecycled()) {
                            mImageBitmap.recycle();
                            mImageBitmap = null;
                        }
                    }
                });
            }

            mPicImageList.set(mOpImageIndex, mImagePicFile.getAbsolutePath());
        }
    }
    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
//        inflater.inflate(R.menu.home_menu_items, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
//                Toast.makeText(getContext(), "点击了设置", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getContext(), DynamicToggleActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

//    boolean mLastUpload;
    /**
     * 显示或隐藏 进度和覆盖一层不可点击的透明Layout
     * @param isUpload 是否是上传
     * @param progress 进度值
     */
    private void showOrHideProgressAndCover (boolean isUpload, int progress) {

        if (isUpload) {
            mBtnSubmit.setIndeterminateProgressMode(false); //不会一直转
        } else {
            mBtnSubmit.setIndeterminateProgressMode(true);//压缩的时候一直转
        }

        mBtnSubmit.setProgress(progress);
        if ((progress == 100) || (progress == -1)) {
            showOrHideCover(getActivity(), false); //隐藏透明层, 恢复点击
        } else {
            showOrHideCover(getActivity(), true); //显示透明层 屏蔽点击
        }

    }

    /**
     * 是否显示 覆盖层屏蔽点击
     * @param host activity
     * @param inProgress 是否正在 提交中 是的话:使屏幕不可点击
     */
    private void showOrHideCover(Activity host, boolean inProgress) {
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

    private Dialog getDialog(String msg) {

        if (builder == null) {
            builder = new CustomDialog.Builder(getContext());
            if (dialog == null) {
                dialog = builder
                        .setTitle("离线日志")
                        .setMessage(msg)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
            } else {
                builder.setMessage(msg);
            }
        } else {

            builder.setMessage(msg);
        }
        return dialog;
    }


    private Dialog getSuccessDialog(String msg) {
        getDialog(msg);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                ((Activity)getContext()).finish();
            }
        });
        return dialog;
    }

    private void showAndSaveMsg(int code, String msg) {
        // 显示TextView
        String time = sdf.format(new Date()) + " ";
        String saveMsg = time + msg;
        mMsg = saveMsg + "\n" + mMsg;
        mTvMsg.setText(mMsg);
        // 弹Toast
//                  Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
        //弹对话框
        if (code == ApiConstants.SUCCESS_CODE) {
            mEditBugDetails.setText("");
            mEditContacts.setText("");
            getSuccessDialog(msg).show();
//            mYotaLogSwitch.setChecked(true);
        } else if (code == ApiConstants.TOKEN_CODE) {
            getDialog(msg).show();
        }
        //存文件
        LogUtil.saveInfoToFile(CYConstants.TYPE_LOG, saveMsg, getContext());
        scrollToBottom();
    }

    Handler mHandler = new Handler();
    ScrollView mScrollView;
    private void scrollToBottom(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }

    private Fragment getFragment() {
        return this;
    }

}
