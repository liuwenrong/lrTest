package com.coolyota.logreport.ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.coolyota.logreport.R;

/**
 * Created by liuwenrong on 2017/6/02.
 */
public class RotateInButton extends RelativeLayout {
    private static final String TAG = RotateInButton.class.getSimpleName();
    private CharSequence mText;
    private TextView mTextView;
    private ProgressBar mProgressBar;
    private boolean mInProgress;
    private View mShelter;

    public RotateInButton(Context context) {
        this(context, null);
    }

    public RotateInButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RotateInButton(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RotateInButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initChildrenView(context, attrs, defStyleAttr, defStyleRes);
        initFromAttributes(context, attrs, defStyleAttr, defStyleRes);
    }

    private void initChildrenView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mTextView = new TextView(context);
        mTextView.setTextAppearance(R.style.SubmitTextCommonStyle);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mTextView.setLayoutParams(params);
        mTextView.setId(R.id.rotate_in_button_text_id);
        addView(mTextView);

        mProgressBar = new ProgressBar(context);
        mProgressBar.setIndeterminate(true);
        int size = getResources().getDimensionPixelSize(R.dimen.button_progress_size);
        params = new RelativeLayout.LayoutParams(size, size);
        params.addRule(RelativeLayout.CENTER_VERTICAL);
        params.addRule(RelativeLayout.END_OF, mTextView.getId());
        params.setMarginStart(30);
        mProgressBar.setLayoutParams(params);
        addView(mProgressBar);

        mProgressBar.setVisibility(View.GONE);
    }

    public void setText(String text) {
        mText = text;
        mTextView.setText(text);
    }

    private void initFromAttributes(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.RotateInButton, defStyleAttr, defStyleRes);
        int textStyle = a.getResourceId(R.styleable.RotateInButton_textStyle, -1);
        if (-1 != textStyle) {
            mTextView.setTextAppearance(textStyle);
        }
        mText = a.getText(R.styleable.RotateInButton_text);
        mTextView.setText(mText);
        boolean enabled = a.getBoolean(R.styleable.RotateInButton_enabled, false);
        setEnabled(enabled);
        mTextView.setEnabled(enabled);
        a.recycle();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        mTextView.setEnabled(enabled);
    }

    public boolean isInProgress() {
        return mInProgress;
    }

    public void setInProgress(Activity host, boolean inProgress) {
        if (mInProgress == inProgress) return;
        this.mInProgress = inProgress;
        if (inProgress) requestFocus();
        mProgressBar.setVisibility(mInProgress ? View.VISIBLE : View.GONE);
        setEnabled(!mInProgress);
        mTextView.setEnabled(!mInProgress);
        showOrHideCover(host, inProgress);
    }

    private void showOrHideCover(Activity host, boolean inProgress) {
        if (null == mShelter) {
            mShelter = new View(getContext());
            mShelter.setOnClickListener(new OnClickListener() {
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
}
