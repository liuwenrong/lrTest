package com.coolyota.logreport;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.os.AsyncTaskCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.coolyota.logreport.base.CloudBaseActivity;
import com.coolyota.logreport.tools.AccountNameMask;
import com.coolyota.logreport.tools.CompressAppendixService;
import com.coolyota.logreport.tools.ImageDecoder;
import com.coolyota.logreport.tools.permissiongen.PermissionFail;
import com.coolyota.logreport.tools.permissiongen.PermissionGen;
import com.coolyota.logreport.tools.permissiongen.PermissionSuccess;
import com.coolyota.logreport.ui.RotateInButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class BugReportActivity extends CloudBaseActivity implements View.OnClickListener, View.OnFocusChangeListener {
    public static final String FILE_PATH = "file_path";
    public static final String LOG_FILE_PATH = "log_file_path";
    public static final String ACTION_SCREEN_VIDEO_FILE_OK = "journeyui.intent.action.SCREEN_VIDEO_OK";
    public static final String ACTION_FOR_OFFLINE_LOG_OVER = "journeyui.intent.action.OFFLINE_LOG_OK";
    private static final int MENU_ITEM_ID_OFFLINE_LOG = Menu.FIRST + 100;
    private static final int MENU_ITEM_ID_MY_FEEDBACK = Menu.FIRST + 101;
    private static final int MENU_ITEM_ID_SCREEN_VIDEO = Menu.FIRST + 102;
    private static final int MENU_ITEM_ID_MY_AWARD = Menu.FIRST + 103;
    private static final int MENU_ITEM_ID_LOGOUT = Menu.FIRST + 104;
    private static final int MENU_ITEM_ID_GPS_CONTROL = Menu.FIRST + 105;
    private static final int REQUEST_CODE_ACTIVITY_LOG_SETTING = 101;
    private static final int REQUEST_CODE_ACTIVITY_SCREEN_VIDEO = 102;
    private static final int REQUEST_CODE_ACTIVITY_PICK_PHOTO = 103;
    private static final int REQUEST_CODE_PERMISSION_ACCESS_FILE = 201;
    private static final int REQUEST_CODE_PERMISSION_READ_PHONE_STATE = 203;
    //    private EditText mEditBugTitle;
    //    private CheckBox mCheckMustHappen;
//    private EditText mEditDate;
//    private EditText mEditTime;

    private Spinner mSpinnerBugType;
    private EditText mEditBugDetails;
    private ViewGroup mPicImageParent;
    private ViewGroup mDeletePicParent;
    private EditText mEditContacts;
    private RotateInButton mBtnSubmit;
    private int mCurrentAddPic;
    private List<String> mPicImageList = new ArrayList<>();
    private String mAccountID;
    private TextView mTextFilePath;
    //    private TextView mTextLogFilePath;
    private Calendar mBugCalendar;
    private BroadcastReceiver mBetaReportReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            logger.debug("Rec intent: " + intent);
            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(BugReportActivity.this);
            if (ACTION_SCREEN_VIDEO_FILE_OK.equals(intent.getAction())) {
                mTextFilePath.setText(preferences.getString(FILE_PATH, ""));
                View deleteButton = findViewById(R.id.button_delete_file);
                if (preferences.contains(FILE_PATH)) {
                    deleteButton.setVisibility(View.VISIBLE);
                    deleteButton.setOnClickListener(BugReportActivity.this);
                }
            } else if (ACTION_FOR_OFFLINE_LOG_OVER.equals(intent.getAction())) {
//                mTextLogFilePath.setText(preferences.getString(LOG_FILE_PATH, ""));
//                View deleteButton = findViewById(R.id.button_delete_log);
                if (preferences.contains(LOG_FILE_PATH)) {
//                    deleteButton.setVisibility(View.VISIBLE);
//                    deleteButton.setOnClickListener(BugReportActivity.this);
                }
            }
        }
    };
    private ReportFileMonitorConnection mReportFileMonitorConnection;

    public static Intent getLaunchIntent(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        ComponentName compMain = new ComponentName(context.getPackageName(),
                LogSettingActivity.class.getName());
        intent.setComponent(compMain);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setContentView(R.layout.activity_bug_report);
        setTitle(getTitle() + getVersionName());

        mSpinnerBugType = (Spinner) findViewById(R.id.spinner_bug_type);
        mSpinnerBugType.setSelection(mSpinnerBugType.getFirstVisiblePosition());
        mEditBugDetails = (EditText) findViewById(R.id.edit_bug_details);

        mBugCalendar = Calendar.getInstance();
//        mEditDate = (EditText) findViewById(R.id.edit_bug_date);
/*        mEditDate.setKeyListener(null);
        mEditDate.setOnFocusChangeListener(this);
        mEditDate.setText(String.format("%04d-%02d-%02d", mBugCalendar.get(Calendar.YEAR), (mBugCalendar.get(Calendar.MONTH) + 1),
                mBugCalendar.get(Calendar.DAY_OF_MONTH)));*/
//        mEditTime = (EditText) findViewById(R.id.edit_bug_time);
/*        mEditTime.setKeyListener(null);
        mEditTime.setOnFocusChangeListener(this);
        mEditTime.setText(String.format("%02d:%02d", mBugCalendar.get(Calendar.HOUR_OF_DAY), mBugCalendar.get(Calendar.MINUTE)));*/

        mPicImageParent = (ViewGroup) findViewById(R.id.parent_add_pic);
        initPicImageParent();
        mDeletePicParent = (ViewGroup) findViewById(R.id.parent_delete_pic);
        mEditContacts = (EditText) findViewById(R.id.edit_phone_number);

        mTextFilePath = (TextView) findViewById(R.id.text_file_path);
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mTextFilePath.setText(preferences.getString(FILE_PATH, ""));
        View deleteButton = findViewById(R.id.button_delete_file);
        if (preferences.contains(FILE_PATH)) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(this);
        }

