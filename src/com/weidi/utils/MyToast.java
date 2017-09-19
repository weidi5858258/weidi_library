package com.weidi.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by root on 17-2-23.
 */

public class MyToast {

    private volatile static Toast mToast;
    private static Context mContext;

    private static Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            // 不要直接使用getInstance().cancel();
            mHandler.removeCallbacks(runnable);
            getInstance().setText((String) msg.obj);
            if (msg.arg1 == 0) {
                getInstance().setDuration(Toast.LENGTH_SHORT);
            } else {
                getInstance().setDuration(Toast.LENGTH_LONG);
            }
            getInstance().show();
            mHandler.postDelayed(runnable, 5000);
            super.handleMessage(msg);
        }
    };

    private static Runnable runnable = new Runnable() {

        @Override
        public void run() {
            getInstance().cancel();
        }
    };

    /**
     * 1.在Application中先设置一下
     *
     * @param context
     */
    public static void setContext(Context context) {
        mContext = context;
    }

    /**
     * 2.在Application中先调用一下
     *
     * @return
     */
    public static Toast getInstance() {
        if (mToast == null) {
            synchronized (MyToast.class) {
                if (mToast == null) {
                    if (mContext == null) {
                        throw new NullPointerException("MyToast mContext is null.");
                    }
                    mToast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT);
                    mToast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 100);
                }
            }
        }
        return mToast;
    }

    public static void show(String text) {
        show(text, 0);
    }

    public static void show(int strId) {
        if (mContext != null) {
            show(mContext.getString(strId), 0);
        }
    }

    public static void show(String text, int duration) {
        Message msg = mHandler.obtainMessage();
        msg.obj = text;
        msg.arg1 = duration;
        mHandler.sendMessage(msg);
    }

    public static void show(int strId, int duration) {
        if (mContext != null) {
            show(mContext.getString(strId), duration);
        }
    }

    public static void dismiss() {
        getInstance().cancel();
    }

}
