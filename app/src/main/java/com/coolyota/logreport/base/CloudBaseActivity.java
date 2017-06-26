package com.coolyota.logreport.base;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.coolyota.logreport.R;
import com.coolyota.logreport.tools.log.CYLog;

/**
 * Created by zeusis on 2016/7/23.
 */
public abstract class CloudBaseActivity extends AppCompatActivity{
    private final static int[] sSystemBarColors = {R.color.color_FF69C7F9, R.color.color_FF3BB3F5, R.color.color_FF2E9BE5, R.color.color_FF147DCD,
            R.color.color_FF0065B3, R.color.color_FF005799};
    protected final CYLog logger = new CYLog(getClass().getSimpleName());
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (null != getSupportActionBar()) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        /*((CloudApplication)getApplicationContext()).addNumActivities();
        if (isNeedUpdateSystemBarColor() && null != getSupportActionBar()) {
            updateSystemBarColor();
        }*/
    }

    private void updateSystemBarColor() {
       /* int levelOfActivities = ((CloudApplication) getApplicationContext()).getNumActivities();
        if (levelOfActivities > sSystemBarColors.length){
            levelOfActivities = sSystemBarColors.length;
        }
        final int color = getResources().getColor(sSystemBarColors[levelOfActivities - 1], null);
        getWindow().setStatusBarColor(color);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(color));*/
    }

    protected boolean isNeedUpdateSystemBarColor() {
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*((CloudApplication)getApplicationContext()).removeNumActivities();
        mNetworkRequestManager.finishAll();
        mNetworkRequestManager.release();*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showProgressDialog(final int requestCode, String title, String message) {
        this.showProgressDialog(requestCode, title, message, true);
    }

    public void showProgressDialog(final int requestCode, String title, String message, boolean canceled) {
        if (null != mProgressDialog) mProgressDialog.dismiss();
        mProgressDialog = ProgressDialog.show(this, title, message, true, true, new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
//                mNetworkRequestManager.cancel(requestCode);
            }
        });
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.setCancelable(canceled);
    }

    public void dismissProgressDialog() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

}
