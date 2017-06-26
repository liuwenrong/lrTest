package com.coolyota.logreport;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.coolyota.logreport.base.CloudBaseActivity;
import com.coolyota.logreport.tools.AccountNameMask;
import com.coolyota.logreport.tools.CompressAppendixService;
import com.coolyota.logreport.tools.ImageDecoder;
import com.coolyota.logreport.tools.LogUtil;
import com.coolyota.logreport.tools.NotificationShow;
import com.coolyota.logreport.tools.SystemProperties;
import com.coolyota.logreport.tools.permissiongen.PermissionGen;
import com.coolyota.logreport.tools.permissiongen.PermissionSuccess;
import com.coolyota.logreport.ui.RotateInButton;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.support.v4.os.AsyncTaskCompat.executeParallel;

public class LogSettingActivity extends CloudBaseActivity {
    public static final String PERSIST_SYS_YOTA_LOG = "persist.sys.yotalog.record";
    public static final Map<String, String> sMapPropOn = new HashMap<>();
    private static final Map<String, String> sMapLogStatus = new HashMap<>();
    private static final Map<String, String> sMapPropOff = new HashMap<>();

    private Spinner mSpinnerBugType;
    private EditText mEditBugDetails;
    private ViewGroup mPicImageParent;
    private ViewGroup mDeletePicParent;
    private EditText mEditContacts;
    private RotateInButton mBtnSubmit;
    private int mCurrentAddPic;
    private List<String> mPicImageList = new ArrayList<>();

    private static final int REQUEST_CODE_PERMISSION_ACCESS_FILE = 201;

    static {
        sMapPropOn.put(PERSIST_SYS_YOTA_LOG, "true");
    }

    static {
    }

    static {
        sMapPropOff.put(PERSIST_SYS_YOTA_LOG, "false");
    }

