package com.coolyota.logreport.base;

import android.support.v4.app.Fragment;
import android.text.TextUtils;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/9/1
 */
public abstract class BaseFragment extends Fragment{

    private String mTabName;
    protected int tabNameResId;

  /*  public BaseFragment(String mTabName) {
        super();
        this.mTabName = mTabName;
    }*/

    public abstract int getTabNameResId();

    public BaseFragment setTabName(String tabName) {
        mTabName = tabName;
        return this;
    }

    protected void setTabName(int resId) {
        mTabName = getContext().getResources().getString(resId);
    }

    public String getTabName() {

        if (TextUtils.isEmpty(mTabName)) {

            return getResources().getResourceName(getTabNameResId());
        }

        return mTabName;
    }

    protected BaseActivity getBaseActivity() {

        if (getActivity() == null) {
            return null;
        }

        if (getActivity() instanceof BaseActivity){
            return (BaseActivity)getActivity();
        }
        return null;
    }

}
