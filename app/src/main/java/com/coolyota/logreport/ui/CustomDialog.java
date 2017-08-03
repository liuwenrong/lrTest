package com.coolyota.logreport.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.coolyota.logreport.R;

/**
 * des:
 *
 * @author liuwenrong
 * @version 1.0, 2017/7/24
 */
public class CustomDialog extends Dialog {
    public CustomDialog(@NonNull Context context) {
        super(context);
    }

    public CustomDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    protected CustomDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {

        private Context context;
        private String title;
        private String message;
        private String positiveButtonText;
        private String negativeButtonText;
        private View contentView;

        private DialogInterface.OnClickListener positiveButtonClickListener, negativeButtonClickListener;
        public CustomDialog customDialog;
        public TextView titleTv;
        public Button posBtn;
        public TextView msgTv;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Set the Dialog message from String
         *
         * @param message
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            if (customDialog != null && msgTv != null) {
                msgTv.setText(message);
            }
            return this;
        }

        /**
         * Set the Dialog message from resource
         *
         * @param message
         * @return
         */
        /*public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }*/

        /**
         * Set the Dialog title from resource
         *
         * @param title
         * @return
         */
        /*public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }*/

        /**
         * Set the Dialog title from String
         *
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            if (customDialog != null && titleTv != null) {
                titleTv.setText(title);
            }
            return this;
        }

        /**
         * Set a custom content view for the Dialog.
         * If a message is set, the contentView is not
         * added to the Dialog...
         *
         * @param v
         * @return
         */
        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         *
         * @param positiveButtonText
         * @param listener
         * @return
         */
        /*public Builder setPositiveButton(int positiveButtonText, DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context.getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }*/

        /**
         * Set the positive button text and it's listener
         *
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(String positiveButtonText, DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            if (customDialog != null && posBtn != null) {
                posBtn.setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    posBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            customDialog.dismiss();
                            positiveButtonClickListener.onClick(customDialog, DialogInterface.BUTTON_POSITIVE);
                        }
                    });
                }
            }
            return this;
        }

        /**
         * Set the negative button resource and it's listener
         *
         * @param negativeButtonText
         * @param listener
         * @return
         */
        /*public Builder setNegativeButton(int negativeButtonText, DialogInterface.OnClickListener listener) {
            this.negativeButtonText = (String) context.getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }*/

        /**
         * Set the negative button text and it's listener
         *
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(String negativeButtonText, DialogInterface.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;

            return this;
        }

        /**
         * Create the custom dialog
         */
        public CustomDialog create() {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            customDialog = new CustomDialog(context);
//            customDialog = new CustomDialog(context, R.style.Dialog);
            customDialog.setCanceledOnTouchOutside(false);
            View layout = inflater.inflate(R.layout.custom_dialog_layout, null);
//            customDialog.setContentView(layout);
            customDialog.addContentView(layout, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            // set the dialog title
            titleTv = ((TextView) layout.findViewById(R.id.title));
            titleTv.setText(title);
            // set the confirm button
//            if (positiveButtonText != null) {
            posBtn = ((Button) layout.findViewById(R.id.positiveButton));
            posBtn.setText(positiveButtonText);
                if (positiveButtonClickListener != null) {
                    posBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            customDialog.dismiss();
                            positiveButtonClickListener.onClick(customDialog, DialogInterface.BUTTON_POSITIVE);
                        }
                    });
                }
           /* } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.positiveButton).setVisibility(View.GONE);
            }*/

            // set the cancel button
            /*if (negativeButtonText != null) {
                ((Button) layout.findViewById(R.id.negativeButton)).setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    ((Button) layout.findViewById(R.id.negativeButton)).setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            negativeButtonClickListener.onClick(Dialog, DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                layout.findViewById(R.id.negativeButton).setVisibility(View.GONE);
            }*/

            // set the content message
            if (message != null) {
                msgTv = ((TextView) layout.findViewById(R.id.message));
                msgTv.setText(message);
            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.content)).addView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                customDialog.setContentView(layout);
            }
            customDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);  //设置对话框背景透明 ，对于AlertDialog 就不管用了

            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            int displayWidth = dm.widthPixels;
            int displayHeight = dm.heightPixels;
            android.view.WindowManager.LayoutParams p = customDialog.getWindow().getAttributes();  //获取对话框当前的参数值
            p.width = (int) (displayWidth * 0.8);    //宽度设置为屏幕的0.8
            p.height = (int) (displayHeight * 0.4);    //高度设置为屏幕的0.28
            customDialog.setCanceledOnTouchOutside(false);// 设置点击屏幕Dialog不消失
            customDialog.getWindow().setAttributes(p);     //设置生效

            return customDialog;
        }
    }
}