    private final Map<String, Switch> mMutualProp = new HashMap<>();
    private final List<AsyncTask<?, ?, ?>> mTasks = new ArrayList<>();
    View.OnClickListener mQxdmOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            Intent intent = new Intent(getContext(), QxdmSettingActivity.class);
            getContext().startActivity(intent);

        }
    };
    View.OnClickListener mCleanLogOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {

            new AlertDialog.Builder(getContext()).setMessage(R.string.confirm_clean_log)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //先关闭开关,防止写入和删除冲突
                            SystemProperties.set(QxdmSettingActivity.PERSIST_SYS_YOTALOG_MDTYPE, "0");
                            SystemProperties.set(QxdmSettingActivity.PERSIST_SYS_YOTALOG_MDLOG, "false");
                            mYotaLogSwitch.setChecked(false);
                            mTasks.add(new PropSetTask(mYotaLogSwitch, PERSIST_SYS_YOTA_LOG).execute(false));

                            Toast.makeText(getContext(), "正在清除日志,请稍等...", Toast.LENGTH_SHORT).show();
                            new Thread(){
                                @Override
                                public void run() {
                                    super.run();
                                    LogUtil.cleanSdcardLog();

                                    ((Activity)getContext()).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getContext(), "日志已清除,如有需要,请重新打开开关记录log", Toast.LENGTH_LONG).show();
                                        }
                                    });

                                }
                            }.start();

                        }
                    }).show();


        }
    };
    View.OnClickListener mSubmitOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {

            submitBugReport();
        }
    };
    long mBtnLastClick = 0; // 字段
    View.OnClickListener mSaveSDOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View buttonView) {
            if (System.currentTimeMillis() - mBtnLastClick < 2000) {
                return;
            }
            mBtnLastClick = System.currentTimeMillis();
            Toast.makeText(LogSettingActivity.this, "状态信息已存至sdcard/yota_log目录下", Toast.LENGTH_SHORT).show();
            LogUtil.init(LogSettingActivity.this);
            LogUtil.startLog();
        }
    };
    private AsyncTask<?, ?, ?> mShowNotifyTask;
    private Button mSaveSdcard;
    private Button mQxdmBtn;
    private Button mCleanLogBtn;
    public Switch mYotaLogSwitch;

    public Context getContext() {
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_setting);
        getSupportActionBar().hide();

        mYotaLogSwitch = (Switch) findViewById(R.id.switch_yota_log);
        mTasks.add(executeParallel(new InitSwitchTask(mYotaLogSwitch, PERSIST_SYS_YOTA_LOG)));

        mSaveSdcard = (Button) findViewById(R.id.btn_save_sdcard);
        mSaveSdcard.setOnClickListener(mSaveSDOnClickListener);
        mQxdmBtn = (Button) findViewById(R.id.btn_qxdm);
        mQxdmBtn.setOnClickListener(mQxdmOnClickListener);
        mCleanLogBtn = (Button) findViewById(R.id.btn_clean_log);
        mCleanLogBtn.setOnClickListener(mCleanLogOnClickListener);

        mSpinnerBugType = (Spinner) findViewById(R.id.spinner_bug_type);
        mSpinnerBugType.setSelection(mSpinnerBugType.getFirstVisiblePosition());
        mEditBugDetails = (EditText) findViewById(R.id.edit_bug_details);
        mPicImageParent = (ViewGroup) findViewById(R.id.parent_add_pic);
        initPicImageParent();
        mDeletePicParent = (ViewGroup) findViewById(R.id.parent_delete_pic);
        mEditContacts = (EditText) findViewById(R.id.edit_phone_number);

        mBtnSubmit = (RotateInButton) findViewById(R.id.button_submit);
        mBtnSubmit.setOnClickListener(mSubmitOnClickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();
        hideNofityIfNeed();
    }

    @Override
    protected void onStop() {
        showNotifyIfNeed();
        super.onStop();
    }

    private void initPicImageParent() {
        for (int index = 0; index < mPicImageParent.getChildCount(); index++) {
            final int finalIndex = index;
            mPicImageParent.getChildAt(index).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentAddPic = finalIndex;
                    PermissionGen.needPermission(LogSettingActivity.this, REQUEST_CODE_PERMISSION_ACCESS_FILE,
                            Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            });
            mPicImageList.add(null);
        }
    }

    private void showNotifyIfNeed() {
        mShowNotifyTask = AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                return sMapPropOn.get(PERSIST_SYS_YOTA_LOG).equals(SystemProperties.get(PERSIST_SYS_YOTA_LOG, "true"));
            }

            @Override
            protected void onPostExecute(Boolean result) {
                logger.debug("Show notify need: " + result);
                if (null != result && result) {
                    NotificationShow.startLogRecording(LogSettingActivity.this);
                }
                mShowNotifyTask = null;
            }
        });
    }

    private void hideNofityIfNeed() {
        if (null != mShowNotifyTask) mShowNotifyTask.cancel(false);
        NotificationShow.cancelLogRecording(LogSettingActivity.this);
    }

    @Override
    protected void onDestroy() {
        cancelAllTasks();
        super.onDestroy();
    }

    private void cancelAllTasks() {
        for (AsyncTask<?, ?, ?> task : mTasks) {
            task.cancel(false);
        }
        mTasks.clear();
    }

    private class InitSwitchTask extends PropGetTask {
        public InitSwitchTask(Switch switchButton, String prop) {
            super(switchButton, prop);
        }

        @Override
        protected void onPostExecute(Boolean checked) {
            super.onPostExecute(checked);
            final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked && mMutualProp.containsKey(mProp)) {
                        final Iterator<String> iterator = mMutualProp.keySet().iterator();
                        while (iterator.hasNext()) {
                            final String prop = iterator.next();
                            final Switch aSwitch = mMutualProp.get(prop);
                            if (!prop.equals(mProp) && isChecked == aSwitch.isChecked()) {
                                aSwitch.setOnCheckedChangeListener(null);
                                aSwitch.setChecked(!isChecked);
                                aSwitch.setOnCheckedChangeListener((CompoundButton.OnCheckedChangeListener) aSwitch.getTag());
                                new PropSetTask(aSwitch, prop, false).execute(!isChecked);
                            }
                        }
                    }
                    mTasks.add(new PropSetTask(mSwitchButton, mProp).execute(isChecked));
                }
            };
            mSwitchButton.setOnCheckedChangeListener(listener);
            mSwitchButton.setTag(listener);
        }
    }

    private class PropGetTask extends AsyncTask<Void, Void, Boolean> {

        protected final Switch mSwitchButton;
        protected final String mProp;

        public PropGetTask(Switch switchButton, String prop) {
            this.mSwitchButton = switchButton;
            this.mProp = prop;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            String propValue = sMapPropOn.get(mProp);
            propValue = null != propValue ? propValue : "1";
            return propValue.equals(SystemProperties.get(mProp, "0"));
        }

        @Override
        protected void onPostExecute(Boolean checked) {
            mTasks.remove(this);
            mSwitchButton.setChecked(checked);
            logger.debug("Get prop:" + mProp + " " + checked);
        }
    }

    private class PropSetTask extends AsyncTask<Boolean, Void, Void> {

        private final Switch mSwitchButton;
        private final String mProp;
        private final boolean mPrompt;

        public PropSetTask(Switch switchButton, String prop) {
            this(switchButton, prop, true);
        }

        public PropSetTask(Switch switchButton, String prop, boolean prompt) {
            this.mSwitchButton = switchButton;
            this.mProp = prop;
            this.mPrompt = prompt;
        }

        @Override
        protected void onPreExecute() {
            mSwitchButton.setEnabled(false);
        }

        @Override
        protected Void doInBackground(Boolean... params) {
            final Boolean onoff = params[0];
            String propValue;
            if (onoff) {
                propValue = sMapPropOn.get(mProp);
                propValue = null != propValue ? propValue : "1";
            } else {
                propValue = sMapPropOff.get(mProp);
                propValue = null != propValue ? propValue : "0";
            }
            SystemProperties.set(mProp, propValue);
            waitForFinish(mProp, onoff);
            return null;
        }

        private void waitForFinish(String prop, Boolean onoff) {
            final String svc = sMapLogStatus.get(prop);
            if (null != svc) {
                String status = onoff ? "running" : "stopped";
                int maxTime = 4;
                while (maxTime-- > 0 && !status.equals(SystemProperties.get(svc, null))) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                logger.info("Svc[" + svc + "] status: " + SystemProperties.get(svc, null) + " after set prop: " + prop);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mTasks.remove(this);
            mSwitchButton.setEnabled(true);
            logger.debug("Set prop:" + mProp + " " + mSwitchButton.isChecked());
            if (mPrompt) {
//                Toast.makeText(LogSettingActivity.this, mSwitchButton.getText() + getString(R.string.prop_setting_success), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void submitBugReport() {
//        if (checkBugDesc(false) && checkUserContacts()) {
        doRealSubmitBugReport();
//        }
    }
    private void doRealSubmitBugReport() {

        AsyncTaskCompat.executeParallel(new AsyncTask<Object, Object, Bundle>() {
            @Override
            protected void onPreExecute() {
                mBtnSubmit.setInProgress((Activity)getContext(), true);
            }

            @Override
            protected Bundle doInBackground(Object... params) {
//                return getLoginAccountStatus();
                return new Bundle();
            }

            @Override
            protected void onPostExecute(Bundle result) {
                if (null != result) {
                    if (TextUtils.isEmpty(mEditContacts.getText().toString())) {
                        mEditContacts.setText("10086");
                    }
                    fireServiceForSubmit();
//                    Toast.makeText(LogSettingActivity.this, R.string.report_save_ok, Toast.LENGTH_LONG).show();
                    finish();
                } else if (!isDestroyed()) {
                    mBtnSubmit.setInProgress(LogSettingActivity.this, false);
                }
            }
        });

    }

    /**
     * 压缩 并 提交到服务器
     */
    private void fireServiceForSubmit() {
        Intent serviceIntent = new Intent(getContext(), CompressAppendixService.class);

//        serviceIntent.putExtra(CompressAppendixService.EVENT_CONTENT, mEditBugDetails.getText().toString());

        serviceIntent.putExtra(CompressAppendixService.USER_CONTACT, mEditContacts.getText().toString());

//        serviceIntent.putExtra(CompressAppendixService.PIC_IMAGE_LIST, mPicImageList.toArray(new String[mPicImageList.size()]));
//        serviceIntent.putExtra(CompressAppendixService.DELETE_UPLOAD_FILES, ((CheckBox) findViewById(R.id.check_delete_upload_files)).isChecked());
        startService(serviceIntent);
    }

    @PermissionSuccess(requestCode = REQUEST_CODE_PERMISSION_ACCESS_FILE)
    private void failToAccessFile() {
    }

    private static final int REQUEST_CODE_ACTIVITY_PICK_PHOTO = 103;

    @PermissionSuccess(requestCode = REQUEST_CODE_PERMISSION_ACCESS_FILE)
    private void addTestPhoto() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_ACTIVITY_PICK_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CODE_ACTIVITY_PICK_PHOTO == requestCode) {
            if (Activity.RESULT_OK == resultCode && null != data) {
                final String path = getPath(data.getData());
                if (null != path && mPicImageList.contains(path)) {
                    Toast.makeText(this, R.string.please_not_reset_pic, Toast.LENGTH_SHORT).show();
                } else {
                    AsyncTaskCompat.executeParallel(new DecodeImageTask(new File(path)));
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private String getPath(Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(this, uri)) {
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
        Cursor cursor = getContentResolver().query(uri, projection, selection, selectionArgs, null);
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

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public void onBackPressed() {
        if (!checkBugDesc(true) && !checkAttachmentExist()) {
            super.onBackPressed();
        } else {
            new AlertDialog.Builder(this).setMessage(R.string.abandon_current_edit_content)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LogSettingActivity.super.onBackPressed();
                        }
                    }).show();
        }
    }
    private boolean checkUserContacts() {
        final String contact = mEditContacts.getText().toString();
        final boolean result = TextUtils.isEmpty(contact) || AccountNameMask.checkMobile(contact) || AccountNameMask.checkEmail(contact);
        if (!result) {
            Toast.makeText(this, R.string.please_input_valid_contacts, Toast.LENGTH_SHORT).show();
        }
        return result;
    }

    private boolean checkBugDesc(boolean backKeyPressed) {
        boolean result = !TextUtils.isEmpty(mEditBugDetails.getText().toString());
        if (!result && !backKeyPressed) {
            Toast.makeText(this, R.string.please_input_bug_desc, Toast.LENGTH_SHORT).show();
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
                        logger.debug("photoUri:" + photoUri);
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
                        mImageView.setImageResource(R.drawable.ic_add_selector);
                        mImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mCurrentAddPic = mOpImageIndex;
                                PermissionGen.needPermission((Activity)getContext(), REQUEST_CODE_PERMISSION_ACCESS_FILE,
                                        Manifest.permission.READ_EXTERNAL_STORAGE);
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

    public static Intent getLaunchIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        ComponentName compMain = new ComponentName(context.getPackageName(),
                LogSettingActivity.class.getName());
        intent.setComponent(compMain);
        return intent;
    }

}