//        mTextLogFilePath = (TextView) findViewById(R.id.text_log_record_path);
//        mTextLogFilePath.setText(preferences.getString(LOG_FILE_PATH, ""));
//        deleteButton = findViewById(R.id.button_delete_log);
        if (preferences.contains(LOG_FILE_PATH)) {
//            deleteButton.setVisibility(View.VISIBLE);
//            deleteButton.setOnClickListener(this);
        }

        mBtnSubmit = (RotateInButton) findViewById(R.id.button_submit);
        mBtnSubmit.setOnClickListener(this);

        listenVideoLogBroadcast();
    }

    /**
     * 获取版本号
     *
     * @return 当前应用的版本号
     */
    private String getVersionName() {
        try {
            PackageManager manager = this.getPackageManager();
            PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
            return getString(R.string.version_name_format, info.versionName);
        } catch (Exception e) {
            e.printStackTrace();
            return getString(R.string.version_name_format, "1.0");
        }
    }

    private void listenVideoLogBroadcast() {
//        final IntentFilter filter = new IntentFilter();
//        filter.addAction(ACTION_FOR_OFFLINE_LOG_OVER);
//        filter.addAction(ACTION_SCREEN_VIDEO_FILE_OK);
//        registerReceiver(mBetaReportReceiver, filter);
//
//        mReportFileMonitorConnection = new ReportFileMonitorConnection();
//        bindService(new Intent(this, ReportFileMonitorService.class), mReportFileMonitorConnection, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
//        mReportFileMonitorConnection.unregister();
//        unbindService(mReportFileMonitorConnection);
//        unlistenVideoLogBroadcast();
        super.onDestroy();
    }

    private void unlistenVideoLogBroadcast() {
        unregisterReceiver(mBetaReportReceiver);
    }

    private void initPicImageParent() {
        for (int index = 0; index < mPicImageParent.getChildCount(); index++) {
            final int finalIndex = index;
            mPicImageParent.getChildAt(index).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCurrentAddPic = finalIndex;
                    PermissionGen.needPermission(BugReportActivity.this, REQUEST_CODE_PERMISSION_ACCESS_FILE,
                            Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            });
            mPicImageList.add(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, MENU_ITEM_ID_OFFLINE_LOG, 0, R.string.offline_log);
       /* menu.add(Menu.NONE, MENU_ITEM_ID_SCREEN_VIDEO, 1, R.string.screen_video);
        menu.add(Menu.NONE, MENU_ITEM_ID_MY_FEEDBACK, 2, R.string.my_feedback);
        menu.add(Menu.NONE, MENU_ITEM_ID_MY_AWARD, 3, R.string.my_award);
        menu.add(Menu.NONE, MENU_ITEM_ID_LOGOUT, 4, R.string.logout);
        menu.add(Menu.NONE, MENU_ITEM_ID_GPS_CONTROL, 5, "");*/
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
/*        if (isServiceWork(GPSPersistenceService.class.getName())) {
            menu.getItem(5).setTitle(R.string.stop_gps_persistence);
        } else {
            menu.getItem(5).setTitle(R.string.restart_gps_persistence);
        }*/
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (MENU_ITEM_ID_OFFLINE_LOG == item.getItemId()) {
            startActivityForResult(new Intent(this, LogSettingActivity.class), REQUEST_CODE_ACTIVITY_LOG_SETTING);
            //startActivityForResult(new Intent("journeyui.intent.action.VOC_LOG_SETTING"), REQUEST_CODE_ACTIVITY_LOG_SETTING);
            return true;
        }/* else if (MENU_ITEM_ID_SCREEN_VIDEO == item.getItemId()) {
            startActivityForResult(new Intent(this, ScreenRecordActivity.class), REQUEST_CODE_ACTIVITY_SCREEN_VIDEO);
            //startActivityForResult(new Intent(ScreenRecordActivity.ACTION_VOC_VIDEO_RECORD), REQUEST_CODE_ACTIVITY_SCREEN_VIDEO);
            return true;
        } else if (MENU_ITEM_ID_MY_AWARD == item.getItemId()) {
            startActivity(new Intent(this, MyAwardActivity.class));
        } else if (MENU_ITEM_ID_MY_FEEDBACK == item.getItemId()) {
            startActivity(new Intent(this, ReportListActivity.class));
        } else if (MENU_ITEM_ID_LOGOUT == item.getItemId()) {
            logoutCurrentAccount();
        } else if (MENU_ITEM_ID_GPS_CONTROL == item.getItemId()) {
            switchGpsPersistence();
        }*/
        return super.onOptionsItemSelected(item);
    }

    private void switchGpsPersistence() {
       /* if (!isServiceWork(GPSPersistenceService.class.getName())) {
            //GpsEnforceJobService.cacelJobServiceDelay(this);
            startService(new Intent(this, GPSPersistenceService.class));
        } else {
            stopService(new Intent(this, GPSPersistenceService.class));
            //GpsEnforceJobService.startJobServiceByDelay(this, 60 * 5);
            //Toast.makeText(this, R.string.restart_gps_persistence_after_five_minues, Toast.LENGTH_LONG).show();
        }*/
    }

    private boolean isServiceWork(String serviceName) {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (runningServices.size() <= 0) {
            return false;
        }
        for (int i = 0; i < runningServices.size(); i++) {
            String name = runningServices.get(i).service.getClassName().toString();
            if (name.equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    private void logoutCurrentAccount() {
        AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                Bundle loginStatus = getLoginAccountStatus();
                if (null == loginStatus) {
                    return false;
                }
                doLogout();
                return null;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (null != result && !result) {
                    Toast.makeText(BugReportActivity.this, R.string.not_login, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(BugReportActivity.this, R.string.logout_successfully, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void doLogout() {
       /* Bundle input = new Bundle();
        // 指定启动界面的屏幕方向
        input.putInt(Constants.KEY_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Coolcloud2 coolCloud = Coolcloud2.get(BugReportActivity.this, CloudApplication.APP_ID);
        ResultFuture<Bundle> future = coolCloud.logout(BugReportActivity.this, input, null, null);
        // 等待结果的返回，接口为阻塞式的，必须在线程中调用，否则可能导致UI线程ANR
        future.getResult();*/
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_submit:
                submitBugReport();
                break;
            case R.id.button_delete_file:
                confirmDeleteUpload(getString(R.string.confirm_cancel_upload_video), mTextFilePath, FILE_PATH, v.getId());
                break;
            /*case R.id.button_delete_log:
                confirmDeleteUpload(getString(R.string.confirm_cancel_upload_log), mTextLogFilePath, LOG_FILE_PATH, v.getId());
                break;*/
        }
    }

    private void confirmDeleteUpload(String message, final TextView textView, final String prfName, final int buttonId) {
        new AlertDialog.Builder(this).setMessage(message).setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        textView.setText("");
                        PreferenceManager.getDefaultSharedPreferences(BugReportActivity.this).edit().remove(prfName).apply();
                        findViewById(buttonId).setVisibility(View.GONE);
                    }
                }).show();
    }

    private void submitBugReport() {
//        if (checkBugDesc(false) && checkUserContacts()) {
        doRealSubmitBugReport();
//        }
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

    private void doRealSubmitBugReport() {
        checkLoginAccountTKT();
    }

    private void checkLoginAccountTKT() {
        AsyncTaskCompat.executeParallel(new AsyncTask<Object, Object, Bundle>() {
            @Override
            protected void onPreExecute() {
                mBtnSubmit.setInProgress(BugReportActivity.this, true);
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
//                    Toast.makeText(BugReportActivity.this, R.string.report_save_ok, Toast.LENGTH_LONG).show();
                    finish();
                } else if (!isDestroyed()) {
                    mBtnSubmit.setInProgress(BugReportActivity.this, false);
                }
            }
        });
    }

    @Nullable
    private Bundle getLoginAccountStatus() {
/*        Coolcloud2 coolCloud = Coolcloud2.get(BugReportActivity.this, CloudApplication.APP_ID);
        // 调用接口，这里不设置回调接口，使用getResult的方式
        ResultFuture<Bundle> future = coolCloud.getDefaultAccount(BugReportActivity.this, null, null, null);
        // 等待返回结果，该接口必须在线程中调用，否则可能导致UI线程ANR
        Bundle result = future.getResult();
        if (null != result) {
            future = coolCloud.getTKTCached(BugReportActivity.this, null, null, null);
            result = future.getResult(30, TimeUnit.SECONDS);
            if (null != result) {
                Bundle input = new Bundle();
                // 设置校验类型
                input.putString(Params.KEY_AUTHENTICATE_TYPE, Params.AUTHENTICATE_TYPE_TKT);
                input.putString(Params.KEY_UID, result.getString(Params.KEY_UID));
                input.putString(Params.KEY_TKT, result.getString(Params.KEY_TKT));
                input.putString(Params.KEY_APP_ID, result.getString(Params.KEY_APP_ID));

                // 调用校验接口，这里不设置回调接口，使用getResult的方式
                future = coolCloud.authenticate(BugReportActivity.this, input, null, null);
                if (null == future.getResult()) {
                    return null;
                }
            }
        }
        return result;*/
        return null;

    }

    /**
     * 提交到服务器
     */
    private void fireServiceForSubmit() {
        Intent serviceIntent = new Intent(BugReportActivity.this, CompressAppendixService.class);

//        serviceIntent.putExtra(CompressAppendixService.EVENT_CONTENT, mEditBugDetails.getText().toString());

        serviceIntent.putExtra(CompressAppendixService.USER_CONTACT, mEditContacts.getText().toString());

//        serviceIntent.putExtra(CompressAppendixService.PIC_IMAGE_LIST, mPicImageList.toArray(new String[mPicImageList.size()]));
//        serviceIntent.putExtra(CompressAppendixService.DELETE_UPLOAD_FILES, ((CheckBox) findViewById(R.id.check_delete_upload_files)).isChecked());
        startService(serviceIntent);
    }

    @PermissionFail(requestCode = REQUEST_CODE_PERMISSION_READ_PHONE_STATE)
    private void notPermittedLoginAccount() {
        Toast.makeText(this, R.string.not_permitted_login, Toast.LENGTH_SHORT).show();
    }

    @PermissionSuccess(requestCode = REQUEST_CODE_PERMISSION_READ_PHONE_STATE)
    private void gotoLoginAccount() {
/*        AsyncTaskCompat.executeParallel(new AsyncTask<Void, Void, Bundle>() {
            @Override
            protected void onPreExecute() {
                mBtnSubmit.setInProgress(BugReportActivity.this, true);
            }

            @Override
            protected Bundle doInBackground(Void... params) {
                Bundle input = new Bundle();
                // 设置横屏显示
                input.putInt(Constants.KEY_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                // 设置登录方式，这里采用新建账户登录
                input.putString(Constants.KEY_LOGIN_TYPE, Constants.LOGIN_TYPE_SYSTEM_ACCOUNT);

                // 调用登录接口，这里不设置回调接口，使用getResult的方式
                Coolcloud2 coolCloud = Coolcloud2.get(BugReportActivity.this, CloudApplication.APP_ID);
                ResultFuture<Bundle> future = coolCloud.login(BugReportActivity.this, input, null, null);

                // 等待登录返回，该接口必须在线程中调用，否则可能导致UI线程ANR
                Bundle result = future.getResult();
                return result;
            }

            @Override
            protected void onPostExecute(Bundle result) {
                if (null != result) {
                    if (mEditContacts.length() == 0) {
                        mEditContacts.setText(result.getString(Params.KEY_ACCOUNT));
                    }
                    mAccountID = result.getString(Params.KEY_UID);
                    fireServiceForSubmit();
                    Toast.makeText(BugReportActivity.this, R.string.report_save_ok, Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    mBtnSubmit.setInProgress(BugReportActivity.this, false);
                    Toast.makeText(BugReportActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                }
            }
        });*/
    }

    @PermissionSuccess(requestCode = REQUEST_CODE_PERMISSION_ACCESS_FILE)
    private void failToAccessFile() {
    }

    @PermissionSuccess(requestCode = REQUEST_CODE_PERMISSION_ACCESS_FILE)
    private void addTestPhoto() {
        /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image*//*");
        intent.setPackage("com.journeyui.gallery3d");*/
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_ACTIVITY_PICK_PHOTO);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            /*case R.id.edit_bug_date:
                if (!hasFocus) break;
                DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        mEditDate.setText(String.format("%04d-%02d-%02d", year, (month + 1), dayOfMonth));
                        mBugCalendar.set(year, month, dayOfMonth);
                    }
                }, mBugCalendar.get(Calendar.YEAR), mBugCalendar.get(Calendar.MONTH), mBugCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        findViewById(R.id.focus_view).requestFocus();
                        //mEditDate.clearFocus();
                    }
                });
                try {
                    datePickerDialog.show();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                break;*/
            /*case R.id.edit_bug_time:
                if (!hasFocus) break;
                TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        mEditTime.setText(String.format("%02d:%02d", hourOfDay, minute));
                        mBugCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        mBugCalendar.set(Calendar.MINUTE, minute);
                    }
                }, mBugCalendar.get(Calendar.HOUR_OF_DAY), mBugCalendar.get(Calendar.MINUTE), true);
                timePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        findViewById(R.id.focus_view).requestFocus();
                        //mEditTime.clearFocus();
                    }
                });
                try {
                    timePickerDialog.show();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                break;*/
        }
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
                            BugReportActivity.super.onBackPressed();
                        }
                    }).show();
        }
    }

    private boolean checkAttachmentExist() {
        return /*!TextUtils.isEmpty(mEditBugTitle.getText().toString()) || */!checkPicListEmpty()
                || !TextUtils.isEmpty(mTextFilePath.getText().toString()) /*|| !TextUtils.isEmpty(mTextLogFilePath.getText().toString())*/;
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    private class ReportFileMonitorConnection implements ServiceConnection {
//        private IReportFileMonitor mReportFileMonitor;
//        private IOnReportFileCreatedListener.Stub mOnReportFileCreatedListener;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            /*mReportFileMonitor = IReportFileMonitor.Stub.asInterface(service);
            mOnReportFileCreatedListener = new IOnReportFileCreatedListener.Stub() {
                @Override
                public void onLogZipCreated(Uri log) throws RemoteException {
                }

                @Override
                public void onVideoFileCreated(final Uri videoUri) throws RemoteException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextFilePath.setText(
                                    PreferenceManager.getDefaultSharedPreferences(BugReportActivity.this).getString(FILE_PATH, ""));
                            View deleteButton = findViewById(R.id.button_delete_video);
                            deleteButton.setVisibility(View.VISIBLE);
                            deleteButton.setOnClickListener(BugReportActivity.this);
                        }
                    });
                }
            };
            try {
                mReportFileMonitor.registerReportFileListener(mOnReportFileCreatedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }*/
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        public void unregister() {
           /* try {
                mReportFileMonitor.unregisterReportFileListener(mOnReportFileCreatedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }*/
        }
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
                        Uri photoUri = FileProvider.getUriForFile(BugReportActivity.this, "com.coolyota.logreport.fileprovider", mImagePicFile);
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
                                PermissionGen.needPermission(BugReportActivity.this, REQUEST_CODE_PERMISSION_ACCESS_FILE,
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
}
